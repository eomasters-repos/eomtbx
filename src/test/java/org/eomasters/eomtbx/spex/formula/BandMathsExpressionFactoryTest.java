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

package org.eomasters.eomtbx.spex.formula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.eomasters.eomtbx.spex.AbstractSpex;
import org.eomasters.eomtbx.spex.CustomSpex;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.jupiter.api.Test;

class BandMathsExpressionFactoryTest {

  @Test
  void testFindWvlBandSingleMatch() {
    Product product = new Product("p", "t", 10, 10);
    addBand(product, "band1", 520.0f);
    String bandName = BandMathsExpressionFactory.findWvlBand(500.0, 530.0, List.of(product.getBands()));
    assertEquals("band1", bandName, "Expected to find a single matching band.");
  }

  @Test
  void testFindWvlBandMultipleMatches() {
    Product product = new Product("p", "t", 10, 10);
    addBand(product, "band1", 520.0f);
    addBand(product, "band2", 525.0f);
    String bandName = BandMathsExpressionFactory.findWvlBand(500.0, 530.0, List.of(product.getBands()));
    assertEquals("band1", bandName, "Expected to return the closest matching band in case of multiple matches.");
  }

  @Test
  void testSentinel2NirBands() {
    Product product = new Product("p", "t", 10, 10);
    addBand(product, "B8", 842.0f);
    addBand(product, "B8A", 865.0f);

    SpexBand nirBand = SpexBand.N;
    String bandName = BandMathsExpressionFactory.findWvlBand(nirBand.getMinWavelength(), nirBand.getMaxWavelength(), List.of(product.getBands()));
    assertEquals("B8", bandName, "Expected to return the closest matching band in case of multiple matches.");
  }

  @Test
  void testFindWvlBandNoMatch() {
    Product product = new Product("p", "t", 10, 10);
    addBand(product, "band1", 400.0f);
    String bandName = BandMathsExpressionFactory.findWvlBand(500.0, 530.0, List.of(product.getBands()));
    assertEquals(null, bandName, "Expected null when no bands match the wavelength range.");
  }

  @Test
  void testExpressionGeneration() {
    Product product = new Product("p", "t", 10, 10);
    addBand(product, "green", 520.3f);
    addBand(product, "swir", 2310.0f);
    AbstractSpex spex = new CustomSpex("test");
    spex.setFormula("C1 * (G1 / bnd(2280:2315))");

    String expression = BandMathsExpressionFactory.expandFormula(spex, product);
    assertEquals("6.0 * (green / swir)", expression);
  }

  @Test
  void testAreCompatible() {
    Product product = new Product("p", "t", 10, 10);
    addBand(product, "green", 520.3f);
    AbstractSpex spex = new CustomSpex("test");
    spex.setFormula("C1 * (G1 / bnd(2280:2315))");

    assertFalse(BandMathsExpressionFactory.areCompatible(spex, product));
  }

  private static void addBand(Product product, String name, float wvl) {
    Band band = product.addBand(name, ProductData.TYPE_INT8);
    band.setSpectralWavelength(wvl);
  }

}
