/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Basic for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2025 Marco Peters
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

class StxFunctionsTest {

  private static Product product;
  private RasterDataEvalEnv evalEnv;

  @BeforeEach
  void beforeEach() {
    product = TestUtils.createProduct();
    evalEnv = new RasterDataEvalEnv(0, 0, product.getSceneRasterWidth(), product.getSceneRasterHeight());
  }

  @Test
  void testException_onlyOneParameter() throws ParseException {
    Term term = BandArithmetic.parseExpression("stx(B1)", new Product[]{product}, 0);
    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0)); // Both bands are nan
    assertThrows(EvalException.class, () -> term.evalD(evalEnv));
  }

  @Test
  void testException_moreThanThreeParameters() {
    // fails already before evaluation
    assertThrows(ArrayIndexOutOfBoundsException.class,
        () -> BandArithmetic.parseExpression("stx(B1, \"min\", false, true)", new Product[]{product}, 0));
  }

  @Test
  void testMin() throws ParseException {
    Term term = BandArithmetic.parseExpression("stx(B1, \"min\", false)", new Product[]{product}, 0);
    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    double min = term.evalD(evalEnv);
    assertEquals(1.0, min, 1.0e-8);
    assertEquals(product.getBand("B1").getStx().getMinimum(), min, 1.0e-8);
  }

  @Test
  void testMax() throws ParseException {
    Term term = BandArithmetic.parseExpression("stx(B1, \"max\", false)", new Product[]{product}, 0);
    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    double max = term.evalD(evalEnv);
    assertEquals(98.0, max, 1.0e-8);
    assertEquals(product.getBand("B1").getStx().getMaximum(), max, 1.0e-8);
  }

  @Test
  void testMean() throws ParseException {
    Term term = BandArithmetic.parseExpression("stx(B1, \"mean\", false)", new Product[]{product}, 0);
    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    double mean = term.evalD(evalEnv);
    assertEquals(43.26153846, mean, 1.0e-8);
    assertEquals(product.getBand("B1").getStx().getMean(), mean, 1.0e-8);
  }

  @Test
  void testMedian() throws ParseException {
    Term term = BandArithmetic.parseExpression("stx(B1, \"median\", false)", new Product[]{product}, 0);
    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    double median = term.evalD(evalEnv);
    assertEquals(41.06933594, median, 1.0e-8);
    assertEquals(product.getBand("B1").getStx().getMedian(), median, 1.0e-8);
  }

  @Test
  void testSigma() throws ParseException {
    Term term = BandArithmetic.parseExpression("stx(B1, \"sigma\", false)", new Product[]{product}, 0);
    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    double sigma = term.evalD(evalEnv);
    assertEquals(27.12371939, sigma, 1.0e-8);
    assertEquals(product.getBand("B1").getStx().getStandardDeviation(), sigma, 1.0e-8);
  }

  @Test
  void testRsd() throws ParseException {
    Term term = BandArithmetic.parseExpression("stx(B1, \"rsd\", false)", new Product[]{product}, 0);
    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    double rsd = term.evalD(evalEnv);
    assertEquals(0.62212921, rsd, 1.0e-8);
    assertEquals(product.getBand("B1").getStx().getCoefficientOfVariation(), rsd, 1.0e-8);
  }

  @Test
  void testEnl() throws ParseException {
    Term term = BandArithmetic.parseExpression("stx(B1, \"enl\", false)", new Product[]{product}, 0);
    fillRasterSymbols(term, evalEnv);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    double enl = term.evalD(evalEnv);
    assertEquals(0.98488936, enl, 1.0e-8);
    assertEquals(product.getBand("B1").getStx().getEquivalentNumberOfLooks(), enl, 1.0e-8);
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
