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

package org.eomasters.eomtbx.pyeditor.gui.tabs;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.eomasters.eomtbx.pyeditor.gui.PyEditor;
import org.eomasters.eomtbx.pyeditor.gui.action.FileSaveAction;
import org.eomasters.eomtbx.pyeditor.gui.action.FileSaveAllAction;
import org.eomasters.eomtbx.pyeditor.gui.tabs.TabComponent.TabCloseListener;
import org.eomasters.eomtbx.pyeditor.gui.tabs.TextEditorComponent.TextChangedListener;
import org.eomasters.eomtbx.pyeditor.pyrun.EditorTheme;
import org.eomasters.eomtbx.pyeditor.pyrun.Project;
import org.eomasters.eomtbx.pyeditor.pyrun.Project.ChangeListener;
import org.eomasters.eomtbx.pyeditor.pyrun.PyRunnerException;
import org.eomasters.eomtbx.pyeditor.pyrun.PythonEnvironment;
import org.graalvm.polyglot.Context;
import org.openide.util.RequestProcessor;

/**
 * A component that manages editor tabs for files. This class encapsulates all tab-related functionality including
 * opening, closing, and saving files.
 */
public class FileEditorTabs extends JTabbedPane implements TabCloseListener, TextChangedListener {

  private static final AtomicReference<Context> PARSER_CONTEXT = new AtomicReference<>();

  private final PyEditor pyEditor;
  private final Map<Path, Integer> openedFileTabs = new HashMap<>(); // Maps file paths to tab indices
  private final List<FileTabListener> fileTabListeners = new ArrayList<>();
  private Project project;
  private final ChangeListener projectChangeListener = new ThemeChangeListener();

  /**
   * Creates a new EditorTabs component.
   */
  public FileEditorTabs(PyEditor pyEditor) {
    this.pyEditor = pyEditor;
    // Add a change listener to handle tab selection
    addChangeListener(e -> {
      var textEditor = getSelectedTextEditor();
      if (textEditor == null) {
        return;
      }
      // Get the file path
      Path filePath = textEditor.getFilePath();
      // Notify listeners
      notifyTabSelectionListeners(filePath);
    });
  }

  /**
   * Gets the current project.
   *
   * @return the project
   */
  public Project getProject() {
    return project;
  }

  /**
   * Sets the current project.
   *
   * @param project the project
   */
  public void setProject(Project project) {
    if (this.project != null) {
      this.project.removeChangeListener(projectChangeListener);
    }
    this.project = project;
    if (this.project != null) {
      this.project.addChangeListener(projectChangeListener);
    }
  }

  /**
   * Adds a file status listener.
   *
   * @param listener the listener to add
   */
  public void addFileStatusListener(FileTabListener listener) {
    fileTabListeners.add(listener);
  }

  /**
   * Removes a file status listener.
   *
   * @param listener the listener to remove
   */
  public void removeFileStatusListener(FileTabListener listener) {
    fileTabListeners.remove(listener);
  }

  /**
   * Notifies all tab selection listeners that a tab has been selected.
   *
   * @param filePath the path of the file in the selected tab
   */
  private void notifyTabSelectionListeners(Path filePath) {
    for (FileTabListener listener : fileTabListeners) {
      listener.onFileSelected(filePath);
    }
  }

  /**
   * Notifies all file status listeners that a file has been opened.
   *
   * @param filePath the path of the file that was opened
   */
  private void notifyFileOpenedListeners(Path filePath) {
    for (FileTabListener listener : fileTabListeners) {
      listener.onFileOpened(filePath);
    }
  }

  /**
   * Notifies all file status listeners that a file has been closed.
   *
   * @param filePath the path of the file that was closed
   */
  private void notifyFileClosedListeners(Path filePath) {
    for (FileTabListener listener : fileTabListeners) {
      listener.onFileClosed(filePath);
    }
  }

  public boolean isOpened(Path filePath) {
    return openedFileTabs.containsKey(filePath);
  }

  /**
   * Opens a file in a new tab or switches to an existing tab if the file is already open.
   *
   * @param filePath the path of the file to open
   */
  public void openFileInTab(Path filePath) {
    // Check if the file is already open
    if (openedFileTabs.containsKey(filePath)) {
      // Switch to the existing tab
      setSelectedIndex(openedFileTabs.get(filePath));
      return;
    }

    try {
      // Read the file content
      String content = Files.readString(filePath);

      // Create a text editor component with syntax highlighting
      TextEditorComponent textEditor = new TextEditorComponent(content, filePath, project.getTheme());

      // Register this EditorTabs as a listener for modified state changes
      textEditor.addModifiedStateListener(this);

      var saveKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
      textEditor.addKeyAction(new FileSaveAction(pyEditor, filePath), "saveFile", saveKeyStroke);

      var saveAllKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
      textEditor.addKeyAction(new FileSaveAllAction(pyEditor), "saveAllFiles", saveAllKeyStroke);

      if (project != null && project.getSrcDirectory() != null) {
        String relativePath = project.getSrcDirectory().relativize(filePath).toString();
        if (textEditor.getSyntaxType().equals("text/python")) {
          RequestProcessor.getDefault().post(() -> {
            try {
              EditorPythonParser parser = new EditorPythonParser(relativePath, getParserContext());
              SwingUtilities.invokeLater(() -> textEditor.addParser(parser));
            } catch (PyRunnerException e) {
              SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                  this,
                  "Error creating Python context: " + e.getMessage(),
                  "Error",
                  JOptionPane.ERROR_MESSAGE
              ));
            }
          });
        }
      }

      // Add the tab with the file name
      String tabTitle = filePath.getFileName().toString();
      addTab(tabTitle, textEditor);
      int tabIndex = getTabCount() - 1;
      setSelectedIndex(tabIndex);

      // Create a custom tab component with a close button
      TabComponent tabComponent = new TabComponent(tabTitle, this, filePath, this);
      setTabComponentAt(tabIndex, tabComponent);

      // Store the tab index
      openedFileTabs.put(filePath, tabIndex);

      // Notify listeners
      notifyFileOpenedListeners(filePath);

    } catch (IOException e) {
      JOptionPane.showMessageDialog(this,
                                    "Error opening file: " + e.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
    }
  }

  private Context getParserContext() throws PyRunnerException {
    if (PARSER_CONTEXT.get() == null) {
      PARSER_CONTEXT.set(project.getPyEnvironment().createContext(PythonEnvironment.getDefaultOptions()));
    }
    return PARSER_CONTEXT.get();
  }

  /**
   * Handles tab close events. Checks if the content has been modified and prompts the user to save if necessary.
   *
   * @param filePath the path of the file in the tab being closed
   * @return true if the tab should be closed, false otherwise
   */
  @Override
  public boolean onTabClose(Path filePath) {
    var textEditor = getSelectedTextEditor();
    if (textEditor == null) {
      return true;
    }
    // Check if the content has been modified
    if (textEditor.isModified()) {
      // Ask the user if they want to save
      int option = JOptionPane.showConfirmDialog(
          this,
          "The file has been modified. Do you want to save changes?",
          "Save Changes",
          JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE
      );

      if (option == JOptionPane.YES_OPTION) {
        // Save the file
        pyEditor.getFileManager().saveFile(filePath, textEditor.getText());
      } else if (option == JOptionPane.CANCEL_OPTION) {
        // Cancel closing
        return false;
      } else {
        // User chose not to save, but we still need to reset the modified state
        pyEditor.getFileManager().setFileModified(filePath, true);
        textEditor.setModified(false);
      }
    }

    // Update the openedFileTabs map
    openedFileTabs.remove(filePath);

    // Update the indices of tabs that come after the closed tab
    int tabIndex = openedFileTabs.getOrDefault(filePath, -1);
    for (Map.Entry<Path, Integer> entry : openedFileTabs.entrySet()) {
      if (entry.getValue() > tabIndex) {
        entry.setValue(entry.getValue() - 1);
      }
    }

    // Notify listeners
    notifyFileClosedListeners(filePath);

    return true; // Allow the tab to be closed
  }

  /**
   * Handles modified state changes in text editor components.
   *
   * @param filePath the path of the file being edited
   * @param modified the new modified state
   */
  @Override
  public void onChange(Path filePath, boolean modified) {
    pyEditor.getFileManager().setFileModified(filePath, modified);
  }

  public void setModified(Path filePath, boolean b) {
    int tabIndex = openedFileTabs.getOrDefault(filePath, -1);
    if (tabIndex != -1 && tabIndex < getTabCount()) {
      var textEditorComponent = (TextEditorComponent) getComponentAt(tabIndex);
      textEditorComponent.setModified(b);
      var tabComponent = (TabComponent) getTabComponentAt(tabIndex);
      tabComponent.setModified(b);
    }
  }

  public String getTextOfSelectedEditor() {
    var selectedTextEditor = getSelectedTextEditor();
    if (selectedTextEditor != null) {
      return selectedTextEditor.getText();
    }
    return null;
  }

  private TextEditorComponent getSelectedTextEditor() {
    int selectedIndex = getSelectedIndex();
    if (selectedIndex != -1) {
      return (TextEditorComponent) getComponentAt(selectedIndex);
    }
    return null;
  }

  public Path getCurrentFile() {
    var selectedTextEditor = getSelectedTextEditor();
    if (selectedTextEditor != null) {
      return selectedTextEditor.getFilePath();
    }
    return null;

  }

  public String getModifiedText(Path filePath) {
    int tabIndex = openedFileTabs.getOrDefault(filePath, -1);
    if (tabIndex != -1) {
      TextEditorComponent textEditor = (TextEditorComponent) getComponentAt(tabIndex);
      return textEditor.getText();
    }
    return null;
  }

  public void closeAllTabs() {
    removeAll();
    openedFileTabs.clear();
  }

  /**
   * Closes the tab for a file or all opened tabs for files contained in a directory for the specified path.
   *
   * @param filePath the path whose tab(s) should be closed
   * @return a list of paths of the closed files
   */
  public List<Path> closeTab(Path filePath) {
    List<Path> closedPaths = new ArrayList<>();
    if (Files.isDirectory(filePath)) {
      // Create a copy of the keys to avoid ConcurrentModificationException
      List<Path> filePaths = new ArrayList<>(openedFileTabs.keySet());

      for (Path path : filePaths) {
        if (path.startsWith(path)) {
          closedPaths.addAll(closeTab(path));
        }
      }
    } else {
      if (openedFileTabs.containsKey(filePath)) {
        int tabIndex = openedFileTabs.get(filePath);
        remove(tabIndex);
        // Update the openedFileTabs map
        openedFileTabs.remove(filePath);
        closedPaths.add(filePath);
        // Update the indices of tabs that come after the closed tab
        for (Map.Entry<Path, Integer> entry : openedFileTabs.entrySet()) {
          if (entry.getValue() > tabIndex) {
            entry.setValue(entry.getValue() - 1);
          }
        }

        // Notify listeners
        notifyFileClosedListeners(filePath);
      }
    }
    return closedPaths;
  }

  /**
   * Interface for handling file status changes.
   */
  public interface FileTabListener {

    /**
     * Called when a tab is selected.
     *
     * @param filePath the path of the file in the selected tab
     */
    void onFileSelected(Path filePath);

    /**
     * Called when a file is opened.
     *
     * @param filePath the path of the file that was opened
     */
    void onFileOpened(Path filePath);

    /**
     * Called when a file is closed.
     *
     * @param filePath the path of the file that was closed
     */
    void onFileClosed(Path filePath);

  }

  private class ThemeChangeListener extends ChangeListener {

    @Override
    public void themeChanged(EditorTheme theme) {
      var editor = getSelectedTextEditor();
      if (editor != null) {
        editor.setTheme(theme);
      }
    }
  }
}
