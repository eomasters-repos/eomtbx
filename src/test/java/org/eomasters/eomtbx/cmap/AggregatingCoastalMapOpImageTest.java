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

package org.eomasters.eomtbx.cmap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AggregatingCoastalMapOpImageTest {

  @Test
  void testAggregation() {
    int result = AggregatingCoastalMapOpImage.doAggregation(new int[]{
        FlagsAndMasks.COASTLINE_FLAG_VALUE | FlagsAndMasks.WATER_VICINITY_MID_FLAG_VALUE,
        FlagsAndMasks.LAND_FLAG_VALUE | FlagsAndMasks.WATER_VICINITY_MID_FLAG_VALUE
            | FlagsAndMasks.INTERTIDAL_FLAG_VALUE,
        FlagsAndMasks.LAND_FLAG_VALUE | FlagsAndMasks.WATER_VICINITY_MID_FLAG_VALUE
            | FlagsAndMasks.INTERTIDAL_FLAG_VALUE,
        FlagsAndMasks.LAND_FLAG_VALUE | FlagsAndMasks.WATER_VICINITY_MID_FLAG_VALUE
            | FlagsAndMasks.INTERTIDAL_FLAG_VALUE,
        FlagsAndMasks.LAND_FLAG_VALUE | FlagsAndMasks.WATER_VICINITY_LOW_FLAG_VALUE
            | FlagsAndMasks.INTERTIDAL_FLAG_VALUE,
        FlagsAndMasks.LAND_VICINITY_HIGH_FLAG_VALUE,
    });

    Assertions.assertEquals(FlagsAndMasks.LAND_FLAG_VALUE, result & FlagsAndMasks.LW_MASK);
    Assertions.assertEquals(FlagsAndMasks.COASTLINE_FLAG_VALUE, result & FlagsAndMasks.COASTLINE_MASK);
    Assertions.assertEquals(FlagsAndMasks.INTERTIDAL_FLAG_VALUE, result & FlagsAndMasks.INTERTIDAL_MASK);
    Assertions.assertEquals(FlagsAndMasks.WATER_VICINITY_MID_FLAG_VALUE, result & FlagsAndMasks.WATER_VIC_MASK);
    Assertions.assertEquals(0, result & FlagsAndMasks.LAND_VIC_MASK);
  }
}
