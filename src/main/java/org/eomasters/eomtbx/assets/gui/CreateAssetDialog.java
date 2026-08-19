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
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import org.eomasters.eomtbx.assets.AssetCreationService;
import org.eomasters.eomtbx.assets.AssetLibrary;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.gui.Dialogs;
import org.eomasters.icons.Icon;
import org.esa.snap.ui.ModalDialog;

class CreateAssetDialog extends ModalDialog {

  private final Component alignmentComponent;
  private final AssetLibrary library;
  private final AssetCreationPanel defPanel;


  public CreateAssetDialog(Component alignmentComponent, AssetLibrary library, AssetType type,
      AssetCreationService factory) {
    super(null, "Create an Asset from " + factory.getName(), ModalDialog.ID_OK_CANCEL_HELP, factory.getHelpId());
    this.alignmentComponent = alignmentComponent;
    this.library = library;
    getJDialog().setIconImage(type.getIcon().getImageIcon(Icon.SIZE_16).getImage());
    getJDialog().setModalityType(ModalityType.DOCUMENT_MODAL);
    defPanel = new AssetCreationPanel(factory);
    initUi();
  }

  private void initUi() {
    JScrollPane scrollPane = new JScrollPane(defPanel);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    setContent(scrollPane);
  }

  @Override
  public void center() {
    getJDialog().setLocationRelativeTo(alignmentComponent);
  }

  @Override
  protected boolean verifyUserInput() {
    if (library.contains(defPanel.getAssetName())) {
      String msgFormat = "The library contains already an asset with the name '%s'.";
      Dialogs.error("Add asset to the library", String.format(msgFormat, defPanel.getAssetName()));
      return false;
    }
    return defPanel.verifyUserInput();
  }

  public String getAssetName() {
    return defPanel.getAssetName();
  }

  public String getAssetDescription() {
    return defPanel.getAssetDescription();
  }

  public String[] getAssetTags() {
    return defPanel.getAssetTags();
  }

  public PropertySet getAssetProperties() {
    return defPanel.getAssetProperties();
  }

}
