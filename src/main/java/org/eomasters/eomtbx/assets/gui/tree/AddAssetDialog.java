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
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.SubProgressMonitor;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import java.awt.Component;
import javax.swing.SwingUtilities;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.gui.AddAssetGuiService;
import org.eomasters.gui.Dialogs;
import org.eomasters.icons.Icon;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.ui.ModalDialog;

class AddAssetDialog extends ModalDialog {


  private final Component parent;
  private final Asset asset;
  private final PropertySet addConfiguration;
  private final Product product;
  private final AddAssetGuiService guiService;

  public AddAssetDialog(Component parent, Asset asset, PropertySet addConfiguration, Product product,
      AddAssetGuiService guiService) {
    super(null, "Add to Product", ModalDialog.ID_OK_CANCEL_HELP, guiService.getHelpId());
    this.parent = parent;
    this.asset = asset;
    this.addConfiguration = addConfiguration;
    this.product = product;
    this.guiService = guiService;
    getJDialog().setIconImage(asset.getType().getIcon().getImageIcon(Icon.SIZE_16).getImage());

    setContent(guiService.createPanel(new BindingContext(addConfiguration)));
  }


  @Override
  public void center() {
    getJDialog().setLocationRelativeTo(parent);
  }

  @Override
  protected boolean verifyUserInput() {
    if (getButtonID() == ID_OK) {
      try {
        var swingWorker = createAddingSwingWorker(addConfiguration);
        swingWorker.execute();
        swingWorker.get();
      } catch (Exception ex) {
        String message = String.format("Cannot add asset to product: %n%s", ex.getCause().getMessage());
        Dialogs.error(this.getContent(), "Add Asset", message);
        return false;
      }
      SwingUtilities.invokeLater(() -> guiService.finishAddingAsset(product, addConfiguration));
    }
    return true;
  }

  private ProgressMonitorSwingWorker<Void, Void> createAddingSwingWorker(PropertySet addConfiguration) {
    return new ProgressMonitorSwingWorker<>(AddAssetDialog.this.getContent(), "Adding Asset") {
      @Override
      protected Void doInBackground(ProgressMonitor pm) throws Exception {
        pm.beginTask(String.format("Adding '%s' to product.", asset.getName()), 1);
        asset.getType().addToProduct(asset, product, addConfiguration, new SubProgressMonitor(pm, 1));
        return null;
      }
    };
  }
}
