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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WktGeometryFactoryTest {

  private WktGeometryCreationService service;

  @BeforeEach
  public void setUp() {
    service = new WktGeometryCreationService();
  }

  @Test
  void testBasicProperties() {
    assertEquals("WKT Geometry", service.getName());
    assertEquals("Creates a geometry from a WKT string", service.getDescription());
    assertEquals("eomtbx.assetLibrary.type.geometry", service.getHelpId());
    assertEquals(GeometryType.class, service.getAssetTypeClass());
  }

  @Test
  void createAsset() throws AssetException {
    PropertySet props = service.createFactoryProperties();
    props.setValue(GeometryType.PROP_WKT, "POINT(1 2)");
    props.setValue(GeometryType.PROP_CRS, "EPSG:32632");
    Asset asset = service.createAsset("test", "test_descr", new String[0], props, ProgressMonitor.NULL);
    assertEquals(GeometryType.class, asset.getType().getClass());
    assertEquals("test", asset.getName());
    assertEquals("test_descr", asset.getDescription());
    assertEquals(3, asset.getAssetProperties().getProperties().length);
    assertEquals("POINT(1 2)", asset.getAssetProperties().getValue(GeometryType.PROP_WKT));
    assertEquals("EPSG:32632", asset.getAssetProperties().getValue(GeometryType.PROP_CRS));
    assertNull(asset.getAssetProperties().getValue(GeometryType.PROP_SOURCE_FILE));

  }

  @Test
  void createFactoryProperties() {
    PropertySet props = service.createFactoryProperties();

    PropertyDescriptor wktDescriptor = props.getProperty(GeometryType.PROP_WKT).getDescriptor();
    assertEquals("wkt", wktDescriptor.getName());
    assertEquals("The Geometry in WKT format", wktDescriptor.getDescription());
    assertTrue(wktDescriptor.isNotEmpty());
    assertTrue(wktDescriptor.isNotNull());

    PropertyDescriptor crsDescriptor = props.getProperty(GeometryType.PROP_CRS).getDescriptor();
    assertEquals("crs", crsDescriptor.getName());
    assertEquals("The CRS as EPSG code", crsDescriptor.getDescription());
    assertTrue(crsDescriptor.isNotEmpty());
    assertTrue(crsDescriptor.isNotNull());

  }
}
