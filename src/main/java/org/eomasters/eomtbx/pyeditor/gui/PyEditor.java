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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import org.eomasters.eomtbx.pyeditor.gui.action.ConsoleClearAction;
import org.eomasters.eomtbx.pyeditor.gui.action.ConsoleToggleAction;
import org.eomasters.eomtbx.pyeditor.gui.action.ExecutionToggleAction;
import org.eomasters.eomtbx.pyeditor.gui.action.FileDeleteAction;
import org.eomasters.eomtbx.pyeditor.gui.action.FileNewAction;
import org.eomasters.eomtbx.pyeditor.gui.action.FileSaveAction;
import org.eomasters.eomtbx.pyeditor.gui.action.FileSaveAllAction;
import org.eomasters.eomtbx.pyeditor.gui.action.FolderNewAction;
import org.eomasters.eomtbx.pyeditor.gui.action.HelpAction;
import org.eomasters.eomtbx.pyeditor.gui.action.ProjectCloseAction;
import org.eomasters.eomtbx.pyeditor.gui.action.ProjectConfigAction;
import org.eomasters.eomtbx.pyeditor.gui.action.ProjectManageAction;
import org.eomasters.eomtbx.pyeditor.gui.filetree.ProjectFileTree;
import org.eomasters.eomtbx.pyeditor.gui.filetree.ProjectFileTree.FileSelectionListener;
import org.eomasters.eomtbx.pyeditor.gui.tabs.FileEditorTabs;
import org.eomasters.eomtbx.pyeditor.gui.tabs.FileEditorTabs.FileTabListener;
import org.eomasters.eomtbx.pyeditor.pyrun.Project;
import org.eomasters.eomtbx.pyeditor.pyrun.ProjectIO;
import org.eomasters.eomtbx.pyeditor.pyrun.PyRunner;
import org.eomasters.eomtbx.pyeditor.pyrun.PythonEnvironment;
import org.eomasters.eomtbx.utils.CaptureStreamCallback;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.api.progress.ProgressHandle;

/**
 * PyEditor - A Python editor component with a split layout.
 */
public class PyEditor extends JPanel implements FileTabListener {

  private static final int MIN_TREE_WIDTH = 200;
  private static final double TREE_SPLIT_RATIO = 0.2; // 20% for tree view
  private static final double CONSOLE_SPLIT_RATIO = 0.7; // 70% for editor, 30% for console

  private final FileManager fileManager;

  private Project project;
  private PyRunner currentPyRunner;

  private ProjectFileTree projectTree;
  private FileEditorTabs editorTabs;
  private PyEditorToolBar toolBar;
  private JSplitPane splitPane;
  private JSplitPane verticalSplitPane;
  private PythonConsole console;
  private JPanel consolePlaceholder;

  public PyEditor() {
    initUI();
    fileManager = new FileManager(this);
    fileManager.addFileStatusListener((file, modified) -> {
      editorTabs.setModified(file, modified);
      projectTree.markFileAsModified(file);
      updateToolbar();
    });
  }

  private void initUI() {
    setLayout(new BorderLayout());

    createToolBar();
    createMainContent();
    updateToolbar();
  }

  private void createToolBar() {
    toolBar = new PyEditorToolBar(this);
    // Add actions to the toolbar
    toolBar.addAction(new ProjectManageAction(this));
    toolBar.addAction(new ProjectCloseAction(this));
    toolBar.addAction(new ProjectConfigAction(this));

    toolBar.addSeparator();

    toolBar.addAction(new FileNewAction(this));
    toolBar.addAction(new FolderNewAction(this));
    toolBar.addAction(new FileDeleteAction(this));
    toolBar.addAction(new FileSaveAction(this));
    toolBar.addAction(new FileSaveAllAction(this));

    toolBar.addSeparator();

    toolBar.addAction(new ExecutionToggleAction(this));
    toolBar.addAction(new ConsoleToggleAction(this));
    toolBar.addAction(new ConsoleClearAction(this));

    toolBar.addSeparator();

    toolBar.addAction(new HelpAction(this));
    add(toolBar, BorderLayout.WEST);
  }

  /**
   * Updates the editor components after project changes.
   */
  public void updateToolbar() {
    SwingUtilities.invokeLater(() -> {
      if (toolBar != null) {
        toolBar.updateActions();
      }
    });
  }

  private void createMainContent() {
    projectTree = new ProjectFileTree(this);
    projectTree.setFileSelectionListener(new FileSelectionListener() {
      @Override
      public void onFileDoubleClicked(Path filePath) {
        if (Files.isRegularFile(filePath)) {
          openFile(filePath);
        }
      }

      @Override
      public void onFileSelected(Path filePath) {
        updateToolbar();
      }
    });
    // Wrap the tree in a scroll pane
    JScrollPane treeScrollPane = new JScrollPane(projectTree);
    treeScrollPane.setMinimumSize(new Dimension(MIN_TREE_WIDTH, 0));

    // Create tabbed panel
    editorTabs = new FileEditorTabs(this);
    // Add this PyEditor as a listener for file status events
    editorTabs.addFileStatusListener(this);

    // Create console component
    console = new PythonConsole();
    console.setVisible(false);
    // Console is hidden by default (use placeholder in split pane)
    consolePlaceholder = new JPanel();
    consolePlaceholder.setMinimumSize(new Dimension(0, 0));
    consolePlaceholder.setPreferredSize(new Dimension(0, 0));

    // Create vertical split pane for editor and console (start hidden)
    verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorTabs, consolePlaceholder);
    verticalSplitPane.setResizeWeight(1.0); // all space to editor by default
    verticalSplitPane.setDividerSize(0);    // hide divider while console hidden
    verticalSplitPane.setEnabled(false);

    // Create horizontal split pane for tree and editor+console
    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, verticalSplitPane);

    // Add component listener to set divider location after the component becomes visible
    addComponentListener(new DividerLocationAdapter());

    add(splitPane, BorderLayout.CENTER);
  }

  /**
   * Initializes the divider location based on the current component width.
   */
  private void initDividerLocation() {
    if (splitPane != null && getWidth() > 0) {
      int splitLocation = (int) (getWidth() * TREE_SPLIT_RATIO);
      splitPane.setDividerLocation(Math.max(MIN_TREE_WIDTH, splitLocation));
    }

    if (verticalSplitPane != null && verticalSplitPane.isShowing() && console != null && console.isVisible()) {
      int height = verticalSplitPane.getHeight();
      if (height > 0) {
        int splitLocation = (int) (height * CONSOLE_SPLIT_RATIO);
        verticalSplitPane.setDividerLocation(splitLocation);
      }
    }
  }

  /**
   * Gets the parent frame of this component.
   *
   * @return the parent frame, or null if not found
   */
  public Frame getParentFrame() {
    Component parent = this;
    while (parent != null && !(parent instanceof Frame)) {
      parent = parent.getParent();
    }
    return (Frame) parent;
  }

  /**
   * Toggles the visibility of the console panel.
   */
  public void toggleConsoleVisibility() {
    consoleVisible(!console.isVisible());
  }

  public void consoleVisible(boolean visible) {
    if (console == null || console.isVisible() == visible) {
      return;
    }

    if (visible) {
      verticalSplitPane.setBottomComponent(console);
      console.setVisible(true);
      verticalSplitPane.setDividerSize(6);
      verticalSplitPane.setEnabled(true);
      verticalSplitPane.setResizeWeight(CONSOLE_SPLIT_RATIO);
      initDividerLocation();
    } else {
      console.setVisible(false);
      verticalSplitPane.setBottomComponent(consolePlaceholder);
      verticalSplitPane.setDividerSize(0);
      verticalSplitPane.setEnabled(false);
      verticalSplitPane.setResizeWeight(1.0);
      verticalSplitPane.setDividerLocation(1.0);
    }
    verticalSplitPane.revalidate();
    verticalSplitPane.repaint();
  }

  public void appendConsoleText(String text, boolean isError) {
    if (console != null) {
      console.appendText(text, isError);
    }
  }

  /**
   * Clears the console text area.
   */
  public void clearConsole() {
    if (console != null) {
      console.clear();
    }
  }

  public void openProject(Project project) {
    if (this.project != null) {
      return; // We still have an open project
    }

    if (project != null) {
      project.updateLastUsed();
      var failure = initProjectInBackground(project);
      if (failure) {
        return;
      }
      try {
        ProjectIO.saveProject(project);
      } catch (IOException ignore) {
      }
      this.project = project;
      clearConsole();
      editorTabs.closeAllTabs();
      fileManager.clearModifiedFiles();
      projectUpdated();
    }
    updateToolbar();
  }

  public Project getProject() {
    return project;
  }

  public void closeProject() {
    if (hasUnsavedChanges()) {
      return;
    }
    editorTabs.closeAllTabs();
    fileManager.clearModifiedFiles();
    clearConsole();
    project = null;
    projectUpdated();
    updateToolbar();
  }

  private void projectUpdated() {// Update tree view with the new project
    if (project != null && project.getSrcDirectory() != null) {
      projectTree.updateProject(project, fileManager);
    } else {
      projectTree.updateProject(null, null);
    }

    // Update editor tabs with the new project
    editorTabs.setProject(project);
  }

  /**
   * Selects the node in the tree view that corresponds to the given file path.
   *
   * @param filePath the path of the file to select in the tree view
   */
  public void selectPathInTree(Path filePath) {
    if (filePath == null || projectTree == null) {
      return;
    }

    projectTree.selectNodeInTree(filePath);
  }

  /**
   * Called when a tab is selected.
   *
   * @param filePath the path of the file in the selected tab
   */
  @Override
  public void onFileSelected(Path filePath) {
    selectPathInTree(filePath);
  }

  /**
   * Called when a file is opened.
   *
   * @param filePath the path of the file that was opened
   */
  @Override
  public void onFileOpened(Path filePath) {
    if (projectTree != null) {
      projectTree.markFileAsOpened(filePath);
    }
    updateToolbar();
  }

  /**
   * Called when a file is closed.
   *
   * @param filePath the path of the file that was closed
   */
  @Override
  public void onFileClosed(Path filePath) {
    if (projectTree != null) {
      projectTree.markFileAsClosed(filePath);
    }
    updateToolbar();
  }

  public void openFile(Path newFilePath) {
    editorTabs.openFileInTab(newFilePath);
  }

  public boolean isOpened(Path filePath) {
    return editorTabs.isOpened(filePath);
  }

  private boolean initProjectInBackground(Project loadedProject) {
    clearConsole();
    final StringBuilder errorStringBuilder = new StringBuilder();
    AtomicBoolean hasError = new AtomicBoolean(false);
    BaseProgressUtils.showProgressDialogAndRun(handle -> {
      try {
        handle.progress("Setting up the Python environment");
        PythonEnvironment pyEnvironment = loadedProject.getPyEnvironment();
        handle.progress("Warming up the Python environment");
        CaptureStreamCallback errorListener = new ProgressAndErrorCallback(handle, errorStringBuilder);
        Map<String, String> options = PythonEnvironment.getDefaultOptions();
        options.put("python.VerboseFlag", "true");
        try (Context context = pyEnvironment.createContext(options, errorListener)) {
          Source warmUpCode = Source.newBuilder("python", "", "warmUpCode").build();
          context.eval(warmUpCode);
        }
      } catch (PolyglotException pe) {
        hasError.set(true);
        handleError(errorStringBuilder.toString());
      } catch (Exception e) {
        hasError.set(true);
        handleError(e.getMessage());
      } finally {
        handle.finish();
        if (hasError.get()) {
          toggleConsoleVisibility();
        }
      }
      return null;
    }, String.format("Initialising Python project '%s' (may take a minute or two)", loadedProject.getName()), true);
    return hasError.get();
  }

  private void handleError(String errorText) {
    SwingUtilities.invokeLater(() -> {
      Frame parentFrame = getParentFrame();
      console.setText(errorText, true);
      JOptionPane.showMessageDialog(parentFrame, "Failed to initialize Python environment. See console output.",
                                    "Error", JOptionPane.ERROR_MESSAGE);
    });
  }


  public void recreateProjectTree() {
    projectTree.rebuildFileTree();
  }

  public List<Path> closeTab(Path filePath) {
    return editorTabs.closeTab(filePath);
  }

  /**
   * Gets the currently selected file or folder in the tree view.
   *
   * @return the path of the selected file or folder, or null if nothing is selected
   */
  public Path getSelectedFile() {
    return projectTree != null ? projectTree.getSelectedFile() : null;
  }

  /**
   * Gets the currently selected files or folders in the tree view.
   *
   * @return the paths of the selected files or folders, or empty list if nothing is selected
   */
  public List<Path> getSelectedFiles() {
    return projectTree != null ? projectTree.getSelectedFiles() : Collections.emptyList();
  }

  /**
   * Gets the directory where a new file or folder should be created based on the current selection. If a file is
   * selected, returns its parent directory. If a directory is selected, returns that directory. If nothing is selected,
   * returns the project's source directory.
   *
   * @return the target directory for creating a new file or folder
   */
  public Path getCurrentTargetDirectory() {
    Path selectedDir = projectTree != null ? projectTree.getSelectedTargetDirectory() : null;

    // If no valid selection is found, default to the project's source directory
    if (selectedDir == null && project != null) {
      selectedDir = project.getSrcDirectory();
    }

    return selectedDir;
  }

  private void showNoProjectMessage() {
    Frame parentFrame = getParentFrame();
    JOptionPane.showMessageDialog(parentFrame, "Please create or open a project first.",
                                  "No Project", JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Checks if there are any unsaved files and prompts the user to save them.
   *
   * @return true if the operation should continue, false if it should be cancelled
   */
  public boolean hasUnsavedChanges() {
    return project != null && fileManager.hasModifiedFiles(); // No project, no unsaved changes
  }

  public String getModifiedText(Path filePath) {
    return editorTabs.getModifiedText(filePath);
  }

  public FileManager getFileManager() {
    return fileManager;
  }

  private static final class ProgressAndErrorCallback implements CaptureStreamCallback {

    private static final int MAX_WORKLOAD = 90;
    private final ProgressHandle handle;
    private final StringBuilder errorStringBuilder;
    int workUnit;

    private ProgressAndErrorCallback(ProgressHandle handle, StringBuilder errorStringBuilder) {
      this.handle = handle;
      this.errorStringBuilder = errorStringBuilder;
      this.workUnit = 0;
    }


    @Override
    public void onCapture(String text, boolean isError) {
      if (isError) {
        errorStringBuilder.append(text);
      }
      if (text.contains("\n")) {
        workUnit++;
        if (workUnit == 1) {
          handle.switchToDeterminate(MAX_WORKLOAD); // 82 or 86 was determined by counting once
        }
        if (workUnit <= MAX_WORKLOAD) {
          handle.progress(workUnit);
        }
      }
    }
  }

  private class DividerLocationAdapter extends ComponentAdapter {

    @Override
    public void componentShown(ComponentEvent e) {
      initDividerLocation();
    }

    @Override
    public void componentResized(ComponentEvent e) {
      if (isShowing()) {
        initDividerLocation();
      }
    }
  }

}
