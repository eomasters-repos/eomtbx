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

import static org.eomasters.eomtbx.assets.type.bmaths.BandMathsType.PROP_DATA_TYPE;
import static org.eomasters.eomtbx.assets.type.bmaths.BandMathsType.PROP_EXPRESSION;
import static org.eomasters.eomtbx.assets.type.bmaths.BandMathsType.PROP_GEO_NO_DATA;
import static org.eomasters.eomtbx.assets.type.bmaths.BandMathsType.PROP_VIRTUAL;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetCreationService;
import org.eomasters.eomtbx.assets.AssetException;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.PropertyHelper;

public class BandMathsCreationService extends AssetCreationService {

  @Override
  public String getName() {
    return "Band Maths";
  }

  @Override
  public String getDescription() {
    return "Creates a band using an expression";
  }

  @Override
  public Class<? extends AssetType> getAssetTypeClass() {
    return BandMathsType.class;
  }

  @Override
  public Asset createAsset(String name, String description, String[] tags, PropertySet properties, ProgressMonitor pm)
      throws AssetException {
    try {
      Asset maskAsset = new Asset(name, new BandMathsType(), description, tags);
      maskAsset.getAssetProperties().setValue(PROP_EXPRESSION, properties.getValue(PROP_EXPRESSION));
      maskAsset.getAssetProperties().setValue(PROP_VIRTUAL, properties.getValue(PROP_VIRTUAL));
      maskAsset.getAssetProperties().setValue(PROP_DATA_TYPE, properties.getValue(PROP_DATA_TYPE));
      maskAsset.getAssetProperties().setValue(PROP_GEO_NO_DATA, properties.getValue(PROP_GEO_NO_DATA));
      return maskAsset;
    } catch (Throwable t) {
      throw new AssetException("Failed to create band maths asset", t);
    }
  }

  @Override
  protected void initFactoryProperties(PropertySet properties) {
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
}
