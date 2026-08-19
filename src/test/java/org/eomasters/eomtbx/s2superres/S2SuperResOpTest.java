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

package org.eomasters.eomtbx.s2superres;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;
import org.junit.jupiter.api.*;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

class S2SuperResOpTest {

  private static final URL S2_PRODUCT_URL1 = S2SuperResOpTest.class.getResource(
      "optest/S2C_MSIL1C_20251103T143801_N0511_R096_T19KER_subset.znap.zip");
  private static final URL S2_PRODUCT_URL2 = S2SuperResOpTest.class.getResource(
      "optest/spectrum_subset_S2B_MSIL1C_20240802T154809.znap.zip");
  private static final String[] SPECTRUM_BAND_NAMES = new String[]{"B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8",
      "B8A", "B9", "B10", "B11", "B12"};
  private static final Point TEST_PIXEL_P1 = new Point(10, 11);
  private static final Point TEST_Pin1_P2 = new Point( 247,145);
  private static final Point TEST_Pin2_P2 = new Point(171, 120);

  private static Path utmShapeFileP1;
  private static Geometry clipGeometryP1;
  private static Product s2Product1;
  private static Product s2Product2;

  @BeforeAll
  static void beforeAll() throws URISyntaxException, IOException {
    utmShapeFileP1 = Path.of(S2SuperResOpTest.class.getResource("polygon_utm/geometry_Polygon.shp").toURI());
    GeometryFactory geometryFactory = new GeometryFactory();
    clipGeometryP1 = geometryFactory.createPolygon(
        geometryFactory.createLinearRing(new org.locationtech.jts.geom.Coordinate[]{
            new org.locationtech.jts.geom.Coordinate(-72.0, -21.0),
            new org.locationtech.jts.geom.Coordinate(-66.0, -21.0),
            new org.locationtech.jts.geom.Coordinate(-66.0, -33.0),
            new org.locationtech.jts.geom.Coordinate(-72.0, -33.0),
            new org.locationtech.jts.geom.Coordinate(-72.0, -21.0)
        })
    );
    s2Product1 = ProductIO.readProduct(new File(S2_PRODUCT_URL1.toURI()));
    s2Product2 = ProductIO.readProduct(new File(S2_PRODUCT_URL2.toURI()));
  }

  @Test
  void testLoadShapeFileGeometryWGS84() {
    var geometry = S2SuperResOp.loadShapeFileGeometryWGS84(utmShapeFileP1, clipGeometryP1);
    assertEquals(
        "POLYGON ((-68.93743620419316 -22.32400518530544, "
            + "-68.93743620419316 -22.317030138916824, "
            + "-68.92538161924325 -22.317030138916824, "
            + "-68.92538161924325 -22.32400518530544, "
            + "-68.93743620419316 -22.32400518530544))",
        geometry.toText());
  }

  @Test
  void testOperator_defaults_1() throws IOException {
    var parameters = new HashMap<String, Object>();
    parameters.put("wktRegion", """
                                POLYGON ((-68.86091526555502 -22.341172560716227, -68.85107997807506 -22.341163819626132,\s
                                   -68.8510706967341 -22.349900746783245, -68.8609065971868 -22.349909491652138,\s
                                   -68.86091526555502 -22.341172560716227))""");
    var superResProduct = GPF.createProduct("S2SuperRes", parameters, s2Product1);
    assertEquals(10, superResProduct.getNumBands());

    var spectrum = getSpectrum(superResProduct, TEST_PIXEL_P1);
    assertEquals(0.2488, spectrum.get("B2"), 1e-6);
    assertEquals(0.2720, spectrum.get("B3"), 1e-6);
    assertEquals(0.3254, spectrum.get("B4"), 1e-6);
    assertEquals(0.3392, spectrum.get("B5"), 1e-6);
    assertEquals(0.3479, spectrum.get("B6"), 1e-6);
    assertEquals(0.3539, spectrum.get("B7"), 1e-6);
    assertEquals(0.3395, spectrum.get("B8"), 1e-6);
    assertEquals(0.3553, spectrum.get("B8A"), 1e-6);
    assertEquals(0.3672, spectrum.get("B11"), 1e-6);
    assertEquals(0.3065, spectrum.get("B12"), 1e-6);

    var tiePointGridGroup = superResProduct.getTiePointGridGroup();
    assertEquals(18, tiePointGridGroup.getNodeCount());
  }

  @Test
  void testOperator_defaults_2() throws IOException {
    var parameters = new HashMap<String, Object>();
    var superResProduct = GPF.createProduct("S2SuperRes", parameters, s2Product2);
    assertEquals(10, superResProduct.getNumBands());

    // src Pin1 139,88 -
    //0.30930, 0.26470, 0.26890, 0.30220, 0.30400, 0.41470, 0.50580, 0.42990, 0.53950, 0.06920, 0.00120, 0.41570, 0.30500
    // dst Pin1 247,145 -
    var spectrum1 = getSpectrum(superResProduct, TEST_Pin1_P2);
    assertEquals(0.2764, spectrum1.get("B2"), 2e-3);
    assertEquals(0.2689, spectrum1.get("B3"), 5e-3);
    assertEquals(0.2912, spectrum1.get("B4"), 2e-3);
    assertEquals(0.3004, spectrum1.get("B5"), 2e-3);
    assertEquals(0.4099, spectrum1.get("B6"), 2e-3);
    assertEquals(0.4964, spectrum1.get("B7"), 2e-3);
    assertEquals(0.4342, spectrum1.get("B8"), 2e-3);
    assertEquals(0.5288, spectrum1.get("B8A"), 2e-3);
    assertEquals(0.4278, spectrum1.get("B11"), 2e-3);
    assertEquals(0.3017, spectrum1.get("B12"), 2e-3);

    // src Pin2 101,76 -
    //0.12190, 0.11010, 0.10730, 0.09470, 0.10810, 0.25890, 0.37570, 0.33860, 0.41170, 0.03460, 0.00080, 0.22990, 0.11990
    // dst Pin2 171,120 -
    var spectrum2 = getSpectrum(superResProduct, TEST_Pin2_P2);
    assertEquals(0.1114, spectrum2.get("B2"), 2e-3);
    assertEquals(0.1077, spectrum2.get("B3"), 2e-3);
    assertEquals(0.0921, spectrum2.get("B4"), 2e-3);
    assertEquals(0.1078, spectrum2.get("B5"), 2e-3);
    assertEquals(0.2478, spectrum2.get("B6"), 2e-3);
    assertEquals(0.3563, spectrum2.get("B7"), 2e-3);
    assertEquals(0.3356, spectrum2.get("B8"), 2e-3);
    assertEquals(0.3909, spectrum2.get("B8A"), 2e-3);
    assertEquals(0.2317, spectrum2.get("B11"), 2e-3);
    assertEquals(0.1269, spectrum2.get("B12"), 2e-3);

  }

  @Test
  void testOperator_withSelectedResolvedBands() throws IOException {
    var parameters = new HashMap<String, Object>();
    parameters.put("wktRegion", """
                                POLYGON ((-68.86091526555502 -22.341172560716227, -68.85107997807506 -22.341163819626132,\s
                                   -68.8510706967341 -22.349900746783245, -68.8609065971868 -22.349909491652138,\s
                                   -68.86091526555502 -22.341172560716227))""");
    parameters.put("superResolveBands", "B2,B3,B4,B8,B11,B12");
    var superResProduct = GPF.createProduct("S2SuperRes", parameters, s2Product1);
    assertEquals(6, superResProduct.getNumBands());

    var spectrum = getSpectrum(superResProduct, TEST_PIXEL_P1);
    assertEquals(0.2488, spectrum.get("B2"), 1e-6);
    assertEquals(0.2720, spectrum.get("B3"), 1e-6);
    assertEquals(0.3254, spectrum.get("B4"), 1e-6);
    assertEquals(0.3395, spectrum.get("B8"), 1e-6);
    assertEquals(0.3672, spectrum.get("B11"), 1e-6);
    assertEquals(0.3065, spectrum.get("B12"), 1e-6);

    var tiePointGridGroup = superResProduct.getTiePointGridGroup();
    assertEquals(18, tiePointGridGroup.getNodeCount());
  }
  @Test
  void testOperator_withMoreBands() throws IOException {
    var parameters = new HashMap<String, Object>();
    parameters.put("wktRegion", """
                                POLYGON ((-68.86091526555502 -22.341172560716227, -68.85107997807506 -22.341163819626132,\s
                                   -68.8510706967341 -22.349900746783245, -68.8609065971868 -22.349909491652138,\s
                                   -68.86091526555502 -22.341172560716227))""");
    parameters.put("superResolveBands", "B2,B3,B4,B8,B11,B12");
    parameters.put("resampleOtherBands", true);

    var superResProduct = GPF.createProduct("S2SuperRes", parameters, s2Product1);
    var bandGroup = superResProduct.getBandGroup();
    assertEquals(9, bandGroup.getNodeCount());

    assertTrue(bandGroup.contains("B2"));
    assertTrue(bandGroup.contains("B3"));
    assertTrue(bandGroup.contains("B4"));
    assertTrue(bandGroup.contains("B8"));
    assertTrue(bandGroup.contains("B11"));
    assertTrue(bandGroup.contains("B12"));
    assertTrue(bandGroup.contains("B_opaque_clouds"));
    assertTrue(bandGroup.contains("B_cirrus_clouds"));
    assertTrue(bandGroup.contains("B_snow_and_ice_areas"));
  }

  @Test
  void testOperator_defaults_with60mBands() throws IOException {
    var parameters = new HashMap<String, Object>();
    parameters.put("wktRegion", """
                                POLYGON ((-68.86091526555502 -22.341172560716227, -68.85107997807506 -22.341163819626132,\s
                                   -68.8510706967341 -22.349900746783245, -68.8609065971868 -22.349909491652138,\s
                                   -68.86091526555502 -22.341172560716227))""");
    parameters.put("resample60mBands", true);
    var superResProduct = GPF.createProduct("S2SuperRes", parameters, s2Product1);
    assertEquals(13, superResProduct.getNumBands());

    var spectrum = getSpectrum(superResProduct, TEST_PIXEL_P1);
    assertEquals(0.2504, spectrum.get("B1"), 1e-6);
    assertEquals(0.2488, spectrum.get("B2"), 1e-6);
    assertEquals(0.2720, spectrum.get("B3"), 1e-6);
    assertEquals(0.3254, spectrum.get("B4"), 1e-6);
    assertEquals(0.3392, spectrum.get("B5"), 1e-6);
    assertEquals(0.3479, spectrum.get("B6"), 1e-6);
    assertEquals(0.3539, spectrum.get("B7"), 1e-6);
    assertEquals(0.3395, spectrum.get("B8"), 1e-6);
    assertEquals(0.3553, spectrum.get("B8A"), 1e-6);
    assertEquals(0.2516, spectrum.get("B9"), 1e-6);
    assertEquals(0.0245, spectrum.get("B10"), 1e-6);
    assertEquals(0.3672, spectrum.get("B11"), 1e-6);
    assertEquals(0.3065, spectrum.get("B12"), 1e-6);

    var tiePointGridGroup = superResProduct.getTiePointGridGroup();
    assertEquals(18, tiePointGridGroup.getNodeCount());
  }

  private static HashMap<String, Double> getSpectrum(Product product, Point pixel) throws IOException {
    var spectrum = new HashMap<String, Double>();
    for (String bandName : SPECTRUM_BAND_NAMES) {
      var band = product.getBand(bandName);
      if (band == null) {
        spectrum.put(bandName, null);
      } else {
        var doubles = band.readPixels(pixel.x, pixel.y, 1, 1, new double[1]);
        spectrum.put(bandName, doubles[0]);
      }
    }
    return spectrum;
  }
}
