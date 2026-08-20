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

package org.eomasters.eomtbx.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ArrayHelperTest {

  @Test
  public void testAs1DArray_withValid2DArray() {
    double[][] data2D = new double[][]{
        {1.0, 2.0, 3.0},
        {4.0, 5.0, 6.0}
    };

    double[] expected = new double[]{
        1.0, 2.0, 3.0,
        4.0, 5.0, 6.0
    };

    double[] result = ArrayHelper.as1DArray(data2D);

    assertArrayEquals(expected, result, 1.0e-8);
  }

  @Test
  public void testAs1DArray_withEmpty2DArray() {
    double[][] empty = new double[0][0];

    double[] result = ArrayHelper.as1DArray(empty);

    assertArrayEquals(new double[0], result, 1.0e-8);
  }

  @Test
  public void testExtrapolateNaN_withNaNValuesOnly() {
    double[] data = new double[]{Double.NaN, Double.NaN, Double.NaN};
    ArrayHelper.extrapolateNaN(data);

    for (double value : data) {
      assertTrue(Double.isNaN(value));
    }
  }


  @Test
  public void testExtrapolateNaN_withOnlyFirstValue() {
    double[] data = new double[]{1.0, Double.NaN, Double.NaN, Double.NaN};
    double[] expected = new double[]{1.0, 1.0, 1.0, 1.0};
    ArrayHelper.extrapolateNaN(data);

    assertArrayEquals(expected, data, 1.0e-8);
  }

  @Test
  public void testExtrapolateNaN_withOnlyLastValue() {
    double[] data = new double[]{Double.NaN, Double.NaN, Double.NaN, 5.0};
    double[] expected = new double[]{5.0, 5.0, 5.0, 5.0};
    ArrayHelper.extrapolateNaN(data);

    assertArrayEquals(expected, data, 1.0e-8);
  }

  @Test
  public void testExtrapolateNaN_withNoNaNValues() {
    double[] data = new double[]{1.0, 2.0, 3.0};
    double[] expected = new double[]{1.0, 2.0, 3.0};
    ArrayHelper.extrapolateNaN(data);

    assertArrayEquals(expected, data, 1.0e-8);
  }

  @Test
  public void testExtrapolateNaN_atEdges() {
    double[] data = new double[]{Double.NaN, 2.0, 45.0, Double.NaN};
    double[] expected = new double[]{-41.0, 2.0, 45.0, 88.0};
    ArrayHelper.extrapolateNaN(data);

    assertArrayEquals(expected, data, 1.0e-8);
  }

}
