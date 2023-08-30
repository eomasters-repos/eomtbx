/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * --> https://www.eomasters.org/sw/EOMTBX
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
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.bandmathsext;

import java.util.stream.IntStream;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;

public class BandMathsTestUtils {
  private static final int W = 10;
  private static final int H = 10;


  /**
   * Creates Product with band 'B1' with the following data:
   * <pre>
   *      0   1   2   3   4   5   6   7   8   9
   *  0: NAN,  1,  2,  3,  4,INV,  6,  7,  8,  9,
   *  1: NAN, 11, 12, 13, 14,INV, 16, 17, 18, 19,
   *  2: NAN, 21, 22, 23, 24,INV, 26, 27, 28, 29,
   *  3: NAN, 31, 32, 33, 34,INV, 36, 37, 38, 39,
   *  4: NAN, 41, 42, 43,INV,INV,INV, 47, 48, 49,
   *  5: NAN, 51, 52, 53, 54,INV, 56, 57, 58, 59,
   *  6: NAN, 61, 62, 63, 64,INV, 66, 67, 68, 69,
   *  7: NAN, 71, 72, 73, 74,INV, 76, 77, 78, 79,
   *  8: NAN, 81, 82, 83, 84,INV, 86, 87, 88, 89,
   *  9: NAN, 91, 92, 93, 94,INV, 96, 97, 98, 99
   *  </pre>
   */
  static Product createProduct() {
    Product product = new Product("N", "T", W, H);
    Band band = product.addBand("B1", ProductData.TYPE_FLOAT64);

    double[] data = createData();
    band.setData(ProductData.createInstance(data));
    band.setValidPixelExpression("B1 % 5 != 0 && B1 != 44 && B1 != 46");
    return product;
  }

  // resulting data array contain values from 0 to w*h-1.
  // the first element of each row is set to NaN
  private static double[] createData() {
    double[] array = IntStream.range(0, W * H).asDoubleStream().toArray();
    for (int y = 0; y < H; y++) {
      array[y * W] = Double.NaN;
    }
    return array;
  }

  static int toElemIndex(int x, int y) {
    return y * W + x;
  }
}
