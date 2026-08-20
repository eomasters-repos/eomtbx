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

package org.eomasters.eomtbx.s2norm;

import java.util.Arrays;

public class ExtrapolateNaN {
    public static void main(String[] args) {
        double[] array = {Double.NaN, 3.4, Double.NaN, 3.7, Double.NaN, Double.NaN};  // Example with just two elements

        System.out.println("Original array: " + Arrays.toString(array));

        extrapolateNaN(array);

        System.out.println("Array after extrapolation: " + Arrays.toString(array));
    }

    public static void extrapolateNaN(double[] array) {
        int n = array.length;

        if (n == 2) {
            // If there are exactly two elements
            if (Double.isNaN(array[0])) {
                array[0] = array[1];
            }
            if (Double.isNaN(array[1])) {
                array[1] = array[0];
            }
            return;
        }

        // Find the first non-NaN value to handle beginning NaNs
        int firstValidIndex = 0;
        while (firstValidIndex < n && Double.isNaN(array[firstValidIndex])) {
            firstValidIndex++;
        }

        // If all values are NaN, nothing can be done
        if (firstValidIndex == n) {
            return;
        }

        // Handle NaNs at the beginning
        if (firstValidIndex > 0) {
            int secondValidIndex = firstValidIndex + 1;
            while (secondValidIndex < n && Double.isNaN(array[secondValidIndex])) {
                secondValidIndex++;
            }

            if (secondValidIndex < n) {
                double step = (array[secondValidIndex] - array[firstValidIndex]) / (secondValidIndex - firstValidIndex);
                for (int k = 0; k < firstValidIndex; k++) {
                    array[k] = array[firstValidIndex] - step * (firstValidIndex - k);
                }
            } else {
                // If there's only one valid value, copy it back to fill the NaNs at the beginning
                for (int k = 0; k < firstValidIndex; k++) {
                    array[k] = array[firstValidIndex];
                }
            }
        }

        // Handle NaNs and interpolate
        for (int i = firstValidIndex + 1; i < n; i++) {
            if (Double.isNaN(array[i])) {
                int nextValidIndex = i + 1;
                while (nextValidIndex < n && Double.isNaN(array[nextValidIndex])) {
                    nextValidIndex++;
                }

                if (nextValidIndex < n) {
                    double step = (array[nextValidIndex] - array[i - 1]) / (nextValidIndex - i + 1);
                    for (int k = 1; k <= (nextValidIndex - i); k++) {
                        array[i - 1 + k] = array[i - 1] + step * k;
                    }
                    i = nextValidIndex - 1; // Move i to the last filled position
                } else {
                    // Handle NaNs at the end using the last known values
                    double step = (array[i - 1] - array[i - 2]);
                    for (int k = i; k < n; k++) {
                        array[k] = array[i - 1] + step * (k - i + 1);
                    }
                    break; // All remaining NaNs are filled
                }
            }
        }
    }
}
