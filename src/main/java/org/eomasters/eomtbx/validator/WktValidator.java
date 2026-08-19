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

package org.eomasters.eomtbx.validator;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.Validator;
import org.locationtech.jts.io.WKTReader;

public class WktValidator implements Validator {

  @Override
  public void validateValue(Property property, Object value) throws ValidationException {
    String s = String.valueOf(value);
    if (value == null || s.isEmpty()) {
      throw new ValidationException("Value for WKT must not be empty");
    }
    try {
      new WKTReader().read(s);
    } catch (Exception e) {
      String errMsg = String.format("Invalid WKT: %s\n"
          + "Valid examples: POINT(1 2) or Polygon((0 0, 0 1, 1 1, 1 0, 0 0))", e.getMessage());
      throw new ValidationException(errMsg);
    }
  }
}
