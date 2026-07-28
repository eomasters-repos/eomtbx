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

package org.eomasters.eomtbx.pyeditor.gui.action;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.eomasters.eomtbx.pyeditor.Properties;
import org.eomasters.eomtbx.pyeditor.graalpy.GraalPy;
import org.eomasters.eomtbx.pyeditor.pyrun.EditorTheme;
import org.eomasters.eomtbx.pyeditor.pyrun.Project;
import org.eomasters.eomtbx.pyeditor.pyrun.ProjectBuilder;
import org.eomasters.gui.FileChooserComponent;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;
import org.netbeans.api.progress.BaseProgressUtils;

/**
 * Dialog for creating a new project with a two-step process. First step: Project name and type Second step: Directory
 * settings with defaults derived from the name
 */
public class ProjectNewDialog extends JDialog {

  private static final int DIALOG_WIDTH = 500;
  private static final int DIALOG_HEIGHT = 300;
  private final ProjectBuilder projectBuilder;


  // Step 1 components
  private JPanel nameAndThemePanel;
  private JTextField nameTextField;
  private EnumComboBoxModel<EditorTheme> themeModel;
  private JComboBox<EditorTheme> themeComboBox;
  private JButton nextButton;
  private JButton cancelButton1;

  // Step 2 components
  private JPanel dirsPanel;
  private FileChooserComponent srcDirectoryChooser;
  private FileChooserComponent venvDirectoryChooser;
  private FileChooserComponent workingDirectoryChooser;
  private JCheckBox initVenvCheckBox;
  private FileChooserComponent pythonHome;
  private JButton backButton;
  private JButton finishButton;
  private JButton cancelButton2;

  private boolean approved = false;

  /**
   * Creates a new NewProjectDialog.
   *
   * @param parent the parent frame
   */
  public ProjectNewDialog(JFrame parent) {
    super(parent, "Create New Project", true);
    projectBuilder = new ProjectBuilder();

    initComponents();
    initLayout();
    showNameAndThemePanel();

    setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
    setLocationRelativeTo(parent);
    setResizable(false);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
  }

  private void initComponents() {
    // Step 1 components
    nameTextField = new JTextField(20);
    themeModel = new EnumComboBoxModel<>(EditorTheme.class);
    themeModel.setSelectedItem(Properties.getTheme());
    //noinspection rawtypes,unchecked
    themeComboBox = new JComboBox<>(themeModel);
    nextButton = new JButton("Next");
    cancelButton1 = new JButton("Cancel");

    // Step 2 components
    srcDirectoryChooser = new FileChooserComponent(JFileChooser.DIRECTORIES_ONLY,
                                                   "Select Source Directory", 16);
    venvDirectoryChooser = new FileChooserComponent(JFileChooser.DIRECTORIES_ONLY,
                                                    "Select Virtual Environment Directory", 16);
    workingDirectoryChooser = new FileChooserComponent(JFileChooser.DIRECTORIES_ONLY,
                                                       "Select Working Directory", 16);
    initVenvCheckBox = new JCheckBox("Initialise Virtual Environment", true);
    pythonHome = new FileChooserComponent(JFileChooser.DIRECTORIES_ONLY,
                                          "Select Graal Python Home", 16);
    pythonHome.setCurrentPath(Properties.getPythonHome());

    backButton = new JButton("Back");
    finishButton = new JButton("Finish");
    cancelButton2 = new JButton("Cancel");

    // Initialize listeners
    initListeners();
  }

  private void initLayout() {
    // Step 1 panel
    nameAndThemePanel = new JPanel(new GridBagLayout());
    nameAndThemePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Project name
    nameAndThemePanel.add(new JLabel("Project Name:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    nameAndThemePanel.add(nameTextField, gbc);

    // Editor Theme
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    nameAndThemePanel.add(new JLabel("Editor Theme:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    nameAndThemePanel.add(themeComboBox, gbc);

    // Buttons for step 1
    JPanel buttonPanel1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel1.add(nextButton);
    buttonPanel1.add(cancelButton1);

    // Step 2 panel
    dirsPanel = new JPanel(new GridBagLayout());
    dirsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Source directory
    dirsPanel.add(new JLabel("Source Directory:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    dirsPanel.add(srcDirectoryChooser, gbc);

    // Virtual Environment directory
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    dirsPanel.add(new JLabel("Virtual Environment:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    dirsPanel.add(venvDirectoryChooser, gbc);

    // Initialize venv checkbox
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 2;
    dirsPanel.add(initVenvCheckBox, gbc);

    // Working directory
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    dirsPanel.add(new JLabel("Working Directory:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    dirsPanel.add(workingDirectoryChooser, gbc);

    // Python executable
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    dirsPanel.add(new JLabel("Graal Python Home:"), gbc);

    gbc.gridx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    dirsPanel.add(pythonHome, gbc);

    // Buttons for step 2
    JPanel buttonPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel2.add(backButton);
    buttonPanel2.add(finishButton);
    buttonPanel2.add(cancelButton2);

    // Main layout
    setLayout(new BorderLayout());
    add(nameAndThemePanel, BorderLayout.CENTER);
    add(buttonPanel1, BorderLayout.SOUTH);
  }

  private void initListeners() {
    // Step 1 listeners
    nextButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (validateNameAndTheme()) {
          updateProjectWithNameAndTheme();
          showDirsPanel();
        }
      }
    });

    cancelButton1.addActionListener(e -> {
      approved = false;
      dispose();
    });

    // Step 2 listeners
    backButton.addActionListener(e -> showNameAndThemePanel());

    finishButton.addActionListener(e -> {
      if (validateDirs()) {
        updateProjectWithDirs();
        approved = true;
        dispose();
      }
    });

    cancelButton2.addActionListener(e -> {
      approved = false;
      dispose();
    });

    // Enable/disable Python executable chooser based on checkbox
    initVenvCheckBox.addActionListener(e -> {
      pythonHome.setEnabled(initVenvCheckBox.isSelected());
    });
  }

  private boolean validateNameAndTheme() {
    String name = nameTextField.getText().trim();
    if (name.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Project name cannot be empty", "Validation Error",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // Check if name contains invalid characters for directory name
    if (name.matches(".*[\\\\/:*?\"<>|].*")) {
      JOptionPane.showMessageDialog(this, "Project name contains invalid characters", "Validation Error",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // Check if project directory already exists
    Path projectDir = Properties.getUserProjectsDirectory().resolve(name);
    if (Files.exists(projectDir)) {
      JOptionPane.showMessageDialog(this, "A project with this name already exists", "Validation Error",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
    }

    return true;
  }

  private boolean validateDirs() {
    // Validate source directory
    Path srcDir = srcDirectoryChooser.getCurrentPath();
    if (srcDir == null) {
      JOptionPane.showMessageDialog(this, "Please select a source directory", "Validation Error",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // Validate venv directory
    Path venvDir = venvDirectoryChooser.getCurrentPath();
    if (venvDir == null) {
      JOptionPane.showMessageDialog(this, "Please select a virtual environment directory", "Validation Error",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // Validate working directory
    Path workingDir = workingDirectoryChooser.getCurrentPath();
    if (workingDir == null) {
      JOptionPane.showMessageDialog(this, "Please select a working directory", "Validation Error",
                                    JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // Validate Python executable if venv initialization is selected
    if (initVenvCheckBox.isSelected()) {
      Path pythonHomePath = pythonHome.getCurrentPath();
      if (!GraalPy.isGraalPyHome(pythonHomePath)) {
        JOptionPane.showMessageDialog(this, "Please select a valid Graal Python home. You can install Graal Python in the options.", "Validation Error",
                                      JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }

    return true;
  }

  private void updateProjectWithNameAndTheme() {
    String name = nameTextField.getText().trim();
    projectBuilder.name(name);
    projectBuilder.theme(themeModel.getSelectedItem());

    // Update step 2 file choosers with derived paths
    var projectDir = Properties.getUserProjectsDirectory().resolve(name);
    var srcDir = projectDir.resolve("src");
    srcDirectoryChooser.setCurrentPath(srcDir);
    venvDirectoryChooser.setCurrentPath(projectDir.resolve("venv"));
    workingDirectoryChooser.setCurrentPath(srcDir);
  }

  private void updateProjectWithDirs() {
    projectBuilder.srcDirectory(srcDirectoryChooser.getCurrentPath());
    projectBuilder.workingDirectory(workingDirectoryChooser.getCurrentPath());

    // Set venv directory and Python home if venv is enabled
    if (initVenvCheckBox.isSelected()) {
      projectBuilder.venvDirectory(venvDirectoryChooser.getCurrentPath());
    }
  }

  private void showNameAndThemePanel() {
    getContentPane().removeAll();
    getContentPane().add(nameAndThemePanel, BorderLayout.CENTER);
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(nextButton);
    buttonPanel.add(cancelButton1);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    revalidate();
    repaint();
  }

  private void showDirsPanel() {
    getContentPane().removeAll();
    getContentPane().add(dirsPanel, BorderLayout.CENTER);
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(backButton);
    buttonPanel.add(finishButton);
    buttonPanel.add(cancelButton2);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    revalidate();
    repaint();
  }

  /**
   * Shows the dialog and returns the project if approved, or null if canceled. Also creates the project directory
   * structure and initializes the virtual environment if requested.
   *
   * @return the project if approved, or null if canceled
   */
  public Project showDialog() {
    setVisible(true); // show the dialog

    if (!approved) {
      return null;
    }

    Project result = BaseProgressUtils.showProgressDialogAndRun(handle -> {
      try {
        handle.progress("Creating project ...");
        return projectBuilder.initVenv(initVenvCheckBox.isSelected())
                             .pythonHome(pythonHome.getCurrentPath())
                             .build();
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
                                      "Error creating project: " + e.getMessage(),
                                      "Error", JOptionPane.ERROR_MESSAGE);
        return null;
      } finally {
        handle.finish();
      }
    }, "Creating project structure...", true);

    // Handle the result
    if (result != null) {
      setVisible(false);
      return result;
    }
    return null;

  }

}
