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

package org.eomasters.eomtbx.s2geom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class CombineDetectorImagesTest {


  @Test
  public void testCombineVertically_withNaNValues() {
    double[][] input = new double[][]{
        {Double.NaN, Double.NaN, 3.0},
        {Double.NaN, 2.0, Double.NaN},
        {7.0, Double.NaN, Double.NaN}
    };

    double[] expected = new double[]{7.0, 2.0, 3.0};

    double[] result = CombineDetectorImages.combineVertically(input);

    assertArrayEquals(expected, result, 1.0e-8);
  }

  @Test
  public void testCombineVertically_withEmpty2DArray() {
    double[][] input = new double[0][0];

    double[] result = CombineDetectorImages.combineVertically(input);

    assertArrayEquals(new double[0], result, 1.0e-8);
  }


}
