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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;
import org.eomasters.eomtbx.assets.TestDummies.AnotherAssetCreationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AssetCreationServiceRegistryTest {

  private static AssetCreationRegistry registry;

  @BeforeAll
  public static void beforeClass() {
    registry = AssetCreationRegistry.instance();
  }

  @Test
  public void instance() {
    assertNotNull(registry);
  }

  @Test
  public void getRegisteredTypeNames() {
    final List<String> registeredTypeNames = registry.getServiceNames();
    assertFalse(registeredTypeNames.isEmpty());
  }

  @Test
  public void getServiceIgnoresCase() {
    assertNotNull(registry.getService("PlacemarkFileCreationService"));
    assertNotNull(registry.getService("PlacemarkFILECreationService"));
    assertNotNull(registry.getService("PlacemarkfilecreationService"));
  }

  @Test
  public void size() {
    assertEquals(registry.getServiceNames().size(), registry.size());
  }

  @Test
  public void contains() {
    assertTrue(registry.contains(registry.getService("PlacemarkFileCreationService")));
    assertTrue(registry.contains(registry.getService("CsvFileCreationService")));
    assertTrue(registry.contains(registry.getService("WktGeometryCreationService")));
    assertFalse(registry.contains(new AnotherAssetCreationService()));
  }

  @Test
  public void iterator() {
    final Iterator<AssetCreationService> iterator = registry.iterator();
    int counter = 0;
    while (iterator.hasNext()) {
      assertNotNull(iterator.next());
      counter++;
    }
    assertEquals(registry.size(), counter);
  }

}
