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

package org.eomasters.eomtbx.preferences;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.EomToolbox;
import org.eomasters.eomtbx.quickmenu.QuickMenuOptionsPanelController;
import org.eomasters.utils.ErrorHandler;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@SuppressWarnings("unused")
@OptionsPanelController.TopLevelRegistration(
    id = "EOMTBX",
    categoryName = "EOMTBX",
    iconBase = "org/eomasters/eomtbx/preferences/eomtbx-logo_trans_32.png",
    keywordsCategory = "EOMTBX",
    keywords = "EOMTBX, EOMASTERS, Toolbox",
    position = 9000
)
public class EomtbxOptionsPanelController extends PropertyChangeOptionsPanelController {

  public static final String HID_EOMTBX_OPTIONS = "hid_eomtbx.options";
  private final List<OptionsPanelController> subControllers;
  private JComponent mainPanel;

  public EomtbxOptionsPanelController() {
    QuickMenuOptionsPanelController quickMenuController = new QuickMenuOptionsPanelController();
    PropertyChangeDelegate propertyChangeDelegate = new PropertyChangeDelegate();
    quickMenuController.addPropertyChangeListener(propertyChangeDelegate);
    subControllers = List.of(quickMenuController);
  }

  @Override
  public void update() {
    SwingUtilities.invokeLater(() -> subControllers.forEach(OptionsPanelController::update));
  }

  @Override
  public void applyChanges() {
    SwingUtilities.invokeLater(() -> subControllers.forEach(OptionsPanelController::applyChanges));
    try {
      Preferences preferences = EomToolbox.getPreferences();
      preferences.flush();
    } catch (BackingStoreException e) {
      ErrorHandler.handle("Could not store options for EOMasters Toolbox", e);
    }

  }

  @Override
  public void cancel() {
    subControllers.forEach(OptionsPanelController::cancel);
  }

  @Override
  public boolean isValid() {
    return subControllers.stream().allMatch(OptionsPanelController::isValid);
  }

  @Override
  public boolean isChanged() {
    return subControllers.stream().anyMatch(OptionsPanelController::isChanged);
  }

  @Override
  public JComponent getComponent(Lookup lookup) {
    if (mainPanel == null) {
      MigLayout layout = new MigLayout("fillx");
      JPanel prefPanel = new JPanel(layout);
      for (OptionsPanelController subController : subControllers) {
        prefPanel.add(subController.getComponent(Lookup.getDefault()), "wrap, growx, gapy 5");
      }
      prefPanel.add(new JPanel(), "pushy, grow, wrap");

      JScrollPane scrollPane = new JScrollPane(prefPanel);
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      mainPanel = scrollPane;
    }
    return mainPanel;
  }

  @Override
  public HelpCtx getHelpCtx() {
    return new HelpCtx(HID_EOMTBX_OPTIONS);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    super.addPropertyChangeListener(propertyChangeListener);
    for (OptionsPanelController subController : subControllers) {
      subController.addPropertyChangeListener(propertyChangeListener);
    }
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    super.removePropertyChangeListener(propertyChangeListener);
    for (OptionsPanelController subController : subControllers) {
      subController.removePropertyChangeListener(propertyChangeListener);
    }
  }

  private class PropertyChangeDelegate implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
  }
}
