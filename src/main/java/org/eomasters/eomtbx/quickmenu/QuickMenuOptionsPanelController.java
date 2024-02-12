/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2024 Marco Peters
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

package org.eomasters.eomtbx.quickmenu;

import java.util.prefs.BackingStoreException;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.EomToolbox;
import org.eomasters.eomtbx.preferences.PropertyChangeOptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Controller for the QuickMenu options panel. Allows to change the preferences for the QuickMenu.
 */
public class QuickMenuOptionsPanelController extends PropertyChangeOptionsPanelController {

  public static final String HID_EOMTBX_QUICKMENU = "eomtbx.quickMenu";
  private JPanel mainPanel;
  private QuickMenuOptions currentOptions;
  private QuickMenuOptions backup;
  private SpinnerNumberModel numActionsModel;

  @Override
  public void update() {
    if (currentOptions == null) {
      currentOptions = new QuickMenuOptions();
    }
    backup = currentOptions.clone();
    numActionsModel.setValue(currentOptions.getNumActions());
    QuickMenu.getInstance().getPreferences().addPreferenceChangeListener(evt -> {
      if (evt.getKey().equals(QuickMenuOptions.PREFERENCE_KEY_NUM_QUICK_ACTIONS)) {
        numActionsModel.setValue(Integer.parseInt(evt.getNewValue()));
      }
    });
    fireChange();
  }

  @Override
  public void applyChanges() {
    storePreferences();
    backup = currentOptions.clone();
    fireChange();
  }

  @Override
  public void cancel() {
    currentOptions = backup.clone();
    fireChange();
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public boolean isChanged() {
    return !currentOptions.equals(backup);
  }

  @Override
  public JComponent getComponent(Lookup masterLookup) {
    if (mainPanel == null) {
      MigLayout mainLayout = new MigLayout("wrap 1, gap 5, insets 5");
      mainPanel = new JPanel(mainLayout);
      mainPanel.setBorder(new TitledBorder("Quick Menu"));

      JLabel label = new JLabel("Number of actions displayed (5-10):");
      numActionsModel = new SpinnerNumberModel(QuickMenuOptions.DEFAULT_NUM_QUICK_ACTIONS, 5, 10, 1);
      JSpinner spinner = new JSpinner(numActionsModel);
      spinner.addChangeListener(e -> {
        currentOptions.setNumActions((Integer) spinner.getValue());
        fireChange();
      });
      MigLayout qmLayout = new MigLayout();
      JPanel numActionsOptionPanel = new JPanel(qmLayout);
      numActionsOptionPanel.add(label, "gapright 10");
      numActionsOptionPanel.add(spinner);

      mainPanel.add(numActionsOptionPanel);
    }
    return mainPanel;
  }

  @Override
  public HelpCtx getHelpCtx() {
    return new HelpCtx(HID_EOMTBX_QUICKMENU);
  }

  private void storePreferences() {
    QuickMenuOptions.putToPreferences(currentOptions, QuickMenu.getInstance().getPreferences());
    try {
      QuickMenu.getInstance().getPreferences().flush();
    } catch (BackingStoreException e) {
      EomToolbox.handleUnexpectedException("Could not store QuickMenu options", e);
    }
  }
}
