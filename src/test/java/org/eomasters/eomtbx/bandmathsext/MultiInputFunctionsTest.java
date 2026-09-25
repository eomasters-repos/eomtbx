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

package org.eomasters.eomtbx.bandmathsext;

import static org.eomasters.eomtbx.TestUtils.toElemIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.Raster;
import org.eomasters.eomtbx.TestUtils;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.dataop.barithm.BandArithmetic;
import org.esa.snap.core.dataop.barithm.RasterDataEvalEnv;
import org.esa.snap.core.dataop.barithm.RasterDataSymbol;
import org.esa.snap.core.jexp.EvalException;
import org.esa.snap.core.jexp.ParseException;
import org.esa.snap.core.jexp.Term;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class MultiInputFunctionsTest {

  private static Product product;
  private RasterDataEvalEnv evalEnv;

  @BeforeEach
  void beforeEach() {
    product = TestUtils.createProduct();
    evalEnv = new RasterDataEvalEnv(0, 0, product.getSceneRasterWidth(), product.getSceneRasterHeight());
  }

  @Test
  void testMin_withBandsOnly() throws ParseException {
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
  void testMin_withBandsAndValues() throws ParseException {
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
  void testMin_withScaledBandAndValue() throws ParseException {
    product.getBand("B1").setScalingFactor(0.1);
    Term term = BandArithmetic.parseExpression("minOf(B1, 5)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(4, 1));
    assertEquals(1.4, term.evalD(evalEnv), 1e-8); // B1.raw = 14 scaled = 1.4
  }

  @Test
  void testMinIndex_withBandsOnly() throws ParseException {
    Term term = BandArithmetic.parseExpression("indexOfMin(B1, B2)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0)); // Both bands are nan
    assertEquals(-1, term.evalI(evalEnv));
    evalEnv.setElemIndex(toElemIndex(4, 4)); // B2 valid
    assertEquals(1, term.evalI(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 4)); // both valid
    assertEquals(0, term.evalI(evalEnv));
  }

  @Test
  void testMinIndex_withBandsAndValues() throws ParseException {
    Term term = BandArithmetic.parseExpression("indexOfMin(B1, 30, B2, 86)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0)); // Both bands are nan
    assertEquals(1, term.evalI(evalEnv));
    evalEnv.setElemIndex(toElemIndex(4, 4)); // B2 valid
    assertEquals(1, term.evalI(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 4)); // both valid
    assertEquals(1, term.evalI(evalEnv));
  }

  @Test
  void testMinIndex_withScaledBandAndValue() throws ParseException {
    product.getBand("B1").setScalingFactor(0.1);
    Term term = BandArithmetic.parseExpression("indexOfMin(B1, 5)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(4, 1));
    assertEquals(0, term.evalI(evalEnv)); // B1.raw = 14 scaled = 1.4
    evalEnv.setElemIndex(toElemIndex(8, 6));
    assertEquals(1, term.evalI(evalEnv)); // B1.raw = 68 scaled = 6.8
  }

  @Test
  void testMax_withBandsOnly() throws ParseException {
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
  void testMax_withBandsAndValues() throws ParseException {
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
  void testMax_withScaledBandAndValue() throws ParseException {
    product.getBand("B1").setScalingFactor(0.1);
    Term term = BandArithmetic.parseExpression("maxOf(B1, 5)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(4, 1));
    assertEquals(5, term.evalD(evalEnv), 1e-8); // B1.raw = 14 scaled = 1.4
    evalEnv.setElemIndex(toElemIndex(8, 6));
    assertEquals(6.8, term.evalD(evalEnv), 1e-8); // B1.raw = 68 scaled = 6.8
  }

  @Test
  void testMaxIndex_withBandsOnly() throws ParseException {
    Term term = BandArithmetic.parseExpression("indexOfMax(B1, B2)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0)); // Both bands are nan
    assertEquals(-1, term.evalI(evalEnv));
    evalEnv.setElemIndex(toElemIndex(4, 4)); // B2 valid
    assertEquals(1, term.evalI(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 4)); // both valid
    assertEquals(1, term.evalI(evalEnv));
  }

  @Test
  void testMaxIndex_withBandsAndValues() throws ParseException {
    Term term = BandArithmetic.parseExpression("indexOfMax(B1, 30, B2, 86)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0)); // Both bands are nan
    assertEquals(3, term.evalI(evalEnv));
    evalEnv.setElemIndex(toElemIndex(4, 4)); // B2 valid
    assertEquals(2, term.evalI(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 4)); // both valid
    assertEquals(2, term.evalI(evalEnv));
  }

  @Test
  void testMaxIndex_withScaledBandAndValue() throws ParseException {
    product.getBand("B1").setScalingFactor(0.1);
    Term term = BandArithmetic.parseExpression("indexOfMax(B1, 5)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(4, 1));
    assertEquals(1, term.evalI(evalEnv)); // B1.raw = 14 scaled = 1.4
    evalEnv.setElemIndex(toElemIndex(8, 6));
    assertEquals(0, term.evalI(evalEnv)); // B1.raw = 68 scaled = 6.8
  }

  @Test
  void testMean_withBandsOnly() throws ParseException {
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
  void testMean_withBandsAndValues() throws ParseException {
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
  void testMean_withScaledBandAndValue() throws ParseException {
    product.getBand("B1").setScalingFactor(0.1);
    Term term = BandArithmetic.parseExpression("meanOf(B1, 5)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(4, 1));
    assertEquals(3.2, term.evalD(evalEnv), 1e-8); // (1.4 + 5) / 2 = 3.2
    evalEnv.setElemIndex(toElemIndex(8, 6));
    assertEquals(5.9, term.evalD(evalEnv), 1e-8); // (6.8 + 5) / 2 = 5.9
  }

  @Test
  void testMedian_withBandsOnly() throws ParseException {
    Term term = BandArithmetic.parseExpression("medianOf(B1, B2)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0)); // Both bands are NaN
    assertEquals(Double.NaN, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 1)); // Both bands excluded by valid-pixel expressions
    assertEquals(Double.NaN, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(4, 4)); // Only B2 is valid
    assertEquals(88, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 4)); // Both valid: (48 + 96) / 2
    assertEquals(72, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(9, 9)); // B1 is no-data
    assertEquals(198, term.evalD(evalEnv));
  }

  @Test
  void testMedian_withBandsAndValues() throws ParseException {
    Term term = BandArithmetic.parseExpression("medianOf(B1, 30, B2, 86)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0)); // 30, 86
    assertEquals(58, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(4, 4)); // 30, 86, 88
    assertEquals(86, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 4)); // 30, 48, 86, 96
    assertEquals(67, term.evalD(evalEnv));
  }

  @Test
  void testMedian_withScaledBandAndValue() throws ParseException {
    product.getBand("B1").setScalingFactor(0.1);
    Term term = BandArithmetic.parseExpression("medianOf(B1, 5)", new Product[]{product}, 0);

    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(4, 1));
    assertEquals(3.2, term.evalD(evalEnv), 1e-8);
    evalEnv.setElemIndex(toElemIndex(8, 6));
    assertEquals(5.9, term.evalD(evalEnv), 1e-8);
  }

  @ParameterizedTest
  @CsvSource({
      "'medianOf(9, 1, 3)', 3",
      "'medianOf(9, 1, 5, 3)', 4",
      "'medianOf(9, 3, 3)', 3",
      "'medianOf(-1, -9, -3)', -3",
      "'medianOf(7)', 7",
      "'medianOf(0.0 / 0.0, 1.0 / 0.0, -1.0 / 0.0, 7, 3)', 5",
      "'medianOf(0.0 / 0.0, 1.0 / 0.0, -1.0 / 0.0)', NaN",
      "'medianOf(1.6e308, 1.6e308)', 1.6e308",
      "'medianOf(-1.6e308, -1.6e308)', -1.6e308",
      "'medianOf(-1.6e308, 1.6e308)', 0",
      "'medianOf(9, 1, 5, 3, \"average\")', 4",
      "'medianOf(9, 1, 3, \"average\")', 3",
      "'medianOf(7, \"average\")', 7",
      "'medianOf(NaN, 1.0 / 0.0, 7, 3, \"average\")', 5",
      "'medianOf(NaN, \"average\")', NaN",
      "'medianOf(9, 1, 5, 3, \"lower\")', 3",
      "'medianOf(9, 1, 5, 3, \"upper\")', 5",
      "'medianOf(9, 1, 3, \"lower\")', 3",
      "'medianOf(9, 1, 3, \"upper\")', 3",
      "'medianOf(7, \"lower\")', 7",
      "'medianOf(7, \"upper\")', 7",
      "'medianOf(NaN, 1.0 / 0.0, 7, 3, \"lower\")', 3",
      "'medianOf(NaN, 1.0 / 0.0, 7, 3, \"upper\")', 7",
      "'medianOf(NaN, \"lower\")', NaN",
      "'medianOf(NaN, \"upper\")', NaN"
  })
  void testMedian_withValues(String expression, double expected) throws ParseException {
    Term term = BandArithmetic.parseExpression(expression, new Product[]{product}, 0);
    assertEquals(expected, term.evalD(evalEnv));
  }

  @ParameterizedTest
  @CsvSource({"average, 58, 67", "lower, 30, 48", "upper, 86, 86"})
  void testMedian_withBandsAndMode(String mode, double twoValues, double fourValues) throws ParseException {
    Term term = BandArithmetic.parseExpression("medianOf(B1, 30, B2, 86, \"" + mode + "\")",
        new Product[]{product}, 0);
    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0)); // 30, 86
    assertEquals(twoValues, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(4, 4)); // 30, 86, 88
    assertEquals(86, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 4)); // 30, 48, 86, 96
    assertEquals(fourValues, term.evalD(evalEnv));
  }

  @ParameterizedTest
  @ValueSource(strings = {"medianOf()", "medianOf(\"lower\")", "medianOf(1, 2, \"unknown\")",
      "medianOf(1, \"lower\", 2)", "medianOf(1, true)", "medianOf(NaN, \"unknown\")"})
  void testMedian_rejectsInvalidArguments(String expression) throws ParseException {
    Term term = BandArithmetic.parseExpression(expression, new Product[]{product}, 0);
    assertThrows(EvalException.class, () -> term.evalD(evalEnv));
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
