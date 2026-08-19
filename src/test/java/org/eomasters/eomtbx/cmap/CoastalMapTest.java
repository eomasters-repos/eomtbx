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

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CoastalMapTest {

  @Test
  void getInstance() {
    assertNotNull(CoastalMap.getInstance());
  }

  @Test
  void getWaterTileIds() throws IOException {
    Set<Point> waterTileIds = CoastalMap.getInstance().getWaterTileIds();
    assertEquals(3723, waterTileIds.size());
    assertTrue(waterTileIds.contains(new Point(-27, 0)));
    assertTrue(waterTileIds.contains(new Point(42, -51)));
  }
}
