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

package org.eomasters.eomtbx.spex.formula;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpexBandTest {

  @Test
  void testCreationFromJson() {
    SpexBand[] spexBands = SpexBand.values();
    assertEquals(21, spexBands.length);
    List<String> expectedBands = List.of("A", "B", "G", "G1", "N", "N2", "R", "RE1", "RE2", "RE3", "S1", "S2", "T",
        "T1", "T2", "WV", "Y", "VH", "VV", "HV", "HH");
    assertTrue(Arrays.stream(spexBands).anyMatch(band -> expectedBands.contains(band.name())));

    SpexBand g1Band = SpexBand.valueOf("G1");
    assertEquals("G1", g1Band.name());
    assertEquals("Green 1", g1Band.getLongName());
    assertEquals("green", g1Band.getCommonName());
    assertEquals(550, g1Band.getMaxWavelength());
    assertEquals(510, g1Band.getMinWavelength());
    assertArrayEquals(new String[]{"modis", "planetscope"}, g1Band.getPlatforms());
  }

}
