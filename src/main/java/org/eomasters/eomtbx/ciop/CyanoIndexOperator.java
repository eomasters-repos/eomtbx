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

package org.eomasters.eomtbx.ciop;

import static org.eomasters.utils.Exceptions.throwIf;

import java.awt.image.RenderedImage;
import java.nio.file.Path;
import java.util.HashMap;
import javax.media.jai.OpImage;
import org.eomasters.eomtbx.converter.ConverterRegistrar;
import org.eomasters.eomtbx.utils.BandUtils;
import org.eomasters.snap.utils.MaskedOpImage;
import org.eomasters.snap.utils.ValidMaskBuilderException;
import org.eomasters.snap.utils.ValidMaskImageBuilder;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.image.ImageManager;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.core.util.converters.JtsGeometryConverter;
import org.locationtech.jts.geom.Geometry;

/**
 * CyanoIndexOperator is a class that represents the Cyanobacteria Index (CI) operator. It is used for the detection of
 * cyanobacteria blooms.
 * <a href="https://www.mdpi.com/2072-4292/15/6/1601">Reference document</a>
 */
@OperatorMetadata(alias = "CyanoIndex",
    category = "Optical/Thematic Water Processing",
    version = "1.0",
    authors = "Marco Peters",
    copyright = "(c) 2024 Marco Peters",
    description = "Cyanobacteria index (CI) for detection of cyanobacteria blooms.")
public class CyanoIndexOperator extends Operator {

  private static final int UPPER_WAVELENGTH = 725;
  private static final int UPPER_TOLERANCE = 25;
  private static final int LOWER_WAVELENGTH = 665;
  private static final int LOWER_TOLERANCE = 5;
  private static final int CENTER_WAVELENGTH = 680;
  private static final int CENTER_TOLERANCE = 5;
  private static final int TARGET_DATA_TYPE = ProductData.TYPE_FLOAT32;
  @SourceProduct(description = "The source product")
  private Product sourceProduct;
  @TargetProduct(description = "The target product")
  private Product targetProduct;

  @Parameter(description = "The valid expression to be used for the spectral index.")
  private String validExpression;
  @Parameter(description = "An ESRI shapefile, providing the considered geographical region(s).")
  private Path shapefile;
  @Parameter(converter = JtsGeometryConverter.class,
      description = "The considered geographical region as a geometry in well-known text format (WKT).")
  private Geometry wktRegion;

  @Override
  public void initialize() throws OperatorException {

    Product inputProduct = getInputProduct();
    Band lowBand = getLowBand(inputProduct);
    Band centerBand = getCenterBand(inputProduct);
    Band highBand = getHighBand(inputProduct);
    targetProduct = new Product(inputProduct.getName() + "_CI", "CYANO_IDX", centerBand.getRasterWidth(),
                                centerBand.getRasterHeight());
    ProductUtils.copyGeoCoding(centerBand, targetProduct);
    ProductUtils.copyTimeInformation(inputProduct, targetProduct);
    ProductUtils.copyPreferredTileSize(inputProduct, targetProduct);
    MetadataElement origin = new MetadataElement("Origin");
    MetadataElement metadataRoot = targetProduct.getMetadataRoot();
    metadataRoot.addElement(origin);
    ProductUtils.copyMetadata(inputProduct.getMetadataRoot(), origin);
    addMetadata(targetProduct.getMetadataRoot(), centerBand, lowBand, highBand);
    Band cyanoIndex = targetProduct.addBand("cyano_index", TARGET_DATA_TYPE);
    cyanoIndex.setDescription("Cyanobacteria bloom index");
    cyanoIndex.setNoDataValueUsed(true);
    cyanoIndex.setNoDataValue(Float.NaN);
    CyanoIndexOpImage ciImage = new CyanoIndexOpImage(lowBand, centerBand, highBand,
                                                      ImageManager.getDataBufferType(TARGET_DATA_TYPE));
    ValidMaskImageBuilder vmiBuilder = new ValidMaskImageBuilder(inputProduct);
    RenderedImage sourceMaskImage = getSourceMaskImage(vmiBuilder, centerBand, lowBand, highBand);
    OpImage maskedCiImage = new MaskedOpImage(ciImage, sourceMaskImage, Float.NaN);
    OpImage finalImage = applyMasks(inputProduct, maskedCiImage);

    cyanoIndex.setSourceImage(finalImage);
  }

  private Product getInputProduct() {
    Product sourceProduct = getSourceProduct();
    Band centerBand = getCenterBand(sourceProduct);
    Band lowBand = getLowBand(sourceProduct);
    Band highBand = getHighBand(sourceProduct);
    Product resampledSource = sourceProduct;
    if (!ProductUtils.areRastersEqualInSize(centerBand, lowBand, highBand)) {
      resampledSource = createResampledSource(sourceProduct, centerBand.getRasterWidth(), centerBand.getRasterHeight());
      MetadataElement metadataRoot = resampledSource.getMetadataRoot();
      MetadataElement[] elements = metadataRoot.getElements();
      for (MetadataElement element : elements) {
        metadataRoot.removeElement(element);
      }
      ProductUtils.copyMetadata(sourceProduct, resampledSource);
    }
    return resampledSource;
  }

  private Product createResampledSource(Product source, int width, int height) {
    // Because we only use radiometric bands and not the geometry bands it is not necessary to use the S2Resampling
    HashMap<String, Object> parameters = new HashMap<>();
    parameters.put("targetWidth", width);
    parameters.put("targetHeight", height);
    return GPF.createProduct("Resample", parameters, source);
  }

  private static RenderedImage getSourceMaskImage(ValidMaskImageBuilder vmiBuilder, Band centerBand, Band lowBand,
                                                  Band highBand) {
    RenderedImage sourceMaskImage;
    try {
      sourceMaskImage = vmiBuilder.withMaskImage(centerBand.getValidMaskImage())
                                  .withMaskImage(lowBand.getValidMaskImage())
                                  .withMaskImage(highBand.getValidMaskImage())
                                  .create();
    } catch (ValidMaskBuilderException e) {
      throw new OperatorException("Could not create valid mask image", e);
    }
    return sourceMaskImage;
  }

  private Band getHighBand(Product sourceProduct) {
    Band band = BandUtils.findMinimumBandInRange(sourceProduct.getBands(),
                                                 UPPER_WAVELENGTH - UPPER_TOLERANCE, UPPER_WAVELENGTH + UPPER_TOLERANCE);
    throwIf(band == null, new OperatorException("No suitable high band near " + UPPER_WAVELENGTH + "nm found"));
    return band;
  }

  private Band getLowBand(Product sourceProduct) {
    Band band = BandUtils.findClosestToCenterBand(sourceProduct.getBands(), LOWER_WAVELENGTH,
                                                  LOWER_WAVELENGTH + LOWER_TOLERANCE);
    throwIf(band == null, new OperatorException("No suitable low band near " + LOWER_WAVELENGTH + "nm found"));
    return band;
  }

  private Band getCenterBand(Product sourceProduct) {
    Band band = BandUtils.findClosestToCenterBand(sourceProduct.getBands(), CENTER_WAVELENGTH,
                                                  CENTER_WAVELENGTH + CENTER_TOLERANCE);
    throwIf(band == null, new OperatorException("No suitable center band near " + CENTER_WAVELENGTH + "nm found"));
    return band;
  }

  private void addMetadata(MetadataElement metadataRoot, Band centerBand, Band lowBand, Band highBand) {
    MetadataElement ci = new MetadataElement("Cyanobacteria Index");
    ci.setAttributeString("centerBand",
                          String.format("%s @%.1fnm", centerBand.getName(), centerBand.getSpectralWavelength()));
    ci.setAttributeString("lowBand",
                          String.format("%s @%.1fnm", lowBand.getName(), lowBand.getSpectralWavelength()));
    ci.setAttributeString("highBand",
                          String.format("%s @%.1fnm", highBand.getName(), highBand.getSpectralWavelength()));
    if (validExpression != null && !validExpression.isBlank()) {
      ci.setAttributeString("validExpression", validExpression);
    }
    if (shapefile != null) {
      ci.setAttributeString("shapefile", shapefile.toAbsolutePath().toString());
    }
    if (wktRegion != null) {
      ci.setAttributeString("wktRegion", wktRegion.toText());
    }

    metadataRoot.addElement(ci);
  }

  private OpImage applyMasks(Product sourceProduct, OpImage ciImage) {
    if (validExpression != null || wktRegion != null || shapefile != null) {
      try {
        RenderedImage maskImage = new ValidMaskImageBuilder(sourceProduct)
            .withExpression(validExpression)
            .withGeometryArea(wktRegion)
            .withShapeFile(shapefile)
            .withTileSize(sourceProduct.getPreferredTileSize())
            .create();
        return new MaskedOpImage(ciImage, maskImage, Float.NaN);
      } catch (ValidMaskBuilderException e) {
        throw new OperatorException("Not able to create mask.", e);
      }
    }
    return ciImage;
  }

  public static class Spi extends OperatorSpi {

    static {
      ConverterRegistrar.registerConverter();
    }

    public Spi() {
      super(CyanoIndexOperator.class);
    }
  }
}
