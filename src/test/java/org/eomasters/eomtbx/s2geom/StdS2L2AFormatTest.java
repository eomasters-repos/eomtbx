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

package org.eomasters.eomtbx.s2geom;

import static org.eomasters.eomtbx.s2geom.S2GeometryAngles.GRID_WIDTH;
import static org.eomasters.eomtbx.s2geom.S2GeometryAngles.NUM_S2_BANDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Map;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.junit.jupiter.api.Test;

class StdS2L2AFormatTest {

  @Test
  void testGetGeometricInfo() throws Exception {
    Product product = ProductIO.readProduct(new File(StdS2L2AFormatTest.class.getResource(
        "ESA_S2B_MSIL2A_metadata.znap.zip").toURI()));

    S2DataFormat format = new StdS2L2AFormat(product);
    assertEquals("Standard_S2_MSI_Level-2A", format.getName());
    S2GeometryAngles s2GeometryAngles = format.getGeometryAngles();
    Map<Integer, Map<Integer, double[][]>> viewAzimuthAngles = s2GeometryAngles.getExtrapolatedViewAzimuthAngleGrids();
    Map<Integer, Map<Integer, double[][]>> viewZenithAngles = s2GeometryAngles.getExtrpolatedViewZenithAngleGrids();
    double[][] sunAzimuthAngles = s2GeometryAngles.getSunAzimuthAngles();
    double[][] sunZenithAngles = s2GeometryAngles.getSunZenithAnglGrids();
    Map<Integer, Map<Integer, double[][]>> relativeAzimuthGrids = s2GeometryAngles.getRelativeAzimuthGrids();
    assertEquals(NUM_S2_BANDS, viewAzimuthAngles.size());
    assertEquals(NUM_S2_BANDS, viewZenithAngles.size());
    assertEquals(NUM_S2_BANDS, relativeAzimuthGrids.size());
    Map<Integer, double[][]> viewAziAngleB2Detectors = viewAzimuthAngles.get(1);
    assertEquals(6, viewAziAngleB2Detectors.size());
    assertEquals(GRID_WIDTH, viewAziAngleB2Detectors.get(7).length);
    assertEquals(GRID_WIDTH, sunAzimuthAngles.length);

    assertEquals(320.13299999, viewAziAngleB2Detectors.get(7)[0][12], 1.0e-8);
    assertEquals(284.50099999, viewAziAngleB2Detectors.get(8)[10][15], 1.0e-8);
    assertEquals(4.8636200000, viewZenithAngles.get(1).get(7)[0][12], 1.0e-8);
    assertEquals(7.3308800000, viewZenithAngles.get(1).get(8)[10][15], 1.0e-8);
    assertEquals(119.428, sunAzimuthAngles[0][12], 1.0e-8);
    assertEquals(118.607, sunAzimuthAngles[10][15], 1.0e-8);
    assertEquals(18.8709, sunZenithAngles[0][12], 1.0e-8);
    assertEquals(18.5092, sunZenithAngles[10][15], 1.0e-8);
    assertEquals(200.704999999, relativeAzimuthGrids.get(1).get(7)[0][12], 1.0e-8);
    assertEquals(165.893999999, relativeAzimuthGrids.get(1).get(8)[10][15], 1.0e-8);

  }
}
