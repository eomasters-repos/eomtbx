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

package org.eomasters.eomtbx.s2norm;

import static org.esa.snap.core.image.ImageManager.getDataBufferType;

import java.awt.image.RenderedImage;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.jai.operator.AddConstDescriptor;
import javax.media.jai.operator.FormatDescriptor;
import javax.media.jai.operator.MultiplyDescriptor;
import org.eomasters.eomtbx.EomOperator;
import org.eomasters.eomtbx.EomOperatorSpi;
import org.eomasters.eomtbx.s2geom.S2DataFormat;
import org.eomasters.eomtbx.s2geom.S2FormatHelper;
import org.eomasters.eomtbx.s2geom.StdS2L2AFormat;
import org.eomasters.snap.utils.MaskedOpImage;
import org.eomasters.snap.utils.ReplaceNaNOpImage;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.util.ProductUtils;

/**
 * Operator to compute Nadir BRDF Adjusted Reflectance.
 */
@OperatorMetadata(alias = "S2NormSR",
    category = "Optical/Preprocessing",
    version = "1.0",
    authors = "Marco Peters",
    copyright = "(c) 2024 Marco Peters",
    description = "Computation Sentinel-2 of Nadir BRDF Adjusted Reflectance")
public class S2NormSROperator extends EomOperator {


  @SourceProduct(label = "Sentinel-2 L2A product", description = "The Sentinel-2 L2A source product")
  private Product sourceProduct;
  @TargetProduct(description = "The target product")
  private Product targetProduct;

  @Override
  public void initialize() throws OperatorException {
    S2DataFormat format;
    try {
      format = getSupportedFormat(sourceProduct);
    } catch (Exception e) {
      throw new OperatorException(e);
    }

    NormBands normBands = NormBands.create(format);
    targetProduct = new Product(sourceProduct.getName() + "_normSR", sourceProduct.getProductType(),
                                sourceProduct.getSceneRasterWidth(), sourceProduct.getSceneRasterHeight());
    ProductUtils.copyProductNodes(sourceProduct, targetProduct);
    ProductUtils.copyPreferredTileSize(sourceProduct, targetProduct);
    targetProduct.setDescription("Nadir BRDF Adjusted Reflectance Sentinel-2 L2A product");
    GeoCoding srcProductGeoCoding = sourceProduct.getSceneGeoCoding();
    for (String name : sourceProduct.getBandNames()) {
      Band band;
      if (normBands.isSupportedBand(name)) {
        band = ProductUtils.copyBand(name, sourceProduct, targetProduct, false);
        band.setDescription("Nadir BRDF Adjusted " + band.getDescription());
      } else {
        band = ProductUtils.copyBand(name, sourceProduct, targetProduct, true);
      }
      GeoCoding srcBandGeoCoding = sourceProduct.getBand(name).getGeoCoding();
      if(!srcBandGeoCoding.equals(srcProductGeoCoding)) {
        band.setGeoCoding(srcBandGeoCoding);
      }
    }
    ProductUtils.copyMasks(sourceProduct, targetProduct);

    NormCFactors normCFactors = NormCFactors.create(format);
    Map<String, Integer> s2BandNames = format.getNameBandIndexMap();
    for (Entry<String, Integer> nbarBandIndexEntry : s2BandNames.entrySet()) {
      String bandName = nbarBandIndexEntry.getKey();
      if (!normBands.isSupportedBand(bandName)) {
        continue;
      }
      int bandIndex = nbarBandIndexEntry.getValue();
      Band band = sourceProduct.getBand(bandName);
      if (band != null) {

        RenderedImage cFactorImage = normCFactors.getScaledCFactorImage(bandIndex);
        if (isDebugMode()) {
          String cfBandName = "cf_" + S2FormatHelper.S2_INDEX_NAME_MAP.get(bandIndex);
          if (!targetProduct.containsBand(cfBandName)) {
            Band cfBand = new Band(cfBandName, ProductData.TYPE_FLOAT64,
                                   cFactorImage.getWidth(), cFactorImage.getHeight());
            cfBand.setGeoCoding(band.getGeoCoding());
            cfBand.setSourceImage(cFactorImage);
            targetProduct.addBand(cfBand);
          }
        }

        RenderedImage surfReflImage = format.harmonizeSourceImage(band);
        RenderedImage normalizedImage = normalizeSurfRefl(surfReflImage, cFactorImage);
        RenderedImage harmonizedImage = format.harmonizeTargetImage(band, normalizedImage);
        MaskedOpImage maskedImage = new MaskedOpImage(harmonizedImage, band.getValidMaskImage(), Float.NaN);
        int dataBufferType = getDataBufferType(band.getDataType());
        RenderedImage finalImage = roundAndFormatImageData(maskedImage, dataBufferType, band.getNoDataValue());
        targetProduct.getBand(bandName).setSourceImage(finalImage);
      }
    }

  }

  private S2DataFormat getSupportedFormat(Product product) throws Exception {
    List<S2DataFormat> knownFormats = List.of(new StdS2L2AFormat(product)
                                              // currently not supported due to issue https://gitlab.orfeo-toolbox.org/maja/maja/-/issues/370
                                              // new TheiaS2L2AFormat(product)
    );
    for (S2DataFormat format : knownFormats) {
      if (format.isProductSupported()) {
        return format;
      }
    }
    throw new Exception(
        String.format("Product '%s' not supported. Must be a Sentinel-2 L2A standard product.", product.getName()));
  }


  private static RenderedImage normalizeSurfRefl(RenderedImage sourceImage, RenderedImage cFactorImage) {
    return MultiplyDescriptor.create(sourceImage, cFactorImage, null);
  }

  private static RenderedImage roundAndFormatImageData(RenderedImage image, int dataBufferType, double noDataValue) {
    RenderedImage adjustedImage = AddConstDescriptor.create(image, new double[]{0.5}, null);
    // nan must be replaced before the format operation. Afterward it is not of floating point type anymore.
    ReplaceNaNOpImage replacedNaImage = new ReplaceNaNOpImage(adjustedImage, noDataValue);
    return FormatDescriptor.create(replacedNaImage, dataBufferType, null);
  }

  public static class Spi extends EomOperatorSpi {

    public Spi() {
      super(S2NormSROperator.class);
    }

  }
}
