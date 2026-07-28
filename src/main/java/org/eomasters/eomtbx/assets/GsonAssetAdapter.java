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

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.ValidationException;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter class implementing how {@link AssetType} is de-serialized to json code by Gson.
 */
public class GsonAssetAdapter implements JsonSerializer<Asset>, JsonDeserializer<Asset> {

  private static final String FIELD_NAME_TYPE = "type";
  private static final String FIELD_NAME_NAME = "name";
  private static final String FIELD_NAME_TAGS = "tags";
  private static final String FIELD_NAME_DESCR = "description";
  private static final String FIELD_NAME_USER_NOTES = "userNotes";
  private static final String FIELD_NAME_ATTRIBUTES = "attributes";
  private final AssetTypeRegistry typeRegistry;

  public GsonAssetAdapter(AssetTypeRegistry typeRegistry) {
    this.typeRegistry = typeRegistry;
  }

  @Override
  public Asset deserialize(final JsonElement json, final Type typeOfT,
      final JsonDeserializationContext context) throws JsonParseException {
    final JsonObject jsonObject = json.getAsJsonObject();
    final String name = jsonObject.get(FIELD_NAME_NAME).getAsString();
    final String typeId = jsonObject.get(FIELD_NAME_TYPE).getAsString();
    final String description = jsonObject.get(FIELD_NAME_DESCR).getAsString();
    JsonArray jsonTags = jsonObject.get(FIELD_NAME_TAGS).getAsJsonArray();
    List<String> tags = new ArrayList<>();
    for (JsonElement tagElement : jsonTags) {
      tags.add(tagElement.getAsString());
    }
    String[] tagsArray = tags.toArray(new String[0]);

    final Asset asset = new Asset(name, typeRegistry.getService(typeId), description, tagsArray);

    final JsonObject attributesElem = jsonObject.get(FIELD_NAME_ATTRIBUTES).getAsJsonObject();
    for (Map.Entry<String, JsonElement> entry : attributesElem.entrySet()) {
      try {
        asset.getAssetProperties().getProperty(entry.getKey()).setValueFromText(entry.getValue().getAsString());
      } catch (ValidationException e) {
        throw new JsonParseException(e);
      }
    }

    final String userNotes = jsonObject.get(FIELD_NAME_USER_NOTES).getAsString();
    asset.setUserNotes(userNotes);

    return asset;
  }

  @Override
  public JsonElement serialize(final Asset asset, final Type typeOfSrc,
      final JsonSerializationContext context) {
    final JsonObject retObject = new JsonObject();
    retObject.addProperty(FIELD_NAME_NAME, asset.getName());
    retObject.addProperty(FIELD_NAME_TYPE, asset.getType().getId());
    retObject.addProperty(FIELD_NAME_DESCR, asset.getDescription());
    String[] assetTags = asset.getTags();
    JsonArray jsonTags = new JsonArray(assetTags.length);
    for (String tag : assetTags) {
      jsonTags.add(tag);
    }
    retObject.add(FIELD_NAME_TAGS, jsonTags);
    retObject.addProperty(FIELD_NAME_USER_NOTES, asset.getUserNotes());

    final JsonObject attributesObject = new JsonObject();
    for (Property properties : asset.getAssetProperties().getProperties()) {
      attributesObject.addProperty(properties.getName(), properties.getValueAsText());
    }
    retObject.add(FIELD_NAME_ATTRIBUTES, attributesObject);

    return retObject;
  }

}
