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

import java.util.stream.IntStream;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.graalvm.polyglot.Value;

public class ListDataTransferImpl extends DataTransfer {

  ListDataTransferImpl() {}

  @Override
  public void transferToArrayImpl(RasterDataNode rdn, Value pyArr, boolean geophysical) {
    ProductData elems = rdn.getRasterData();
    Value firstElem = pyArr.getArrayElement(0);

    IntStream.range(0, elems.getNumElems())
             .forEach(i -> {
               if (firstElem.fitsInInt()) {
                 if (geophysical && rdn.isScalingApplied()) {
                   pyArr.setArrayElement(i, (long) rdn.scale(elems.getElemIntAt(i)));
                 } else {
                   pyArr.setArrayElement(i, elems.getElemIntAt(i));
                 }
               } else if (firstElem.fitsInDouble()) {
                 if (geophysical && rdn.isScalingApplied()) {
                   pyArr.setArrayElement(i, rdn.scale(elems.getElemDoubleAt(i)));
                 } else {
                   pyArr.setArrayElement(i, elems.getElemDoubleAt(i));
                 }
               }
             });
  }

  @Override
  public void transferToRasterImpl(Value pyArr, RasterDataNode rdn, boolean geophysical) {
    ProductData rasterData;
    if (rdn.hasRasterData()) {
      rasterData = rdn.getRasterData();
    } else {
      rasterData = rdn.createCompatibleRasterData();
    }
    if (pyArr.hasArrayElements()) {
      for (int i = 0; i < rasterData.getNumElems(); i++) {
        Value elem = pyArr.getArrayElement(i);
        if (elem.fitsInInt()) {
          int intVal = elem.asInt();
          if (geophysical && rdn.isScalingApplied()) {
            rasterData.setElemDoubleAt(i, rdn.scaleInverse(intVal));
          } else {
            rasterData.setElemIntAt(i, intVal);
          }
        } else if (elem.fitsInLong()) {
          long longVal = elem.asLong();
          if (geophysical && rdn.isScalingApplied()) {
            rasterData.setElemDoubleAt(i, rdn.scaleInverse(longVal));
          } else {
            rasterData.setElemLongAt(i, longVal);
          }
        } else if (elem.fitsInDouble() || elem.isNull()) {
          double doubleVal = elem.isNull() ? Double.NaN : elem.asDouble();
          if (geophysical && rdn.isScalingApplied()) {
            rasterData.setElemDoubleAt(i, rdn.scaleInverse(doubleVal));
          } else {
            rasterData.setElemDoubleAt(i, doubleVal);
          }
        } else {
          throw new IllegalArgumentException("Unknown or unsupported type");
        }
      }
    }
    rdn.setRasterData(null);
    rdn.setSourceImage(null);
    rdn.setRasterData(rasterData);
  }

}
