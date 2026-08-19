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

package org.eomasters.eomtbx.spex;

import static org.esa.snap.core.datamodel.ProductData.TYPE_FLOAT32;

import java.awt.image.RenderedImage;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.eomasters.eomtbx.EomtbxException;
import org.eomasters.eomtbx.converter.ConverterRegistrar;
import org.eomasters.eomtbx.spex.formula.BandMathsExpressionFactory;
import org.eomasters.snap.utils.MaskedOpImage;
import org.eomasters.snap.utils.ValidMaskBuilderException;
import org.eomasters.snap.utils.ValidMaskImageBuilder;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.dataop.barithm.BandArithmetic;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.image.VirtualBandOpImage;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.core.util.converters.JtsGeometryConverter;
import org.locationtech.jts.geom.Geometry;

@OperatorMetadata(alias = "Spex",
    category = "Raster/Optical",
    version = "1.0",
    authors = "Marco Peters",
    copyright = "(c) 2023 Marco Peters",
    description = "Creates SpeX (SPEctral indeX) images for a source product. See help pages for detailed instructions.")
public class SpexOperator extends Operator {

  @SourceProduct(description = "The source product")
  private Product sourceProduct;
  @TargetProduct(description = "The target product")
  private Product targetProduct;

  @Parameter(description = "List of short names of SpeX to be calculated.")
  private String[] spexList;

  @Parameter(description = "The valid expression to be used for the spectral index.")
  private String validExpression;
  @Parameter(description = "An ESRI shapefile, providing the considered geographical region(s).")
  private Path shapefile;
  @Parameter(converter = JtsGeometryConverter.class,
      description = "The considered geographical region as a geometry in well-known text format (WKT).")
  private Geometry wktRegion;

  @Parameter(description = "The definition of custom or modified spectral index to be calculated.",
      domConverter = CustomSpexArrayDomConverter.class, itemAlias = "index")
  private CustomSpex[] customIndices;

  private final AtomicReference<Product> resampledSource = new AtomicReference<>();

  // for testing
  void setSpexList(String[] spexList) {
    this.spexList = spexList;
  }

  void setCustomSpex(CustomSpex[] customSpex) {
    this.customIndices = customSpex;
  }

  void setValidExpression(String expression) {
    this.validExpression = expression;
  }

  void setWktRegion(Geometry wktRegion) {
    this.wktRegion = wktRegion;
  }


  @Override
  public void initialize() throws OperatorException {
    boolean spexNameListSpecified = spexList != null && spexList.length > 0;
    boolean customSpexNameSpecified = customIndices != null && customIndices.length > 0;
    if (!spexNameListSpecified && !customSpexNameSpecified) {
      throw new OperatorException("No spectral indices specified. Specify wither 'spexNameList' or 'customSpexList'.");
    }

    Map<String, CustomSpex> usedSpexMap = new HashMap<>();
    if (spexNameListSpecified) {
      SpexDb spexDb = SpexDb.getInstance();
      for (String spexName : spexList) {
        if (spexName == null || spexName.isBlank()) {
          throw new OperatorException("One of spectral indices is null or empty.");
        }
        AbstractSpex index = spexDb.get(spexName);
        if (index == null) {
          throw new OperatorException("Spectral index '" + spexName + "' is unknown.");
        }

        usedSpexMap.put(index.getName(), new CustomSpex(index));
      }
    }

    if (customSpexNameSpecified) {
      validateCustomSpexList();
      SpexDb spexDb = SpexDb.getInstance();
      for (CustomSpex customSpex : customIndices) {
        if (customSpex.getName() == null || customSpex.getName().isEmpty()) {
          throw new OperatorException("Name must be specified for custom spectral index.");
        }
        CustomSpex actualSpex;
        if (spexDb.contains(customSpex.getName())) {
          AbstractSpex index = spexDb.get(customSpex.getName());
          actualSpex = new CustomSpex(index);
        } else {
          actualSpex = new CustomSpex(customSpex.getName());
        }

        if (customSpex.getFormula() != null && !customSpex.getFormula().isEmpty()) {
          actualSpex.setFormula(customSpex.getFormula());
        }
        if (customSpex.getDescription() != null && !customSpex.getDescription().isEmpty()) {
          actualSpex.setDescription(customSpex.getDescription());
        }
        if (customSpex.getReference() != null && !customSpex.getReference().isEmpty()) {
          actualSpex.setReference(customSpex.getReference());
        }
        if (customSpex.getSourceName() != null && !customSpex.getSourceName().isEmpty()) {
          actualSpex.setSourceName(customSpex.getSourceName());
        }
        if (customSpex.getSourceUrl() != null && !customSpex.getSourceUrl().isEmpty()) {
          actualSpex.setSourceUrl(customSpex.getSourceUrl());
        }

        if (customSpex.getValidExpression() != null && !customSpex.getValidExpression().isEmpty()) {
          actualSpex.setValidExpression(customSpex.getValidExpression());
        }
        if (customSpex.getShapefile() != null) {
          actualSpex.setShapefile(customSpex.getShapefile());
        }
        if (customSpex.getWktRegion() != null) {
          actualSpex.setWktRegion(customSpex.getWktRegion());
        }
        usedSpexMap.put(customSpex.getName(), actualSpex);
      }
    }

    for (CustomSpex value : usedSpexMap.values()) {
      if (value.getDeprecation() != null) {
        getLogger().warning(String.format("Spex '%s' is deprecated: %s", value.getName(), value.getDeprecation()));
      }
    }

    Product sourceProduct = getSourceProduct();
    targetProduct = new Product("SPEX", "SPEX", sourceProduct.getSceneRasterWidth(),
        sourceProduct.getSceneRasterHeight());
    ProductUtils.copyGeoCoding(sourceProduct, targetProduct);
    ProductUtils.copyTimeInformation(sourceProduct, targetProduct);
    ProductUtils.copyPreferredTileSize(sourceProduct, targetProduct);
    MetadataElement origin = new MetadataElement("Origin");
    MetadataElement metadataRoot = targetProduct.getMetadataRoot();
    metadataRoot.addElement(origin);
    // use the metadata from the not resampled source product
    ProductUtils.copyMetadata(this.sourceProduct.getMetadataRoot(), origin);

    try {
      addSpexBands(sourceProduct, usedSpexMap, targetProduct);
    } catch (Exception e) {
      throw new OperatorException("Cannot create bands. " + e.getMessage(), e);
    }
    addMetadata(metadataRoot, usedSpexMap);

    Logger logger = getLogger();
    logger.info("Spectral indices used:");
    for (CustomSpex spex : usedSpexMap.values()) {
      logger.info(String.format("  %s: %s", spex.getName(), spex.getFormula()));
    }

  }

  @Override
  public Product getSourceProduct() {
    if (resampledSource.get() == null) {
      Product source = super.getSourceProduct();
      if (source.isMultiSize()) {
        resampledSource.set(createResampledSource(source));
      } else {
        resampledSource.set(source);
      }
    }
    return resampledSource.get();
  }

  public Product createResampledSource(Product source) {
    // Because we only use radiometric bands and not the geometry bands it is not necessary to use the S2Resampling
    // Class<? extends ProductReader> readerClass = source.getProductReader().getClass();
    // Class<?>[] interfaces = readerClass.getInterfaces();
    // for (Class<?> anInterface : interfaces) {
    //   if (anInterface.getName().equals("eu.esa.opt.dataio.s2.ortho.S2AnglesGeometry")) {
    //     HashMap<String, Object> parameters = new HashMap<>();
    //     parameters.put("resolution", 10);
    //     return GPF.createProduct("S2Resampling", parameters, source);
    //   }
    // }
    HashMap<String, Object> parameters = new HashMap<>();
    parameters.put("targetWidth", source.getSceneRasterWidth());
    parameters.put("targetHeight", source.getSceneRasterHeight());
    return GPF.createProduct("Resample", parameters, source);
  }

  private void validateCustomSpexList() {
    for (CustomSpex spex : customIndices) {
      if (spex.getName() == null || spex.getName().isEmpty()) {
        throw new OperatorException("Short name must be specified for custom spectral index.");
      }
      SpexDb spexDb = SpexDb.getInstance();
      boolean unknownSpex = spexDb.get(spex.getName()) == null;
      if (unknownSpex && (spex.getFormula() == null || spex.getFormula().isEmpty())) {
        throw new OperatorException(
            "Formula must be specified for custom spectral index if name is not known within the database.");
      }
    }
  }

  private void addMetadata(MetadataElement metadataRoot, Map<String, CustomSpex> usedSpex) {
    MetadataElement spectralIndices = new MetadataElement("Spectral Indices");
    for (CustomSpex spexIndex : usedSpex.values()) {
      MetadataElement spexElem = new MetadataElement(spexIndex.getName());
      spexElem.setAttributeString("formula", spexIndex.getFormula());
      String expression = BandMathsExpressionFactory.expandFormula(spexIndex, sourceProduct);
      spexElem.setAttributeString("expression", expression);
      if (spexIndex.getDescription() != null) {
        spexElem.setAttributeString("description", spexIndex.getDescription());
      }

      if (spexIndex.getValidExpression() != null) {
        spexElem.setAttributeString("validExpression", spexIndex.getValidExpression());
      }
      if (spexIndex.getReference() != null) {
        spexElem.setAttributeString("reference", spexIndex.getReference());
      }

      spectralIndices.addElement(spexElem);
    }

    metadataRoot.addElement(spectralIndices);
  }

  private void addSpexBands(Product sourceProduct, Map<String, CustomSpex> usedSpex, Product targetProduct)
      throws ValidMaskBuilderException {
    RenderedImage generalMaskImage = null;
    if (validExpression != null || wktRegion != null || shapefile != null) {
      // if any is given then create the general mask
      generalMaskImage = new ValidMaskImageBuilder(sourceProduct)
          .withExpression(validExpression)
          .withGeometryArea(wktRegion)
          .withShapeFile(shapefile)
          .withTileSize(sourceProduct.getPreferredTileSize())
          .create();
    }

    for (CustomSpex spex : usedSpex.values()) {
      String expression = BandMathsExpressionFactory.expandFormula(spex, sourceProduct);
      Term spexTerm = createSpexTerm(sourceProduct, spex, expression);
      VirtualBandOpImage spexImage = VirtualBandOpImage.builder(spexTerm)
                                                       .dataType(TYPE_FLOAT32)
                                                       .fillValue(Float.NaN)
                                                       .sourceSize(sourceProduct.getSceneRasterSize())
                                                       .tileSize(sourceProduct.getPreferredTileSize())
                                                       .create();
      RenderedImage finalMask = createSpexMask(sourceProduct, spex, spexTerm, generalMaskImage);

      MaskedOpImage processedImage = new MaskedOpImage(spexImage, finalMask, Float.NaN);

      Band band = targetProduct.addBand(spex.getName(), TYPE_FLOAT32);
      band.setSourceImage(processedImage);
      band.setDescription(spex.getDescription());
      band.setNoDataValueUsed(true);
      band.setNoDataValue(Float.NaN);
    }
  }

  private RenderedImage createSpexMask(Product sourceProduct, CustomSpex spex, Term spexTerm,
      RenderedImage generalMaskImage) throws ValidMaskBuilderException {
    String[] bandsToValidate = Arrays.stream(BandArithmetic.getRefRasters(spexTerm)).
                                     filter(RasterDataNode::isValidMaskUsed) // only those which need to be validated
                                     .map(RasterDataNode::getName).toArray(String[]::new);
    ValidMaskImageBuilder maskBuilder = new ValidMaskImageBuilder(sourceProduct);
    if (bandsToValidate.length > 0) {
      maskBuilder = maskBuilder.withExpression(String.format("areValid(%s)", String.join(", ", bandsToValidate)));
    }
    return maskBuilder
        .withMaskImage(generalMaskImage)
        .withExpression(spex.getValidExpression())
        .withGeometryArea(spex.getWktRegion())
        .withShapeFile(spex.getShapefile())
        .withTileSize(sourceProduct.getPreferredTileSize())
        .create();
  }

  private static Term createSpexTerm(Product sourceProduct, CustomSpex spex, String expression) {
    Term spexTerm;
    try {
      spexTerm = VirtualBandOpImage.parseExpression(expression, sourceProduct);
    } catch (IllegalArgumentException e) {
      throw new EomtbxException(
          String.format("Cannot parse expression '%s' for spectral index '%s'", expression, spex.getName()), e);
    }
    return spexTerm;
  }

  public static class Spi extends OperatorSpi {

    static {
      ConverterRegistrar.registerConverter();
    }

    public Spi() {
      super(SpexOperator.class);
    }
  }
}
