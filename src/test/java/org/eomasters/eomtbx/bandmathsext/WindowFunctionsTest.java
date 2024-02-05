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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eomasters.eomtbx.TestUtils;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.dataop.barithm.BandArithmetic;
import org.esa.snap.core.dataop.barithm.RasterDataEvalEnv;
import org.esa.snap.core.jexp.EvalException;
import org.esa.snap.core.jexp.ParseException;
import org.esa.snap.core.jexp.Term;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WindowFunctionsTest {

  private static Product product;
  private static int W;
  private static int H;

  @BeforeAll
  static void beforeAll() {
    product = TestUtils.createProduct();
    W = product.getSceneRasterWidth();
    H = product.getSceneRasterHeight();
  }

  @Test
  void testMinFunction() throws ParseException {
    Term term = BandArithmetic.parseExpression("wnd(B1, 3, \"min\")", new Product[]{product}, 0);

    RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, W, H);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    assertEquals(1, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(9, 0));
    assertEquals(8, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 4));
    assertEquals(34, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 7));
    assertEquals(67, term.evalD(evalEnv));

    evalEnv.setElemIndex(toElemIndex(1, 8)); // all NaN
    assertEquals(Double.NaN, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 8)); // all Inv
    assertEquals(Double.NaN, term.evalD(evalEnv));

  }

  @Test
  void testMaxFunction() throws ParseException {
    Term term = BandArithmetic.parseExpression("wnd(B1, 3, \"max\")", new Product[]{product}, 0);

    RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, W, H);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    assertEquals(11, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(9, 0));
    assertEquals(19, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 4));
    assertEquals(56, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 7));
    assertEquals(89, term.evalD(evalEnv));

    evalEnv.setElemIndex(toElemIndex(1, 8)); // all NaN
    assertEquals(Double.NaN, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 8)); // all Inv
    assertEquals(Double.NaN, term.evalD(evalEnv));

  }

  @Test
  void testSumFunction() throws ParseException {
    Term term = BandArithmetic.parseExpression("wnd(B1, 3, \"sum\")", new Product[]{product}, 0);

    RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, W, H);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    assertEquals(12, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(9, 0));
    assertEquals(54, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 4));
    assertEquals(180, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 7));
    assertEquals(702, term.evalD(evalEnv));

    evalEnv.setElemIndex(toElemIndex(1, 8)); // all NaN
    assertEquals(Double.NaN, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 8)); // all Inv
    assertEquals(Double.NaN, term.evalD(evalEnv));

  }

  @Test
  void testMeanFunction() throws ParseException {
    Term term = BandArithmetic.parseExpression("wnd(B1, 3, \"mean\")", new Product[]{product}, 0);

    RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, W, H);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    assertEquals(12 / 2., term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(9, 0));
    assertEquals(54 / 4., term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 4));
    assertEquals(180 / 4., term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 7));
    assertEquals(702 / 9., term.evalD(evalEnv));

    evalEnv.setElemIndex(toElemIndex(1, 8)); // all NaN
    assertEquals(Double.NaN, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 8)); // all Inv
    assertEquals(Double.NaN, term.evalD(evalEnv));

  }

  @Test
  void testMedianFunction() throws ParseException {
    Term term = BandArithmetic.parseExpression("wnd(B1, 3, \"median\")", new Product[]{product}, 0);

    RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, W, H);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    assertEquals(6, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(9, 0));
    assertEquals(13.5, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 4));
    assertEquals(45, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 7));
    assertEquals(79, term.evalD(evalEnv));

    evalEnv.setElemIndex(toElemIndex(1, 8)); // all NaN
    assertEquals(Double.NaN, term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 8)); // all Inv
    assertEquals(Double.NaN, term.evalD(evalEnv));
  }

  @Test
  public void testException() {
    RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, W, H);

    assertThrows(EvalException.class, () -> {
      Term firstArgInvalidTerm = BandArithmetic.parseExpression("wnd(3, 3, \"median\")", new Product[]{product}, 0);
      firstArgInvalidTerm.evalD(evalEnv);
    });

    assertThrows(EvalException.class, () -> {
      Term secondArgInvalidTerm = BandArithmetic.parseExpression("wnd(B1, 4, \"median\")", new Product[]{product}, 0);
      secondArgInvalidTerm.evalD(evalEnv);
    });

    assertThrows(EvalException.class, () -> {
      Term thirdArgInvalidTerm = BandArithmetic.parseExpression("wnd(B1, 5, \"inv\")", new Product[]{product}, 0);
      thirdArgInvalidTerm.evalD(evalEnv);
    });

    assertThrows(EvalException.class, () -> {
      Term thirdArgInvalidTerm = BandArithmetic.parseExpression("wnd(B1, 5, 1)", new Product[]{product}, 0);
      thirdArgInvalidTerm.evalD(evalEnv);
    });
  }

}
