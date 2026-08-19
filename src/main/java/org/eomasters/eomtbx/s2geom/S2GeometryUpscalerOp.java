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

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.jai.operator.FormatDescriptor;
import org.eomasters.eomtbx.EomOperator;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.util.ProductUtils;

@OperatorMetadata(alias = "S2GeometryUpscaler",
    category = "Optical/Preprocessing",
    version = "1.0",
    authors = "Marco Peters",
    copyright = "(c) 2024 Marco Peters",
    description = "Scales up viewing and solar geometry angles considering the detector footprints.")
public class S2GeometryUpscalerOp extends EomOperator {

  @SourceProduct(description = "The source product")
  private Product sourceProduct;
  @TargetProduct(description = "The target product")
  private Product targetProduct;

  @Parameter(valueSet = {"All", "10", "20", "60"}, defaultValue = "All", label = "Sun Geometry Resolution",
      description = "Whether all or only one of the resolutions should be generated.")
  private String sunGeometryResolution;

  @Parameter(defaultValue = "True", label = "Include Source Bands",
      description = "Whether to include source data or only generate geometry bands")
  private boolean includeSourceBands;

  @Override
  public void initialize() throws OperatorException {
    try {
      Product inputProduct = getSourceProduct();
      S2DataFormat format = S2DataFormat.getSupportedFormat(inputProduct);

      validateParameters(format);

      targetProduct = createTargetProduct(inputProduct);

      addSunAngleBands(inputProduct, format);
      addViewAngleBands(inputProduct, format);

      if (isDebugMode()) {
        addDetectorMaskImages(inputProduct, format);
      }
    } catch (Exception e) {
      throw new OperatorException(e);
    }
  }

  private void validateParameters(S2DataFormat format) {
    Map<Integer, String[]> resolutionBandNamesMap = format.getBandNamesPerResolution();
    if (!sunGeometryResolution.equals("All") && !resolutionBandNamesMap.containsKey(
        Integer.parseInt(sunGeometryResolution))) {
      throw new OperatorException(
          "The '" + format.getName() + "' source product does not provide a resolution at " + sunGeometryResolution);
    }
  }

  private void addSunAngleBands(Product product, S2DataFormat format) {
    Integer[] sunResolutions = getAvailableResolutions(format);
    S2GeometryImages s2GeometryImages = format.getS2GeometryImages();
    Arrays.stream(sunResolutions).forEach(resolution -> {
      Band resBand = getBandForResolution(resolution, format, product);

      String sunZenithName = String.format("%s_%d", S2GeometryImages.SUN_ZENITH_NAME, resolution);
      Band sunZen = new Band(sunZenithName, ProductData.TYPE_FLOAT32, resBand.getRasterWidth(),
                             resBand.getRasterHeight());
      targetProduct.addBand(sunZen);
      sunZen.setGeoCoding(resBand.getGeoCoding());
      sunZen.setDescription("Sun zenith angle at " + resolution + "m resolution");
      RenderedImage sunZenithImage = FormatDescriptor.create(
          s2GeometryImages.getSunZenithImageForResolution(resolution),
          DataBuffer.TYPE_FLOAT, null);
      sunZen.setSourceImage(sunZenithImage);

      String sunAzimuthName = String.format("%s_%d", S2GeometryImages.SUN_AZIMUTH_NAME, resolution);
      Band sunAzi = new Band(sunAzimuthName, ProductData.TYPE_FLOAT32, resBand.getRasterWidth(),
                             resBand.getRasterHeight());
      targetProduct.addBand(sunAzi);
      sunAzi.setGeoCoding(resBand.getGeoCoding());
      sunAzi.setDescription("Sun azimuth angle at " + resolution + "m resolution");
      RenderedImage sunAzimuthImage = FormatDescriptor.create(
          s2GeometryImages.getSunAzimuthImageForResolution(resolution),
          DataBuffer.TYPE_FLOAT, null);
      sunAzi.setSourceImage(sunAzimuthImage);
    });
  }

  private Integer[] getAvailableResolutions(S2DataFormat format) {
    return sunGeometryResolution.equals("All") ? format.getBandNamesPerResolution().keySet().toArray(new Integer[0])
        : new Integer[]{Integer.parseInt(sunGeometryResolution)};
  }

  private void addViewAngleBands(Product inputProduct, S2DataFormat format) {
    S2GeometryImages s2GeometryImages = format.getS2GeometryImages();
    format.getNameBandIndexMap().forEach((name, index) -> {
      Band refBand = getBand(inputProduct, name);
      int resolution = format.getResolutionOfBand(name);
      if (refBand != null) {
        String generalBandName = format.getCommonSpectralBandName(name);
        String viewZenBandName = String.format("%s_%s", S2GeometryImages.VIEW_ZENITH_NAME, generalBandName);
        if (!targetProduct.containsBand(viewZenBandName)) {
          Band satZen = new Band(viewZenBandName, ProductData.TYPE_FLOAT32, refBand.getRasterWidth(),
                                 refBand.getRasterHeight());
          targetProduct.addBand(satZen);
          satZen.setGeoCoding(refBand.getGeoCoding());
          satZen.setDescription("View zenith angle for " + name + "at " + resolution + "m resolution");
          satZen.setNoDataValueUsed(true);
          satZen.setNoDataValue(Float.NaN);
          satZen.setSourceImage(FormatDescriptor.create(s2GeometryImages.getViewZenithAngleImage(index),
                                                        DataBuffer.TYPE_FLOAT, null));
        }

        String viewAziBandName = String.format("%s_%s", S2GeometryImages.VIEW_AZIMUTH_NAME, generalBandName);
        if (!targetProduct.containsBand(viewAziBandName)) {
          Band satAzi = new Band(viewAziBandName, ProductData.TYPE_FLOAT32, refBand.getRasterWidth(),
                                 refBand.getRasterHeight());
          targetProduct.addBand(satAzi);
          satAzi.setGeoCoding(refBand.getGeoCoding());
          satAzi.setDescription("View azimuth angle for " + name + "at " + resolution + "m resolution");
          satAzi.setNoDataValueUsed(true);
          satAzi.setNoDataValue(Float.NaN);
          satAzi.setSourceImage(FormatDescriptor.create(s2GeometryImages.getViewAzimuthAngleImage(index),
                                                        DataBuffer.TYPE_FLOAT, null));
        }
      }
    });
  }

  private void addDetectorMaskImages(Product product, S2DataFormat format) {
    Map<Integer, Map<Integer, RenderedImage>> detectorMaskImages = format.getDetectorMaskImages();
    for (Entry<Integer, Map<Integer, RenderedImage>> integerMapEntry : detectorMaskImages.entrySet()) {
      Integer bandIndex = integerMapEntry.getKey();
      Map<Integer, RenderedImage> value = integerMapEntry.getValue();
      Band resBand = getBandForResolution(format.getIndexBandResolutionMap().get(bandIndex), format,
                                          product);
      for (Entry<Integer, RenderedImage> integerRenderedImageEntry : value.entrySet()) {
        Integer detIndex = integerRenderedImageEntry.getKey();
        RenderedImage image = integerRenderedImageEntry.getValue();
        String s2BandName = S2FormatHelper.S2_INDEX_NAME_MAP.get(bandIndex);
        Band b = new Band("det_mask_" + s2BandName + "_" + detIndex, ProductData.TYPE_UINT8, image.getWidth(),
                          image.getHeight());
        targetProduct.addBand(b);
        b.setGeoCoding(resBand.getGeoCoding());
        b.setSourceImage(image);
      }
    }
  }

  private Product createTargetProduct(Product inputProduct) {
    Product target = new Product(inputProduct.getName() + "_upS2geom", inputProduct.getProductType(),
                                 inputProduct.getSceneRasterWidth(),
                                 inputProduct.getSceneRasterHeight());
    ProductUtils.copyProductNodes(inputProduct, target);
    ProductUtils.copyPreferredTileSize(inputProduct, target);

    if (includeSourceBands) {
      GeoCoding sourceProductSceneGeoCoding = sourceProduct.getSceneGeoCoding();
      Arrays.stream(inputProduct.getBandNames()).forEach(bandName -> {
        Band band = ProductUtils.copyBand(bandName, inputProduct, target, true);
        GeoCoding srcBandGeoCoding = sourceProduct.getBand(bandName).getGeoCoding();
        if(!srcBandGeoCoding.equals(sourceProductSceneGeoCoding)) {
          band.setGeoCoding(srcBandGeoCoding);
        }
      });
      ProductUtils.copyMasks(inputProduct, target);
    }
    removeOldGeometryBands(target);
    return target;
  }

  private static Band getBandForResolution(int resolution, S2DataFormat format, Product inputProduct) {
    String[] bandNames = format.getBandNamesPerResolution().get(resolution);
    for (String bandName : bandNames) {
      if (inputProduct.containsBand(bandName)) {
        return inputProduct.getBand(bandName);
      }
    }
    throw new IllegalStateException(
        "No band found for resolution " + resolution + " and band names " + Arrays.toString(bandNames));
  }

  private Band getBand(Product sourceProduct, String bandName) {
    return Arrays.stream(sourceProduct.getBands()).filter(b -> b.getName().contains(bandName)).findFirst().orElse(null);
  }


  private void removeOldGeometryBands(Product targetProduct) {
    String[] bandNames = targetProduct.getBandNames();
    for (String bandName : bandNames) {
      if (bandName.startsWith("sun_") || bandName.startsWith("view_")) {
        targetProduct.removeBand(targetProduct.getBand(bandName));
      }
    }
  }


  public static class Spi extends OperatorSpi {

    public Spi() {
      super(S2GeometryUpscalerOp.class);
    }
  }
}
