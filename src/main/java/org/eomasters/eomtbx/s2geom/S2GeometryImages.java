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

package org.eomasters.eomtbx.s2geom;

import static org.eomasters.utils.Exceptions.throwIf;

import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.jai.BorderExtender;
import javax.media.jai.BorderExtenderCopy;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.operator.CropDescriptor;
import javax.media.jai.operator.ScaleDescriptor;
import org.eomasters.eomtbx.utils.ArrayHelper;
import org.eomasters.snap.utils.MaskedOpImage;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.ImageUtils;

public class S2GeometryImages {

  public static final String VIEW_ZENITH_NAME = "view_zenith";
  public static final String VIEW_AZIMUTH_NAME = "view_azimuth";
  public static final String SUN_ZENITH_NAME = "sun_zenith";
  public static final String SUN_AZIMUTH_NAME = "sun_azimuth";
  private static final int GRID_RESOLUTION = 5000;

  private final S2GeometryAngles geometryAngles;
  private final Map<Integer, Map<Integer, RenderedImage>> bandDetectorMaskImages;
  private final Map<Integer, RenderedImage> viewZenithAngleImages = new HashMap<>();
  private final Map<Integer, RenderedImage> viewAzimuthAngleImages = new HashMap<>();
  private final Map<Integer, RenderedImage> sunZenithImagesPerResolution;
  private final Map<Integer, RenderedImage> sunAzimuthImagesPerResolution;
  private final Map<Integer, RenderedImage> detectorImages;
  private final Map<Integer, Integer> bandIndexResolutionMap;

  public S2GeometryImages(S2GeometryAngles geometryAngles,
                          Map<Integer, Map<Integer, RenderedImage>> bandDetectorMaskImages,
                          Map<Integer, RenderedImage> detectorImages,
                          Map<Integer, Integer> bandIndexResolutionMap) {
    this.geometryAngles = geometryAngles;
    this.bandDetectorMaskImages = bandDetectorMaskImages;
    sunZenithImagesPerResolution = getSunAngleImagesPerResolution(geometryAngles.getSunZenithAnglGrids());
    sunAzimuthImagesPerResolution = getSunAngleImagesPerResolution(geometryAngles.getSunAzimuthAngles());
    this.detectorImages = detectorImages;
    this.bandIndexResolutionMap = bandIndexResolutionMap;
  }

  public RenderedImage getSunZenithImageForResolution(int resolution) {
    return sunZenithImagesPerResolution.get(resolution);
  }

  public RenderedImage getSunAzimuthImageForResolution(int resolution) {
    return sunAzimuthImagesPerResolution.get(resolution);
  }

  public RenderedImage getViewZenithAngleImage(int bandIndex) {
    return getViewZenithAngleImage(bandIndex, bandIndexResolutionMap.get(bandIndex), new Dimension(512, 512));
  }

  public RenderedImage getViewZenithAngleImage(int bandIndex, int resolution, Dimension tileDimension) {
    throwIf(bandIndex < 0 || bandIndex >= S2GeometryAngles.NUM_S2_BANDS, new IllegalArgumentException(
        String.format("BandIndex must be between 0 and %d", S2GeometryAngles.NUM_S2_BANDS - 1)));
    return viewZenithAngleImages.computeIfAbsent(bandIndex, index ->
        createCombinedAndMaskedImage(index,
                                     geometryAngles.getExtrpolatedViewZenithAngleGrids().get(index),
                                     bandDetectorMaskImages,
                                     resolution, tileDimension));
  }

  public RenderedImage getViewAzimuthAngleImage(int bandIndex) {
    return getViewAzimuthAngleImage(bandIndex, bandIndexResolutionMap.get(bandIndex), new Dimension(512, 512));
  }

  public RenderedImage getViewAzimuthAngleImage(int bandIndex, int resolution, Dimension tileDimension) {
    throwIf(bandIndex < 0 || bandIndex >= S2GeometryAngles.NUM_S2_BANDS, new IllegalArgumentException(
        String.format("BandIndex must be between 0 and %d", S2GeometryAngles.NUM_S2_BANDS - 1)));
    return viewAzimuthAngleImages.computeIfAbsent(bandIndex, index ->
        createCombinedAndMaskedImage(index,
                                     geometryAngles.getExtrapolatedViewAzimuthAngleGrids().get(index),
                                     bandDetectorMaskImages,
                                     resolution, tileDimension));
  }

  public Map<Integer, Map<Integer, RenderedImage>> getBandDetectorMaskImages() {
    return bandDetectorMaskImages;
  }

  public Map<Integer, RenderedImage> getDetectorImages() {
    return detectorImages;
  }


  private Map<Integer, RenderedImage> getSunAngleImagesPerResolution(double[][] sunAngles) {
    Map<Integer, RenderedImage> map = new HashMap<>();
    double[] angles1d = ArrayHelper.as1DArray(sunAngles);
    throwIf(angles1d.length != S2GeometryAngles.GRID_WIDTH * S2GeometryAngles.GRID_HEIGHT,
            new IllegalArgumentException(String.format("Argument sunAngles should have %d [%dx%d] elements",
                                                       S2GeometryAngles.GRID_WIDTH * S2GeometryAngles.GRID_HEIGHT,
                                                       S2GeometryAngles.GRID_WIDTH, S2GeometryAngles.GRID_HEIGHT)));
    RenderedImage sunAngleGridImage = ImageUtils.createRenderedImage(
        S2GeometryAngles.GRID_WIDTH, S2GeometryAngles.GRID_HEIGHT, ProductData.createInstance(angles1d));
    for (int imageResolution : S2GeometryAngles.IMAGE_RESOLUTIONS) {
      RenderedImage scaledImage = scaleGridImageToResolution(sunAngleGridImage, imageResolution,
                                                             new Dimension(512, 512));
      float imageSize = S2GeometryAngles.RESOLUTION_IMAGE_SIZE_MAP.get(imageResolution);
      map.put(imageResolution, CropDescriptor.create(scaledImage, 0f, 0f,
                                                     imageSize, imageSize, null));
    }
    return map;
  }

  public RenderedImage createCombinedAndMaskedImage(int bandIndex, Map<Integer, double[][]> detectorGridDataMap,
                                                    Map<Integer, Map<Integer, RenderedImage>> bandDetectorMaskImages) {
    return createCombinedAndMaskedImage(bandIndex, detectorGridDataMap, bandDetectorMaskImages,
                                        bandIndexResolutionMap.get(bandIndex), new Dimension(512, 512));
  }

  private RenderedImage createCombinedAndMaskedImage(int bandIndex,
                                                     Map<Integer, double[][]> detectorGridDataMap,
                                                     Map<Integer, Map<Integer, RenderedImage>> bandDetectorMaskImages,
                                                     int targetResolution, Dimension tileDimension) {
    List<RenderedImage> maskedDetectorAngleImages = new ArrayList<>();
    for (Entry<Integer, double[][]> detectorGridDataEntry : detectorGridDataMap.entrySet()) {
      int detectorIndex = detectorGridDataEntry.getKey();
      double[][] detectorGridData = detectorGridDataEntry.getValue();

      RenderedImage detectorGridDataImage = ImageUtils.createRenderedImage(
          S2GeometryAngles.GRID_WIDTH, S2GeometryAngles.GRID_HEIGHT,
          ProductData.createInstance(ArrayHelper.as1DArray(detectorGridData)));

      RenderedImage scaledDetectorDataImage = scaleGridImageToResolution(detectorGridDataImage, targetResolution,
                                                                         tileDimension);

      float cropSize = getCropSize(targetResolution);
      RenderedImage detectorDataImage = CropDescriptor.create(scaledDetectorDataImage,
                                                              0f, 0f, cropSize, cropSize,
                                                              null);

      Map<Integer, RenderedImage> detectorMaskImageMap = bandDetectorMaskImages.get(bandIndex);
      throwIf(detectorMaskImageMap == null, new IllegalStateException(
          String.format("No detector mask images for band index '%d' found", bandIndex)));

      RenderedImage detectorMaskImage = detectorMaskImageMap.get(detectorIndex);
      throwIf(detectorMaskImage == null, new IllegalStateException(
          String.format("Detector '%d' mask image for band index '%d' not found", detectorIndex, bandIndex)));
      float scaleFactor = ((float) detectorDataImage.getWidth() / detectorMaskImage.getWidth());
      if (Float.compare(scaleFactor, 1.0f) != 0) {
        detectorMaskImage = ScaleDescriptor.create(detectorMaskImage,
                                                   scaleFactor, scaleFactor,
                                                   0f, 0f,
                                                   Interpolation.getInstance(Interpolation.INTERP_NEAREST),
                                                   null);
      }
      MaskedOpImage maskedImage = new MaskedOpImage(detectorDataImage, detectorMaskImage, Float.NaN);
      maskedDetectorAngleImages.add(maskedImage);
    }

    return new CombineDetectorImages(maskedDetectorAngleImages.toArray(new RenderedImage[0]));
  }

  private static float getCropSize(int targetResolution) {
    int res10Meter = 10;
    int size10m = S2GeometryAngles.RESOLUTION_IMAGE_SIZE_MAP.get(res10Meter);
    return size10m * ((float) res10Meter / targetResolution);
  }

  public static RenderedImage scaleGridImageToResolution(RenderedImage gridImage, int targetResolution,
                                                         Dimension tileDimension) {
    ImageLayout imageLayout = new ImageLayout();
    imageLayout.setTileWidth(tileDimension.width);
    imageLayout.setTileHeight(tileDimension.height);
    RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, imageLayout);
    hints.put(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtenderCopy.BORDER_COPY));

    float sFactor = (float) GRID_RESOLUTION / targetResolution;
    return ScaleDescriptor.create(gridImage, sFactor, sFactor, 0f, 0f,
                                  Interpolation.getInstance(Interpolation.INTERP_BILINEAR), hints);
  }
}
