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

package org.eomasters.eomtbx.pyeditor.snapkit;

import java.io.IOException;
import java.util.List;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.graalvm.polyglot.Value;

public abstract class DataTransfer {
  private static final List<String> SUPPORTED_NUMPY_TYPES = List.of("int8", "int16", "int32", "uint8", "uint16", "uint32", "float32", "float64");

  public static DataTransfer create( Value pyArr) {
    String pyArrType = pyArr.getMember("__class__").getMember("__name__").asString();
    if (pyArrType.equals("array")) {
      return new PyarrayDataTransferImpl();
    } else if (pyArrType.equals("ndarray")) {
      Value dtype = pyArr.getMember("dtype");
      String dtypeName = dtype.getMember("name").asString();
      if(SUPPORTED_NUMPY_TYPES.contains(dtypeName)) {
        return new NumpyDataTransferImpl();
      } else {
        throw new IllegalArgumentException("Unsupported numpy dtype: " + dtypeName);
      }
    } else {
      return new ListDataTransferImpl();
    }
  }

  @SuppressWarnings("unused")
  public final void transferToArray(RasterDataNode rdn, Value pyArr, boolean geophysical) throws IOException {
    validateNumElements(pyArr, rdn.getNumDataElems());
    if (!rdn.hasRasterData()) {
      rdn.loadRasterData();
    }
    transferToArrayImpl(rdn, pyArr, geophysical);
  }

  @SuppressWarnings("unused")
  public final void transferToRaster(RasterDataNode rdn, Value pyArr, boolean geophysical) {
    validateNumElements(pyArr, rdn.getNumDataElems());
    transferToRasterImpl(pyArr, rdn, geophysical);
  }

  protected abstract void transferToArrayImpl(RasterDataNode rdn, Value pyArr, boolean geophysical);

  protected abstract void transferToRasterImpl(Value pyArr, RasterDataNode rdn, boolean geophysical);

  public static void validateNumElements(Value pyArr, long numElements) {
    String pyArrType = pyArr.getMember("__class__").getMember("__name__").asString();
    long n;

    if (pyArrType.equals("array")) {
      // For regular arrays, use the existing logic
      var itemsize = pyArr.getMember("itemsize").asInt();
      n = pyArr.getBufferSize() / itemsize;
    } else if (pyArrType.equals("ndarray")) {
      // For NumPy arrays, use size instead of buffer-based calculation
      Value size = pyArr.getMember("size");
      n = size.asLong();
    } else {
      // For lists
      n = pyArr.getArraySize();
    }

    if (numElements != n) {
      throw new IllegalArgumentException("Expected " + numElements + " elements from Python, got " + n);
    }
  }

  public interface Scaling {

    double scale(double value);

    double scaleInverse(double value);

    boolean isScalingApplied();

    int scaledType();
  }
}
