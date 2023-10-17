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

import static org.eomasters.eomtbx.TestUtils.toElemIndex;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eomasters.eomtbx.TestUtils;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.dataop.barithm.BandArithmetic;
import org.esa.snap.core.dataop.barithm.RasterDataEvalEnv;
import org.esa.snap.core.jexp.ParseException;
import org.esa.snap.core.jexp.Term;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AreValidFunctionTest {

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
  public void testAreValid() throws ParseException {
    Term term = BandArithmetic.parseExpression("areValid(B1, B2)", new Product[]{product}, 0);

    RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, W, H);
    evalEnv.setElemIndex(toElemIndex(0, 0));
    assertTrue(term.evalB(evalEnv)); // all NaN
    evalEnv.setElemIndex(toElemIndex(0, 9));
    assertTrue(term.evalB(evalEnv)); // one NaN, one value

    evalEnv.setElemIndex(toElemIndex(9, 9));
    assertFalse(term.evalB(evalEnv)); //one is no-data value, one is valid
    evalEnv.setElemIndex(toElemIndex(5, 5));
    assertFalse(term.evalB(evalEnv)); // both invalid
    evalEnv.setElemIndex(toElemIndex(4, 4));
    assertFalse(term.evalB(evalEnv)); // one valid, on invalid

  }

}
