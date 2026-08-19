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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class AssetTypeRegistryTest {

  @Test
  void test() {
    AssetTypeRegistry registry = AssetTypeRegistry.instance();
    List<AssetType> services = registry.getServices();
    assertEquals(4, services.size());

    AssetType sites = registry.getService("SitesType");
    assertNotNull(sites);
    AssetType geometry = registry.getService("GeometryType");
    assertNotNull(geometry);
    AssetType mask = registry.getService("MaskType");
    assertNotNull(mask);
    AssetType bandMaths = registry.getService("BandMathsType");
    assertNotNull(bandMaths);
  }

}
