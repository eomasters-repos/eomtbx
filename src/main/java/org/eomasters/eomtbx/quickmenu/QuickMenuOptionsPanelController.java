/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * --> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.quickmenu;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.EomToolbox;
import org.eomasters.eomtbx.preferences.PropertyChangeOptionsPanelController;
import org.eomasters.utils.ErrorHandler;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

public class QuickMenuOptionsPanelController extends PropertyChangeOptionsPanelController {

  public static final String HID_EOMTBX_QUICKMENU = "HID_EOMTBX_QUICKMENU";
  private JPanel mainPanel;
  private QuickMenuOptions currentOptions;
  private QuickMenuOptions backup;
  private SpinnerNumberModel numActionsModel;


  public QuickMenuOptionsPanelController() {
  }

  @Override
  public void update() {
    if (currentOptions == null) {
      currentOptions = QuickMenuOptions.load();
      backup = currentOptions.clone();
    } else {
      currentOptions = backup.clone();
    }
    numActionsModel.setValue(currentOptions.getNumActions());
  }

  @Override
  public void applyChanges() {
    QuickMenuOptions.putToPreferences(currentOptions);
    backup = currentOptions.clone();
    storePreferences();
    fireChange();
  }

  private static void storePreferences() {
    Preferences preferences = EomToolbox.getPreferences(); // todo - use only node?
    try {
      preferences.flush();
    } catch (BackingStoreException e) {
      ErrorHandler.handle("Could not store QuickMenu options", e);
    }
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
    if (currentOptions == null) {
      return false;
    }
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

}
