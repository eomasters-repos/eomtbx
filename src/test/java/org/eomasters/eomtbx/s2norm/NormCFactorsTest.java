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

package org.eomasters.eomtbx.s2norm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import org.eomasters.eomtbx.s2geom.S2DataFormat;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.junit.jupiter.api.Test;

class NormCFactorsTest {

  @Test
  void getCFactorsMap() throws Exception {
    Product product = ProductIO.readProduct(new File(NormCFactorsTest.class.getResource(
        "/org/eomasters/eomtbx/s2geom/ESA_S2B_MSIL2A_metadata.znap.zip").toURI()));

    S2DataFormat format = S2DataFormat.getSupportedFormat(product);
    NormCFactors normCFactors = NormCFactors.create(format);

    RenderedImage cFactorImageB2 = normCFactors.getScaledCFactorImage(1);
    Raster data = cFactorImageB2.getData(new Rectangle(0, 0, 1001, 3001));
    assertEquals(1.00098194, data.getSampleDouble(0, 0, 0), 1.0e-8);
    assertEquals(1.00353405, data.getSampleDouble(1000, 50, 0), 1.0e-8);
    assertEquals(1.00347201, data.getSampleDouble(100, 3000, 0), 1.0e-8);

    RenderedImage cFactorImageB5 = normCFactors.getScaledCFactorImage(4);
    assertEquals(1.00098194, data.getSampleDouble(0, 0, 0), 1.0e-8);
    assertEquals(1.00353405, data.getSampleDouble(1000, 50, 0), 1.0e-8);
    assertEquals(1.00347201, data.getSampleDouble(100, 3000, 0), 1.0e-8);

    RenderedImage cFactorImageB11 = normCFactors.getScaledCFactorImage(11);
    assertEquals(1.00589594, cFactorImageB11.getData().getSampleDouble(100, 3000, 0), 1.0e-8);
  }
}
