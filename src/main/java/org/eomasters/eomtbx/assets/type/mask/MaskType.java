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

package org.eomasters.eomtbx.assets.type.mask;

import static org.eomasters.utils.Exceptions.throwIf;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.ValueRange;
import com.bc.ceres.core.ProgressMonitor;
import java.awt.Color;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetException;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.PropertyHelper;
import org.eomasters.icons.Icon;
import org.esa.snap.core.datamodel.Product;

public class MaskType extends AssetType {

  static final String PROP_EXPRESSION = "expression";
  static final String PROP_COLOR = "color";
  static final String PROP_TRANSPARENCY = "transparency";
  private static final String PROP_MASK_NAME = "maskName";

  public MaskType() {
    super("Mask", "Image Mask");
  }

  @Override
  public Icon getIcon() {
    return EomtbxIcons.MASK;
  }

  @Override
  public String getHelpId() {
    return super.getHelpId() + ".mask";
  }

  @Override
  protected void initAssetProperties(PropertySet properties) {
    Property expression = PropertyHelper.createProperty(PROP_EXPRESSION, String.class, "The boolean mask expression.");
    expression.getDescriptor().setNotNull(true);
    expression.getDescriptor().setNotEmpty(true);
    properties.addProperty(expression);

    Property color = PropertyHelper.createProperty(PROP_COLOR, Color.class, "The mask color.");
    color.getDescriptor().setNotNull(true);
    color.getDescriptor().setDefaultValue(Color.RED);
    properties.addProperty(color);

    Property transparency = PropertyHelper.createProperty(PROP_TRANSPARENCY, Double.class, "The mask transparency.");
    transparency.getDescriptor().setNotNull(true);
    transparency.getDescriptor().setValueRange(new ValueRange(0.0, 1.0));
    transparency.getDescriptor().setDefaultValue(0.5);
    properties.addProperty(transparency);
  }

  @Override
  protected void initAddConfiguration(PropertySet addConfig, Asset asset, Product product) {
    Property name = PropertyHelper.createProperty(PROP_MASK_NAME, String.class, "The name of the new mask.");
    name.getDescriptor().setNotNull(true);
    name.getDescriptor().setNotEmpty(true);
    name.getDescriptor().setDisplayName("Mask name");
    name.getDescriptor().setDefaultValue(asset.getName());
    addConfig.addProperty(name);
  }

  @Override
  public void addToProduct(Asset asset, Product product, PropertySet addConfig, ProgressMonitor pm)
      throws AssetException {
    String maskName = addConfig.getValue(PROP_MASK_NAME);
    if (product.containsRasterDataNode(maskName)) {
      throw new AssetException(String.format("The product already contains a node with the name '%s'", maskName));
    }

    String expression = asset.getAssetProperties().getValue(PROP_EXPRESSION);
    throwIf(!product.isCompatibleBandArithmeticExpression(expression),
        new AssetException("The expression of the mask is not compatible with the selected product"));

    try {
      product.addMask(maskName,
          expression,
          asset.getDescription(),
          asset.getAssetProperties().getValue(PROP_COLOR),
          asset.getAssetProperties().getValue(PROP_TRANSPARENCY));
    } catch (Throwable e) {
      throw new AssetException("Could not add mask asset to product", e);
    }
  }
}
