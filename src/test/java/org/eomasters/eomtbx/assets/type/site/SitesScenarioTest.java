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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import java.nio.file.Paths;
import java.util.List;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetCreationService;
import org.eomasters.eomtbx.assets.AssetCreationRegistry;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.AssetTypeRegistry;
import org.junit.jupiter.api.Test;

public class SitesScenarioTest {

  @Test
  public void createNewAsset() throws Exception {
    // get the available asset types and show to the user
    List<AssetType> availableTypes = AssetTypeRegistry.instance().getServices();
    // the user selects based on name and description a type
    AssetType type = selectType(availableTypes);
    // there might be multiple factories available for one type
    List<AssetCreationService> factories = AssetCreationRegistry.instance().getFactories(type.getClass());
    // the user selects based on name and description a factory
    AssetCreationService factory = selectFactory(factories);
    PropertySet props = factory.createFactoryProperties();
    // the user interface to modify the properties; has default implementation, can be overridden
    // JPanel propertyPane = factory.createFactoryPanel(props);
    props.setValue("sourceFile", Paths.get(getClass().getResource("test_Shanghai.placemark").toURI()));

    // this must be done here, because only the factory knows the factory properties and the asset type
    Asset asset = factory.createAsset("Shanghai", "The POIs in the city of Shanghai",
        new String[]{"City", "Tourism", "POI"}, props, ProgressMonitor.NULL);

    assertNotNull(asset);
    assertEquals("Shanghai", asset.getName());
    assertEquals("The POIs in the city of Shanghai", asset.getDescription());
    assertEquals(2, asset.getAssetProperties().getProperties().length);
    assertArrayEquals(new String[]{"City", "Tourism", "POI"}, asset.getTags());
    Property property = asset.getAssetProperties().getProperty(SitesType.PROP_SITES);
    Site[] sites = property.getValue();
    assertEquals(1, sites.length);
    assertEquals("Shanghai_POI", sites[0].getName());
    assertEquals(31.233334, sites[0].getY());
    assertEquals(121.45, sites[0].getX());

  }

  private static AssetCreationService selectFactory(List<AssetCreationService> factories) {
    for (AssetCreationService factory : factories) {
      if (factory.getName().equals("Placemark File")) {
        return factory;
      }
    }
    return null;
  }

  private static AssetType selectType(List<AssetType> availableTypes) {
    for (AssetType availableType : availableTypes) {
      if (availableType.getName().equals("Sites")) {
        return availableType;
      }
    }
    return null;
  }

}
