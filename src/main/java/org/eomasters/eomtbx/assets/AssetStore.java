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

import static org.eomasters.utils.Exceptions.throwIf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AssetStore {

  private final Gson gson;
  private final TypeToken<List<Asset>> assetTypeToken;

  public AssetStore() {
    this(AssetTypeRegistry.instance());
  }

  // for testing
  protected AssetStore(AssetTypeRegistry typeRegistry) {
    assetTypeToken = new TypeToken<>() {
    };
    final GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
    builder.registerTypeHierarchyAdapter(Asset.class, new GsonAssetAdapter(typeRegistry));
    gson = builder.create();
  }

  public List<Asset> load(Path location) throws IOException {
    List<Asset> assets;
    try (BufferedReader reader = Files.newBufferedReader(location)) {
      assets = gson.fromJson(reader, assetTypeToken);
      if (assets == null) {
        throw new IOException(
            String.format("Failed to load assets from location: %s%n The source might be empty.", location));
      }
      return assets;
    }
  }

  public void save(List<Asset> assets, Path location) throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(location)) {
      gson.toJson(assets, writer);
    }
  }

  public String toJson(List<Asset> assets) {
    throwIf(assets == null, new NullPointerException("assets == null"));
    return gson.toJson(assets);
  }


  public List<Asset> fromJson(String text) {
    throwIf(text == null, new NullPointerException("text == null"));
    throwIf (text.isBlank(), new IllegalStateException("text is empty"));
    return gson.fromJson(text, assetTypeToken);
  }

}
