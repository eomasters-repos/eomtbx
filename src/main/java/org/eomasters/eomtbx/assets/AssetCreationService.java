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
import org.eomasters.eomtbx.utils.Service;

/**
 * A {@link AssetCreationService} is a service for creating {@link Asset Assets}. It follows the well known Java standard as
 * define in {@link java.util.ServiceLoader}.
 * <p>
 * New implementations of this class are registered to the system by placing a provider-configuration file in the
 * directory META-INF/services with the name {@code org.eomasters.snap.asset.AssetFactory}. The file contains a list of
 * fully-qualified class names of implementation of this class.
 */
public abstract class AssetCreationService implements Service {

  /**
   * A human-readable name for this factory.
   *
   * @return the name
   */
  public abstract String getName();

  @Override
  public String getId() {
    return getClass().getSimpleName();
  }

  /**
   * @return the description
   */
  public abstract String getDescription();

  /**
   * The help id to the page where the user can find help about this factory.
   *
   * @return the default help id if not otherwise overridden.
   *
   * @see AssetLibrary#HELP_ID
   */
  public String getHelpId() {
    return AssetLibrary.HELP_ID;
  }

  /**
   * @return the {@link AssetType type} this factory is responsible for
   */
  public abstract Class<? extends AssetType> getAssetTypeClass();


  /**
   * Creates a new asset with the given name, description, tags and properties. The progress monitor should be used to
   * indicate progress if the creation might take some time (~ 1 second). The property container should be created
   * using the {@link #createFactoryProperties()} to create an initialised instance of a {@link PropertySet}.
   *
   * @param name        The name of the asset
   * @param description The description of the asset
   * @param tags        The tags associated with the asset
   * @param properties  The properties of the asset
   * @param pm          The progress monitor
   * @return The created asset
   * @throws AssetException If an error occurs while creating the asset
   */
  public abstract Asset createAsset(String name, String description, String[] tags, PropertySet properties,
      ProgressMonitor pm)
      throws AssetException;

  /**
   * Creates a new {@link PropertySet} object with default factory properties. These can be modified and further
   * passed to the {@link #createAsset(String, String, String[], PropertySet, ProgressMonitor) createAsset} method.
   *
   * @return The created set of properties
   */
  public final PropertySet createFactoryProperties() {
    PropertyContainer props = new PropertyContainer();
    initFactoryProperties(props);
    props.setDefaultValues();
    return props;
  }

  /**
   * Initializes the factory properties by adding the required properties to the provided {@link PropertySet}.
   * This method should be implemented by subclasses to define the specific factory properties.
   *
   * @param properties The {@link PropertySet} to which the factory properties should be added
   */
  protected abstract void initFactoryProperties(PropertySet properties);

  /**
   * Checks if the given type is supported by the factory.
   *
   * @param type The type to check
   * @return {@code true} if the factory supports the given type, otherwise {@code false}
   */
  boolean supports(Class<? extends AssetType> type) {
    return getAssetTypeClass().isAssignableFrom(type);
  }
}
