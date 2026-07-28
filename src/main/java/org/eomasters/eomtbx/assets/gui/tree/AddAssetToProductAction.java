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

package org.eomasters.eomtbx.assets.gui.tree;

import com.bc.ceres.binding.PropertySet;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.gui.AddAssetGuiService;
import org.eomasters.eomtbx.assets.gui.AssetAddingGuiRegistry;
import org.esa.snap.core.datamodel.Product;

class AddAssetToProductAction extends AbstractAction {

  private final Asset asset;
  private final Product product;
  private final Component alignmentComp;

  public AddAssetToProductAction(Component alignmentComponent, String title, Asset asset, Product product) {
    super(title);
    this.alignmentComp = alignmentComponent;
    this.asset = asset;
    this.product = product;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    execute();
  }

  public void execute() {
    AddAssetGuiService guiService = AssetAddingGuiRegistry.instance().getUiFor(asset.getType());
    PropertySet addConfiguration = asset.getType().createAddConfiguration(asset, product);
    AddAssetDialog addAssetDialog = new AddAssetDialog(alignmentComp, asset, addConfiguration, product, guiService);
    addAssetDialog.show();
  }

}
