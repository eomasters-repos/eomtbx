/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/eomtbx
 * ======================================================================
 * Copyright (C) 2023 - 2026 Marco Peters
 * ======================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * -> http://www.gnu.org/licenses/gpl-3.0.html
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.s2superres;

import static org.eomasters.eomtbx.s2superres.S2SuperResOp.TARGET_PIXEL_SIZE;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.Result;
import ai.onnxruntime.OrtSession.RunOptions;
import ai.onnxruntime.OrtSession.SessionOptions;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.ScaleOpImage;
import javax.media.jai.operator.ScaleDescriptor;
import org.apache.commons.lang3.time.StopWatch;
import org.eomasters.eomtbx.s2geom.S2DataFormat;
import org.eomasters.eomtbx.s2geom.TheiaS2L2AFormat;
import org.eomasters.eomtbx.s2superres.onnx.Onnx;
import org.eomasters.eomtbx.utils.ArrayHelper;
import org.eomasters.snap.utils.ValidMaskBuilderException;
import org.eomasters.snap.utils.ValidMaskImageBuilder;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.image.FillConstantOpImage;

public class S2SuperResolveImage extends ScaleOpImage {

  public static final int EXPECTED_SRC_RESOLUTION = 10; // meter
  public static final int SCALING = EXPECTED_SRC_RESOLUTION / TARGET_PIXEL_SIZE;
  private static final int SRC_TILE_Buffer = 16;
  private static final int DST_TILE_Buffer = SRC_TILE_Buffer * 2;
  private static final int NUM_SRC_BANDS = 10;
  private static final int[] s2toModelMapping = {0, 1, 2, 4, 5, 6, 3, 7, 8, 9};
  private static final int[] modelToS2Mapping = {0, 1, 2, 6, 3, 4, 5, 7, 8, 9};

  private final OrtSession session;
  private final RunOptions onnxRunOptions;
  private final boolean debugMode;

  public S2SuperResolveImage(RenderedImage image, Dimension tileSize, boolean debug) {
    super(image, null, null, false, null,
          // interpolation not important, but it defines the padding for map and source dest rectangle
          Interpolation.getInstance(Interpolation.INTERP_NEAREST), SCALING, SCALING, 0, 0);
    this.debugMode = debug;
    setImageLayout(createImageLayout(getSourceImage(0), tileSize));
    try {
      byte[] modelData = Onnx.retrieveModelData("/org/eomasters/eomtbx/s2superres/carn_3x3x64g4sw_bootstrap.onnx");
      SessionOptions options = Onnx.createDefaultSessionOptions();
      session = Onnx.createOrtSession(modelData, options);
      onnxRunOptions = Onnx.createDefaultRunOptions("S2_superresolve");
    } catch (Exception e) {
      throw new IllegalStateException("Not able to create inference engine", e);
    }

  }

  public static RenderedImage create(S2DataFormat format, Product source, List<String> targetBandNames,
                                     Dimension tileSize, boolean debugMode) {
    List<RenderedImage> superResolveImages = new ArrayList<>();
    for (String targetBandName : targetBandNames) {
      int resolution = format.getResolutionOfBand(targetBandName);
      Band srcBand = source.getBand(targetBandName);
      RenderedImage srcImage = srcBand.getSourceImage();

      RenderedImage validMaskImage = getValidMaskImage(format, srcBand);
      if (resolution == 20) {
        // creates a border of 3 pixels which are zero - use border extender copy?
        float scaleFactor = (float) resolution / S2SuperResolveImage.EXPECTED_SRC_RESOLUTION;
        srcImage = ScaleDescriptor.create(srcImage, scaleFactor, scaleFactor, 0.0f, 0.0f,
                                          Interpolation.getInstance(Interpolation.INTERP_BICUBIC),
                                          new RenderingHints(JAI.KEY_TILE_CACHE, null));
        validMaskImage = ScaleDescriptor.create(validMaskImage, scaleFactor, scaleFactor, 0.0f, 0.0f,
                                                Interpolation.getInstance(Interpolation.INTERP_NEAREST),
                                                new RenderingHints(JAI.KEY_TILE_CACHE, null));

      }
      FillConstantOpImage nanImage = new FillConstantOpImage(srcImage, validMaskImage, Float.NaN);
      superResolveImages.add(nanImage);
    }

    // bring into necessary model order
    RenderedImage[] orderedImagesArray = new RenderedImage[superResolveImages.size()];
    for (int i = 0; i < superResolveImages.size(); i++) {
      orderedImagesArray[s2toModelMapping[i]] = superResolveImages.get(i);
    }
    List<RenderedImage> orderedImages = List.of(orderedImagesArray);

    // merge into one image
    RenderedImage mergedImage = mergeImages(orderedImages, tileSize);
    return new S2SuperResolveImage(mergedImage, tileSize, debugMode);
  }

  private static RenderedImage getValidMaskImage(S2DataFormat format, Band band) {
    if (!(format instanceof TheiaS2L2AFormat)) {
      String name = band.getName();
      if (format.isCommonSpectralBandName(name)) {
        try {
          // Using the detector-footprint as valid-mask
          // std. products have within the data some no-data pixels, which lead to big no-data patches in the
          // super-resolved product
          String mask = "B_detector_footprint_" + name;
          ValidMaskImageBuilder builder = new ValidMaskImageBuilder(band.getProduct());
          if (band.getProduct().containsBand(mask)) {
            builder.withExpression(mask + "!= 0");
          } else {
            builder.withExpression("true");
          }
          return builder.create();
        } catch (ValidMaskBuilderException ignored) {
        }
      }
    }
    return band.getValidMaskImage();
  }

  private static RenderedImage mergeImages(List<RenderedImage> superResolveImages, Dimension tileSize) {
    ParameterBlockJAI pBlock = new ParameterBlockJAI("BandMerge", "rendered");
    for (RenderedImage superResolveImage : superResolveImages) {
      pBlock.addSource(superResolveImage);
    }
    ImageLayout layout = new ImageLayout();
    layout.setTileWidth(tileSize.width);
    layout.setTileHeight(tileSize.height);
    RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);

    return JAI.create("BandMerge", pBlock, hints);
  }




  @Override
  public synchronized void dispose() {
    super.dispose();
    try {
      onnxRunOptions.setTerminate(true);
    } catch (OrtException e) {
      throw new RuntimeException("Not able to terminate inference session", e);
    }
  }

  @Override
  protected void computeRect(PlanarImage[] sources, WritableRaster destRaster, Rectangle destRect) {
    StopWatch stopWatch = null;
    if (debugMode) {
      stopWatch = new StopWatch();
      stopWatch.start();
    }
    List<RenderedImage> sourceImageList = getSourceImageList(sources);
    Rectangle bufferedSrcTileRect = getBufferedSourceTileRect(destRect);

    try {
      S2SuperResolveTensorInput tensorInput = new S2SuperResolveTensorInput(bufferedSrcTileRect, sourceImageList.size());
      OnnxTensor onnxTensor = tensorInput.createOnnxTensor(sourceImageList);

      try (Result output = session.run(Map.of("input", onnxTensor), onnxRunOptions)) {
        Optional<OnnxValue> onnxOutput = output.get("output");
        if (onnxOutput.isPresent()) {
          float[][][] inferenceResult = ((float[][][][]) onnxOutput.get().getValue())[0];
          // inferenceResult is in this order: B2, B3, B4, B8, B5, B6, B7, B8A, B11, B12
          for (int bIndex = 0; bIndex < inferenceResult.length; bIndex++) {
            float[][] croppedArray = cropArray(inferenceResult[bIndex]);
            float[] destData = ArrayHelper.as1DArray(croppedArray);
            var s2bandIndex = modelToS2Mapping[bIndex]; // remap from model to S2 band index
            destRaster.setSamples(destRect.x, destRect.y, destRect.width, destRect.height, s2bandIndex, destData);
          }
        } else {
          throw new RuntimeException("Inference model did not return any output for region %s".formatted(destRect));
        }
      }
    } catch (OrtException e) {
      throw new RuntimeException(e);
    }
    if (debugMode) {
      stopWatch.stop();
      System.out.printf("S2SuperResolveImage %s: %s%n", format(destRect), stopWatch.formatTime());
    }
  }

  private Rectangle getBufferedSourceTileRect(Rectangle destRect) {
    Rectangle sourceRect = mapDestRect(destRect, 0);
    Rectangle bufferedSrcTileRect = new Rectangle(sourceRect.x, sourceRect.y, sourceRect.width, sourceRect.height);
    bufferedSrcTileRect.grow(SRC_TILE_Buffer, SRC_TILE_Buffer);
    return bufferedSrcTileRect;
  }

  private static List<RenderedImage> getSourceImageList(PlanarImage[] sources) {
    PlanarImage source = sources[0];
    List<RenderedImage> sourcesList = new ArrayList<>();
    for (int i = 0; i < source.getNumSources(); i++) {
      sourcesList.add(source.getSourceImage(i));
    }
    return sourcesList;
  }

  private static String format(Rectangle rect) {
    return "[x=" + rect.x + ",y=" + rect.y + ",width=" + rect.width + ",height=" + rect.height + "]";
  }

  private float[][] cropArray(float[][] floats) {
    int buffer = S2SuperResolveImage.DST_TILE_Buffer;
    int rows = floats.length - SCALING * buffer;
    int cols = floats[0].length - SCALING * buffer;
    float[][] croppedArray = new float[rows][cols];

    for (int y = buffer; y < floats.length - buffer; y++) {
      System.arraycopy(floats[y], buffer, croppedArray[y - buffer], 0, cols);
    }
    return croppedArray;
  }

  private ImageLayout createImageLayout(RenderedImage srcImage, Dimension tileSize) {
    if (srcImage.getSampleModel().getNumBands() != NUM_SRC_BANDS) {
      throw new IllegalArgumentException("Input image must have " + NUM_SRC_BANDS + " required.");
    }
    ImageLayout layout = new ImageLayout();
    layout.setTileWidth(tileSize.width);
    layout.setTileHeight(tileSize.height);
    return layout;
  }

}
