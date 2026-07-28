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

import java.awt.Frame;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;
import org.apache.commons.io.file.PathUtils;

public class FileManager {

  private final EventListenerList listenerList = new EventListenerList();
  private final transient Set<Path> editedFiles = new HashSet<>();
  private final PyEditor editor;

  public FileManager(PyEditor editor) {
    this.editor = editor;
  }

  /**
   * Checks if the project has unsaved changes.
   *
   * @return true if the project has unsaved changes, false otherwise
   */
  public boolean hasModifiedFiles() {
    return !editedFiles.isEmpty();
  }

  /**
   * Checks if a file is modified.
   *
   * @param filePath the path of the file to check
   * @return true if the file is modified, false otherwise
   */
  public boolean isFileModified(Path filePath) {
    if (filePath == null) {
      return false;
    }
    return editedFiles.contains(filePath);
  }

  /**
   * Sets the modified state of a file.
   *
   * @param filePath the path of the file
   * @param modified the new modified state
   */
  public void setFileModified(Path filePath, boolean modified) {
    if (editedFiles.contains(filePath) && !modified) {
      editedFiles.remove(filePath);
      fireFileModification(filePath, false);
    }
    if (!editedFiles.contains(filePath) && modified) {
      editedFiles.add(filePath);
      fireFileModification(filePath, true);
    }
  }

  /**
   * Gets a copy of edited files map.
   *
   * @return the set of edited files
   */
  public Collection<Path> getModifiedFiles() {
    return List.copyOf(editedFiles);
  }

  public void clearModifiedFiles() {
    editedFiles.clear();
  }

  public void addFileStatusListener(FileStatusListener l) {
    listenerList.add(FileStatusListener.class, l);
  }

  public void removeFileStatusListener(FileStatusListener l) {
    listenerList.remove(FileStatusListener.class, l);
  }

  public void newFile(Path targetDirectory) {
    var project = editor.getProject();
    if (project == null) {
      showNoProjectMessage();
      return;
    }

    // Prompt the user for the file name
    String fileName = showInputDialog("Enter file name:", "New File");
    if (fileName == null || fileName.trim().isEmpty()) {
      return; // User cancelled or entered an empty name
    }

    // Create the new file
    try {
      Path newFilePath = targetDirectory.resolve(fileName);

      // Check if file already exists
      if (Files.exists(newFilePath)) {
        showMessageDialog("File already exists: " + newFilePath,
                          "File Exists", JOptionPane.WARNING_MESSAGE);
        return;
      }
      Files.createFile(newFilePath);

      editor.recreateProjectTree();
      editor.selectPathInTree(newFilePath);
      editor.openFile(newFilePath);
      editor.updateToolbar();
    } catch (IOException e) {
      showMessageDialog("Error creating file: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public void newFolder(Path targetDirectory) {
    var project = editor.getProject();
    if (project == null) {
      showNoProjectMessage();
      return;
    }

    // Prompt the user for the folder name
    String folderName = showInputDialog("Enter folder name:", "New Folder");
    if (folderName == null || folderName.trim().isEmpty()) {
      return;
    }

    // Create the new folder
    try {
      Path newFolderPath = targetDirectory.resolve(folderName);

      // Check if folder already exists
      if (Files.exists(newFolderPath)) {
        showMessageDialog("Folder already exists: " + newFolderPath,
                          "Folder Exists", JOptionPane.WARNING_MESSAGE);
        return;
      }

      // Create the folder
      Files.createDirectory(newFolderPath);

      editor.recreateProjectTree();
      editor.selectPathInTree(newFolderPath);
      editor.updateToolbar();
    } catch (IOException e) {
      showMessageDialog("Error creating folder: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public void saveFile(Path filePath, String text) {
    try {
      _saveFile(filePath, text);
      editor.updateToolbar();
    } catch (IOException e) {
      var relativePath = filePath.relativize(editor.getProject().getSrcDirectory());
      var message = "<html>Error saving file:<br>" +
          relativePath + " - " + e.getMessage();
      JOptionPane.showMessageDialog(editor, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public void saveAll(Collection<Path> editedFiles) {
    List<String> errors = new ArrayList<>();
    var project = editor.getProject();
    if (project == null) {
      showNoProjectMessage();
      return;
    }
    for (Path filePath : editedFiles) {
      var text = editor.getModifiedText(filePath);
      try {
        _saveFile(filePath, text);
      } catch (IOException e) {
        errors.add(filePath.relativize(project.getSrcDirectory()) + " - " + e.getMessage());
      }
    }
    editor.updateToolbar();
    if (!errors.isEmpty()) {
      var message = "<html>Error saving file(s):<br>" + String.join("<br>", errors);
      JOptionPane.showMessageDialog(editor, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Prompts the user to handle unsaved files in the project and allows them to save or discard changes. If the user
   * opts to save changes, the method saves all modified files. If the user opts to discard changes or proceed without
   * saving, the operation continues. If the user cancels the operation, no changes are made.
   *
   * @return true if the operation is confirmed (save or discard changes), false if the operation is canceled
   */
  public boolean promptForUnsavedFiles() {
    // Get all edited files from the project
    Collection<Path> modifiedFiles = getModifiedFiles();
    if (modifiedFiles.isEmpty()) {
      return true;
    }
    // Build message with list of unsaved files
    StringBuilder message = new StringBuilder("The following files have unsaved changes:\n\n");
    var maxFiles = 5;
    int listedFileCount = 0;
    for (Path modifiedFile : modifiedFiles) {
      message.append("• ").append(modifiedFile.getFileName()).append("\n");
      listedFileCount++;
      if (listedFileCount == maxFiles) {
        var moreFiles = modifiedFiles.size() - listedFileCount;
        if (moreFiles == 1) {
          message.append("and ").append(moreFiles).append(" more file").append("\n");
        } else if (moreFiles > 1) {
          message.append("and ").append(moreFiles).append(" more files").append("\n");
        }
        break;
      }
    }
    message.append("\nDo you want to save all changes?");

    // Ask the user if they want to save changes
    Frame parentFrame = editor.getParentFrame();
    int option = JOptionPane.showConfirmDialog(parentFrame, message.toString(), "Unsaved Changes",
                                               JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

    if (option == JOptionPane.YES_OPTION) {
      saveAll(modifiedFiles);
      return true;
    } else // noinspection RedundantIfStatement
      if (option == JOptionPane.NO_OPTION) {
        // Continue without saving
        return true;
      } else {
        // Cancel the operation
        return false;
      }
  }


  private void _saveFile(Path filePath, String text) throws IOException {
    var project = editor.getProject();
    if (project == null) {
      showNoProjectMessage();
      return;
    }
    Files.writeString(filePath, text);
    editor.getFileManager().setFileModified(filePath, false);
    editor.updateToolbar();
  }

  public void deleteFile(Path filePath) {
    var project = editor.getProject();
    if (project == null) {
      showNoProjectMessage();
      return;
    }

    // Get the selected file or folder
    if (filePath == null) {
      showMessageDialog("Please select a file or folder to delete.",
                        "No Selection", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    // Check if the path is within the project directory
    if (!filePath.startsWith(project.getSrcDirectory())) {
      showMessageDialog("Cannot delete files outside the project directory.",
                        "Invalid Selection", JOptionPane.WARNING_MESSAGE);
      return;
    }

    // Determine if it's a file or directory
    boolean isDirectory = Files.isDirectory(filePath);
    String itemType = isDirectory ? "folder" : "file";

    // Ask for confirmation
    int option = showConfirmDialog(
        "Are you sure you want to delete this " + itemType + "?\n"
            + project.getSrcDirectory().relativize(filePath));

    if (option == JOptionPane.YES_OPTION) {
      try {
        // Close any open tabs for the file or files in the directory
        editor.closeTab(filePath);
        deletePath(filePath);

        editor.recreateProjectTree();
      } catch (IOException e) {
        showMessageDialog("Error deleting " + itemType + ": " + e.getMessage(),
                          "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
    editor.updateToolbar();
  }

  public void renameFile(Path filePath) {
    var project = editor.getProject();
    if (project == null) {
      showNoProjectMessage();
      return;
    }

    // Get the selected file or folder
    if (filePath == null) {
      showMessageDialog("Please select a file or folder to rename.",
                        "No Selection", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    // Check if the path is within the project directory
    if (!filePath.startsWith(project.getSrcDirectory())) {
      showMessageDialog("Cannot rename files outside the project directory.",
                        "Invalid Selection", JOptionPane.WARNING_MESSAGE);
      return;
    }

    String currentName = filePath.getFileName().toString();
    String newName = showRenameDialog(currentName);

    if (newName == null || newName.trim().isEmpty()) {
      return; // User cancelled or entered empty name
    }

    newName = newName.trim();

    // Check if name actually changed
    if (newName.equals(currentName)) {
      return;
    }

    Path newPath = filePath.getParent().resolve(newName);

    // Check if target already exists
    if (Files.exists(newPath)) {
      showMessageDialog("A file or folder with this name already exists.",
                        "Name Conflict", JOptionPane.WARNING_MESSAGE);
      return;
    }

    try {
      var wasOpened = editor.isOpened(filePath);
      editor.closeTab(filePath);
      Files.move(filePath, newPath);
      editor.recreateProjectTree();
      editor.selectPathInTree(newPath);
      if (wasOpened && Files.isRegularFile(newPath)) {
        editor.openFile(newPath);
      }
    } catch (IOException e) {
      showMessageDialog("Error renaming: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
    }
    editor.updateToolbar();
  }

  /**
   * Deleted a file or a directory and all its contents.
   *
   * @param directoryPath the path of the file or directory to delete
   * @throws IOException if an I/O error occurs
   */
  private void deletePath(Path directoryPath) throws IOException {
    if (Files.isRegularFile(directoryPath)) {
      Files.delete(directoryPath);
      return;
    }
    PathUtils.deleteDirectory(directoryPath);
  }

  /**
   * Shows a message dialog using the parent frame.
   *
   * @param message     the message to display
   * @param title       the title of the dialog
   * @param messageType the type of message
   */
  @SuppressWarnings("MagicConstant")
  private void showMessageDialog(String message, String title, int messageType) {
    JOptionPane.showMessageDialog(editor, message, title, messageType);
  }

  private int showConfirmDialog(String message) {
    return JOptionPane.showConfirmDialog(editor, message, "Confirm", JOptionPane.YES_NO_OPTION,
                                         JOptionPane.WARNING_MESSAGE);
  }

  private String showInputDialog(String message, String title) {
    return JOptionPane.showInputDialog(editor, message, title, JOptionPane.QUESTION_MESSAGE);
  }

  private String showRenameDialog(String currentName) {
    return (String) JOptionPane.showInputDialog(editor, "Enter new name:", "Rename " + currentName,
                                                JOptionPane.QUESTION_MESSAGE, null, null,
                                                currentName);
  }

  private void showNoProjectMessage() {
    JOptionPane.showMessageDialog(editor, "Please create or open a project first.",
                                  "No Project", JOptionPane.INFORMATION_MESSAGE);
  }

  private void fireFileModification(Path filePath, boolean modified) {
    for (FileStatusListener listener : listenerList.getListeners(
        FileStatusListener.class)) {
      listener.fileStatusChanged(filePath, modified);
    }
  }


  public interface FileStatusListener extends EventListener {

    void fileStatusChanged(Path file, boolean modified);
  }
}
