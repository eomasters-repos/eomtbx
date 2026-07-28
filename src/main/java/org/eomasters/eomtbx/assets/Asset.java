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

import com.bc.ceres.binding.PropertyContainer;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class is a generic representation of an asset in the {@link AssetLibrary}. Each asset has a specific
 * {@link AssetType type} which defines attributes necessary for creating the actual asset. Besides the type it consists
 * by user defined values like name, description, tags and other properties.
 */
public class Asset {

  private AssetType type;

  private String name;
  private String description;
  private String[] tags;
  private String userNotes;
  private transient PropertyContainer assetProperties;


  @SuppressWarnings("unused") // used by gson
  private Asset() {
  }

  /**
   * Creates a new asset with the given name and the provided type.
   *
   * @param name The name of the asset
   * @param type The type of the created asset
   */
  public Asset(final String name, AssetType type) {
    this(name, type, "", new String[0]);
  }

  /**
   * Creates a new asset with the given name and the provided type and description and tags.
   *
   * @param name        The name of the asset
   * @param type        The type of the created asset
   * @param description The description of the created asset
   * @param tags        The tags of the created asset
   */
  public Asset(final String name, AssetType type, String description, String[] tags) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name must not be null or empty");
    }
    if (type == null) {
      throw new IllegalArgumentException("type must not be null");
    }
    this.name = name;
    this.description = description == null ? "" : description;
    this.type = type;
    this.tags = tags == null ? new String[]{} : tags;
    assetProperties = type.createAssetProperties();
    userNotes = "";
  }


  /**
   * @return The type of this asset
   */
  public AssetType getType() {
    return type;
  }

  /**
   * @return The name of this asset
   */
  public String getName() {
    return name;
  }

  /**
   * @param name The new name for this asset
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * @return The descriptive text of this asset
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description The new description for this asset
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * @return The tags associated with this asset
   */
  public String[] getTags() {
    if (tags == null) {
      return new String[0];
    }
    return Arrays.copyOf(tags, tags.length);
  }

  /**
   * @param tags The new tags to associate with this asset
   */
  public void setTags(final String[] tags) {
    this.tags = tags;
  }

  /**
   * Gets the user notes.
   *
   * @return The notes of the user
   */
  public String getUserNotes() {
    return userNotes;
  }

  /**
   * Sets the user notes.
   *
   * @param notes The notes of the user
   */
  public void setUserNotes(String notes) {
    userNotes = notes;
  }

  /**
   * @return The properties of this asset
   */
  public PropertyContainer getAssetProperties() {
    return assetProperties;
  }

  @Override
  public String toString() {
    return String.format("Asset{name='%s', type=%s}", name, type.getName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Asset that = (Asset) o;
    return Objects.equals(getType(), that.getType()) && Objects.equals(getName(), that.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getType(), getName());
  }
}
