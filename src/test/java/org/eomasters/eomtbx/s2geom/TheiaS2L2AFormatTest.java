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

import static org.eomasters.eomtbx.s2geom.S2GeometryAngles.GRID_HEIGHT;
import static org.eomasters.eomtbx.s2geom.S2GeometryAngles.GRID_WIDTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.util.Map;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.junit.jupiter.api.Test;

class TheiaS2L2AFormatTest {

  @Test
  void testGetGeometricInfo() throws Exception {
    Product product = ProductIO.readProduct(new File(StdS2L2AFormatTest.class.getResource(
        "Theia_S2B_MSIL2A_metadata.dim").toURI()));

    S2DataFormat format = new TheiaS2L2AFormat(product);
    assertEquals("Theia_S2_MSI_Level-2A", format.getName());
    S2GeometryAngles s2GeometryAngles = format.getGeometryAngles();
    Map<Integer, Map<Integer, double[][]>> viewAzimuthAngles = s2GeometryAngles.getExtrapolatedViewAzimuthAngleGrids();
    Map<Integer, Map<Integer, double[][]>> viewZenithAngles = s2GeometryAngles.getExtrpolatedViewZenithAngleGrids();
    double[][] sunZenithAngles = s2GeometryAngles.getSunZenithAnglGrids();
    assertNull(viewAzimuthAngles.get(0));
    assertEquals(10, viewAzimuthAngles.size());
    assertEquals(6, viewAzimuthAngles.get(1).size());
    assertEquals(GRID_HEIGHT, viewAzimuthAngles.get(1).get(4).length);
    assertEquals(GRID_WIDTH, viewAzimuthAngles.get(1).get(4)[10].length);
    assertEquals(10, viewZenithAngles.size());
    assertEquals(6, viewZenithAngles.get(1).size());
    assertEquals(GRID_HEIGHT, viewZenithAngles.get(1).get(4).length);
    assertEquals(GRID_WIDTH, viewZenithAngles.get(1).get(4)[0].length);

    double[][] sunAzimuthAngles = s2GeometryAngles.getSunAzimuthAngles();
    assertEquals(GRID_HEIGHT, sunAzimuthAngles.length);
    assertEquals(GRID_WIDTH, sunZenithAngles[0].length);

    Map<Integer, Map<Integer, double[][]>> relativeAzimuthGrids = s2GeometryAngles.getRelativeAzimuthGrids();
    assertEquals(10, relativeAzimuthGrids.size());
    assertEquals(6, relativeAzimuthGrids.get(1).size());

    assertEquals(106.64600000, viewAzimuthAngles.get(1).get(1)[0][12], 1.0e-8);
    assertEquals(Double.NaN, viewAzimuthAngles.get(1).get(6)[10][15], 1.0e-8); // can be NaN because only a few lines cross the detector
    assertEquals(7.35680000, viewZenithAngles.get(1).get(1)[0][12], 1.0e-8);
    assertEquals(Double.NaN, viewZenithAngles.get(1).get(6)[10][15], 1.0e-8);
    assertEquals(105.79, sunAzimuthAngles[0][12], 1.0e-8);
    assertEquals(104.831, sunAzimuthAngles[10][15], 1.0e-8);
    assertEquals(21.6509, sunZenithAngles[0][12], 1.0e-8);
    assertEquals(21.3796, sunZenithAngles[10][15], 1.0e-8);
    assertEquals(0.85600000, relativeAzimuthGrids.get(1).get(1)[0][12], 1.0e-8);
    assertEquals(Double.NaN, relativeAzimuthGrids.get(1).get(6)[10][15], 1.0e-8);
  }

}
