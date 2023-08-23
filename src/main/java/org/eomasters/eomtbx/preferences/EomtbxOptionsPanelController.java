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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.EomToolbox;
import org.eomasters.eomtbx.icons.Icons;
import org.eomasters.eomtbx.quickmenu.QuickMenuOptionsPanelController;
import org.eomasters.eomtbx.utils.ErrorHandler;
import org.esa.snap.rcp.util.Dialogs;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@SuppressWarnings("unused")
@OptionsPanelController.TopLevelRegistration(
    id = "EOMTBX",
    categoryName = "EOMTBX",
    iconBase = "org/eomasters/eomtbx/icons/Eomtbx_48.png",
    keywordsCategory = "EOMTBX",
    keywords = "EOMTBX, EOMASTERS, Toolbox",
    position = 9000
)
public class EomtbxOptionsPanelController extends PropertyChangeOptionsPanelController {

  public static final String HID_EOMTBX_OPTIONS = "hid_eomtbx.options";
  private static final String PREFERNCES_FILE_EXTENSION = "prefs";
  private static final FileFilter PREFERENCES_FILE_FILTER = new FileNameExtensionFilter("Preferences files",
      PREFERNCES_FILE_EXTENSION);
  private final List<OptionsPanelController> subControllers;
  private final Preferences preferences = EomToolbox.getPreferences();
  private JComponent mainPanel;

  public EomtbxOptionsPanelController() {
    SubControllerChangeDelegate propertyChangeDelegate = new SubControllerChangeDelegate();

    QuickMenuOptionsPanelController quickMenuController = new QuickMenuOptionsPanelController();
    quickMenuController.addPropertyChangeListener(propertyChangeDelegate);
    subControllers = List.of(quickMenuController);

    preferences.addPreferenceChangeListener(evt -> update());
  }

  @Override
  public void update() {
    SwingUtilities.invokeLater(() ->    subControllers.forEach(OptionsPanelController::update));
  }

  @Override
  public void applyChanges() {
    SwingUtilities.invokeLater(() -> subControllers.forEach(OptionsPanelController::applyChanges));
    try {
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
      MigLayout layout = new MigLayout("fillx, gapy 5");
      JPanel prefPanel = new JPanel(layout);
      JPanel generalPanel = createGeneralPanel();
      prefPanel.add(generalPanel, "growx, wrap");

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

  private JPanel createGeneralPanel() {
    JPanel eomtbxPanel = new JPanel(new MigLayout("gap 5, insets 5"));
    eomtbxPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("EOMTBX General Options"));
    eomtbxPanel.add(new JLabel("Import/Export options:"), "gapright 10");
    JButton importButton = new JButton("Import", Icons.IMPORT.s24);
    JButton exportButton = new JButton("Export", Icons.EXPORT.s24);
    eomtbxPanel.add(importButton);
    eomtbxPanel.add(exportButton, "wrap");

    importButton.addActionListener(e -> importPreferences(eomtbxPanel));
    exportButton.addActionListener(e -> exportPreferences(eomtbxPanel));

    return eomtbxPanel;
  }

  private static void exportPreferences(JPanel eomtbxPanel) {
    try {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setDialogTitle("Export EOMTBX preferences");
      fileChooser.setAcceptAllFileFilterUsed(false);
      fileChooser.setFileFilter(PREFERENCES_FILE_FILTER);
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

      int returnVal = fileChooser.showSaveDialog(eomtbxPanel);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        Boolean doWrite = true;
        Path path = ensurePreferencesExtension(fileChooser.getSelectedFile().toPath());
        if (Files.exists(path)) {
          doWrite = Dialogs.requestOverwriteDecision("Preferences file already exists",
              path.toFile());
        }
        if (doWrite) {
          EomToolbox.exportPreferences(Files.newOutputStream(path));
        }
      }
    } catch (IOException ex) {
      ErrorHandler.handle("Could not export preferences", ex);
    }
  }

  private void importPreferences(JPanel eomtbxPanel) {
    try {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setDialogTitle("Import EOMTBX preferences");
      fileChooser.setAcceptAllFileFilterUsed(false);
      fileChooser.setFileFilter(PREFERENCES_FILE_FILTER);
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

      int returnVal = fileChooser.showOpenDialog(eomtbxPanel);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        Path path = fileChooser.getSelectedFile().toPath();
        EomToolbox.importPreferences(Files.newInputStream(path));
        update();
      }
    } catch (IOException ex) {
      ErrorHandler.handle("Could not import preferences", ex);
    }
  }

  private static Path ensurePreferencesExtension(Path path) {
    if (!path.toString().endsWith(EomtbxOptionsPanelController.PREFERNCES_FILE_EXTENSION)) {
      path = path.resolveSibling(path.getFileName() + EomtbxOptionsPanelController.PREFERNCES_FILE_EXTENSION);
    }
    return path;
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

  private class SubControllerChangeDelegate implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
  }
}
