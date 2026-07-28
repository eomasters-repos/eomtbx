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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.eomasters.eomtbx.pyeditor.graalpy.GraalPy;
import org.esa.snap.rcp.windows.ToolTopComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@TopComponent.Description(
    preferredID = "PyEditorTopComponent",
    iconBase = "org/eomasters/eomtbx/icons/PyEditor_16.png"
)
@TopComponent.Registration(
    mode = "output", // is at the bottom
    openAtStartup = false,
    position = 0
)
@ActionID(category = "Window", id = "org.eomasters.eomtbx.pyeditor.gui.PyEditorTopComponent")
@ActionReferences({
    @ActionReference(path = "Menu/View/Tool Windows", position = 12),
    @ActionReference(path = "Menu/Tools", position = 10),
})
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_PyEditorTopComponentName",
    preferredID = "PyEditorTopComponent"
)
@NbBundle.Messages({
    "CTL_PyEditorTopComponentName=PyEditor (Beta)",
    "CTL_PyEditorTopComponentDescription=Edit Python Code",
})
public class PyEditorTopComponent extends ToolTopComponent {

  private static final String HELP_ID = "eomtbx.pyEditor";
  private PyEditor pyEditor;

  public PyEditorTopComponent() {
    setName(Bundle.CTL_PyEditorTopComponentName());
    initUI();
  }

  private void initUI() {
    setDisplayName(Bundle.CTL_PyEditorTopComponentName());

    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(4, 4, 4, 4));
    pyEditor = new PyEditor();
    add(pyEditor, BorderLayout.CENTER);
  }

  @Override
  protected void componentOpened() {
    super.componentOpened();
    if (!GraalPy.isInstalled()) {
      // Use SwingUtilities.invokeLater to delay showing dialog until UI is fully rendered
      SwingUtilities.invokeLater(this::showGraalPyMissingDialog);
    }
  }

  private void showGraalPyMissingDialog() {
    var jPanel = new JPanel(new GridBagLayout());
    var gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets.bottom = 10;
    jPanel.add(new JLabel("<html><b>Missing GraalPython Configuration</b><br><br>"
                              + "To start coding, please configure the Python environment first.<br>"
                              + "A Graal Python installation is required."), gbc);
    gbc.gridy = 1;
    var jButton = new JButton("Configure Python");

    JOptionPane optionPane = new JOptionPane(jPanel, JOptionPane.INFORMATION_MESSAGE);
    JDialog welcomeDialog = optionPane.createDialog(this, "Missing Configuration");

    jButton.addActionListener(e -> {
      welcomeDialog.dispose();
      OptionsDisplayer.getDefault().open("eomtbx/pyeditor");
    });
    jPanel.add(jButton, gbc);

    // Show the dialog
    welcomeDialog.setVisible(true);
  }


  @Override
  public boolean canClose() {
    return !pyEditor.hasUnsavedChanges() || pyEditor.getFileManager().promptForUnsavedFiles();
  }

  @Override
  public HelpCtx getHelpCtx() {
    return new HelpCtx(HELP_ID);
  }
}
