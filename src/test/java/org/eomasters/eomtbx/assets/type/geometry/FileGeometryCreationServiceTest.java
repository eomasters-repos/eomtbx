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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetException;
import org.eomasters.eomtbx.converter.ConverterRegistrar;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileGeometryCreationServiceTest {

  private FileGeometryCreationService factory;

  @BeforeAll
  static void beforeAll() {
    ConverterRegistrar.registerConverter();
  }

  @AfterAll
  static void afterAll() {
    ConverterRegistrar.deregisterConverter();
  }

  @BeforeEach
  public void setUp() {
    factory = new FileGeometryCreationService();
  }

  @Test
  void testBasicProperties() {
    assertEquals("Geometry File", factory.getName());
    assertEquals("eomtbx.assetLibrary.type.geometry", factory.getHelpId());
    assertEquals("Reads a geometry from a file (Shapefile, WKT)", factory.getDescription());
    assertEquals(GeometryType.class, factory.getAssetTypeClass());
  }


  @Test
  void createAssetWithShapefile() throws AssetException, URISyntaxException {
    PropertySet properties = factory.createFactoryProperties();
    properties.setValue(FileGeometryCreationService.PROP_SOURCE_FILE,
        Paths.get(GeometryType.class.getResource("test_northgermany.shp").toURI()));

    Asset asset = factory.createAsset("testName", "testDescr", new String[]{"noTag"}, properties, ProgressMonitor.NULL);

    assertEquals(GeometryType.class, asset.getType().getClass());
    assertEquals("testName", asset.getName());
    assertEquals("testDescr", asset.getDescription());
    assertArrayEquals(new String[]{"noTag"}, asset.getTags());
    PropertyContainer assetProperties = asset.getAssetProperties();
    assertEquals(3, assetProperties.getProperties().length);
    assertNotNull(assetProperties.getProperty(GeometryType.PROP_WKT).getValue());
    assertEquals("EPSG:4326", assetProperties.getProperty(GeometryType.PROP_CRS).<String>getValue());
    String shapefilePath = String.join(File.separator, "org", "eomasters", "eomtbx", "assets",
        "type", "geometry", "test_northgermany.shp");
    assertTrue(assetProperties.getProperty(FileGeometryCreationService.PROP_SOURCE_FILE).getValueAsText()
                              .endsWith(shapefilePath));
  }

  @Test
  void createAssetWithWkt() throws AssetException, URISyntaxException {
    PropertySet properties = factory.createFactoryProperties();
    properties.setValue(FileGeometryCreationService.PROP_SOURCE_FILE,
        Paths.get(GeometryType.class.getResource("test_SF_Bay_WKT.txt").toURI()));

    Asset asset = factory.createAsset("testName2", "testDescr2", new String[]{"noTag2"}, properties,
        ProgressMonitor.NULL);

    assertEquals(GeometryType.class, asset.getType().getClass());
    assertEquals("testName2", asset.getName());
    assertEquals("testDescr2", asset.getDescription());
    assertArrayEquals(new String[]{"noTag2"}, asset.getTags());
    PropertyContainer assetProperties = asset.getAssetProperties();
    assertEquals(3, assetProperties.getProperties().length);
    assertNotNull(assetProperties.getProperty(GeometryType.PROP_WKT).getValue());
    assertEquals("EPSG:4326", assetProperties.getProperty(GeometryType.PROP_CRS).getValue());
    String wktfilePath = String.join(File.separator, "org", "eomasters", "eomtbx", "assets",
        "type", "geometry", "test_SF_Bay_WKT.txt");
    assertTrue(assetProperties.getProperty(FileGeometryCreationService.PROP_SOURCE_FILE).getValueAsText()
                              .endsWith(wktfilePath));
  }

  @Test
  void initFactoryProperties() {
    PropertySet properties = factory.createFactoryProperties();

    PropertyDescriptor sourceFileDescriptor = properties.getProperty(FileGeometryCreationService.PROP_SOURCE_FILE)
                                                        .getDescriptor();
    assertEquals("sourceFile", sourceFileDescriptor.getName());
    assertEquals("Path to file which defines a geometry.", sourceFileDescriptor.getDescription());
    assertTrue(sourceFileDescriptor.isNotEmpty());
    assertTrue(sourceFileDescriptor.isNotNull());

    PropertyDescriptor crsDescriptor = properties.getProperty(FileGeometryCreationService.PROP_CRS).getDescriptor();
    assertEquals("crs", crsDescriptor.getName());
    assertEquals("The CRS as EPSG code. The CRS defined by the input, if available, has priority.",
        crsDescriptor.getDescription());
    assertTrue(crsDescriptor.isNotEmpty());
    assertTrue(crsDescriptor.isNotNull());

    assertNotNull(properties.getProperty(FileGeometryCreationService.PROP_SOURCE_FILE));


  }
}
