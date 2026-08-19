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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import java.nio.file.Paths;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.AssetTypeRegistry;
import org.esa.snap.core.datamodel.CrsGeoCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.VectorDataNode;
import org.esa.snap.core.util.SystemUtils;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GeometryTypeTest {

  @BeforeAll
  static void beforeAll() {
    // setting longitude first
    try {
      // GeoTools.init(new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, true));
      SystemUtils.initGeoTools();
      // Calling SystemUtils.initGeoTools changes the LoggerFactory, thus prevents a NPE when calling GeoTools.init directly
      // which sometimes occurs when running the tests with maven.
      // java.lang.NullPointerException
      // 	at org.geotools.util.logging.Logging.forceMonolineConsoleOutput(Logging.java:425)
      // 	at org.geotools.util.logging.Logging.forceMonolineConsoleOutput(Logging.java:406)
      // 	at org.geotools.util.factory.GeoTools.init(GeoTools.java:803)
      // 	at org.geotools.util.factory.GeoTools.init(GeoTools.java:725)
      // 	at org.eomasters.eomtbx.assets.type.geometry.GeometryTypeTest.beforeAll(GeometryTypeTest.java:55)
      // In addition it ensures we have the same settings as in SNAP.
    } catch (Throwable e) {
      e.printStackTrace();
      fail("Not able to init GeoTools");
    }
  }

  private static final String CRS_UTM32N_WKT =
      "PROJCS[\"WGS 84 / UTM zone 32N\", \n"
          + "    GEOGCS[\"WGS 84\", \n"
          + "      DATUM[\"World Geodetic System 1984\", \n"
          + "        SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]], \n"
          + "        AUTHORITY[\"EPSG\",\"6326\"]], \n"
          + "      PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], \n"
          + "      UNIT[\"degree\", 0.017453292519943295], \n"
          + "      AXIS[\"Geodetic longitude\", EAST], \n"
          + "      AXIS[\"Geodetic latitude\", NORTH], \n"
          + "      AUTHORITY[\"EPSG\",\"4326\"]], \n"
          + "    PROJECTION[\"Transverse_Mercator\", AUTHORITY[\"EPSG\",\"9807\"]], \n"
          + "    PARAMETER[\"central_meridian\", 9.0], \n"
          + "    PARAMETER[\"latitude_of_origin\", 0.0], \n"
          + "    PARAMETER[\"scale_factor\", 0.9996], \n"
          + "    PARAMETER[\"false_easting\", 500000.0], \n"
          + "    PARAMETER[\"false_northing\", 0.0], \n"
          + "    UNIT[\"m\", 1.0], \n"
          + "    AXIS[\"Easting\", EAST], \n"
          + "    AXIS[\"Northing\", NORTH], \n"
          + "    AUTHORITY[\"EPSG\",\"32632\"]]";

  @Test
  void testBasicProperties() {
    GeometryType type = new GeometryType();
    assertEquals("Geometry", type.getName());
    assertEquals("A geometry", type.getDescription());
    assertEquals("eomtbx.assetLibrary.type.geometry", type.getHelpId());
  }

  @Test
  void testAssetProperties() {
    GeometryType type = new GeometryType();
    PropertyContainer propertyContainer = type.createAssetProperties();

    assertNull(propertyContainer.getValue(GeometryType.PROP_WKT));
    assertEquals("EPSG:4326", propertyContainer.getValue(GeometryType.PROP_CRS));
  }

  @Test
  void getFromRegistry() {
    AssetTypeRegistry registry = AssetTypeRegistry.instance();

    AssetType type = registry.getService("GeometryType");
    assertNotNull(type);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void addToProduct_WithShapefile(boolean cropToProduct) throws Exception {
    var factory = new FileGeometryCreationService();
    PropertySet properties = factory.createFactoryProperties();
    properties.setValue(FileGeometryCreationService.PROP_SOURCE_FILE,
        Paths.get(GeometryType.class.getResource("test_northgermany.shp").toURI()));

    Asset asset = factory.createAsset("Test Geometry", "Test Description", new String[]{"tag1", "tag2"},
        properties, ProgressMonitor.NULL);

    Product product = new Product("Test Product", "Test Product Type", 10000, 10000);
    product.setSceneGeoCoding(
        new CrsGeoCoding(CRS.parseWKT(CRS_UTM32N_WKT), 10000, 10000, 600000, 6000000, 10, 10, 0.0, 0.0));

    PropertySet addConfig = asset.getType().createAddConfiguration(asset, product);
    addConfig.getProperty(GeometryType.PROP_CREATE_VECTOR_NODE).setValue(true);
    addConfig.getProperty(GeometryType.PROP_NEW_NODE_NAME).setValue("northern_germany");
    addConfig.getProperty(GeometryType.PROP_CROP_TO_BOUNDS).setValue(cropToProduct);
    asset.getType().addToProduct(asset, product, addConfig, ProgressMonitor.NULL);
    VectorDataNode vectorDataNode = product.getVectorDataGroup().get("northern_germany");
    assertEquals(1, vectorDataNode.getFeatureCollection().getCount());
  }

}
