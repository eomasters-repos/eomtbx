/*-
 * ========================LICENSE_START=================================
 * EOMTBX PRO - EOMasters Toolbox PRO for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
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
 * -> http://www.gnu.org/licenses/gpl-3.0.html
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.preferences;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.EomToolbox;
import org.eomasters.gui.FileIo;
import org.eomasters.icons.Icon.SIZE;
import org.eomasters.icons.Icons;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

public class ImExportOptionsPanelController extends PropertyChangeOptionsPanelController {

    public static final String HID_EOMTBX_PREFERENCES = "eomtbxOptions";
    private static final FileFilter PREFERENCES_FILE_FILTER = FileIo.createFileFilter("Preferences file", "prefs");
    private final Preferences preferences = EomToolbox.getPreferences();
    private JComponent mainPanel;

    private static void exportPreferences(JPanel eomtbxPanel) {
      FileIo fileIo = new FileIo("Export EOMTBX preferences");
      fileIo.setParent(eomtbxPanel);
      fileIo.setFileName("eomtbx.prefs");
      fileIo.setFileFilters(PREFERENCES_FILE_FILTER);
      fileIo.save(EomToolbox::exportPreferences);
    }

    @Override
    public void update() {

    }

    @Override
    public void applyChanges() {
      try {
        preferences.flush();
      } catch (BackingStoreException e) {
        EomToolbox.handleUnexpectedException("Could not store options for EOMasters Toolbox", e);
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
      return false;
    }

    @Override
    public JComponent getComponent(Lookup lookup) {
      if (mainPanel == null) {
        mainPanel = createGeneralPanel();
      }
      return mainPanel;
    }

    private JPanel createGeneralPanel() {
      JPanel eomtbxPanel = new JPanel(new MigLayout("gap 5, insets 5"));
      eomtbxPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Import / Export"));
      eomtbxPanel.add(new JLabel("Import/Export options:"), "gapright 10");
      JButton importButton = new JButton("Import", Icons.IMPORT.getImageIcon(SIZE.S24));
      JButton exportButton = new JButton("Export", Icons.EXPORT.getImageIcon(SIZE.S24));
      eomtbxPanel.add(importButton);
      eomtbxPanel.add(exportButton, "wrap");

      importButton.addActionListener(e -> importPreferences(eomtbxPanel));
      exportButton.addActionListener(e -> exportPreferences(eomtbxPanel));

      return eomtbxPanel;
    }

    private void importPreferences(JPanel eomtbxPanel) {
      FileIo fileIo = new FileIo("Import EOMTBX preferences");
      fileIo.setParent(eomtbxPanel);
      fileIo.setFileName("eomtbx.prefs");
      fileIo.setFileFilters(PREFERENCES_FILE_FILTER);
      fileIo.load(inputStream -> {
        EomToolbox.importPreferences(inputStream);
        update();
      });
    }

    @Override
    public HelpCtx getHelpCtx() {
      return new HelpCtx(HID_EOMTBX_PREFERENCES);
    }

}
