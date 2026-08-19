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
import static org.eomasters.eomtbx.s2superres.S2SuperResolvedBands.SUPER_RESOLVED_BANDS;
import static org.eomasters.eomtbx.utils.JaiUtils.cropAndTranslate;

import com.bc.ceres.multilevel.MultiLevelImage;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.FormatDescriptor;
import javax.media.jai.operator.ScaleDescriptor;
import org.apache.commons.lang3.ArrayUtils;
import org.eomasters.eomtbx.s2geom.S2DataFormat;
import org.eomasters.snap.utils.ReplaceNaNOpImage;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.image.FillConstantOpImage;
import org.esa.snap.core.image.ImageManager;
import org.esa.snap.core.util.ProductUtils;
import org.opengis.referencing.operation.MathTransform;

public class OtherBands {

  private static final String B1 = "B1";
  private static final String B9 = "B9";
  private static final String B10 = "B10";
  public static final String[] SPECTRAL_60M_BANDS = {B1, B9, B10};
  private static final int BAND_NAME_NOT_FOUND = -1;
  private final Dimension tileSize;

  public OtherBands(Dimension tileSize) {
    this.tileSize = tileSize;
  }

  public void create60mBandsAndImages(S2DataFormat sourceFormat, Product source, Product target,
                                      Rectangle tgtSceneRegion) {
    List<String> bandNames60m = get60mBandNames(sourceFormat);

    createBandsAndImages(source, bandNames60m, target, tgtSceneRegion);
  }

  public void createOtherBandsAndImages(Product source, List<String> requestedSpectralBands, Product target,
                                        Rectangle tgtSceneRegion) {
    List<String> otherBandNames = getOtherBandNames(source, requestedSpectralBands);
    createBandsAndImages(source, otherBandNames, target, tgtSceneRegion);
  }

  private void createBandsAndImages(Product source, List<String> srcBandNames, Product target,
                                    Rectangle targetSceneRegion) {

    for (String srcBandName : srcBandNames) {
      float resolution = getResolution(source.getBand(srcBandName));
      Band sourceBand = source.getBand(srcBandName);
      Band targetBand = copyBandToTarget(sourceBand, target);
      RenderedImage image = createImage(sourceBand, resolution);
      ImageLayout layout = new ImageLayout();
      layout.setTileWidth(tileSize.width);
      layout.setTileHeight(tileSize.height);
      RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
      RenderedImage dataImage = cropAndTranslate(image, targetSceneRegion, hints);
      targetBand.setSourceImage(dataImage);
    }
  }

  private static List<String> get60mBandNames(S2DataFormat sourceFormat) {
    List<String> bandNames60m = new ArrayList<>();
    Map<String, String[]> spectralGroups = sourceFormat.getSpectralGroups();
    for (Entry<String, String[]> stringEntry : spectralGroups.entrySet()) {
      String[] formatBandNames = stringEntry.getValue();
      for (String formatBandName : formatBandNames) {
        String commonSpectralBandName = sourceFormat.getCommonSpectralBandName(formatBandName);
        if (ArrayUtils.contains(SPECTRAL_60M_BANDS, commonSpectralBandName)) {
          bandNames60m.add(formatBandName);
        }
      }
    }
    return bandNames60m;
  }

  private static List<String> getOtherBandNames(Product source, List<String> requestedSpectralBands) {
    List<String> bandNames = new ArrayList<>();

    for (String srcBandName : source.getBandNames()) {
      String refBandName = extractRefBandName(srcBandName);
      if (!isBandEligible(refBandName)) {
        continue; // skip band
      } else {
        bandNames.add(srcBandName);
      }
    }
    return bandNames;
  }

  private static boolean isSpectralBand(String srcBandName) {
    return ArrayUtils.contains(SUPER_RESOLVED_BANDS, srcBandName)
        || ArrayUtils.contains(SPECTRAL_60M_BANDS, srcBandName);
  }

  private static boolean isBandEligible(String bandName) {
    return !bandName.startsWith("sun") && !bandName.startsWith("view") && !isSpectralBand(bandName);
  }

  static String extractRefBandName(String bandName) {
    int underscoreIndex = bandName.lastIndexOf("_");
    if (underscoreIndex > BAND_NAME_NOT_FOUND) {
      String extension = bandName.substring(underscoreIndex + 1);
      if (isSpectralBand(extension)) {
        return extension;
      }
    }
    return bandName;
  }

  private static RenderedImage createImage(Band sourceBand, float resolution) {
    int geophysicalDataType = sourceBand.getGeophysicalDataType();
    int resampling = ProductData.isFloatingPointType(geophysicalDataType) ? Interpolation.INTERP_BICUBIC
        : Interpolation.INTERP_NEAREST;
    float scaleFactor = resolution / TARGET_PIXEL_SIZE;
    return resampleRaster(sourceBand, scaleFactor, resampling);
  }

  private static Band copyBandToTarget(Band srcBand, Product target) {
    Band targetBand = new Band(srcBand.getName(), srcBand.getDataType(), target.getSceneRasterWidth(),
                               target.getSceneRasterHeight());
    ProductUtils.copyRasterDataNodeProperties(srcBand, targetBand);
    target.addBand(targetBand);
    return targetBand;
  }

  private static PlanarImage resampleRaster(Band band, float scaleFactor, int resampling) {
    RenderedImage sourceImage = band.getSourceImage();
    double noDataValue = band.getNoDataValue();
    MultiLevelImage validMaskImage = band.getValidMaskImage();
    if (validMaskImage != null) {
      sourceImage = new FillConstantOpImage(sourceImage, validMaskImage, Float.NaN);
    }
    RenderedImage scaledImage = ScaleDescriptor.create(sourceImage, scaleFactor, scaleFactor, 0.0f, 0.0f,
                                                       Interpolation.getInstance(resampling), null);
    if (validMaskImage != null) {
      scaledImage = new ReplaceNaNOpImage(scaledImage, noDataValue);
    }
    return FormatDescriptor.create(scaledImage, ImageManager.getDataBufferType(band.getDataType()), null);
  }

  private static float getResolution(Band band) {
    GeoCoding geoCoding = band.getGeoCoding();
    MathTransform imageToMapTransform = geoCoding.getImageToMapTransform();
    if (imageToMapTransform instanceof AffineTransform affineTransform) {
      return (float) affineTransform.getScaleX();
    } else {
      throw new IllegalStateException(String.format("Not able to determine resolution of band '%s' ", band.getName()));
    }
  }


}
