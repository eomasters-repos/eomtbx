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

package org.eomasters.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EventListener;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * A component for selecting files or directories. Displays the selected path in a text field and provides a button to
 * open a file chooser dialog.
 */
public class FileChooserComponent extends JPanel {

  private static final int MAX_WIDTH = 220;
  private static final Path USER_HOME_PATH = Paths.get(System.getProperty("user.home"));
  private File defaultChooserDir;
  private final JTextField pathField;
  private final int fileSelectionMode;
  private final String dialogTitle;
  private Path currentPath;
  private int maxPathLength = 45;
  private final EventListenerList listenerList = new EventListenerList();

  /**
   * Creates a new FileChooserComponent with the specified file selection mode.
   *
   * @param fileSelectionMode the file selection mode (JFileChooser.FILES_ONLY, JFileChooser.DIRECTORIES_ONLY, or
   *                          JFileChooser.FILES_AND_DIRECTORIES)
   * @param dialogTitle       the title for the file chooser dialog
   * @param iconSize          the icon size
   */
  public FileChooserComponent(int fileSelectionMode, String dialogTitle, int iconSize) {
    this.fileSelectionMode = fileSelectionMode;
    this.dialogTitle = dialogTitle;
    this.currentPath = USER_HOME_PATH;

    setLayout(new BorderLayout(0, 0));
    setPreferredSize(new Dimension(MAX_WIDTH, 25));

    MaterialDesignF icon = MaterialDesignF.FOLDER;
    if (fileSelectionMode == JFileChooser.FILES_ONLY) {
      icon = MaterialDesignF.FILE;
    } else if (fileSelectionMode == JFileChooser.FILES_AND_DIRECTORIES) {
      icon = MaterialDesignF.FILE_MULTIPLE;
    }

    JButton browseButton = new JButton(FontIcon.of(icon, iconSize));
    browseButton.setToolTipText(dialogTitle);
    browseButton.addActionListener(e -> openFileChooser());
    browseButton.setPreferredSize(new Dimension(30, 25));

    pathField = new JTextField();
    pathField.setEditable(false);
    updatePathField();

    add(browseButton, BorderLayout.EAST);
    add(pathField, BorderLayout.CENTER);
  }

  public int getMaxPathLength() {
    return maxPathLength;
  }

  public void setMaxPathLength(int maxPathLength) {
    this.maxPathLength = maxPathLength;
  }

  /**
   * Opens a file chooser dialog and updates the text field with the selected path.
   */
  private void openFileChooser() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(
        currentPath == null ? getDefaultChooserDir() : currentPath.toFile());
    fileChooser.setDialogTitle(dialogTitle);
    fileChooser.setFileSelectionMode(fileSelectionMode);

    // Only disable "All Files" filter for directory selection
    if (fileSelectionMode == JFileChooser.DIRECTORIES_ONLY) {
      fileChooser.setAcceptAllFileFilterUsed(false);
    }

    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      Path oldPath = currentPath;
      currentPath = fileChooser.getSelectedFile().toPath();
      updatePathField();
      fireFileChanged(oldPath, currentPath);
    }
  }

  public File getDefaultChooserDir() {
    return defaultChooserDir != null ? defaultChooserDir : USER_HOME_PATH.toFile();
  }

  public void setDefaultChooserDir(Path path) {
    if (path != null) {
      if (Files.isDirectory(path)) {
        defaultChooserDir = path.toFile();
      } else {
        defaultChooserDir = path.getParent().toFile();
      }
    } else {
      defaultChooserDir = null;
    }
  }

  /**
   * Updates the path field with the current path. If the path is longer than 30 characters, it is shortened at the
   * start.
   */
  private void updatePathField() {
    String text = "";
    String fieldText = "";
    if (currentPath != null) {
      text = currentPath.toAbsolutePath().toString();
      fieldText = text;
      if (text.length() > maxPathLength) {
        fieldText = "..." + text.substring(text.length() - (maxPathLength - 3));
      }
    }
    pathField.setText(fieldText);
    pathField.setToolTipText(text);
  }

  /**
   * Returns the currently selected path.
   */
  public Path getCurrentPath() {
    return currentPath;
  }

  /**
   * Sets the current path.
   *
   * @param path the path to set
   */
  public void setCurrentPath(Path path) {
    Path oldPath = currentPath;
    currentPath = path;
    updatePathField();
    fireFileChanged(oldPath, currentPath);
  }

  /**
   * Adds a file changed listener.
   *
   * @param listener the listener to add
   */
  public void addFileChangedListener(FileChangedListener listener) {
    listenerList.add(FileChangedListener.class, listener);
  }

  /**
   * Removes a file changed listener.
   *
   * @param listener the listener to remove
   */
  public void removeFileChangedListener(FileChangedListener listener) {
    listenerList.remove(FileChangedListener.class, listener);
  }

  /**
   * Fires a file changed event to all registered listeners.
   *
   * @param oldPath the previous path
   * @param newPath the new path
   */
  private void fireFileChanged(Path oldPath, Path newPath) {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == FileChangedListener.class) {
        ((FileChangedListener) listeners[i + 1]).fileChanged(oldPath, newPath);
      }
    }
  }

  /**
   * Interface for listening to file changes.
   */
  public interface FileChangedListener extends EventListener {

    /**
     * Called when the file selection changes.
     *
     * @param oldPath the previous path (may be null)
     * @param newPath the new path (may be null)
     */
    void fileChanged(Path oldPath, Path newPath);
  }

}
