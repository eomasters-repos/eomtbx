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

package org.eomasters.eomtbx.utils;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.*;

class GsonStoreTest {

  @SuppressWarnings("unused")
  private static class DummyObject {

    private String name;
    private int[] numbers;
    private Color color;

  }

  private static class ColorAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {

    @Override
    public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      JsonObject jsonObject = json.getAsJsonObject();
      int colorInt = jsonObject.getAsJsonPrimitive("coloring").getAsInt();
      return new Color(colorInt, true);
    }

    @Override
    public JsonElement serialize(Color value, Type typeOfSrc, JsonSerializationContext context) {
      JsonObject colorObject = new JsonObject();
      colorObject.addProperty("coloring", value.getRGB());
      return colorObject;
    }
  }

  private static GsonStore<DummyObject> store;

  @BeforeAll
  public static void beforeClass() {
    store = new GsonStore<>(DummyObject.class,
        builder -> builder.registerTypeAdapter(Color.class, new ColorAdapter()));
  }

  @Test
  void loadSyntaxError() throws URISyntaxException {
    try {
      final Path location = Paths.get(Objects.requireNonNull(GsonStoreTest.class.getResource("SyntaxError.jsn")).toURI());
      store.load(location);
      Assertions.fail("Expected exception");
    } catch (IOException e) {
      Assertions.assertTrue(e.getCause() instanceof JsonSyntaxException);
    }
  }

  @Test
  void load() throws URISyntaxException, IOException {
      final Path location = Paths.get(Objects.requireNonNull(GsonStoreTest.class.getResource("DummyObject.jsn")).toURI());
      DummyObject loaded = store.load(location);
      Assertions.assertEquals("Winnie", loaded.name);
      Assertions.assertArrayEquals(new int[]{8,8,888}, loaded.numbers);
      Assertions.assertEquals(895613501, loaded.color.getRGB());
  }

  @Test
  void save() throws IOException {
    try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
      final Path storeDir = fs.getPath("store");
      Files.createDirectory(storeDir);
      final Path target = storeDir.resolve("target");
      DummyObject dummyObject = new DummyObject();
      dummyObject.name = "test";
      dummyObject.numbers = new int[]{1, 2, 3};
      dummyObject.color = new Color(255, 255, 255, 127);
      store.save(dummyObject, target);
      Files.exists(target);
    }
  }

  @Test
  void asString() {
    DummyObject dummyObject = new DummyObject();
    dummyObject.name = "test";
    dummyObject.numbers = new int[]{1, 2, 3};
    dummyObject.color = new Color(255, 255, 255, 127);
    final String text = store.asString(dummyObject);
    Assertions.assertEquals("{\n"
        + "  \"name\": \"test\",\n"
        + "  \"numbers\": [\n"
        + "    1,\n"
        + "    2,\n"
        + "    3\n"
        + "  ],\n"
        + "  \"color\": {\n"
        + "    \"coloring\": 2147483647\n"
        + "  }\n"
        + "}", text);
  }

}
