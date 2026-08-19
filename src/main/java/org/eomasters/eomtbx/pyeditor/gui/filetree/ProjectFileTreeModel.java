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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.eomasters.eomtbx.pyeditor.pyrun.Project;

/**
 * A TreeModel implementation that represents a file system directory structure.
 */
public class ProjectFileTreeModel implements TreeModel {

  private static final Logger LOGGER = Logger.getLogger("ProjectFileTreeModel");

  private final Project project;
  // Map to track opened files
  private final Map<Path, Boolean> openedFiles = new HashMap<>();
  private final EventListenerList listenerList = new EventListenerList();

  /**
   * Creates a new ProjectFileTreeModel based on the provided project.
   *
   * @param project     the project from which the source directory and name will be used
   */
  public ProjectFileTreeModel(Project project) {
    this.project = project;
    if (project.getSrcDirectory() == null) {
      throw new IllegalArgumentException("Source directory of project cannot be null");
    }
  }

  public static TreeModel getNoProjectModel() {
    return new DefaultTreeModel(new DefaultMutableTreeNode("<No Project>"));
  }

  public static Path getPathFromTreePath(TreePath treePath) {
    Object selectedNode = treePath.getLastPathComponent();
    if (selectedNode instanceof Path selectedPath) {
      return selectedPath;
    } else {
      throw new IllegalArgumentException("Tree path does not contain a Path object");
    }
  }

  public Project getProject() {
    return project;
  }

  @Override
  public Object getRoot() {
    return project.getSrcDirectory();
  }

  @Override
  public Object getChild(Object parent, int index) {
    if (parent instanceof Path parentPath) {
      if (Files.isDirectory(parentPath)) {
        try {
          List<Path> children = getSortedChildren(parentPath);
          if (index >= 0 && index < children.size()) {
            return children.get(index);
          }
        } catch (Exception e) {
          LOGGER.log(Level.FINER, e.getMessage(), e);
        }
      }
    }
    return null;
  }

  @Override
  public int getChildCount(Object parent) {
    if (parent instanceof Path parentPath) {
      if (Files.isDirectory(parentPath)) {
        try {
          return getSortedChildren(parentPath).size();
        } catch (Exception e) {
          LOGGER.log(Level.FINER, e.getMessage(), e);
        }
      }
    }
    return 0;
  }

  @Override
  public boolean isLeaf(Object node) {
    if (node instanceof Path path) {
      return Files.isRegularFile(path);
    }
    return true;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    // Not supporting renaming files through the tree
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    if (parent instanceof Path parentPath && child instanceof Path) {
      if (Files.isDirectory(parentPath)) {
        try {
          List<Path> children = getSortedChildren(parentPath);
          return children.indexOf(child);
        } catch (Exception e) {
          LOGGER.log(Level.FINER, e.getMessage(), e);
        }
      }
    }
    return -1;
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    listenerList.add(TreeModelListener.class, l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    listenerList.remove(TreeModelListener.class, l);
  }

  /**
   * Gets the sorted list of children for a directory. Directories are listed first, followed by files, both in
   * alphabetical order.
   *
   * @param directory the directory to get children for
   * @return a sorted list of child paths
   */
  private List<Path> getSortedChildren(Path directory) {
    List<Path> directories = new ArrayList<>();
    List<Path> files = new ArrayList<>();

    try {
      File[] childFiles = directory.toFile().listFiles();
      if (childFiles != null) {
        for (File childFile : childFiles) {
          Path childPath = childFile.toPath();
          if (childFile.isDirectory()) {
            directories.add(childPath);
          } else {
            files.add(childPath);
          }
        }
      }
    } catch (Exception e) {
      LOGGER.log(Level.FINER, e.getMessage(), e);
    }

    // Sort directories and files alphabetically
    directories.sort((p1, p2) -> p1.getFileName().toString().compareToIgnoreCase(p2.getFileName().toString()));
    files.sort((p1, p2) -> p1.getFileName().toString().compareToIgnoreCase(p2.getFileName().toString()));

    // Combine lists with directories first
    List<Path> children = new ArrayList<>(directories);
    children.addAll(files);

    return children;
  }

  public void rebuildFileTree() {
    SwingUtilities.invokeLater(this::fireTreeStructureChanged);
  }

  /**
   * Notifies all listeners that the tree structure has changed. This should only be used when the actual structure
   * changes (add/remove files/directories).
   */
  void fireTreeStructureChanged() {
    TreeModelEvent event = new TreeModelEvent(this, new Object[]{getRoot()});
    for (TreeModelListener listener : listenerList.getListeners(TreeModelListener.class)) {
      listener.treeStructureChanged(event);
    }
  }

  private TreeModelEvent getModelEvent(Path nodePath) {
    TreePath path = getTreePath(nodePath);
    if (path == null || path.getParentPath() == null) {
      return null;
    }

    Object parent = path.getParentPath().getLastPathComponent();
    int childIndex = getIndexOfChild(parent, nodePath);

    // Verify the child index is valid to prevent ArrayIndexOutOfBoundsException
    if (childIndex < 0 || childIndex >= getChildCount(parent)) {
      // Log the issue and return null to skip the event
      LOGGER.log(Level.FINE, "Invalid child index {0} for node {1} (parent has {2} children)",
                 new Object[]{childIndex, nodePath, getChildCount(parent)});
      return null;
    }

    // Create event for the node
    return new TreeModelEvent(this, path.getParentPath(),
                              new int[]{childIndex}, new Object[]{nodePath});
  }


  /**
   * Notifies listeners that a specific node has changed.
   *
   * @param nodePath the path of the node that changed
   */
  void fireTreeNodeChanged(Path nodePath) {
    TreeModelEvent event = getModelEvent(nodePath);
    if (event == null) {
      // If we can't create a valid event, skip the notification
      return;
    }
    // Notify listeners
    for (TreeModelListener listener : listenerList.getListeners(TreeModelListener.class)) {
      listener.treeNodesChanged(event);
    }
  }


  /**
   * Checks if a file is opened.
   *
   * @param path the file path to check
   * @return true if the file is opened, false otherwise
   */
  public boolean isFileOpened(Path path) {
    return openedFiles.getOrDefault(path, false);
  }

  /**
   * Sets the opened state of a file.
   *
   * @param path   the file path to set the state for
   * @param opened true if the file is opened, false otherwise
   */
  public void setFileOpened(Path path, boolean opened) {
    openedFiles.put(path, opened);
    SwingUtilities.invokeLater(() -> fireTreeNodeChanged(path));
  }

  /**
   * Sets the edited state of a file.
   *
   * @param path the file path which is modified
   */
  public void updateFileTree(Path path) {
    SwingUtilities.invokeLater(() -> fireTreeNodeChanged(path));
  }

  /**
   * Checks if a file is write-protected.
   *
   * @param path the file path to check
   * @return true if the file is write-protected, false otherwise
   */
  public boolean isFileWriteProtected(Path path) {
    return Files.exists(path) && !Files.isWritable(path);
  }

  /**
   * Retrieves the {@link TreePath} corresponding to the given file path within the tree model. If the file path is not
   * part of the tree model or the root directory is not set, this method returns {@code null}.
   *
   * @param filePath the file path for which the tree path is to be retrieved
   * @return the {@link TreePath} from the root to the specified file path, or {@code null} if the file path is not
   * under the root directory or the root directory is not set
   */
  public TreePath getTreePath(Path filePath) {
    // Get the root directory
    Path rootDirectory = (Path) getRoot();
    // Check if the file is in the project
    if (!filePath.startsWith(rootDirectory)) {
      return null;
    }

    // Build the path from the root to the file
    List<Path> pathElements = new ArrayList<>();
    Path current = filePath;

    // Add all path elements from the file to the root (inclusive)
    while (current != null && !current.equals(rootDirectory.getParent())) {
      pathElements.addFirst(current);
      current = current.getParent();
    }

    // Convert the list of path elements to an array for the TreePath constructor
    return new TreePath(pathElements.toArray());
  }
}
