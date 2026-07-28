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

package org.eomasters.eomtbx.pyeditor.gui;

import com.bc.ceres.swing.progress.DialogProgressMonitor;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.file.PathUtils;
import org.eomasters.eomtbx.preferences.PropertyChangeOptionsPanelController;
import org.eomasters.eomtbx.EomtbxRuntime;
import org.eomasters.eomtbx.pyeditor.Properties;
import org.eomasters.eomtbx.pyeditor.graalpy.GraalPy;
import org.eomasters.eomtbx.pyeditor.pyrun.EditorTheme;
import org.eomasters.gui.FileChooserComponent;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Controller for the PyCode options panel.
 */
@SuppressWarnings("unused")
@OptionsPanelController.SubRegistration(
    id = "pyeditor",
    location = "eomtbx",
    keywordsCategory = "EOMTBX",
    keywords = "EOMTBX, EOMASTERS, Toolbox, PyCode, Python",
    position = 3,
    displayName = "PyEditor")
public class PyEditorPreferencePanelController extends PropertyChangeOptionsPanelController {

  public static final String HID_EOMTBX_PYCODE_OPTIONS = "eomtbx.options.pyeditor";

  // Default values
  private static final String DEFAULT_THEME = "Default";
  private static final String DEFAULT_PYTHON_EXECUTABLE = "python";
  private static final String PREFERRED_GRAALPY_VERSION = "24.2.2";

  // GUI components
  private JPanel mainPanel;
  private JComboBox<String> themeComboBox;
  private FileChooserComponent graalPyHomeChooser;
  private FileChooserComponent userProjectsDirChooser;

  @Override
  public void update() {
    // Update GUI components if they exist
    if (themeComboBox != null) {
      EditorTheme currentTheme = Properties.getTheme();
      themeComboBox.setSelectedItem(currentTheme);
    }
    if (graalPyHomeChooser != null) {
      graalPyHomeChooser.setCurrentPath(Properties.getPythonHome());
      try {
        Path graalpyPath = getGraalpyPath();
        graalPyHomeChooser.setDefaultChooserDir(graalpyPath);
      } catch (IOException ignored) {
      }
      graalPyHomeChooser.setEnabled(true);
    }

    if (userProjectsDirChooser != null) {
      Path userProjectsDirectory = Properties.getUserProjectsDirectory();
      userProjectsDirChooser.setCurrentPath(userProjectsDirectory);
    }
  }

  @Override
  public void applyChanges() {
    // Save current values to preferences
    Properties.setTheme((EditorTheme) themeComboBox.getSelectedItem());
    Properties.setPythonHome(graalPyHomeChooser.getCurrentPath());
    Properties.setUserProjectsDirectory(userProjectsDirChooser.getCurrentPath());
  }

  @Override
  public void cancel() {
    // Reset current values to stored preferences
    update();
  }

  @Override
  public boolean isValid() {
    if (!GraalPy.isGraalPyHome(graalPyHomeChooser.getCurrentPath())) {
      return false;
    }
    if (!Files.isDirectory(userProjectsDirChooser.getCurrentPath())) {
      return false;
    }
    return true;
  }

  @Override
  public boolean isChanged() {
    EditorTheme storedTheme = Properties.getTheme();
    Path storedPythonHome = Properties.getPythonHome();
    Path storedUserProjectsDirectory = Properties.getUserProjectsDirectory();

    return !storedTheme.equals(themeComboBox.getSelectedItem()) ||
        !Objects.equals(storedPythonHome, graalPyHomeChooser.getCurrentPath()) ||
        !Objects.equals(storedUserProjectsDirectory, userProjectsDirChooser.getCurrentPath());
  }

  @Override
  public JComponent getComponent(Lookup masterLookup) {
    if (mainPanel == null) {
      createMainPanel();
      update(); // Load initial values
    }
    return mainPanel;
  }

  private void createMainPanel() {
    var layout = new MigLayout("top, left, gap 5, ins 5, fillx", "[grow 0][fill][grow 0]");
    mainPanel = new JPanel(layout);

    // Default Theme selection
    mainPanel.add(new JLabel("Default Theme:"));
    EnumComboBoxModel<EditorTheme> themeModel = new EnumComboBoxModel<>(EditorTheme.class);
    themeComboBox = new JComboBox<>(themeModel);
    mainPanel.add(themeComboBox, "span 2, wrap");

    // Default Python executable path selection
    mainPanel.add(new JLabel("Default Graal Python Home:"));
    graalPyHomeChooser = new FileChooserComponent(JFileChooser.DIRECTORIES_ONLY, "Graal Python Home", 16);
    graalPyHomeChooser.addFileChangedListener((oldPath, newPath) -> {
      Properties.setPythonHome(newPath);
    });
    mainPanel.add(graalPyHomeChooser, "wrap");

    JButton installButton = new JButton("Install GraalPython");
    installButton.addActionListener(new InstallActionListener());
    mainPanel.add(installButton, "skip 1, wrap");

    // Default User Project Directory
    mainPanel.add(new JLabel("Default Projects Directory:"));
    userProjectsDirChooser = new FileChooserComponent(JFileChooser.DIRECTORIES_ONLY, "Projects Directory", 16);
    mainPanel.add(userProjectsDirChooser, "wrap");
  }


  @Override
  public HelpCtx getHelpCtx() {
    return new HelpCtx(HID_EOMTBX_PYCODE_OPTIONS);
  }

  private class InstallActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        var releaseVersions = GraalPy.getReleaseVersions();
        JComboBox<String> releaseTagCombo = new JComboBox<>(releaseVersions.toArray(new String[0]));
        releaseTagCombo.setSelectedItem(PREFERRED_GRAALPY_VERSION);
        int result = JOptionPane.showConfirmDialog(mainPanel,
                                                   releaseTagCombo,
                                                   "Select GraalPython Version",
                                                   JOptionPane.OK_CANCEL_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
          return;
        }
        var selectedVersion = (String) releaseTagCombo.getSelectedItem();
        var graalpyPath = getGraalpyPath();
        var installPath = graalpyPath.resolve("graalpy-" + selectedVersion);
        if (Files.exists(installPath)) {
          var userResponse = JOptionPane.showConfirmDialog(mainPanel,
                                                           "<html>GraalPython " + selectedVersion
                                                               + "is already installed.<br>"
                                                               + "Reinstall?", "Warning", JOptionPane.YES_NO_OPTION);
          if (JOptionPane.NO_OPTION == userResponse) {
            return;
          }
          PathUtils.deleteDirectory(installPath);
          Files.createDirectories(installPath);
        }
        // Run installation in background thread
        new Thread(() -> {
          try {
            var pm = new DialogProgressMonitor(mainPanel, "Installing GraalPython v" + selectedVersion,
                                               ModalityType.DOCUMENT_MODAL);
            GraalPy.install(selectedVersion, installPath, pm);

            SwingUtilities.invokeLater(() -> {
              graalPyHomeChooser.setCurrentPath(installPath);
            });
          } catch (IOException ex) {
            // Handle errors on EDT
            SwingUtilities.invokeLater(() -> {
              ex.printStackTrace();
              JOptionPane.showMessageDialog(mainPanel, "Can not install GraalPython: " + ex.getMessage(), "Error",
                                            JOptionPane.ERROR_MESSAGE);
            });
          }
        }, "GraalPython-Installation").start();

      } catch (IOException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(mainPanel, "Can not install GraalPython: " + ex.getMessage(), "Error",
                                      JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private static Path getGraalpyPath() throws IOException {
    var pyEditorAuxdata = EomtbxRuntime.getModuleAuxdataDir("PyEditor");
    var graalpyPath = pyEditorAuxdata.resolve("graalpy");
    Files.createDirectories(graalpyPath);
    return graalpyPath;
  }
}
