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

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.eomasters.eomtbx.assets.AssetLibrary;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@TopComponent.Description(
    preferredID = "AssetLibraryTopComponent",
    iconBase = "org/eomasters/eomtbx/icons/AssetLibrary_16.png"
)
@TopComponent.Registration(
    mode = "rightSlidingSide",
    openAtStartup = true,
    position = 1
)
@ActionID(category = "Window", id = "org.eomasters.snap.assets.gui.AssetLibraryTopComponent")
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_AssetLibraryTopComponent_Name",
    preferredID = "AssetLibraryTopComponent"
)
@ActionReferences({
    @ActionReference(path = "Menu/View/Tool Windows", position = 1320),
    @ActionReference(path = "Toolbars/Tool Windows", position = 11)
})
@NbBundle.Messages({
    "CTL_AssetLibraryTopComponent_Name=Asset Library",
    "CTL_AssetLibraryTopComponent_ComponentName=Asset_Library"
})
public class AssetLibraryTopComponent extends TopComponent {

  private AssetLibraryForm assetLibraryForm;

  public AssetLibraryTopComponent() {
    setName(Bundle.CTL_AssetLibraryTopComponent_ComponentName());
    initUI();
  }

  private void initUI() {
    setDisplayName(Bundle.CTL_AssetLibraryTopComponent_Name());

    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(4, 4, 4, 4));
    add(BorderLayout.CENTER, new JPanel(new BorderLayout()));
    assetLibraryForm = new AssetLibraryForm(AssetLibrary.getInstance());
    add(assetLibraryForm, BorderLayout.CENTER);
  }

  @Override
  protected void componentDeactivated() {
    new Thread(() -> AssetLibrary.getInstance().saveToPreferences()).start();
    super.componentDeactivated();
  }

  @Override
  protected void componentShowing() {
    assetLibraryForm.updateTreeAndModel();
    super.componentShowing();
  }

  @Override
  public HelpCtx getHelpCtx() {
    return new HelpCtx(AssetLibrary.HELP_ID);
  }
}
