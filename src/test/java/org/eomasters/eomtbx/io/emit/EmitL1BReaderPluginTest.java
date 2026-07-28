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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EmitL1BReaderPluginTest {

  @Test
  void testFileNameExpression() {
    assertTrue("EMIT_L1B_RAD_001_20241011T131626_2428509_004.nc".toUpperCase().matches(EmitL1bReaderPlugin.FILENAME_REGEX));
    assertTrue("emit_L1B_RAD_001_20241011T131626_2428509_004.NC".toUpperCase().matches(EmitL1bReaderPlugin.FILENAME_REGEX));
    assertFalse("EMIT_L2A_RFL_001_20241011T131626_2428509_004.nc".toUpperCase().matches(EmitL1bReaderPlugin.FILENAME_REGEX));
  }
}
