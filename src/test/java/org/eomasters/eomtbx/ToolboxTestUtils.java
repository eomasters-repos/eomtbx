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

package org.eomasters.eomtbx;

import java.util.stream.IntStream;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.CrsGeoCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

public class ToolboxTestUtils {

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
   *  7: NAN,NAN,NAN, 73,INV,INV,INV, 77, 78, 79,
   *  8: NAN,NAN,NAN, 83,INV,INV,INV, 87, 88, 89,
   *  9: NAN,NAN,NAN, 93,INV,INV,INV, 97, 98, ND
   *  </pre>
   * and B2 with the following data:
   * <pre>
   *      0   1   2   3   4   5   6   7   8   9
   *  0: NAN,  2,  4,  6,  8,INV, 12, 14, 16, 18,
   *  1: NAN, 22, 24, 26, 28,INV, 32, 34, 36, 38,
   *  2: NAN, 42, 44, 46, 48,INV, 52, 54, 56, 58,
   *  3: NAN, 62, 64, 66, 68,INV, 72, 74, 76, 78,
   *  4: NAN, 82, 84, 86, 88,INV, 92, 94, 96, 98,
   *  5: NAN,102,104,106,108,INV,112,114,116,118,
   *  6: NAN,122,124,126,128,INV,132,134,136,138,
   *  7: 140,142,144,146,148,INV,152,154,156,158,
   *  8: 160,162,164,166,168,INV,172,174,176,178,
   *  9: 180,182,184,186,188,INV,192,194, ND,198
   *  </pre>
   */
  public static Product createProduct() {
    Product product = new Product("N", "T", W, H);

    Band b1 = product.addBand("B1", ProductData.TYPE_FLOAT64);
    double[] b1Data = createB1Data();
    b1.setData(ProductData.createInstance(b1Data));
    b1.setNoDataValue(99);
    b1.setNoDataValueUsed(true);
    b1.setValidPixelExpression("B1 % 5 != 0 && B1 != 44 && B1 != 46 " + "&& B1 != 74 && B1 != 84 && B1 != 94"
        + "&& B1 != 76 && B1 != 86 && B1 != 96");
    Band b2 = product.addBand("B2", ProductData.TYPE_FLOAT64);
    double[] b2Data = createB2Data();
    b2.setData(ProductData.createInstance(b2Data));
    b2.setNoDataValue(196);
    b2.setNoDataValueUsed(true);
    b2.setValidPixelExpression("B2 % 5 != 0");

    product.setSceneGeoCoding(createCrsGeoCoding(W, H));

    return product;
  }

  public static int toElemIndex(int x, int y) {
    return y * W + x;
  }

  // resulting data array contain values from 0 to w*h-1.
  // the first element of row 0-6 is set to NaN
  private static double[] createB2Data() {
    double[] array = IntStream.range(0, W * H).asDoubleStream().map(operand -> operand * 2).toArray();
    for (int y = 0; y < H - 3; y++) {
      array[y * W] = Double.NaN;
    }
    return array;
  }

  // resulting data array contain values from 0 to w*h-1.
  // the first element of each row is set to NaN
  // also the elements 7,1; 7,2; 8,1; 8,2; 9,1; 9,2 are set to NaN
  private static double[] createB1Data() {
    double[] array = IntStream.range(0, W * H).asDoubleStream().toArray();
    for (int y = 0; y < H; y++) {
      array[y * W] = Double.NaN;
    }
    array[7 * W + 1] = Double.NaN;
    array[7 * W + 2] = Double.NaN;
    array[8 * W + 1] = Double.NaN;
    array[8 * W + 2] = Double.NaN;
    array[9 * W + 1] = Double.NaN;
    array[9 * W + 2] = Double.NaN;
    return array;
  }

  private static CrsGeoCoding createCrsGeoCoding(int imageWidth, int imageHeight) {
    try {
      double extend = 10.0;
      double pixelSizeX = extend / imageWidth;
      double pixelSizeY = extend / imageHeight;
      double easting = 0.0;
      double northing = 0.0;
      return new CrsGeoCoding(DefaultGeographicCRS.WGS84,
          imageWidth, imageHeight,
          easting, northing,
          pixelSizeX, pixelSizeY,
          0.0, 0.0);
    } catch (FactoryException | TransformException e) {
      throw new IllegalStateException(e);
    }
  }
}
