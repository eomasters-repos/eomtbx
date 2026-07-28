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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.ValidationException;
import org.junit.jupiter.api.Test;

class WktValidatorTest {

  @Test
  void testValidateValue() throws ValidationException {
    WktValidator validator = new WktValidator();
    Property property = Property.create("test", String.class);

    // Test valid WKT
    validator.validateValue(property, "POINT(1 2)");

    // Test invalid WKT
    ValidationException exception = assertThrows(ValidationException.class, () -> validator.validateValue(property, "INVALID(1 2)"));
    String errMsg = exception.getMessage();
    assertTrue(errMsg.startsWith("Invalid WKT:"));
    assertTrue(errMsg.endsWith("\nValid examples: POINT(1 2) or Polygon((0 0, 0 1, 1 1, 1 0, 0 0))"));
  }
}
