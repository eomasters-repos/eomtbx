/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2024 Marco Peters
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

package org.eomasters.eomtbx.bandmathsext;

import static org.eomasters.eomtbx.TestUtils.toElemIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.Raster;
import java.io.IOException;
import org.eomasters.eomtbx.TestUtils;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.dataop.barithm.BandArithmetic;
import org.esa.snap.core.dataop.barithm.RasterDataEvalEnv;
import org.esa.snap.core.dataop.barithm.RasterDataSymbol;
import org.esa.snap.core.jexp.ParseException;
import org.esa.snap.core.jexp.Term;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MultiInputFunctionsTest {

  private static Product product;
  private RasterDataEvalEnv evalEnv;

  @BeforeEach
  void beforeEach() {
    product = TestUtils.createProduct();
    evalEnv = new RasterDataEvalEnv(0, 0, product.getSceneRasterWidth(), product.getSceneRasterHeight());
  }

  @Test
  void testMin_withBandsOnly() throws ParseException, IOException {
    Term term = BandArithmetic.parseExpression("minOf(B1, B2)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0)); // Both bands are nan
    assertEquals(Double.NaN, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(4, 4)); // B2 valid
    assertEquals(88.0, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 4)); // both valid
    assertEquals(48.0, term.evalD(evalEnv));
  }

  @Test
  void testMax_withBandsOnly() throws ParseException, IOException {
    Term term = BandArithmetic.parseExpression("maxOf(B1, B2)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0)); // Both bands are nan
    assertEquals(Double.NaN, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(4, 4)); // B2 valid
    assertEquals(88.0, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 4)); // both valid
    assertEquals(96, term.evalD(evalEnv));
  }

  @Test
  void testMean_withBandsOnly() throws ParseException, IOException {
    Term term = BandArithmetic.parseExpression("meanOf(B1, B2)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0)); // Both bands are nan
    assertEquals(Double.NaN, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(4, 4)); // B2 valid
    assertEquals(88.0, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 4)); // both valid
    assertEquals(72, term.evalD(evalEnv)); // 48.0 + 96.0 = 144.0 / 2 = 72.0
  }

  @Test
  void testMin_withBandsAndValues() throws ParseException, IOException {
    Term term = BandArithmetic.parseExpression("minOf(B1, 30, B2, 86)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0)); // Both bands are nan
    assertEquals(30, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(4, 4)); // B2 valid
    assertEquals(30, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 4)); // both valid
    assertEquals(30, term.evalD(evalEnv));
  }

  @Test
  void testMax_withBandsAndValues() throws ParseException, IOException {
    Term term = BandArithmetic.parseExpression("maxOf(B1, 30, B2, 86)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0)); // Both bands are nan
    assertEquals(86, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(4, 4)); // B2 valid
    assertEquals(88, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 4)); // both valid
    assertEquals(96, term.evalD(evalEnv));
  }

  @Test
  void testMean_withBandsAndValues() throws ParseException, IOException {
    Term term = BandArithmetic.parseExpression("meanOf(B1, 30, B2, 86)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0)); // Both bands are nan -> (30 + 86) = 116 / 2 = 58
    assertEquals(58, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(4, 4)); // B2 valid -> (30 + 88 + 86) = 204 / 3 = 68
    assertEquals(68, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 4)); // both valid -> (48 + 30 + 96 + 86) = 260 / 4 = 65
    assertEquals(65, term.evalD(evalEnv));
  }

  @Test
  void testMin_withScaledBandAndValue() throws ParseException, IOException {
    product.getBand("B1").setScalingFactor(0.1);
    Term term = BandArithmetic.parseExpression("minOf(B1, 5)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(4, 1));
    assertEquals(1.4, term.evalD(evalEnv), 1e-8); // B1.raw = 14 scaled = 1.4
  }

  @Test
  void testMax_withScaledBandAndValue() throws ParseException, IOException {
    product.getBand("B1").setScalingFactor(0.1);
    Term term = BandArithmetic.parseExpression("maxOf(B1, 5)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(4, 1));
    assertEquals(5, term.evalD(evalEnv), 1e-8); // B1.raw = 14 scaled = 1.4
    evalEnv.setElemIndex(toElemIndex(8, 6));
    assertEquals(6.8, term.evalD(evalEnv), 1e-8); // B1.raw = 68 scaled = 6.8
  }

  @Test
  void testMean_withScaledBandAndValue() throws ParseException, IOException {
    product.getBand("B1").setScalingFactor(0.1);
    Term term = BandArithmetic.parseExpression("meanOf(B1, 5)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(4, 1));
    assertEquals(3.2, term.evalD(evalEnv), 1e-8); // (1.4 + 5) / 2 = 3.2
    evalEnv.setElemIndex(toElemIndex(8, 6));
    assertEquals(5.9, term.evalD(evalEnv), 1e-8); // (6.8 + 5) / 2 = 5.9
  }

  private void fillRasterSymbols(Term term, RasterDataEvalEnv evalEnv) {
    RasterDataSymbol[] refRasterDataSymbols = BandArithmetic.getRefRasterDataSymbols(term);
    for (RasterDataSymbol rasterDataSymbol : refRasterDataSymbols) {
      Raster data = rasterDataSymbol.getRaster().getGeophysicalImage().getData();
      double[] pixels = new double[data.getWidth() * data.getHeight()];
      data.getSamples(evalEnv.getPixelX(), evalEnv.getPixelY(),
          evalEnv.getRegionWidth(), evalEnv.getRegionHeight(), 0, pixels);
      rasterDataSymbol.setData(ProductData.createInstance(pixels));
    }
  }

}
