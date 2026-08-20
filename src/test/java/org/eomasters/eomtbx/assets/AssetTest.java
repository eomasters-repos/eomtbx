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

package org.eomasters.eomtbx.assets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eomasters.eomtbx.assets.TestDummies.DummyType;
import org.junit.jupiter.api.Test;

public class AssetTest {

  @Test
  public void testCreation() {
    DummyType type = new DummyType();
    Asset item = new Asset("Test Item", type);
    assertNotNull(item);
    assertEquals("Test Item", item.getName());
    assertEquals(type, item.getType());
    assertEquals(0, item.getTags().length);
  }

  @Test
  public void testSetters() {
    Asset item = new Asset("Test Item", new DummyType());
    item.setName("Updated Item");
    item.setDescription("Test Description");
    item.setTags(new String[]{"tag1", "tag2"});
    item.setUserNotes("a value");

    assertEquals("Updated Item", item.getName());
    assertEquals("Test Description", item.getDescription());
    assertArrayEquals(new String[]{"tag1", "tag2"}, item.getTags());
    assertEquals("a value", item.getUserNotes());
  }

}
