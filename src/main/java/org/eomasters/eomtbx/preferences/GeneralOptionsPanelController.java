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

package org.eomasters.eomtbx.preferences;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.EomToolbox;
import org.eomasters.eomtbx.quickmenu.QuickMenuOptionsPanelController;
import org.eomasters.gui.FileIo;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 * Controller for the EOMTBX options panel. Allows to change the preferences for the EOMTBX.
 */
@SuppressWarnings("unused")
@OptionsPanelController.SubRegistration(
    id = "general",
    displayName = "General",
    location = "eomtbx",
    keywordsCategory = "EOMTBX",
    keywords = "EOMTBX, EOMASTERS, Toolbox",
    position = 0
)
public class GeneralOptionsPanelController extends PropertyChangeOptionsPanelController {

  public static final String HID_EOMTBX_PREFERENCES = "eomtbxOptions";
  private static final FileFilter PREFERENCES_FILE_FILTER = FileIo.createFileFilter("Preferences file", "prefs");
  private final List<OptionsPanelController> subControllers;
  private final Preferences preferences = EomToolbox.getPreferences();
  private JComponent mainPanel;

  /**
   * Creates a new EomtbxOptionsPanelController.
   */
  public GeneralOptionsPanelController() {
    SubControllerChangeDelegate propertyChangeDelegate = new SubControllerChangeDelegate();

    ImExportOptionsPanelController imExportController = new ImExportOptionsPanelController();
    QuickMenuOptionsPanelController quickMenuController = new QuickMenuOptionsPanelController();
    imExportController.addPropertyChangeListener(propertyChangeDelegate);
    quickMenuController.addPropertyChangeListener(propertyChangeDelegate);


    subControllers = List.of(imExportController, quickMenuController);
    preferences.addPreferenceChangeListener(
        WeakListeners.create(PreferenceChangeListener.class, evt -> update(), preferences));
  }

  @Override
  public void update() {
    SwingUtilities.invokeLater(() -> subControllers.forEach(OptionsPanelController::update));
  }

  @Override
  public void applyChanges() {
    SwingUtilities.invokeLater(() -> subControllers.forEach(OptionsPanelController::applyChanges));
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
      MigLayout layout = new MigLayout("top, left, fillx, gapy 5");
      JPanel prefPanel = new JPanel(layout);

      for (OptionsPanelController subController : subControllers) {
        prefPanel.add(subController.getComponent(Lookup.getDefault()), "growx, wrap");
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
    return new HelpCtx(HID_EOMTBX_PREFERENCES);
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

  private class SubControllerChangeDelegate implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
  }
}


