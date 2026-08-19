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

public class PyarrayDataTransferImpl extends AbstractBufferDataTransfer {

  PyarrayDataTransferImpl() {}

  @Override
  protected void transferToArrayImpl(RasterDataNode rdn, Value pyArr, boolean geophysical) {
    var byteBuffer = getByteBuffer(rdn, geophysical);
    IntStream.range(0, byteBuffer.remaining())
             .forEach(i -> pyArr.setArrayElement(i, (0xFF & byteBuffer.get(i))));
  }

  @Override
  protected void transferToRasterImpl(Value pyArr, RasterDataNode rdn, boolean geophysical) {
    ProductData rasterData;
    if (rdn.hasRasterData()) {
      rasterData = rdn.getRasterData();
    } else {
      rasterData = rdn.createCompatibleRasterData();
    }

    byte[] rawBytes = pyArr.as(byte[].class);
    ByteBuffer buffer = ByteBuffer.wrap(rawBytes).order(ByteOrder.nativeOrder());

    String typeCode = pyArr.getMember("typecode").asString();
    var scaling = new RdnScaling(rdn);
    switch (typeCode) {
      case "b": {
        transferFromByteBuffer(buffer, rasterData, geophysical, scaling);
        break;
      }
      case "B": {
        transferFromUByteBuffer(buffer, rasterData, geophysical, scaling);
        break;
      }
      case "h": {
        transferFromShortBuffer(buffer, rasterData, geophysical, scaling);
        break;
      }
      case "H": {
        transferFromUShortBuffer(buffer, rasterData, geophysical, scaling);
        break;
      }
      case "i": {
        transferFromIntBuffer(buffer, rasterData, geophysical, scaling);
        break;
      }
      case "I": {
        transferFromUIntBuffer(buffer, rasterData, geophysical, scaling);
        break;
      }
      case "l": {
        transferFromLongBuffer(buffer, rasterData, geophysical, scaling);
        break;
      }
      case "f": {
        transferFromFloatBuffer(buffer, rasterData, geophysical, scaling);
        break;
      }
      case "d": {
        transferFromDoubleBuffer(scaling, buffer, geophysical, rasterData);
        break;
      }
      // Add more cases as needed
      default:
        throw new IllegalArgumentException("Unknown or unsupported typecode = " + typeCode);
    }
    rdn.setRasterData(null);
    rdn.setSourceImage(null);
    rdn.setRasterData(rasterData);
  }


  private static ByteBuffer getByteBuffer(RasterDataNode rdn, boolean geophysical) {
    Object elems = rdn.getRasterData().getElems();
    return getByteBuffer(rdn.getRasterData(), geophysical, new RdnScaling(rdn));
  }

  static ByteBuffer getByteBuffer(ProductData arr, boolean asGeophysical, Scaling scaling) {
    ByteBuffer bb;
    if (asGeophysical && scaling.isScalingApplied()) {
      switch (scaling.scaledType()) {
        case ProductData.TYPE_FLOAT64 -> {
          bb = allocateByteBuffer(arr.getNumElems(), Double.BYTES);
          IntStream.range(0, arr.getNumElems()).forEach(i -> bb.putDouble(scaling.scale(arr.getElemDoubleAt(i))));
        }
        case ProductData.TYPE_FLOAT32 -> {
          bb = allocateByteBuffer(arr.getNumElems(), Float.BYTES);
          IntStream.range(0, arr.getNumElems()).forEach(i -> bb.putFloat((float) scaling.scale(arr.getElemDoubleAt(i))));
        }
        case ProductData.TYPE_INT64 -> {
          bb = allocateByteBuffer(arr.getNumElems(), Long.BYTES);
          IntStream.range(0, arr.getNumElems()).forEach(i -> bb.putLong(Math.round(scaling.scale(arr.getElemDoubleAt(i)))));
        }
        case ProductData.TYPE_INT32, ProductData.TYPE_UINT32 -> {
          bb = allocateByteBuffer(arr.getNumElems(), Integer.BYTES);
          IntStream.range(0, arr.getNumElems()).forEach(i -> bb.putInt((int) Math.round(scaling.scale(arr.getElemDoubleAt(i)))));
        }
        case ProductData.TYPE_INT16 -> {
          bb = allocateByteBuffer(arr.getNumElems(), Short.BYTES);
          IntStream.range(0, arr.getNumElems()).forEach(i -> bb.putShort((short) Math.round(scaling.scale(arr.getElemDoubleAt(i)))));
        }
        case ProductData.TYPE_UINT16 -> {
          bb = allocateByteBuffer(arr.getNumElems(), Short.BYTES);
          IntStream.range(0, arr.getNumElems())
                   .forEach(i -> bb.putShort((short) (0xffff & Math.round(scaling.scale(arr.getElemDoubleAt(i))))));
        }
        case ProductData.TYPE_INT8 -> {
          bb = allocateByteBuffer(arr.getNumElems(), Byte.BYTES);
          IntStream.range(0, arr.getNumElems()).forEach(i -> bb.put((byte) Math.round(scaling.scale(arr.getElemDoubleAt(i)))));
        }
        case ProductData.TYPE_UINT8 -> {
          bb = allocateByteBuffer(arr.getNumElems(), Byte.BYTES);
          IntStream.range(0, arr.getNumElems()).forEach(i -> bb.put((byte) (0xff & Math.round(scaling.scale(arr.getElemDoubleAt(i))))));
        }
        default -> throw new IllegalArgumentException("Unsupported geophysical type: " + scaling.scaledType());
      }

    } else {
      switch (arr.getType()) {
        case ProductData.TYPE_FLOAT64 -> {
          bb = allocateByteBuffer(arr.getNumElems(), Double.BYTES);
          bb.asDoubleBuffer().put((double[])arr.getElems());
        }
        case ProductData.TYPE_FLOAT32 -> {
          bb = allocateByteBuffer(arr.getNumElems(), Float.BYTES);
          bb.asFloatBuffer().put((float[])arr.getElems());
        }
        case ProductData.TYPE_INT64 -> {
          bb = allocateByteBuffer(arr.getNumElems(), Long.BYTES);
          bb.asLongBuffer().put((long[])arr.getElems());
        }
        case ProductData.TYPE_INT32, ProductData.TYPE_UINT32 -> {
          bb = allocateByteBuffer(arr.getNumElems(), Integer.BYTES);
          bb.asIntBuffer().put((int[])arr.getElems());
        }
        case ProductData.TYPE_INT16, ProductData.TYPE_UINT16 -> {
          bb = allocateByteBuffer(arr.getNumElems(), Short.BYTES);
          bb.asShortBuffer().put((short[])arr.getElems());
        }
        case ProductData.TYPE_INT8, ProductData.TYPE_UINT8 -> {
          bb = allocateByteBuffer(arr.getNumElems(), Byte.BYTES);
          bb.put((byte[])arr.getElems());
        }
        default -> throw new IllegalArgumentException("Unsupported raw type: " + scaling.scaledType());
      }
    }
    bb.position(0);
    return bb;
  }

  private static ByteBuffer allocateByteBuffer(int length, int bytesPerElem) {
    return ByteBuffer.allocateDirect(length * bytesPerElem).order(ByteOrder.nativeOrder());
  }

}
