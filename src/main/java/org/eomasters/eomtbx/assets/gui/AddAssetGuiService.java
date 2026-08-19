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

package org.eomasters.eomtbx.assets.gui;

import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.binding.PropertyPane;
import javax.swing.JPanel;
import org.eomasters.eomtbx.assets.AssetLibrary;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.utils.Service;
import org.esa.snap.core.datamodel.Product;

/**
 * The AddAssetGuiService interface defines the contract for classes responsible for creating GUI components and
 * performing actions related to a specific asset type.
 */
public abstract class AddAssetGuiService implements Service {

  /**
   * Retrieves the type of asset that this factory is responsible for.
   *
   * @return the asset type
   */
  public abstract Class<? extends AssetType> getAssetType();

  /**
   * Creates a JPanel for the given BindingContext.
   *
   * @param context the BindingContext used for creating the panel
   * @return the JPanel created
   */
  public JPanel createPanel(BindingContext context) {
    return new PropertyPane(context).createPanel();
  }

  /**
   * Finishes the process of adding an asset to a product. Can be implemented to do actions after the asset has been
   * added to the product.
   *
   * @param product          the {@link Product} to which the asset is being added
   * @param addConfiguration the {@link PropertySet} containing the configuration for adding the asset
   */
  public void finishAddingAsset(Product product, PropertySet addConfiguration) {

  }

  @Override
  public String getId() {
    return getClass().getSimpleName();
  }

  /**
   * The help id to the page where the user can find help about this factory.
   *
   * @return the default help id if not otherwise overridden.
   * @see AssetLibrary#HELP_ID
   */
  public String getHelpId() {
    return AssetLibrary.HELP_ID;
  }

}
