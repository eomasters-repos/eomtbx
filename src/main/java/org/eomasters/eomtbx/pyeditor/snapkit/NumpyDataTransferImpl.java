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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.stream.IntStream;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.graalvm.polyglot.Value;

public class NumpyDataTransferImpl extends AbstractBufferDataTransfer {

  protected NumpyDataTransferImpl() {}

  @Override
  protected void transferToArrayImpl(RasterDataNode rdn, Value pyArr, boolean geophysical) {
    ProductData elems = rdn.getRasterData();

    // NumPy arrays can be treated similarly to lists for element-wise access
    IntStream.range(0, elems.getNumElems())
             .forEach(i -> {
               // Check the numpy array's dtype to determine the appropriate data type
               Value dtype = pyArr.getMember("dtype");
               String dtypeName = dtype.getMember("name").asString();

               if (dtypeName.startsWith("int") || dtypeName.startsWith("uint")) {
                 if (geophysical && rdn.isScalingApplied()) {
                   pyArr.setArrayElement(i, (long) rdn.scale(elems.getElemIntAt(i)));
                 } else {
                   pyArr.setArrayElement(i, elems.getElemIntAt(i));
                 }
               } else if (dtypeName.startsWith("float")) {
                 if (geophysical && rdn.isScalingApplied()) {
                   pyArr.setArrayElement(i, rdn.scale(elems.getElemDoubleAt(i)));
                 } else {
                   pyArr.setArrayElement(i, elems.getElemDoubleAt(i));
                 }
               } else {
                 // Default to double for unknown types
                 if (geophysical && rdn.isScalingApplied()) {
                   pyArr.setArrayElement(i, rdn.scale(elems.getElemDoubleAt(i)));
                 } else {
                   pyArr.setArrayElement(i, elems.getElemDoubleAt(i));
                 }
               }
             });
  }

  @Override
  protected void transferToRasterImpl(Value pyArr, RasterDataNode rdn, boolean geophysical) {
    // Try buffer-based transfer first for best performance
    try {
      if (pyArr.hasBufferElements()) {
        copyNumpyArrayViaBuffer(pyArr, rdn, geophysical);
        return;
      }
    } catch (Exception e) {
      // Fall back to list-based approach if buffer fails
    }

    // Convert to Python list and use existing list handling
    try {
      Value pythonList = pyArr.getMember("tolist").execute();
      DataTransfer.create(pythonList).transferToRasterImpl(pythonList, rdn, geophysical);
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert NumPy array for transfer", e);
    }
  }


  private static void copyNumpyArrayViaBuffer(Value pyArr, RasterDataNode rdn, boolean geophysical) {
    ProductData rasterData;
    if (rdn.hasRasterData()) {
      rasterData = rdn.getRasterData();
    } else {
      rasterData = rdn.createCompatibleRasterData();
    }

    Value dtype = pyArr.getMember("dtype");
    String dtypeName = dtype.getMember("name").asString();

    byte[] rawBytes = pyArr.as(byte[].class);
    ByteBuffer buffer = ByteBuffer.wrap(rawBytes).order(ByteOrder.nativeOrder());

    var scaleInverse = geophysical && rdn.isScalingApplied();

    // Map numpy dtypes to processing logic similar to copyArrayToRdn
    var scaling = new RdnScaling(rdn);
    switch (dtypeName) {
      case "int8": {
        transferFromByteBuffer(buffer, rasterData, scaleInverse, scaling);
        break;
      }
      case "uint8": {
        transferFromUByteBuffer(buffer, rasterData, scaleInverse, scaling);
        break;
      }
      case "int16": {
        transferFromShortBuffer(buffer, rasterData, scaleInverse, scaling);
        break;
      }
      case "uint16": {
        transferFromUShortBuffer(buffer, rasterData, scaleInverse, scaling);
        break;
      }
      case "int32": {
        transferFromIntBuffer(buffer, rasterData, scaleInverse, scaling);
        break;
      }
      case "uint32": {
        transferFromUIntBuffer(buffer, rasterData, scaleInverse, scaling);
        break;
      }
      case "int64": {
        transferFromLongBuffer(buffer, rasterData, scaleInverse, scaling);
        break;
      }
      case "float32": {
        transferFromFloatBuffer(buffer, rasterData, scaleInverse, scaling);
        break;
      }
      case "float64": {
        transferFromDoubleBuffer(scaling, buffer, scaleInverse, rasterData);
        break;
      }
      default:
        throw new IllegalArgumentException("Unknown or unsupported numpy dtype = " + dtypeName);
    }

    rdn.setRasterData(null);
    rdn.setSourceImage(null);
    rdn.setRasterData(rasterData);
  }
}
