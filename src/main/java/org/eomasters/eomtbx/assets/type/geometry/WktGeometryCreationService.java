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

package org.eomasters.eomtbx.assets.type.geometry;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetException;
import org.eomasters.eomtbx.assets.AssetCreationService;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.PropertyHelper;
import org.eomasters.eomtbx.validator.EpsgCrsValidator;
import org.eomasters.eomtbx.validator.WktValidator;

public class WktGeometryCreationService extends AssetCreationService {
  // todo - Allow to switch between WKT, geoJson, maybe GML
  public static final String PROP_WKT = GeometryType.PROP_WKT;
  public static final String PROP_CRS = GeometryType.PROP_CRS;

  @Override
  public String getName() {
    return "WKT Geometry";
  }

  @Override
  public String getDescription() {
    return "Creates a geometry from a WKT string";
  }

  @Override
  public Class<? extends AssetType> getAssetTypeClass() {
    return GeometryType.class;
  }

  @Override
  public String getHelpId() {
    return super.getHelpId() + ".type.geometry";
  }

  @Override
  public Asset createAsset(String name, String description, String[] tags, PropertySet properties, ProgressMonitor pm) throws AssetException {
    if (name == null || name.isBlank()) {
      throw new AssetException("A name must not be given for the asset.");
    }
    Asset geometryAsset = new Asset(name, new GeometryType(), description, tags);

    PropertyContainer assetProperties = geometryAsset.getAssetProperties();
    String wktString = properties.getProperty(PROP_WKT).getValue();
    String crsCode = properties.getProperty(PROP_CRS).getValue();

    assetProperties.setValue(GeometryType.PROP_WKT, wktString);
    assetProperties.setValue(GeometryType.PROP_CRS, crsCode);

    return geometryAsset;
  }

  @Override
  protected void initFactoryProperties(PropertySet properties) {
    Property wkt = PropertyHelper.createProperty(PROP_WKT, String.class, "The Geometry in WKT format");
    wkt.getDescriptor().setNotNull(true);
    wkt.getDescriptor().setNotEmpty(true);
    wkt.getDescriptor().setDisplayName("WKT Geometry");
    wkt.getDescriptor().setValidator(new WktValidator());
    properties.addProperty(wkt);

    Property crs = PropertyHelper.createProperty(PROP_CRS, String.class, "The CRS as EPSG code");
    crs.getDescriptor().setNotNull(true);
    crs.getDescriptor().setNotEmpty(true);
    crs.getDescriptor().setDisplayName("CRS Code");
    crs.getDescriptor().setDefaultValue(GeometryType.DEFAULT_CRS);
    crs.getDescriptor().setValidator(new EpsgCrsValidator());
    properties.addProperty(crs);
  }
}
