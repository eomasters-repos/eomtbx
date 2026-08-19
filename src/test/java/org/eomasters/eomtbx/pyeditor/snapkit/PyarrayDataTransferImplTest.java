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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;
import org.eomasters.eomtbx.pyeditor.snapkit.DataTransfer.Scaling;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.jupiter.api.Test;

class PyarrayDataTransferImplTest {

  private static final NoScaling NO_SCALING = new NoScaling();

  @Test
  void testGetByteBufferWithDoubleArray() {
    // Arrange
    double[] data = {-1.1, 2.2, 3.3};

    // Act
    ByteBuffer result = PyarrayDataTransferImpl.getByteBuffer(ProductData.createInstance(data), false, NO_SCALING);

    // Assert
    assertNotNull(result);
    assertEquals(Double.BYTES * data.length, result.capacity());
    assertEquals(-1.1, result.asDoubleBuffer().get(0), 1e-9);
    assertEquals(2.2, result.asDoubleBuffer().get(1), 1e-9);
    assertEquals(3.3, result.asDoubleBuffer().get(2), 1e-9);
  }

  @Test
  void testGetByteBufferWithFloatArray() {
    // Arrange
    float[] data = {-1.1f, 2.2f, 3.3f};

    // Act
    ByteBuffer result = PyarrayDataTransferImpl.getByteBuffer(ProductData.createInstance(data), false, NO_SCALING);

    // Assert
    assertNotNull(result);
    assertEquals(Float.BYTES * data.length, result.capacity());
    assertEquals(-1.1f, result.asFloatBuffer().get(0), 1e-6);
    assertEquals(2.2f, result.asFloatBuffer().get(1), 1e-6);
    assertEquals(3.3f, result.asFloatBuffer().get(2), 1e-6);
  }

  @Test
  void testGetByteBufferWithLongArray() {
    // Arrange
    long[] data = {-1L, 2L, 3L};

    // Act
    ByteBuffer result = PyarrayDataTransferImpl.getByteBuffer(ProductData.createInstance(data), false, NO_SCALING);

    // Assert
    assertNotNull(result);
    assertEquals(Long.BYTES * data.length, result.capacity());
    assertEquals(-1L, result.asLongBuffer().get(0));
    assertEquals(2L, result.asLongBuffer().get(1));
    assertEquals(3L, result.asLongBuffer().get(2));
  }

  @Test
  void testGetByteBufferWithIntArray() {
    // Arrange
    int[] data = {-1, 2, 3};

    // Act
    ByteBuffer result = PyarrayDataTransferImpl.getByteBuffer(ProductData.createInstance(data), false, NO_SCALING);

    // Assert
    assertNotNull(result);
    assertEquals(Integer.BYTES * data.length, result.capacity());
    assertEquals(-1, result.asIntBuffer().get(0));
    assertEquals(2, result.asIntBuffer().get(1));
    assertEquals(3, result.asIntBuffer().get(2));
  }

  @Test
  void testGetByteBufferWithShortArray() {
    // Arrange
    short[] data = {-1, 2, 3};

    // Act
    ByteBuffer result = PyarrayDataTransferImpl.getByteBuffer(ProductData.createInstance(data), false, NO_SCALING);

    // Assert
    assertNotNull(result);
    assertEquals(Short.BYTES * data.length, result.capacity());
    assertEquals(-1, result.asShortBuffer().get(0));
    assertEquals(2, result.asShortBuffer().get(1));
    assertEquals(3, result.asShortBuffer().get(2));
  }

  @Test
  void testGetByteBufferWithByteArray() {
    // Arrange
    byte[] data = {-1, 2, 3};

    // Act
    ByteBuffer result = PyarrayDataTransferImpl.getByteBuffer(ProductData.createInstance(data), false, NO_SCALING);

    // Assert
    assertNotNull(result);
    assertEquals(Byte.BYTES * data.length, result.capacity());
    assertEquals(-1, result.get(0));
    assertEquals(2, result.get(1));
    assertEquals(3, result.get(2));
  }

  @Test
  void testGetByteBufferWithUnsupportedType() {
    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> PyarrayDataTransferImpl.getByteBuffer(ProductData.createInstance("data"), false, NO_SCALING));
  }

  private static class NoScaling implements Scaling {

    @Override
    public double scale(double value) {
      return value;
    }

    @Override
    public double scaleInverse(double value) {
      return value;
    }

    @Override
    public boolean isScalingApplied() {
      return false;
    }

    @Override
    public int scaledType() {
      return -1;
    }
  }
}
