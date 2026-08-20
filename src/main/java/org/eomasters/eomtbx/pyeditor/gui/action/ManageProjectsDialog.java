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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import org.apache.commons.io.file.PathUtils;
import org.eomasters.eomtbx.pyeditor.pyrun.Project;
import org.eomasters.eomtbx.pyeditor.pyrun.ProjectIO;

/**
 * Dialog for managing projects. Lists all projects in the provided directory and allows loading or deleting them.
 */
class ManageProjectsDialog extends JDialog {

  private static final int DIALOG_WIDTH = 600;
  private static final int DIALOG_HEIGHT = 400;
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final Path projectsDirectory;
  private final List<ProjectItem> projectItems = new ArrayList<>();
  private JTable projectTable;
  private ProjectTableModel tableModel;
  private JButton openButton;
  private JButton deleteButton;
  private JButton newProjectButton;
  private JButton cancelButton;

  private Project selectedProject = null;

  /**
   * Creates a new ManageProjectDialog.
   *
   * @param parent            the parent frame
   * @param projectsDirectory the directory containing project files
   */
  public ManageProjectsDialog(Frame parent, Path projectsDirectory) {
    super(parent, "Manage Projects", true);
    this.projectsDirectory = projectsDirectory;

    initComponents();
    initLayout();
    loadProjects();
    initListeners();
    selectLastUsedProject();

    setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
    setLocationRelativeTo(parent);
    setResizable(true);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
  }

  private void initComponents() {
    tableModel = new ProjectTableModel();
    projectTable = new JTable(tableModel);
    projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    projectTable.setRowHeight(25);
    projectTable.getColumnModel().getColumn(ProjectTableModel.NAME_COLUMN_INDEX).setPreferredWidth(250);
    projectTable.getColumnModel()
                .getColumn(ProjectTableModel.NAME_COLUMN_INDEX)
                .setCellRenderer(new DefaultTableCellRenderer() {
                  @Override
                  public void setValue(Object value) {
                    setFont(getFont().deriveFont(Font.BOLD));
                    super.setValue(value);
                  }
                });
    projectTable.getColumnModel().getColumn(ProjectTableModel.USED_COLUMN_INDEX).setPreferredWidth(200);

    // Set up sorting
    TableRowSorter<ProjectTableModel> sorter = new TableRowSorter<>(tableModel);
    projectTable.setRowSorter(sorter);

    // Default sort by last used (descending)
    List<TableRowSorter.SortKey> sortKeys = new ArrayList<>();
    sortKeys.add(new TableRowSorter.SortKey(ProjectTableModel.USED_COLUMN_INDEX, javax.swing.SortOrder.DESCENDING));
    sorter.setSortKeys(sortKeys);
    sorter.sort();

    openButton = new JButton("Open");
    openButton.setEnabled(false);

    deleteButton = new JButton("Delete");
    deleteButton.setEnabled(false);

    newProjectButton = new JButton("New Project");

    cancelButton = new JButton("Cancel");
  }

  private void initLayout() {
    JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JLabel titleLabel = new JLabel("Available Projects:");
    contentPanel.add(titleLabel, BorderLayout.NORTH);

    JScrollPane scrollPane = new JScrollPane(projectTable);
    scrollPane.setPreferredSize(new Dimension(550, 300));
    contentPanel.add(scrollPane, BorderLayout.CENTER);

    JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    leftButtonPanel.add(newProjectButton);
    JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    rightButtonPanel.add(openButton);
    rightButtonPanel.add(deleteButton);
    rightButtonPanel.add(cancelButton);
    JPanel buttonPanel = new JPanel(new BorderLayout(10, 10));
    buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
    buttonPanel.add(rightButtonPanel, BorderLayout.EAST);
    contentPanel.add(buttonPanel, BorderLayout.SOUTH);

    setLayout(new BorderLayout());
    add(contentPanel, BorderLayout.CENTER);
  }

  private void loadProjects() {
    projectItems.clear();

    try {
      if (!Files.exists(projectsDirectory)) {
        Files.createDirectories(projectsDirectory);
      }

      try (DirectoryStream<Path> stream = Files.newDirectoryStream(projectsDirectory)) {
        for (Path dir : stream) {
          if (Files.isDirectory(dir)) {
            try (DirectoryStream<Path> fileStream = Files.newDirectoryStream(dir,
                                                                             "*" + ProjectIO.PROJECT_FILE_EXTENSION)) {
              for (Path file : fileStream) {
                try {
                  Project project = ProjectIO.loadProject(file);
                  ProjectItem item = new ProjectItem(project, file);
                  projectItems.add(item);
                } catch (IOException e) {
                  // Skip files that can't be loaded
                  System.err.println("Error loading project file: " + file + " - " + e.getMessage());
                }
              }
            }
          }
        }
      }

      // Sort projects by last used (newest first)
      projectItems.sort(Comparator.comparing(item -> item.project().getLastUsed(),
                                             Comparator.nullsLast(Comparator.reverseOrder())));

      tableModel.fireTableDataChanged();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this,
                                    "Error loading projects: " + e.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void selectLastUsedProject() {
    if (!projectItems.isEmpty()) {
      // Select the first row (which should be the most recently used project due to sorting)
      projectTable.setRowSelectionInterval(ProjectTableModel.NAME_COLUMN_INDEX, ProjectTableModel.NAME_COLUMN_INDEX);
      openButton.setEnabled(true);
      deleteButton.setEnabled(true);
    }
  }

  private void initListeners() {
    projectTable.getSelectionModel().addListSelectionListener(e -> {
      boolean hasSelection = projectTable.getSelectedRow() != -1;
      openButton.setEnabled(hasSelection);
      deleteButton.setEnabled(hasSelection);
    });

    projectTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && projectTable.getSelectedRow() != -1) {
          loadSelectedProject();
        }
      }
    });

    openButton.addActionListener(e -> loadSelectedProject());
    deleteButton.addActionListener(e -> deleteSelectedProject());
    newProjectButton.addActionListener(e -> createNewProject());
    cancelButton.addActionListener(e -> dispose());
  }

  private void createNewProject() {
    ProjectNewDialog dialog = new ProjectNewDialog((JFrame) getOwner());
    Project newProject = dialog.showDialog();
    if (newProject != null) {
      try {
        ProjectIO.saveProject(newProject);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(this,
                                      "Failed to save project file: " + e.getMessage(),
                                      "Error", JOptionPane.ERROR_MESSAGE);
      }
      // Refresh the project list to show the new project
      loadProjects();
    }
  }

  private void loadSelectedProject() {
    int selectedRow = projectTable.getSelectedRow();
    if (selectedRow >= 0) {
      // Convert view index to model index in case table is sorted
      int modelRow = projectTable.convertRowIndexToModel(selectedRow);
      ProjectItem item = projectItems.get(modelRow);
      selectedProject = item.project();
      dispose();
    }
  }

  private void deleteProjectDirectories(Project project) throws IOException {
    Path srcDir = project.getSrcDirectory();
    if (Files.exists(srcDir)) {
      PathUtils.deleteDirectory(srcDir);
    }

    Path venvDir = project.getVenvDirectory();
    if (Files.exists(venvDir)) {
      PathUtils.deleteDirectory(venvDir);
    }
  }

  private void deleteSelectedProject() {
    int selectedRow = projectTable.getSelectedRow();
    if (selectedRow >= 0) {
      // Convert view index to model index in case table is sorted
      int modelRow = projectTable.convertRowIndexToModel(selectedRow);
      ProjectItem item = projectItems.get(modelRow);

      int result = JOptionPane.showConfirmDialog(this,
                                                 "Are you sure you want to delete the project '" +
                                                     item.project().getName() + "'?",
                                                 "Confirm Delete", JOptionPane.YES_NO_OPTION);

      if (result == JOptionPane.YES_OPTION) {
        try {
          Files.delete(item.filePath());
          int dirResult = JOptionPane.showConfirmDialog(this,
                                                        "Do you also want to delete the project's source "
                                                            + "and virtual environment directories?",
                                                        "Delete Directories", JOptionPane.YES_NO_OPTION);
          if (dirResult == JOptionPane.YES_OPTION) {
            deleteProjectDirectories(item.project());
          }
          projectItems.remove(modelRow);
          tableModel.fireTableDataChanged();
        } catch (IOException e) {
          JOptionPane.showMessageDialog(this,
                                        "Error deleting project: " + e.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  /**
   * Shows the dialog and returns the selected project if loaded, or null if canceled.
   *
   * @return the selected project if loaded, or null if canceled
   */
  public Project showDialog() {
    setVisible(true);
    return selectedProject;
  }

  /**
   * Record to hold a project and its file path.
   */
  private record ProjectItem(Project project, Path filePath) {

  }

  /**
   * Table model for the projects table.
   */
  private class ProjectTableModel extends AbstractTableModel {

    public static final int NAME_COLUMN_INDEX = 0;
    private static final int USED_COLUMN_INDEX = 1;

    private final String[] columnNames = {"Name", "Last Used"};

    @Override
    public int getRowCount() {
      return projectItems.size();
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
      ProjectItem item = projectItems.get(rowIndex);
      return switch (columnIndex) {
        case NAME_COLUMN_INDEX -> item.project().getName();
        case USED_COLUMN_INDEX -> formatLastUsed(item.project().getLastUsed());
        default -> null;
      };
    }

    private String formatLastUsed(LocalDateTime lastUsed) {
      return lastUsed != null ? lastUsed.format(DATE_FORMATTER) : "Never";
    }
  }
}
