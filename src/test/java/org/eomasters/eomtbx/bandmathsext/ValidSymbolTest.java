/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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

import static org.eomasters.eomtbx.bandmathsext.BandMathsTestUtils.toElemIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.dataop.barithm.BandArithmetic;
import org.esa.snap.core.dataop.barithm.RasterDataEvalEnv;
import org.esa.snap.core.jexp.ParseException;
import org.esa.snap.core.jexp.Term;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ValidSymbolTest {

  private static Product product;
  private static int W;
  private static int H;

  @BeforeAll
  static void beforeAll() {
    product = BandMathsTestUtils.createProduct();
    W = product.getSceneRasterWidth();
    H = product.getSceneRasterHeight();
    Band b1 = product.getBand("B1");
    b1.setNoDataValue(8);
    b1.setNoDataValueUsed(true);
  }

  @Test
  void testBasics() {
    ValidSymbol symbol = new ValidSymbol("", product.getBand("B1"));
    assertEquals("B1.valid", symbol.getName());
    assertEquals(Term.TYPE_B, symbol.getRetType());
    assertFalse(symbol.isConst());
  }

  @Test
  void testEvalB() throws ParseException {
    Term term = BandArithmetic.parseExpression("B1.valid", new Product[]{product}, 0);

    RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, W, H);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    assertTrue(term.evalB(evalEnv));
    evalEnv.setElemIndex(toElemIndex(1, 0));
    assertTrue(term.evalB(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 0));
    assertFalse(term.evalB(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 0));
    assertFalse(term.evalB(evalEnv));
  }

  @Test
  void testEvalI() throws ParseException {
    Term term = BandArithmetic.parseExpression("B1.valid", new Product[]{product}, 0);

    RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, W, H);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    assertEquals(1, term.evalI(evalEnv));
    evalEnv.setElemIndex(toElemIndex(1, 0));
    assertEquals(1, term.evalI(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 0));
    assertEquals(0, term.evalI(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 0));
    assertEquals(0, term.evalI(evalEnv));
  }

  @Test
  void testEvalD() throws ParseException {
    Term term = BandArithmetic.parseExpression("!B1.valid", new Product[]{product}, 0);

    RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, W, H);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    assertEquals(0., term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(1, 0));
    assertEquals(0., term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 0));
    assertEquals(1., term.evalD(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 0));
    assertEquals(1., term.evalD(evalEnv));
  }

  @Test
  void testEvalS() throws ParseException {
    Term term = BandArithmetic.parseExpression("B1.valid", new Product[]{product}, 0);

    RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, W, H);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    assertEquals("true", term.evalS(evalEnv));
    evalEnv.setElemIndex(toElemIndex(1, 0));
    assertEquals("true", term.evalS(evalEnv));
    evalEnv.setElemIndex(toElemIndex(5, 0));
    assertEquals("false", term.evalS(evalEnv));
    evalEnv.setElemIndex(toElemIndex(8, 0));
    assertEquals("false", term.evalS(evalEnv));
  }
}
