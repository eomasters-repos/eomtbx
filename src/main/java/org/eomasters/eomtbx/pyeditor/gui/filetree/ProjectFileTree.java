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

package org.eomasters.eomtbx.pyeditor.gui.filetree;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import org.eomasters.eomtbx.EomtbxRuntime;
import org.eomasters.eomtbx.pyeditor.gui.FileManager;
import org.eomasters.eomtbx.pyeditor.gui.PyEditor;
import org.eomasters.eomtbx.pyeditor.gui.action.AbstractEditorAction;
import org.eomasters.eomtbx.pyeditor.gui.action.FileDeleteAction;
import org.eomasters.eomtbx.pyeditor.gui.action.FileNewAction;
import org.eomasters.eomtbx.pyeditor.gui.action.FileRenameAction;
import org.eomasters.eomtbx.pyeditor.gui.action.FileSaveAction;
import org.eomasters.eomtbx.pyeditor.gui.action.FileSaveAllAction;
import org.eomasters.eomtbx.pyeditor.gui.action.FolderNewAction;
import org.eomasters.eomtbx.pyeditor.pyrun.Project;
import org.eomasters.eomtbx.utils.DirectoryWatcher;

/**
 * Component that handles the file tree view in the PyEditor. This component is responsible for displaying the project
 * files in a tree structure and handling tree-related events.
 */
public class ProjectFileTree extends JTree {

  private final DirectoryWatcher dirwatcher;
  private final PyEditor pyEditor;
  private MouseInputAdapter mouseListener;
  private ProjectFileTreeModel fileTreeModel;
  private FileManager fileManager;

  /**
   * Creates a new FileTreeComponent with the specified file selection listener.
   */
  public ProjectFileTree(PyEditor pyEditor) {
    this.pyEditor = pyEditor;
    // Create a default tree model with no project
    setRootVisible(true);
    setShowsRootHandles(true);
    setEmptyTreeModel();
    dirwatcher = new DirectoryWatcher();
    dirwatcher.addListener(event -> {
      if (event.isFileCreated() || event.isFileDeleted()) {
        rebuildFileTree();
      }
    });

  }

  public void rebuildFileTree() {
    fileTreeModel.rebuildFileTree();
    // Force UI refresh to prevent empty spaces
    SwingUtilities.invokeLater(() -> {
      updateUI();
      revalidate();
      repaint();
    });
  }

  public void setFileSelectionListener(FileSelectionListener fileSelectionListener) {
    if (fileSelectionListener != null) {
      mouseListener = new TreeMouseInputAdapter(fileSelectionListener);
      addMouseListener(mouseListener);
    } else {
      removeMouseListener(mouseListener);
      mouseListener = null;
    }
  }

  /**
   * Sets an empty tree model when no project is loaded.
   */
  private void setEmptyTreeModel() {
    setModel(ProjectFileTreeModel.getNoProjectModel());
    setCellRenderer(new DefaultTreeCellRenderer());
  }

  /**
   * Updates the tree model with the specified project.
   *
   * @param project     the project to display in the tree
   * @param fileManager the file manager to use for file status checks
   */
  public void updateProject(Project project, FileManager fileManager) {
    this.fileManager = fileManager;
    if (project != null && fileManager != null) {
      fileTreeModel = new ProjectFileTreeModel(project);
      setModel(fileTreeModel);
      setCellRenderer(new ProjectTreeCellRenderer(fileTreeModel, fileManager));
      try {
        if (dirwatcher.isWatching()) {
          dirwatcher.stopWatching();
        }
        dirwatcher.startWatching(project.getSrcDirectory());
      } catch (IOException e) {
        EomtbxRuntime.LOGGER.warning("Failed to start watching directory");
      }
    } else {
      try {
        if (dirwatcher.isWatching()) {
          dirwatcher.stopWatching();
        }
      } catch (IOException e) {
        EomtbxRuntime.LOGGER.warning("Failed to stop watching directory");
      }
      setEmptyTreeModel();
    }
  }

  /**
   * Selects the node in the tree view that corresponds to the given file path.
   *
   * @param filePath the path of the file to select in the tree view
   */
  public void selectNodeInTree(Path filePath) {
    if (filePath == null || fileTreeModel == null) {
      return;
    }

    // Build the tree path from the file path
    TreePath treePath = buildTreePath(filePath);
    if (treePath != null) {
      // Select the node in the tree view
      setSelectionPath(treePath);
      // Ensure the selected node is visible
      scrollPathToVisible(treePath);
    }
  }

  /**
   * Builds a TreePath for the given file path.
   *
   * @param filePath the path of the file
   * @return the TreePath for the file, or null if the file is not in the tree
   */
  private TreePath buildTreePath(Path filePath) {
    if (filePath == null || fileTreeModel == null) {
      return null;
    }

    return fileTreeModel.getTreePath(filePath);
  }

  /**
   * Marks a file as opened in the tree view.
   *
   * @param filePath the path of the file to mark as opened
   */
  public void markFileAsOpened(Path filePath) {
    if (fileTreeModel != null) {
      fileTreeModel.setFileOpened(filePath, true);
    }
  }

  /**
   * Marks a file as closed (not opened) in the tree view.
   *
   * @param filePath the path of the file to mark as closed
   */
  public void markFileAsClosed(Path filePath) {
    if (fileTreeModel != null) {
      fileTreeModel.setFileOpened(filePath, false);
    }
  }

  /**
   * Marks a file as edited in the tree view.
   *
   * @param filePath the path of the file to mark as edited
   */
  public void markFileAsModified(Path filePath) {
    if (fileTreeModel != null) {
      fileTreeModel.updateFileTree(filePath);
    }
  }

  /**
   * Retrieves the currently selected file from the tree view.
   *
   * @return the path of the selected file if a selection is made, or null if no file is selected
   */
  public Path getSelectedFile() {
    Path filePath = null;
    TreePath selectionPath = getSelectionPath();
    if (selectionPath != null) {
      filePath = ProjectFileTreeModel.getPathFromTreePath(selectionPath);
    }
    return filePath;
  }

  /**
   * Retrieves the currently selected file from the tree view.
   *
   * @return the path of the selected file if a selection is made, or null if no file is selected
   */
  public List<Path> getSelectedFiles() {
    List<Path> filePaths = new ArrayList<>();
    TreePath[] selectionPaths = getSelectionPaths();
    if (selectionPaths != null) {
      for (TreePath treePath : selectionPaths) {
        filePaths.add(ProjectFileTreeModel.getPathFromTreePath(treePath));
      }
    }
    return filePaths;
  }

  /**
   * Determines the target directory based on the user's current selection in the file tree. If a file is selected, it
   * returns the parent directory of the file. If a directory is selected, it returns the selected directory itself. If
   * no valid selection is found, it returns null.
   *
   * @return the target directory path based on the selected file or directory, or null if no valid selection is made
   */
  public Path getSelectedTargetDirectory() {
    Path selectedFile = getSelectedFile();
    if (selectedFile == null) {
      return null;
    }

    if (Files.isRegularFile(selectedFile)) {
      // If a file is selected, use its parent directory
      return selectedFile.getParent();
    } else if (Files.isDirectory(selectedFile)) {
      // If a directory is selected, use it directly
      return selectedFile;
    }

    return null;
  }

  /**
   * Notifies the tree model that the structure has changed. This should be called after files or directories are added,
   * removed, or renamed.
   */
  public void fireTreeStructureChanged() {
    if (fileTreeModel != null) {
      fileTreeModel.fireTreeStructureChanged();
    }
  }

  /**
   * Interface for listening to file selection events in the tree.
   */
  public interface FileSelectionListener {

    /**
     * Called when a file has been double-clicked.
     *
     * @param filePath the path of the selected file
     */
    void onFileDoubleClicked(Path filePath);

    /**
     * Called when a file is selected in the tree.
     *
     * @param filePath the path of the selected file or <code>null</code> if no file is selected
     */
    void onFileSelected(Path filePath);
  }

  private class TreeMouseInputAdapter extends MouseInputAdapter {

    private final FileSelectionListener fileSelectionListener;

    public TreeMouseInputAdapter(
        FileSelectionListener fileSelectionListener) {this.fileSelectionListener = fileSelectionListener;}

    @Override
    public void mouseClicked(MouseEvent e) {
      var filePath = getFilePath(e);
      if (filePath == null) {
        return;
      }

      if (SwingUtilities.isRightMouseButton(e)) {
        // Right-click: show context menu
        showContextMenu(e, filePath);
      } else {
        // Left-click: handle selection and double-click
        if (e.getClickCount() == 1) {
          fileSelectionListener.onFileSelected(filePath);
        }
        if (e.getClickCount() == 2) {
          fileSelectionListener.onFileDoubleClicked(filePath);
        }
      }
    }

    private void showContextMenu(MouseEvent e, Path filePath) {
      if (fileManager == null) {
        return;
      }

      JPopupMenu contextMenu = new JPopupMenu();
      contextMenu.add(new CustomMenuItem(new FileSaveAction(pyEditor, filePath)));
      contextMenu.add(new CustomMenuItem(new FileSaveAllAction(pyEditor, fileManager.getModifiedFiles())));
      contextMenu.addSeparator();

      contextMenu.add(new CustomMenuItem(new FileRenameAction(pyEditor, filePath)));
      contextMenu.add(new CustomMenuItem(new FileDeleteAction(pyEditor, filePath)));
      contextMenu.addSeparator();

      var targetDirectory = Files.isDirectory(filePath) ? filePath : filePath.getParent();
      contextMenu.add(new CustomMenuItem(new FileNewAction(pyEditor, targetDirectory)));
      contextMenu.add(new CustomMenuItem(new FolderNewAction(pyEditor, targetDirectory)));

      contextMenu.show(ProjectFileTree.this, e.getX(), e.getY());
    }

    private Path getFilePath(MouseEvent e) {
      TreePath path = getPathForLocation(e.getX(), e.getY());
      if (path != null) {
        Object node = path.getLastPathComponent();
        if (node instanceof Path filePath) {
          return filePath;
        }
      }
      return null;
    }
  }

  /**
   * Ensures the disabled icon is always visible and sets the icon size.
   */
  private static class CustomMenuItem extends JMenuItem {

    public CustomMenuItem(AbstractEditorAction action) {
      super(action);
      action.setIconSize(16);
      setDisabledIcon(action.getDisabledIcon());
    }

  }

}
