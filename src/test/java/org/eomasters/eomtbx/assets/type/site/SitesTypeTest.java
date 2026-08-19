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

package org.eomasters.eomtbx.assets.type.site;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.bc.ceres.binding.PropertyContainer;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.AssetTypeRegistry;
import org.junit.jupiter.api.Test;

class SitesTypeTest {

  @Test
  void testBasicProperties() {
    SitesType type = new SitesType();
    assertEquals("Sites", type.getName());
    assertEquals("SitesType", type.getId());
    assertEquals("List of sites.", type.getDescription());
    assertEquals("eomtbx.assetLibrary.type.sites", type.getHelpId());
  }

  @Test
  void testGetCrs() {
    SitesType type = new SitesType();
    PropertyContainer propertyContainer = type.createAssetProperties();

    assertNull(propertyContainer.getValue(SitesType.PROP_SITES));
  }

  @Test
  void getFromRegistry() {
    AssetTypeRegistry registry = AssetTypeRegistry.instance();

    AssetType type = registry.getService("SitesType");
    assertNotNull(type);
  }
}
