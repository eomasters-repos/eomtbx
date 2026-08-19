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

import java.awt.Rectangle;
import org.junit.jupiter.api.Test;

public class MarginTest {

  @Test
  public void testCreate_allMarginsNonZero() {
    Rectangle scene = new Rectangle(0, 0, 500, 200);
    Rectangle roi = new Rectangle(20, 20, 160, 179);
    int margin = 30;
    Margin m = Margin.create(scene, roi, margin);

    assertEquals(10, m.getTop());
    assertEquals(10, m.getLeft());
    assertEquals(0, m.getRight());
    assertEquals(29, m.getBottom());
  }

  @Test
  public void testCreate_allMarginsZero() {
    Rectangle scene = new Rectangle(0, 0, 500, 500);
    Rectangle roi = new Rectangle(150, 150, 100, 100);
    Margin m = Margin.create(scene, roi, 30);

    assertEquals(0, m.getTop());
    assertEquals(0, m.getLeft());
    assertEquals(0, m.getRight());
    assertEquals(0, m.getBottom());
  }

  @Test
  public void testCreate_mixOfZeroAndNonZeroMargins() {
    Rectangle scene = new Rectangle(0, 0, 500, 500);
    Rectangle roi = new Rectangle(10, 10, 280, 100);
    Margin m = Margin.create(scene, roi, 50);

    assertEquals(40, m.getTop());
    assertEquals(40, m.getLeft());
    assertEquals(0, m.getRight());
    assertEquals(0, m.getBottom());
  }

  @Test
  public void testCreate_fullScene() {
    Rectangle scene = new Rectangle(0, 0, 500, 500);
    Rectangle roi = new Rectangle(0, 0, 500, 500);
    Margin m = Margin.create(scene, roi, 32);

    assertEquals(32, m.getTop());
    assertEquals(32, m.getLeft());
    assertEquals(32, m.getRight());
    assertEquals(32, m.getBottom());
  }

  @Test()
  public void testCreate_roiExceedsScene() {
    Rectangle scene = new Rectangle(0, 0, 500, 500);
    assertThrows(IllegalArgumentException.class, () -> Margin.create(scene,
        new Rectangle(-10, 0, 500, 500), 32));
    assertThrows(IllegalArgumentException.class, () -> Margin.create(scene,
        new Rectangle(0, -10, 500, 500), 32));
    assertThrows(IllegalArgumentException.class, () -> Margin.create(scene,
        new Rectangle(0, 0, 600, 500), 32));
    assertThrows(IllegalArgumentException.class, () -> Margin.create(scene,
        new Rectangle(0, 0, 500, 600), 32));
    assertThrows(IllegalArgumentException.class, () -> Margin.create(scene,
        new Rectangle(300, 0, 250, 600), 32));
  }

  @Test()
  public void testCreate_SceneDimensionIllegal() {
    Rectangle roi = new Rectangle(50, 50, 100, 100);
    assertThrows(IllegalArgumentException.class, () -> Margin.create(
        new Rectangle(-5, 0, 500, 500), roi, 32));
    assertThrows(IllegalArgumentException.class, () -> Margin.create(
        new Rectangle(0, -5, 500, 500), roi, 32));
    assertThrows(IllegalArgumentException.class, () -> Margin.create(
        new Rectangle(5, 0, 500, 500), roi, 32));
    assertThrows(IllegalArgumentException.class, () -> Margin.create(
        new Rectangle(0, 5, 500, 500), roi, 32));
  }
}
