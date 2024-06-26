/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Free for SNAP
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.dataop.barithm.BandArithmetic;
import org.esa.snap.core.dataop.barithm.RasterDataEvalEnv;
import org.esa.snap.core.jexp.ParseException;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.util.DummyProductBuilder;
import org.esa.snap.core.util.DummyProductBuilder.GC;
import org.esa.snap.core.util.DummyProductBuilder.Size;
import org.junit.jupiter.api.Test;

class MapPosSymbolTest {


  protected static final double EPS = 1.0e-6;

  @Test
  void testEvalD_withMapGC() throws ParseException {
    Product product =         new DummyProductBuilder().size(Size.SMALL).gc(GC.MAP).create();
    Term xTerm = BandArithmetic.parseExpression("MAPX", new Product[]{product}, 0);
    Term yTerm = BandArithmetic.parseExpression("MAPY", new Product[]{product}, 0);
    Dimension size = product.getSceneRasterSize();

    RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, size.width, size.height);
    evalEnv.setElemIndex(toElemIndex(0, 0, size));
    assertEquals(0.0416666, xTerm.evalD(evalEnv), EPS);
    assertEquals(-0.0416666, yTerm.evalD(evalEnv), EPS);

    evalEnv.setElemIndex(toElemIndex(1, 0, size));
    assertEquals(0.125, xTerm.evalD(evalEnv), EPS);
    assertEquals(-0.0416666, yTerm.evalD(evalEnv), EPS);

    evalEnv.setElemIndex(toElemIndex(2, 0, size));
    assertEquals(0.2083333, xTerm.evalD(evalEnv), EPS);
    assertEquals(-0.0416666, yTerm.evalD(evalEnv), EPS);

    evalEnv.setElemIndex(toElemIndex(1, 1, size));
    assertEquals(0.125, xTerm.evalD(evalEnv), EPS);
    assertEquals(-0.125, yTerm.evalD(evalEnv), EPS);

    evalEnv.setElemIndex(toElemIndex(0, 1, size));
    assertEquals(0.0416666, xTerm.evalD(evalEnv), EPS);
    assertEquals(-0.125, yTerm.evalD(evalEnv), EPS);

    evalEnv.setElemIndex(toElemIndex(1, 3, size));
    assertEquals(0.125, xTerm.evalD(evalEnv), EPS);
    assertEquals(-0.2916666, yTerm.evalD(evalEnv), EPS);

    evalEnv.setElemIndex(toElemIndex(5, 0, size));
    assertEquals(0.4583333, xTerm.evalD(evalEnv), EPS);
    assertEquals(-0.0416666, yTerm.evalD(evalEnv), EPS);
  }

  @Test
  void testEvalD_withTieGC() throws ParseException {
    Product product =         new DummyProductBuilder().size(Size.SMALL).gc(GC.TIE_POINTS).create();
    Term xTerm = BandArithmetic.parseExpression("MAPX", new Product[]{product}, 0);
    Term yTerm = BandArithmetic.parseExpression("MAPY", new Product[]{product}, 0);
    Dimension size = product.getSceneRasterSize();

    RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, size.width, size.height);
    evalEnv.setElemIndex(toElemIndex(0, 0, size));
    assertEquals(-5.663245, xTerm.evalD(evalEnv), EPS);
    assertEquals(20.903569, yTerm.evalD(evalEnv), EPS);

    evalEnv.setElemIndex(toElemIndex(1, 3, size));
    assertEquals(-5.568550, xTerm.evalD(evalEnv), EPS);
    assertEquals(20.666081, yTerm.evalD(evalEnv), EPS);

    evalEnv.setElemIndex(toElemIndex(5, 0, size));
    assertEquals(-5.189769, xTerm.evalD(evalEnv), EPS);
    assertEquals(20.918042, yTerm.evalD(evalEnv), EPS);
  }

  @Test
  void testEvalD_withPixelGC() throws ParseException {
    Product product =         new DummyProductBuilder().size(Size.SMALL).gc(GC.PER_PIXEL).create();
    Term xTerm = BandArithmetic.parseExpression("MAPX", new Product[]{product}, 0);
    Term yTerm = BandArithmetic.parseExpression("MAPY", new Product[]{product}, 0);
    Dimension size = product.getSceneRasterSize();

    RasterDataEvalEnv evalEnv = new RasterDataEvalEnv(0, 0, size.width, size.height);
    evalEnv.setElemIndex(toElemIndex(0, 0, size));
    assertEquals(-5.710593, xTerm.evalD(evalEnv), EPS);
    assertEquals(21.703290, yTerm.evalD(evalEnv), EPS);
    evalEnv.setElemIndex(toElemIndex(1, 3, size));
    assertEquals(-5.615235, xTerm.evalD(evalEnv), EPS);
    assertEquals(21.458982, yTerm.evalD(evalEnv), EPS);
    evalEnv.setElemIndex(toElemIndex(5, 0, size));
    assertEquals(-5.233497, xTerm.evalD(evalEnv), EPS);
    assertEquals(21.718999, yTerm.evalD(evalEnv), EPS);
  }

  private static int toElemIndex(int x, int y, Dimension size) {
    return y * size.width + x;
  }


}
