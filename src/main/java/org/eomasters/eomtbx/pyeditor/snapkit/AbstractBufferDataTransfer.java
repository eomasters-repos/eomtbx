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
import java.util.stream.IntStream;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.RasterDataNode;

public abstract class AbstractBufferDataTransfer extends DataTransfer {

  protected static void transferFromDoubleBuffer(Scaling scaling, ByteBuffer buffer, boolean geophysical,
                                                 ProductData rasterData) {
    var doubleBuffer = buffer.asDoubleBuffer();
    if (geophysical && scaling.isScalingApplied()) {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        var value = doubleBuffer.get(i);
        rasterData.setElemDoubleAt(i, scaling.scaleInverse(value));
      });
    } else {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        var value = doubleBuffer.get(i);
        rasterData.setElemDoubleAt(i, value);
      });
    }
  }

  protected static void transferFromFloatBuffer(ByteBuffer buffer, ProductData rasterData, boolean geophysical,
                                                Scaling scaling) {
    var floatBuffer = buffer.asFloatBuffer();
    if (geophysical && scaling.isScalingApplied()) {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        var value = floatBuffer.get(i);
        rasterData.setElemDoubleAt(i, scaling.scaleInverse(value));
      });
    } else {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        var value = floatBuffer.get(i);
        rasterData.setElemFloatAt(i, value);
      });
    }
  }

  protected static void transferFromLongBuffer(ByteBuffer buffer, ProductData rasterData, boolean geophysical,
                                               Scaling scaling) {
    var longBuffer = buffer.asLongBuffer();
    if (geophysical && scaling.isScalingApplied()) {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        var value = longBuffer.get(i);
        rasterData.setElemDoubleAt(i, scaling.scaleInverse(value));
      });
    } else {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        var value = longBuffer.get(i);
        rasterData.setElemLongAt(i, value);
      });
    }
  }

  protected static void transferFromUIntBuffer(ByteBuffer buffer, ProductData rasterData, boolean geophysical,
                                               Scaling scaling) {
    var intBuffer = buffer.asIntBuffer();
    if (geophysical && scaling.isScalingApplied()) {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        var value = intBuffer.get(i);
        rasterData.setElemDoubleAt(i, scaling.scaleInverse(value));
      });
    } else {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        var value = intBuffer.get(i);
        rasterData.setElemUIntAt(i, value);
      });
    }
  }

  protected static void transferFromIntBuffer(ByteBuffer buffer, ProductData rasterData, boolean geophysical,
                                              Scaling scaling) {
    var intBuffer = buffer.asIntBuffer();
    if (geophysical && scaling.isScalingApplied()) {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        var value = intBuffer.get(i);
        rasterData.setElemDoubleAt(i, scaling.scaleInverse(value));
      });
    } else {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        var value = intBuffer.get(i);
        rasterData.setElemIntAt(i, value);
      });
    }
  }

  protected static void transferFromUShortBuffer(ByteBuffer buffer, ProductData rasterData, boolean geophysical,
                                                 Scaling scaling) {
    var shortBuffer = buffer.asShortBuffer();
    if (geophysical && scaling.isScalingApplied()) {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        var value = Short.toUnsignedInt(shortBuffer.get(i));
        rasterData.setElemDoubleAt(i, scaling.scaleInverse(value));
      });
    } else {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        var value = Short.toUnsignedInt(shortBuffer.get(i));
        rasterData.setElemUIntAt(i, value);
      });
    }
  }

  protected static void transferFromShortBuffer(ByteBuffer buffer, ProductData rasterData, boolean geophysical,
                                                Scaling scaling) {
    var shortBuffer = buffer.asShortBuffer();
    if (geophysical && scaling.isScalingApplied()) {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        var value = shortBuffer.get(i);
        rasterData.setElemDoubleAt(i, scaling.scaleInverse(value));
      });
    } else {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        var value = shortBuffer.get(i);
        rasterData.setElemIntAt(i, value);
      });
    }
  }

  protected static void transferFromUByteBuffer(ByteBuffer buffer, ProductData rasterData, boolean geophysical,
                                                Scaling scaling) {
    if (geophysical && scaling.isScalingApplied()) {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        rasterData.setElemDoubleAt(i, scaling.scaleInverse(Byte.toUnsignedInt(buffer.get(i))));
      });
    } else {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        rasterData.setElemIntAt(i, Byte.toUnsignedInt(buffer.get(i)));
      });
    }
  }

  protected static void transferFromByteBuffer(ByteBuffer buffer, ProductData rasterData, boolean geophysical,
                                               Scaling scaling) {
    if (geophysical && scaling.isScalingApplied()) {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        rasterData.setElemDoubleAt(i, scaling.scaleInverse(buffer.get(i)));
      });
    } else {
      IntStream.range(0, rasterData.getNumElems()).forEach(i -> {
        rasterData.setElemIntAt(i, buffer.get(i));
      });
    }
  }

  protected record RdnScaling(RasterDataNode rdn) implements Scaling {

    @Override
    public double scale(double value) {
      return rdn.scale(value);
    }

    @Override
    public double scaleInverse(double value) {
      return rdn.scaleInverse(value);
    }

    @Override
    public boolean isScalingApplied() {
      return rdn.isScalingApplied();
    }

    @Override
    public int scaledType() {
      return rdn.getGeophysicalDataType();
    }
  }

}
