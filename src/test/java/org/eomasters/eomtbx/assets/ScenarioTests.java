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

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.core.ProgressMonitor;
import java.util.List;
import org.eomasters.eomtbx.assets.TestDummies.DummyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ScenarioTests {

  TestDummies.AssetTypeRegistry assetTypeRegistry;
  TestDummies.AssetCreationRegistry assetFactoryRegistry;

  @BeforeEach
  void setUp() {
    assetTypeRegistry = new TestDummies.AssetTypeRegistry();
    assetFactoryRegistry = new TestDummies.AssetCreationRegistry();
  }

  @Test
  public void createNewAsset() throws Exception {
    // get the available asset types and show to the user
    List<AssetType> availableTypes = assetTypeRegistry.getServices();
    // the user selects based on name and description a type
    AssetType type = selectType(availableTypes, "DUMMY Type");
    // there might be multiple factories available for one type
    List<AssetCreationService> factories = assetFactoryRegistry.getFactories(type.getClass());
    // the user selects based on name/id and description a factory
    AssetCreationService factory = selectFactory(factories,"DUMMY Factory");
    PropertySet props = factory.createFactoryProperties();
    // No factory properties to set for the dummies

    // this must be done here, because only the factory knows the factory properties and the asset type
    Asset asset = factory.createAsset("Test 1", "Description 1",
        new String[]{"Taggidy", "Tag"}, props, ProgressMonitor.NULL);

    assertNotNull(asset);
    assertEquals("Test 1", asset.getName());
    assertEquals("Description 1", asset.getDescription());
    assertArrayEquals(new String[]{"Taggidy", "Tag"}, asset.getTags());
    PropertyContainer assetProperties = asset.getAssetProperties();
    assertEquals(3, assetProperties.getProperties().length);
    assertEquals(123, assetProperties.<Integer>getValue("Abc"));
    assertEquals("notLast", assetProperties.<String>getValue("Second"));
    assertEquals(true, assetProperties.<Boolean>getValue("3"));
  }

  private static AssetCreationService selectFactory(List<AssetCreationService> factories, String id) {
    for (AssetCreationService factory : factories) {
      if (factory.getName().equals(id)) {
        return factory;
      }
    }
    return null;
  }

  private static AssetType selectType(List<AssetType> availableTypes, String typeName) {
    for (AssetType availableType : availableTypes) {
      if (availableType.getName().equals(typeName)) {
        return availableType;
      }
    }
    return null;
  }

  @Test
  public void editExistingAsset() throws ValidationException {
    Asset asset = new Asset("Dummy", new DummyType());
    // not creating the UI by the item, because the type provider should be able to specify a custom UI.
    // JPanel panel = assetItem.getType().createEditPanel(assetItem);

    PropertyContainer assetProperties = asset.getAssetProperties();
    Property abc = assetProperties.getProperty("Abc");
    int actualValue = abc.getValue();
    assertEquals(123, actualValue);

    abc.setValue(234);
    actualValue = asset.getAssetProperties().getValue("Abc");
    assertEquals(234, actualValue);
    abc.setValueFromText("345");
    actualValue = asset.getAssetProperties().getValue("Abc");
    assertEquals(345, actualValue);
  }

}
