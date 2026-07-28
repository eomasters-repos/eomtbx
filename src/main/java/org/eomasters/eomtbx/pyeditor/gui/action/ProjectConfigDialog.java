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

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import org.eomasters.eomtbx.pyeditor.gui.PythonConsole;
import org.eomasters.eomtbx.pyeditor.pyrun.EditorTheme;
import org.eomasters.eomtbx.pyeditor.pyrun.PackageManager;
import org.eomasters.eomtbx.pyeditor.pyrun.PackageManager.Package;
import org.eomasters.eomtbx.pyeditor.pyrun.Project;
import org.eomasters.eomtbx.pyeditor.pyrun.ProjectIO;
import org.eomasters.eomtbx.utils.CaptureStreamCallback;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * A dialog for configuring a project. This dialog displays project settings and allows the user to manage Python
 * packages in the project's virtual environment.
 */
class ProjectConfigDialog extends JDialog {

  private final Project project;
  private final PackageManager pip;

  // Project settings components
  private JTextField nameField;
  private JTextField srcDirectoryField;
  private JTextField venvDirectoryField;
  private JTextField workingDirectoryField;
  private JButton srcDirectoryButton;
  private JButton venvDirectoryButton;
  private JButton workingDirectoryButton;

  // Package management components
  private JTable packageTable;
  private PackageTableModel packageTableModel;
  private JButton installButton;
  private JButton uninstallButton;
  private JButton refreshButton;
  private JButton closeButton;
  private JComboBox<EditorTheme> themeComboBox;

  /**
   * Custom table model for displaying Package data in a JTable.
   */
  private static class PackageTableModel extends AbstractTableModel {

    private final List<Package> packages = new ArrayList<>();
    private final String[] columnNames = {"Package Name", "Version"};

    @Override
    public int getRowCount() {
      return packages.size();
    }

    @Override
    public int getColumnCount() {
      return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
      return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      Package pkg = packages.get(rowIndex);
      return switch (columnIndex) {
        case 0 -> pkg.name();
        case 1 -> pkg.version();
        default -> null;
      };
    }

    public void addPackage(Package newPackage) {
      this.packages.add(newPackage);
      fireTableDataChanged();
    }

    public void setPackages(List<Package> packages) {
      this.packages.clear();
      this.packages.addAll(packages);
      fireTableDataChanged();
    }

    public Package getPackageAt(int rowIndex) {
      if (rowIndex >= 0 && rowIndex < packages.size()) {
        return packages.get(rowIndex);
      }
      return null;
    }

    public List<Package> getPackages() {
      return packages;
    }

    public void clear() {
      packages.clear();
      fireTableDataChanged();
    }
  }

  /**
   * Creates a new ProjectConfigDialog with the specified parent frame and project.
   *
   * @param parent  the parent frame
   * @param project the project to configure
   */
  public ProjectConfigDialog(JFrame parent, Project project) {
    super(parent, "Project Configuration", true);
    this.project = project;
    this.pip = project.getPyEnvironment().getPackageManager();

    initComponents();
    initLayout();
    initListeners();

    setSize(600, 500);
    setLocationRelativeTo(parent);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
  }

  /**
   * Initializes the dialog components.
   */
  private void initComponents() {
    // Project settings components
    nameField = new JTextField(project.getName());
    nameField.setEditable(false);

    EnumComboBoxModel<EditorTheme> themeModel = new EnumComboBoxModel<>(EditorTheme.class);
    themeModel.setSelectedItem(project.getTheme());
    themeComboBox = new JComboBox<>(themeModel);
    themeComboBox.addActionListener(e -> project.setTheme(themeModel.getSelectedItem()));

    srcDirectoryField = new JTextField(project.getSrcDirectory().toString());
    srcDirectoryField.setEditable(false);

    venvDirectoryField = new JTextField(project.getVenvDirectory().toString());
    venvDirectoryField.setEditable(false);

    workingDirectoryField = new JTextField(project.getWorkingDirectory().toString());
    workingDirectoryField.setEditable(false);

    // Directory explorer buttons
    var fileExplorerIcon = FontIcon.of(MaterialDesignF.FOLDER_FILE, 24);
    srcDirectoryButton = new JButton(fileExplorerIcon);
    srcDirectoryButton.setToolTipText("Open source directory in file explorer");
    srcDirectoryButton.setSize(new Dimension(16, 16));

    venvDirectoryButton = new JButton(fileExplorerIcon);
    venvDirectoryButton.setToolTipText("Open virtual environment directory in file explorer");
    srcDirectoryButton.setSize(new Dimension(16, 16));

    workingDirectoryButton = new JButton(fileExplorerIcon);
    workingDirectoryButton.setToolTipText("Open working directory in file explorer");
    workingDirectoryButton.setSize(new Dimension(16, 16));

    // Package management components
    packageTableModel = new PackageTableModel();
    packageTable = new JTable(packageTableModel);
    packageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    installButton = new JButton("Install");
    uninstallButton = new JButton("Uninstall");
    refreshButton = new JButton("Refresh");
    closeButton = new JButton("Close");

    packageTableModel.setPackages(project.getPackages());
  }

  /**
   * Initializes the dialog layout.
   */
  private void initLayout() {
    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Project settings panel
    JPanel settingsPanel = new JPanel(new GridBagLayout());
    settingsPanel.setBorder(BorderFactory.createTitledBorder("Project Settings"));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Name
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.0;
    settingsPanel.add(new JLabel("Name:"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    gbc.weightx = 1.0;
    settingsPanel.add(nameField, gbc);

    // Editor Theme
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    settingsPanel.add(new JLabel("Editor Theme:"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    gbc.weightx = 1.0;
    settingsPanel.add(themeComboBox, gbc);

    // Source Directory
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.weightx = 0.0;
    settingsPanel.add(new JLabel("Source Directory:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    settingsPanel.add(srcDirectoryField, gbc);

    gbc.gridx = 2;
    gbc.weightx = 0.0;
    settingsPanel.add(srcDirectoryButton, gbc);

    // Virtual Environment Directory
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    settingsPanel.add(new JLabel("Virtual Environment:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    settingsPanel.add(venvDirectoryField, gbc);

    gbc.gridx = 2;
    gbc.weightx = 0.0;
    settingsPanel.add(venvDirectoryButton, gbc);

    // Working Directory
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0.0;
    settingsPanel.add(new JLabel("Working Directory:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    settingsPanel.add(workingDirectoryField, gbc);

    gbc.gridx = 2;
    gbc.weightx = 0.0;
    settingsPanel.add(workingDirectoryButton, gbc);

    // Package management panel
    JPanel packagesPanel = new JPanel(new BorderLayout());
    packagesPanel.setBorder(BorderFactory.createTitledBorder("Python Packages"));

    JScrollPane scrollPane = new JScrollPane(packageTable);
    packagesPanel.add(scrollPane, BorderLayout.CENTER);

    JPanel packageButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    packageButtonsPanel.add(installButton);
    packageButtonsPanel.add(uninstallButton);
    packageButtonsPanel.add(refreshButton);
    packagesPanel.add(packageButtonsPanel, BorderLayout.SOUTH);

    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(closeButton);

    // Add panels to content pane
    contentPane.add(settingsPanel, BorderLayout.NORTH);
    contentPane.add(packagesPanel, BorderLayout.CENTER);
    contentPane.add(buttonPanel, BorderLayout.SOUTH);

    setContentPane(contentPane);
  }

  /**
   * Initializes the dialog listeners.
   */
  private void initListeners() {
    installButton.addActionListener(e -> {
      installPackageWithProgressDialog();
      loadPackagesWithProgressDialog();
    });

    uninstallButton.addActionListener(e -> {
      uninstallPackageWithProgressDialog();
      loadPackagesWithProgressDialog();
    });

    refreshButton.addActionListener(e -> loadPackagesWithProgressDialog());

    closeButton.addActionListener(e -> {
      try {
        ProjectIO.saveProject(project);
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(ProjectConfigDialog.this,
                                      "Error saving project changes: " + ex.getMessage(),
                                      "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      dispose();
    });

    // Directory explorer button listeners
    srcDirectoryButton.addActionListener(e -> openDirectoryInExplorer(project.getSrcDirectory().toString()));
    venvDirectoryButton.addActionListener(e -> openDirectoryInExplorer(project.getVenvDirectory().toString()));
    workingDirectoryButton.addActionListener(e -> openDirectoryInExplorer(project.getWorkingDirectory().toString()));
  }

  /**
   * Opens a directory in the system file explorer.
   *
   * @param directoryPath the path to the directory to open
   */
  private void openDirectoryInExplorer(String directoryPath) {
    try {
      File directory = new File(directoryPath);
      if (directory.exists() && directory.isDirectory()) {
        if (Desktop.isDesktopSupported()) {
          Desktop.getDesktop().open(directory);
        } else {
          JOptionPane.showMessageDialog(this,
                                        "Desktop operations are not supported on this system.",
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
      } else {
        JOptionPane.showMessageDialog(this,
                                      "Directory does not exist: " + directoryPath,
                                      "Error", JOptionPane.ERROR_MESSAGE);
      }
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this,
                                    "Error opening directory: " + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Shows the dialog.
   */
  public void showDialog() {
    setVisible(true);
  }

  /**
   * Executes a task to update Python packages asynchronously. This method registers a progress task, associates it with
   * a runnable task, and runs it using a background worker with progress dialog.
   *
   */
  private void loadPackagesWithProgressDialog() {
    SwingWorker<Void, Void> worker = new ProgressMonitorSwingWorker<>(packageTable,
                                                                      "Updating Python Packages ...") {

      @Override
      protected Void doInBackground(ProgressMonitor pm) {
        pm.beginTask("LoadingPackages", ProgressMonitor.UNKNOWN);
        try {
          loadPackages();
        } finally {
          pm.done();
        }
        return null;
      }
    };

    worker.execute();
  }

  /**
   * Loads the list of installed Python packages synchronously (for refresh operations).
   */
  private void loadPackages() {
    packageTableModel.clear();

    try {
      var packages = pip.listInstalledPackages();
      project.setPackages(packages);
      SwingUtilities.invokeLater(() -> {packageTableModel.setPackages(packages);});
    } catch (IOException | InterruptedException e) {
      JOptionPane.showMessageDialog(this,
                                    "Error loading packages: " + e.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Installs a Python package with progress dialog and live console output.
   */
  private void installPackageWithProgressDialog() {
    String packageName = JOptionPane.showInputDialog(this,
                                                     "Enter package name to install:",
                                                     "Install Package",
                                                     JOptionPane.QUESTION_MESSAGE);

    if (packageName == null || packageName.trim().isEmpty()) {
      return;
    }

    // Create a custom dialog with PythonConsole
    JDialog progressDialog = new JDialog(this, "Installing Package: " + packageName, true);
    progressDialog.setSize(700, 400);
    progressDialog.setLocationRelativeTo(this);

    PythonConsole console = new PythonConsole();

    JPanel dialogPanel = new JPanel(new BorderLayout());
    dialogPanel.add(new JLabel("Installing [" + packageName + "] ..."), BorderLayout.NORTH);
    dialogPanel.add(console, BorderLayout.CENTER);

    JButton cancelButton = new JButton("Close");
    cancelButton.setEnabled(false); // Initially disabled during operation
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(cancelButton);
    dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

    progressDialog.setContentPane(dialogPanel);

    // Create callback for live output
    CaptureStreamCallback callback = (line, isError) -> SwingUtilities.invokeLater(() -> {
      console.appendText(line + "\n", isError);
    });

    // Execute installation in background thread
    SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
      @Override
      protected Boolean doInBackground() throws Exception {
        try {
          pip.installPackage(callback, packageName.trim());
          return true;
        } catch (IOException | InterruptedException e) {
          SwingUtilities.invokeLater(() -> {
            console.appendText("Error: " + e.getMessage() + "\n", true);
          });
          return false;
        }
      }

      @Override
      protected void done() {
        SwingUtilities.invokeLater(() -> {
          cancelButton.setEnabled(true);
          cancelButton.setText("Close");
          try {
            boolean success = get();
            if (success) {
              console.appendText("Package installation completed successfully.\n", false);
              // loadPackages(); // Refresh package list - results in empty list; not sure why
            } else {
              console.appendText("Package installation failed.\n", true);
            }
          } catch (Exception e) {
            console.appendText("Unexpected error: " + e.getMessage() + "\n", true);
          }
        });
      }
    };

    cancelButton.addActionListener(e -> {
      if (worker.isDone()) {
        progressDialog.dispose();
      } else {
        worker.cancel(true);
        progressDialog.dispose();
      }
    });

    worker.execute();
    progressDialog.setVisible(true);
  }

  /**
   * Uninstalls a Python package with progress dialog and live console output.
   */
  private void uninstallPackageWithProgressDialog() {
    int selectedRow = packageTable.getSelectedRow();
    if (selectedRow == -1) {
      JOptionPane.showMessageDialog(this,
                                    "Please select a package to uninstall.",
                                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    Package selectedPackage = packageTableModel.getPackageAt(selectedRow);

    String packageName = selectedPackage.name();

    int confirm = JOptionPane.showConfirmDialog(this,
                                                "Are you sure you want to uninstall '" + packageName + "'?",
                                                "Confirm Uninstall", JOptionPane.YES_NO_OPTION);

    if (confirm != JOptionPane.YES_OPTION) {
      return;
    }

    // Create a custom dialog with PythonConsole
    JDialog progressDialog = new JDialog(this, "Uninstalling Package: " + packageName, true);
    progressDialog.setSize(600, 400);
    progressDialog.setLocationRelativeTo(this);

    PythonConsole console = new PythonConsole();

    JPanel dialogPanel = new JPanel(new BorderLayout());
    dialogPanel.add(new JLabel("Uninstalling " + packageName + "..."), BorderLayout.NORTH);
    dialogPanel.add(console, BorderLayout.CENTER);

    JButton cancelButton = new JButton("Close");
    cancelButton.setEnabled(false); // Initially disabled during operation
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(cancelButton);
    dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

    progressDialog.setContentPane(dialogPanel);

    // Create callback for live output
    CaptureStreamCallback callback = (line, isError) -> {
      SwingUtilities.invokeLater(() -> {
        console.appendText(line + "\n", isError);
      });
    };

    // Execute uninstallation in background thread
    SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
      @Override
      protected Boolean doInBackground() throws Exception {
        try {
          pip.uninstallPackage(callback, packageName);
          return true;
        } catch (IOException | InterruptedException e) {
          SwingUtilities.invokeLater(() -> {
            console.appendText("Error: " + e.getMessage() + "\n", true);
          });
          return false;
        }
      }

      @Override
      protected void done() {
        SwingUtilities.invokeLater(() -> {
          cancelButton.setEnabled(true);
          cancelButton.setText("Close");
          try {
            boolean success = get();
            if (success) {
              console.appendText("Package uninstallation completed successfully.\n", false);
              // loadPackages(); // Refresh package list - results in empty list; not sure why
            } else {
              console.appendText("Package uninstallation failed.\n", true);
            }
          } catch (Exception e) {
            console.appendText("Unexpected error: " + e.getMessage() + "\n", true);
          }
        });
      }
    };

    cancelButton.addActionListener(e -> {
      if (worker.isDone()) {
        progressDialog.dispose();
      } else {
        worker.cancel(true);
        progressDialog.dispose();
      }
    });

    worker.execute();
    progressDialog.setVisible(true);
  }

}
