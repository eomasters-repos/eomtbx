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

package org.eomasters.eomtbx.assets.type.bmaths;

import org.esa.snap.core.datamodel.ProductData;

public enum DataType {

  BYTE(ProductData.TYPE_INT8),
  SHORT(ProductData.TYPE_INT16),
  INT(ProductData.TYPE_INT32),
  LONG(ProductData.TYPE_INT64),
  FLOAT(ProductData.TYPE_FLOAT32),
  DOUBLE(ProductData.TYPE_FLOAT64);

  private final int type;

  DataType(int type) {
    this.type = type;
  }

  public int getProductDataType() {
    return type;
  }
}
