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

package org.eomasters.eomtbx.io.emit;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.BitSet;
import javax.media.jai.PlanarImage;
import org.esa.snap.core.image.ResolutionLevel;
import org.esa.snap.core.image.SingleBandedOpImage;
import ucar.ma2.Array;
import ucar.ma2.Section;
import ucar.nc2.Variable;

public class EmitL2aFlagsOpImage extends SingleBandedOpImage {

  private final Variable masksVariable;

  public EmitL2aFlagsOpImage(Variable masksVariable, Dimension size, Dimension tileSize) {
    super(DataBuffer.TYPE_BYTE, size.width, size.height, tileSize, null, ResolutionLevel.MAXRES);
    this.masksVariable = masksVariable;
  }

  @Override
  protected void computeRect(PlanarImage[] sourceImages, WritableRaster tile, Rectangle destRect) {
    final int[] origin = new int[]{destRect.y, destRect.x, 0};
    final int[] shape = new int[]{destRect.height, destRect.width, 1};
    final int[] stride = new int[]{1, 1, 1};

    synchronized (masksVariable) {
      BitSet flags = new BitSet(destRect.height * destRect.width * Byte.SIZE);
      int[] flagIndices = EmitL2aConstants.FLAG_INDICES;
      for (int i = 0; i < flagIndices.length; i++) {
        try {
          origin[2] = flagIndices[i];
          final Section section = new Section(origin, shape, stride);
          Array array = masksVariable.read(section);
          long size = array.getSize();
          for (int m = 0; m < size; m++) {
            int bitIndex = m * Byte.SIZE + i;
            if (array.getFloat(m) != 0) {
              flags.set(bitIndex);
            }else {
              flags.clear(bitIndex);
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      try {
        byte[] byteArray = flags.toByteArray();
        byte[] ensuredLengthArray = Arrays.copyOf(byteArray, destRect.width * destRect.height);
        tile.setDataElements(destRect.x, destRect.y, destRect.width, destRect.height, ensuredLengthArray);
      } catch (ArrayIndexOutOfBoundsException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
