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

package org.eomasters.eomtbx.cmap.gui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.preferences.PropertyChangeOptionsPanelController;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Controller for the Coastal Map options.
 */
@SuppressWarnings("unused")
@OptionsPanelController.SubRegistration(
    id = "cmap",
    location = "eomtbx",
    keywordsCategory = "EOMTBX",
    keywords = "EOMTBX, EOMASTERS, Toolbox, coast, coastal, map",
    position = 2,
    displayName = "Coastal Map")
public class CoastalMapOptionsController extends PropertyChangeOptionsPanelController {

  public static final String HID_EOMTBX_CMAP_OPTIONS = "eomtbx.options.cmap.options";
  private JPanel mainPanel;
  private MaskOptionsModel flagMasks;
  private CoastalMapOptionsPanel generalOptionsPanel;

  @Override
  public void update() {
  }

  @Override
  public void applyChanges() {
    if (flagMasks != null) {
      flagMasks.store();
    }
    if (generalOptionsPanel != null) {
      generalOptionsPanel.store();
    }
  }

  @Override
  public void cancel() {
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public boolean isChanged() {
    return flagMasks.isChanged() || generalOptionsPanel.isChanged();
  }

  @Override
  public JComponent getComponent(Lookup masterLookup) {
    if (mainPanel == null) {
      mainPanel = new JPanel(new MigLayout("top, left, gap 5, ins 5, fillx", "[fill]"));

      generalOptionsPanel = new CoastalMapOptionsPanel();
      generalOptionsPanel.setBorder(BorderFactory.createTitledBorder("General Options"));

      MaskAppearancePanel appearancePanel = new MaskAppearancePanel();
      appearancePanel.setBorder(BorderFactory.createTitledBorder("Customise Mask Appearance"));
      flagMasks = appearancePanel.getFlagMasks();

      mainPanel.add(generalOptionsPanel, "wrap");
      mainPanel.add(appearancePanel);
    }
    return mainPanel;
  }


  @Override
  public HelpCtx getHelpCtx() {
    return new HelpCtx(HID_EOMTBX_CMAP_OPTIONS);
  }

}
