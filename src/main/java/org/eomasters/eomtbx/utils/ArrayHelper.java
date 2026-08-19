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

import java.util.Arrays;

public class ArrayHelper {

  public static float[] as1DArray(float[][] array2d) {
    int rows = array2d.length;
    if (rows == 0) {
      return new float[0];
    }
    int cols = array2d[0].length;
    float[] floats1D = new float[rows * cols];

    int index = 0;
    for (float[] floats : array2d) {
      for (int j = 0; j < cols; j++) {
        floats1D[index++] = floats[j];
      }
    }
    return floats1D;
  }

  public static int[] as1DArray(int[][] array2d) {
    return Arrays.stream(array2d)
                 .flatMapToInt(Arrays::stream)
                 .toArray();
  }

  public static double[] as1DArray(double[][] array2d) {
    return Arrays.stream(array2d)
                 .flatMapToDouble(Arrays::stream)
                 .toArray();
  }


  public static double[] subtract(double[] comp0, double[] comp1) {
    double[] result = new double[comp0.length];
    for (int i = 0; i < comp0.length; i++) {
      result[i] = comp0[i] - comp1[i];
    }
    return result;
  }

  /**
   * Fills in NaN values in the given array by extrapolating and interpolating from surrounding values.
   * <p>
   * If all values are NaN the array remains unchanged.
   *
   * @param array the array of doubles with possible NaN values to be filled in
   */
  public static void extrapolateNaN(double[] array) {
    int n = array.length;
    if (n == 0) return;
    if (n == 1) return; // Single element array, nothing to fill
    if (n == 2) {
      // Handle exactly two elements
      if (Double.isNaN(array[0])) array[0] = array[1];
      if (Double.isNaN(array[1])) array[1] = array[0];
      return;
    }

    // Find the first non-NaN value
    int firstNonNaN = 0;
    while (firstNonNaN < n && Double.isNaN(array[firstNonNaN])) {
      firstNonNaN++;
    }

    // If all values are NaN, nothing to be done
    if (firstNonNaN == n) return;

    // Handle leading NaNs by extrapolating backwards
    if (firstNonNaN > 0) {
      double step = firstNonNaN < n - 1 && !Double.isNaN(array[firstNonNaN + 1])
          ? (array[firstNonNaN + 1] - array[firstNonNaN]) // Step based on first two valid values
          : 0;
      for (int i = 0; i < firstNonNaN; i++) {
        array[i] = array[firstNonNaN] - step * (firstNonNaN - i);
      }
    }

    // Handle internal NaNs via interpolation
    for (int i = firstNonNaN + 1; i < n; i++) {
      if (Double.isNaN(array[i])) {
        int nextNonNaN = i + 1;
        // Find the next valid value
        while (nextNonNaN < n && Double.isNaN(array[nextNonNaN])) {
          nextNonNaN++;
        }

        if (nextNonNaN < n) {
          double step = (array[nextNonNaN] - array[i - 1]) / (nextNonNaN - i + 1);
          for (int j = 0; j < nextNonNaN - i; j++) {
            array[i + j] = array[i - 1] + step * (j + 1);
          }
          i = nextNonNaN - 1; // Adjust index
        }
      }
    }

    // Handle trailing NaNs by extrapolating forwards
    int lastNonNaN = n - 1;
    while (lastNonNaN >= 0 && Double.isNaN(array[lastNonNaN])) {
      lastNonNaN--;
    }
    if (lastNonNaN < n - 1) {
      double step = lastNonNaN > 0 ? (array[lastNonNaN] - array[lastNonNaN - 1]) : 0;
      for (int i = lastNonNaN + 1; i < n; i++) {
        array[i] = array[lastNonNaN] + step * (i - lastNonNaN);
      }
    }
  }

}
