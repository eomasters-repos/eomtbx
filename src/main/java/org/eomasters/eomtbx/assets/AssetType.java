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
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import java.util.Objects;
import org.eomasters.eomtbx.utils.Service;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.esa.snap.core.datamodel.Product;

/**
 * This class defines the type and is the factory for the properties of a {@link Asset}. Implementations must
 * provide a default no-args constructor.
 */
public abstract class AssetType implements Service {

  public static final String HELP_ID = AssetLibrary.HELP_ID + ".type";
  private final String name;
  private final String description;

  /**
   * Creates instances with the given name, description and properties. This constructor is used in
   * {@link AssetCreationService} and is not intended for usages beside this. The name is used to look up the type from
   * the {@link AssetTypeRegistry} thus it must be unique.
   *
   * @param name        The name of the type
   * @param description The description of the type
   */
  protected AssetType(final String name, final String description) {
    this.name = name;
    this.description = description;
  }

  protected abstract void initAssetProperties(PropertySet properties);

  protected abstract void initAddConfiguration(PropertySet addConfig, Asset asset, Product product);

  public abstract void addToProduct(Asset asset, Product product, PropertySet addConfig, ProgressMonitor pm)
      throws AssetException;

  /**
   * @return The name of the type.
   */
  public String getName() {
    return name;
  }

  @Override
  public String getId() {
    return getClass().getSimpleName();
  }

  /**
   * @return The description of the type
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns a generic {@link Icon} for the asset type this factory is responsible for, can be overridden to change
   * the icon.
   *
   * @return the icon
   */
  public Icon getIcon() {
    return Icons.DOCUMENT;
  }



  /**
   * Creates a new PropertyContainer with asset properties.
   *
   * @return a PropertyContainer with asset properties
   */
  public final PropertyContainer createAssetProperties() {
    PropertyContainer propertyContainer = new PropertyContainer();
    initAssetProperties(propertyContainer);
    PropertyHelper.initDefaults(propertyContainer);
    return propertyContainer;
  }

  /**
   * The help id to the page where the user can find help about this factory.
   *
   * @return the default help id if not otherwise overridden.
   *
   * @see AssetLibrary#HELP_ID
   */
  public String getHelpId() {
    return HELP_ID;
  }

  /**
   * Creates a new PropertyContainer with configuration for adding to a Product.
   *
   * @param product The Product to be added
   * @return A PropertyContainer with the add configuration
   */
  public final PropertySet createAddConfiguration(Asset asset, Product product) {
    PropertyContainer propertyContainer = new PropertyContainer();
    initAddConfiguration(propertyContainer, asset, product);
    PropertyHelper.initDefaults(propertyContainer);
    return propertyContainer;
  }

  @Override
  public String toString() {
    return String.format("AssetType{name='%s'}", name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AssetType type = (AssetType) o;
    return Objects.equals(getName(), type.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getName());
  }

}
