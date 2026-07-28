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

class EpsgCrsValidatorTest {

  @Test
  void testValidateValue() throws ValidationException {
    EpsgCrsValidator validator = new EpsgCrsValidator();
    Property property = Property.create("test", String.class);

    // Test valid CRS
    validator.validateValue(property, "EPSG:4326");

    // Test invalid authority
    ValidationException exception;

    exception = assertThrows(ValidationException.class, () -> validator.validateValue(property, "INVALID:1234"));
    assertTrue(exception.getMessage().startsWith("Invalid CRS:"));
    assertTrue(exception.getMessage().endsWith("Valid examples: EPSG:1234 or AUTO:42001"));

    // Test invalid code
    exception = assertThrows(ValidationException.class, () -> validator.validateValue(property, "EPSG:1A34"));
    assertTrue(exception.getMessage().startsWith("Invalid CRS:"));
    assertTrue(exception.getMessage().endsWith("Valid examples: EPSG:1234 or AUTO:42001"));
  }
}
