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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eomasters.eomtbx.spex.TestPreferences;
import org.esa.snap.core.datamodel.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AssetLibraryTest {

  private AssetLibrary assetLibrary;

  @BeforeEach
  public void setUp() throws IOException {
    assetLibrary = AssetLibrary.create(new TestPreferences());
  }

  @Test
  public void testSize() {
    assertEquals(0, assetLibrary.size());
    assetLibrary.add(new Asset("test1", createDummyType()));
    assertEquals(1, assetLibrary.size());

    final Asset res2 = new Asset("test2", createDummyType());
    assetLibrary.add(res2);
    assertEquals(2, assetLibrary.size());

    assertTrue(assetLibrary.remove(res2));
    assertEquals(1, assetLibrary.size());

    assetLibrary.clear();
    assertEquals(0, assetLibrary.size());
  }

  @Test
  public void testIsEmpty() {
    assertTrue(assetLibrary.isEmpty());

    final Asset asset = new Asset("test1", createDummyType());
    assetLibrary.add(asset);
    assertFalse(assetLibrary.isEmpty());

    assertTrue(assetLibrary.remove(asset));
    assertTrue(assetLibrary.isEmpty());
  }

  @Test
  public void testRemoveFromEmpty() {
    Asset asset = new Asset("test1", createDummyType());
    assertFalse(assetLibrary.remove(asset));
  }

  @Test
  public void testContains() {
    Asset asset1 = new Asset("test1", createDummyType());
    Asset asset2 = new Asset("test2", createDummyType());

    assertFalse(assetLibrary.contains(asset1));
    assertFalse(assetLibrary.contains(asset2));

    assetLibrary.add(asset1);
    assertTrue(assetLibrary.contains(asset1));
    assertFalse(assetLibrary.contains(asset2));

    assetLibrary.add(asset2);
    assertTrue(assetLibrary.contains(asset1));
    assertTrue(assetLibrary.contains(asset2));

    assertTrue(assetLibrary.remove(asset2));
    assertTrue(assetLibrary.contains(asset1));
    assertFalse(assetLibrary.contains(asset2));
  }

  @Test
  public void testGetAssets() {
    assertTrue(assetLibrary.getAssets().isEmpty());

    Asset asset1 = new Asset("test1", createDummyType());
    Asset asset2 = new Asset("test2", createDummyType());
    assetLibrary.add(asset1);
    assetLibrary.add(asset2);

    List<Asset> items = assetLibrary.getAssets();
    assertEquals(2, items.size());
    assertTrue(items.contains(asset1));
    assertTrue(items.contains(asset2));
  }

  @Test
  public void testGetAssetsWithSearchText() {
    Asset asset1 = new Asset("test1", createDummyType());
    Asset asset2 = new Asset("test2", createDummyType());
    assetLibrary.add(asset1, asset2);

    List<Asset> items;
    items = assetLibrary.getAssets("test1");
    assertEquals(1, items.size());
    assertTrue(items.contains(asset1));

    items = assetLibrary.getAssets("PropertyValue");
    assertEquals(0, items.size());
  }

  @Test
  public void testTags() {
    Asset asset1 = new Asset("test1", createDummyType());
    asset1.setTags(new String[]{"Water", "Elbe"});
    Asset asset2 = new Asset("test2", createDummyType());
    asset2.setTags(new String[]{"Colorado", "Water"});
    Asset asset3 = new Asset("test3", createDummyType());

    assetLibrary.add(asset1, asset2, asset3);

    Set<String> tags = assetLibrary.getTags();
    assertEquals(3, tags.size());
    assertTrue(tags.contains("Water"));
    assertTrue(tags.contains("Elbe"));
    assertTrue(tags.contains("Colorado"));
  }

  @Test
  public void testIterator() {
    Asset asset1 = new Asset("test1", createDummyType());
    Asset asset2 = new Asset("test2", createDummyType());
    Asset asset3 = new Asset("test3", createDummyType());

    assetLibrary.add(asset1);
    assetLibrary.add(asset2);
    assetLibrary.add(asset3);

    Iterator<Asset> iterator = assetLibrary.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(asset1, iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(asset2, iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(asset3, iterator.next());
    assertFalse(iterator.hasNext());
  }

  private static AssetType createDummyType() {
    return new AssetType("DUMMY Type", "no description") {
      @Override
      protected void initAssetProperties(PropertySet attributes) {
        attributes.addProperty(Property.create("Abc", 123));
        attributes.addProperty(Property.create("Second", "PropertyValue"));
        attributes.addProperty(Property.create("3", true));
      }

      @Override
      protected void initAddConfiguration(PropertySet addConfig, Asset asset, Product product) {
        // nothing
      }

      @Override
      public void addToProduct(Asset asset, Product product, PropertySet addConfig, ProgressMonitor pm) {
        // nothing
      }
    };
  }

}
