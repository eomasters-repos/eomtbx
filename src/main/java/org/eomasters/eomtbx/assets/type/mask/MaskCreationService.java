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

import static org.eomasters.eomtbx.assets.type.mask.MaskType.PROP_COLOR;
import static org.eomasters.eomtbx.assets.type.mask.MaskType.PROP_EXPRESSION;
import static org.eomasters.eomtbx.assets.type.mask.MaskType.PROP_TRANSPARENCY;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.ValueRange;
import com.bc.ceres.core.ProgressMonitor;
import java.awt.Color;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetCreationService;
import org.eomasters.eomtbx.assets.AssetException;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.PropertyHelper;

public class MaskCreationService extends AssetCreationService {

  @Override
  public String getName() {
    return "Mask";
  }

  @Override
  public String getDescription() {
    return "Creates an image mask.";
  }

  @Override
  public String getHelpId() {
    return super.getHelpId() + ".type.mask";
  }

  @Override
  public Class<? extends AssetType> getAssetTypeClass() {
    return MaskType.class;
  }

  @Override
  public Asset createAsset(String name, String description, String[] tags, PropertySet properties, ProgressMonitor pm)
      throws AssetException {
    try {
      Asset maskAsset = new Asset(name, new MaskType(), description, tags);
      maskAsset.getAssetProperties().setValue(PROP_EXPRESSION, properties.getValue(PROP_EXPRESSION));
      maskAsset.getAssetProperties().setValue(PROP_COLOR, properties.getValue(PROP_COLOR));
      maskAsset.getAssetProperties().setValue(PROP_TRANSPARENCY, properties.getValue(PROP_TRANSPARENCY));
      return maskAsset;
    } catch (Throwable t) {
      throw new AssetException("Failed to create mask asset", t);
    }
  }

  @Override
  protected void initFactoryProperties(PropertySet properties) {
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
}
