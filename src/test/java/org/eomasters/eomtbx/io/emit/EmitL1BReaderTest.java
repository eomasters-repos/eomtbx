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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import org.esa.snap.core.datamodel.ProductData.UTC;
import org.junit.jupiter.api.Test;

class EmitL1BReaderTest {

  @Test
  void testParsingTime() {
    UTC time = AbstractEmitReader.parse("2024-10-13T11:41:36+0000");
    Calendar asCalendar = time.getAsCalendar();
    assertEquals(2024, asCalendar.get(Calendar.YEAR));
    assertEquals(10 - 1, asCalendar.get(Calendar.MONTH));
    assertEquals(13, asCalendar.get(Calendar.DAY_OF_MONTH));
    assertEquals(11, asCalendar.get(Calendar.HOUR_OF_DAY));
    assertEquals(41, asCalendar.get(Calendar.MINUTE));
    assertEquals(36, asCalendar.get(Calendar.SECOND));
    assertEquals(0, asCalendar.get(Calendar.MILLISECOND));
  }
}
