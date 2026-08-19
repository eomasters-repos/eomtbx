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

package org.eomasters.eomtbx.s2superres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class OtherBandsTest {

  @Test
  public void testExtractRefBandName_WithUnderscoreAndValidSpectralBand() {
    String bandName;
    bandName = "Surface_Reflectance_B2"; // Theia
    assertEquals("B2", OtherBands.extractRefBandName(bandName));

    bandName = "B03"; // Copernicus
    assertEquals("B03", OtherBands.extractRefBandName(bandName));
  }

  @Test
  public void testExtractRefBandName_WithUnderscoreAndInvalidSpectralBand() {
    String bandName = "Surface_Reflectance_Unknown";
    String result = OtherBands.extractRefBandName(bandName);
    assertEquals("Surface_Reflectance_Unknown", result);
  }

  @Test
  public void testExtractRefBandName_OtherIvalids() {
    String bandName;

    bandName = "_";
    assertEquals("_", OtherBands.extractRefBandName(bandName));

    bandName = "";
    assertEquals("", OtherBands.extractRefBandName(bandName));
  }

  @Test
  public void testExtractRefBandName_NullBandName() {
    String bandName = null;
    assertThrows(NullPointerException.class, () -> OtherBands.extractRefBandName(bandName));
  }
}
