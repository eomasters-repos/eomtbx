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

package org.eomasters.eomtbx.assets.type.bmaths;

import static org.eomasters.utils.Exceptions.throwIf;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import java.awt.Dimension;
import java.awt.image.RenderedImage;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetException;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.PropertyHelper;
import org.eomasters.icons.Icon;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.VirtualBand;
import org.esa.snap.core.image.VirtualBandOpImage;
import org.esa.snap.core.jexp.Term;

public class BandMathsType extends AssetType {

  static final String PROP_BAND_NAME = "bandName";
  static final String PROP_EXPRESSION = "expression";
  static final String PROP_VIRTUAL = "asVirtualBand";
  static final String PROP_GEO_NO_DATA = "nodataValue";
  static final String PROP_DATA_TYPE = "dataType";

  public BandMathsType() {
    super("BandMaths", "Band Maths Image");
  }

  @Override
  public Icon getIcon() {
    return EomtbxIcons.MATHS;
  }

  @Override
  public String getHelpId() {
    return super.getHelpId() + ".bmaths";
  }

  @Override
  protected void initAssetProperties(PropertySet properties) {
    Property expression = PropertyHelper.createProperty(PROP_EXPRESSION, String.class, "The maths expression.");
    expression.getDescriptor().setNotNull(true);
    expression.getDescriptor().setNotEmpty(true);
    properties.addProperty(expression);

    Property asVirtual = PropertyHelper.createProperty(PROP_VIRTUAL, Boolean.class, "Create as virtual band.");
    asVirtual.getDescriptor().setNotNull(true);
    asVirtual.getDescriptor().setDefaultValue(true);
    properties.addProperty(asVirtual);

    Property dataType = PropertyHelper.createProperty(PROP_DATA_TYPE, DataType.class, "The data type.");
    dataType.getDescriptor().setNotNull(true);
    dataType.getDescriptor().setDefaultValue(DataType.FLOAT);
    properties.addProperty(dataType);

    Property noData = PropertyHelper.createProperty(PROP_GEO_NO_DATA, Double.class, "The geophysical no-data value.");
    properties.addProperty(noData);
  }

  @Override
  protected void initAddConfiguration(PropertySet addConfig, Asset asset, Product product) {
    Property name = PropertyHelper.createProperty(PROP_BAND_NAME, String.class, "The name of the new band.");
    name.getDescriptor().setNotNull(true);
    name.getDescriptor().setNotEmpty(true);
    name.getDescriptor().setDisplayName("Band name");
    name.getDescriptor().setDefaultValue(asset.getName());
    addConfig.addProperty(name);
  }

  @Override
  public void addToProduct(Asset asset, Product product, PropertySet addConfig, ProgressMonitor pm)
      throws AssetException {
    String bandName = addConfig.getValue(PROP_BAND_NAME);
    if (product.containsRasterDataNode(bandName)) {
      throw new AssetException(String.format("The product already contains a node with the name '%s'", bandName));
    }

    String expression = asset.getAssetProperties().getValue(PROP_EXPRESSION);
    throwIf(!product.isCompatibleBandArithmeticExpression(expression),
        new AssetException("The expression of the mask is not compatible with the selected product"));

    try {
      DataType dataType = asset.getAssetProperties().getValue(PROP_DATA_TYPE);
      Double noDataValue = asset.getAssetProperties().getValue(PROP_GEO_NO_DATA);
      Dimension sceneSize = product.getSceneRasterSize();
      Band band;
      if (asset.getAssetProperties().getValue(PROP_VIRTUAL)) {
        band = new VirtualBand(bandName, dataType.getProductDataType(),
            sceneSize.width, sceneSize.height, expression);
        band.setDescription(asset.getDescription());
        if (noDataValue != null) {
          band.setNoDataValueUsed(true);
          band.setGeophysicalNoDataValue(noDataValue);
        }
        if (product.isMultiSize()) {
          band.setGeoCoding(product.getSceneGeoCoding());
        }
        product.addBand(band);
      } else {
        int productDataType = dataType.getProductDataType();
        band = new Band(bandName, productDataType,
            sceneSize.width, sceneSize.height);
        Term term = VirtualBandOpImage.parseExpression(expression, product);
        band.setDescription(asset.getDescription());
        if (noDataValue != null) {
          band.setNoDataValueUsed(true);
          band.setGeophysicalNoDataValue(noDataValue);
        }
        if (product.isMultiSize()) {
          band.setGeoCoding(product.getSceneGeoCoding());
        }
        RenderedImage mathsBand = VirtualBandOpImage.builder(term)
                                                    .dataType(productDataType)
                                                    .fillValue(noDataValue)
                                                    .sourceSize(sceneSize)
                                                    .tileSize(product.getPreferredTileSize())
                                                    .create();
        band.setSourceImage(mathsBand);
        product.addBand(band);
      }
    } catch (Throwable e) {
      throw new AssetException("Could not add band maths asset to product", e);
    }
  }
}
