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

import com.bc.ceres.binding.PropertyContainer;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.eomasters.eomtbx.assets.TestDummies.AnotherDummyType;
import org.eomasters.eomtbx.assets.TestDummies.DummyType;
import org.eomasters.eomtbx.converter.ConverterRegistrar;
import org.eomasters.utils.TextUtils;
import org.eomasters.utils.TextUtils.LineBreak;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class AssetStoreTest {

  private AssetStore assetStore;

  @BeforeEach
  void setUp() {
    assetStore = new AssetStore(new TestDummies.AssetTypeRegistry());
  }

  @Test
  public void testLoadSave_Successful() throws IOException {
    List<Asset> expectedAssets = createTestAssets();
    Path tempPath = Files.createTempFile("AssetTest", "json");
    assetStore.save(expectedAssets, tempPath);
    List<Asset> actualAssets = assetStore.load(tempPath);
    Assertions.assertEquals(expectedAssets, actualAssets);
  }

  @Test
  public void testLoad_SourceEmptyException() throws IOException {
    Path tempPath = Files.createTempFile("AssetTestEmpty", "json");
    Assertions.assertThrows(IOException.class, () -> assetStore.load(tempPath));
  }

  @Test
  public void testLoad_FileNotExistException() {
    Path tempPath = Paths.get("nonExistingPath.json");
    Assertions.assertThrows(IOException.class, () -> assetStore.load(tempPath));
  }

  @Test
  public void testLoad_RealJaf() throws Exception {
    try {
      ConverterRegistrar.registerConverter();
      var realStore = new AssetStore();
      Path jafPath = Paths.get(getClass().getResource("MyAssets.jaf").toURI());
      realStore.load(jafPath);
    } finally {
      ConverterRegistrar.registerConverter();
    }
  }

  @Test
  public void testSave_NullPointerException() {
    Assertions.assertThrows(NullPointerException.class, () -> assetStore.save(createTestAssets(), null));
  }

  @Test
  public void testFromJson_Successful() throws Exception {
    String jsonString = Files.readString(Paths.get(getClass().getResource("assets.json").toURI()));
    List<Asset> loadedAssets = assetStore.fromJson(jsonString);

    assertEquals("item1", loadedAssets.get(0).getName());
    assertEquals("DUMMY Type", loadedAssets.get(0).getType().getName());
    assertEquals(2, loadedAssets.get(0).getTags().length);
    assertEquals(3, loadedAssets.get(0).getAssetProperties().getProperties().length);
    assertEquals("Something", loadedAssets.get(0).getAssetProperties().getValue("Second"));

    assertEquals("item2", loadedAssets.get(1).getName());
    assertEquals("DUMMY Type", loadedAssets.get(1).getType().getName());
    assertEquals(3, loadedAssets.get(1).getAssetProperties().getProperties().length);

    assertEquals("item3", loadedAssets.get(2).getName());
    assertEquals("ANOTHER Type", loadedAssets.get(2).getType().getName());
    assertEquals(1, loadedAssets.get(2).getAssetProperties().getProperties().length);
    assertEquals(Color.BLUE, loadedAssets.get(2).getAssetProperties().getValue("Color"));

  }

  @Test
  public void testToJson_Successful() throws Exception {
    List<Asset> assets = createTestAssets();
    String jsonString = assetStore.toJson(assets);
    String expectedJsonString = Files.readString(Paths.get(getClass().getResource("assets.json").toURI()));
    Assertions.assertEquals(TextUtils.ensureLineBreak(expectedJsonString, LineBreak.LF), jsonString);
  }

  @Test
  public void testFromJson_NullPointerException() {
    Assertions.assertThrows(NullPointerException.class, () -> assetStore.fromJson(null));
  }

  @Test
  public void testFromJson_IllegalStateException() {
    Assertions.assertThrows(IllegalStateException.class, () -> assetStore.fromJson("  "));
  }

  @Test
  public void testFromToJson_Successful() {
    List<Asset> assets = AssetStoreTest.createTestAssets();
    String jsonString = assetStore.toJson(assets);
    List<Asset> loadedAssets = assetStore.fromJson(jsonString);
    Assertions.assertEquals(assets, loadedAssets);
  }

  @Test
  public void testToJson_NullPointerException() {
    Assertions.assertThrows(NullPointerException.class, () -> assetStore.toJson(null));
  }

  private static List<Asset> createTestAssets() {
    List<Asset> assets = new ArrayList<>();
    Asset item1 = new Asset("item1", new DummyType());
    PropertyContainer assetProperties = item1.getAssetProperties();
    assetProperties.setValue("Second", "Something");
    item1.setTags(new String[]{"Water", "Elbe"});
    assets.add(item1);

    Asset item2 = new Asset("item2", new DummyType());
    item2.setUserNotes("belongs to:\nProject A\nProject B");
    item2.setTags(new String[]{"Water", "Colorado"});
    assets.add(item2);

    assets.add(new Asset("item3", new AnotherDummyType()));
    return assets;
  }
}
