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
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;

/**
 * Validates a string if it is valid an CRS code. The code must contain the authority code. Like:
 * <pre><code>
 *   EPSG:1234
 *   AUTO:42001
 * </code></pre>
 */
public class EpsgCrsValidator implements Validator {

  @Override
  public void validateValue(Property property, Object value) throws ValidationException {

    String s = String.valueOf(value);
    if (value == null || s.isEmpty()) {
      throw new ValidationException("Value for CRS must not be empty");
    }
    try {
      CRS.decode(s, true);
    } catch (FactoryException e) {
      String errMsg = String.format("Invalid CRS: %s\nValid examples: EPSG:1234 or AUTO:42001", e.getMessage());
      throw new ValidationException(errMsg);
    }
  }

}
