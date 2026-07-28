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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eomasters.eomtbx.pyeditor.gui.PyEditor;
import org.eomasters.eomtbx.pyeditor.pyrun.Project;
import org.eomasters.eomtbx.pyeditor.pyrun.PyRunner;
import org.eomasters.eomtbx.pyeditor.pyrun.PyRunnerException;
import org.eomasters.eomtbx.pyeditor.pyrun.PythonException;
import org.eomasters.eomtbx.utils.CaptureStreamCallback;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Toggleable action for starting/stopping Python execution.
 * Dynamically switches between start and stop modes based on execution state.
 */
public class ExecutionToggleAction extends AbstractEditorAction {

  private static final FontIcon START_ICON = FontIcon.of(MaterialDesignP.PLAY, ICON_SIZE, Color.GREEN.darker());
  private static final FontIcon STOP_ICON = FontIcon.of(MaterialDesignS.STOP, ICON_SIZE, Color.RED.darker());
  private static final String DESCRIPTION_RUN = "Run the current Python file";
  private static final String NAME_RUN = "Run";
  private static final String NAME_STOP = "Stop";
  private static final String DESCRIPTION_STOP = "Stop the currently running Python execution";
  private final ExecutorService executor;

  /**
   * Creates a new ExecutionToggleAction with the specified editor.
   *
   * @param editor the PyEditor instance
   */
  public ExecutionToggleAction(PyEditor editor) {
    super(editor, NAME_RUN, DESCRIPTION_RUN, START_ICON);
    setEnabled(false);
    executor = Executors.newSingleThreadExecutor();
  }

  @Override
  public boolean isEnabled() {
    var project = getEditor().getProject();
    if (project == null) {
      return false;
    }
    
    if (ExecutionManager.isExecuting(project)) {
      return true; // Always enabled when executing (to allow stopping)
    } else {
      // Only enabled when not executing and we have an executable script
      return PyRunner.isExecutableScript(getEditor().getSelectedFile());
    }
  }

  @Override
  public void updateState() {
    var project = getEditor().getProject();
    boolean isExecuting = project != null && ExecutionManager.isExecuting(project);
    
    if (isExecuting) {
      // Switch to stop mode
      putValue(Action.NAME, NAME_STOP);
      putValue(Action.SHORT_DESCRIPTION, DESCRIPTION_STOP);
      setIcon(STOP_ICON);
    } else {
      // Switch to start mode
      putValue(Action.NAME, NAME_RUN);
      putValue(Action.SHORT_DESCRIPTION, DESCRIPTION_RUN);
      setIcon(START_ICON);
    }
    
    super.updateState();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    var project = getEditor().getProject();
    if (project == null) {
      return;
    }

    if (ExecutionManager.isExecuting(project)) {
      // Stop execution
      ExecutionManager.cancel(project);
    } else {
      // Start execution
      executeAsync(project, getEditor().getSelectedFile());
    }
    getEditor().updateToolbar();
  }

  private void executeAsync(Project project, Path selectedFile) {
    CaptureStreamCallback callback = new PythonCaptureListener(getEditor());

    getEditor().clearConsole();
    getEditor().consoleVisible(true);
    executor.execute(() -> {
      try {
        ExecutionManager.execute(project, selectedFile, callback);
      } catch (PythonException e) {
        handleExecutionError("<html>Error in Python code: " + e.getMessage() + "\nSee console for details.");
      } catch (PyRunnerException e) {
        if (e.isCanceled()) {
          handleExecutionError("Script execution was canceled by the user.");
        } else {
          handleExecutionError("Error while executing Python. See console for details.",
                               ExceptionUtils.getStackTrace(e));
        }
      } catch (Throwable e) {
        handleExecutionError(e.getMessage(), ExceptionUtils.getStackTrace(e));
      } finally {
        SwingUtilities.invokeLater(() -> getEditor().updateToolbar());
      }
    });
  }

  private void handleExecutionError(String message) {
    handleExecutionError(message, null);
  }

  private void handleExecutionError(String message, String stackTrace) {
    SwingUtilities.invokeLater(() -> {
      if (stackTrace != null) {
        getEditor().appendConsoleText(stackTrace, true);
      }
      getEditor().consoleVisible(true);
      JOptionPane.showMessageDialog(getEditor(), message, "Error", JOptionPane.ERROR_MESSAGE);
      getEditor().updateToolbar();
    });
  }

  /**
   * A listener that captures output from Python execution and displays it in the console.
   */
  private record PythonCaptureListener(PyEditor editor) implements CaptureStreamCallback {

    @Override
    public void onCapture(String text, boolean isError) {
      SwingUtilities.invokeLater(() -> {
        editor.appendConsoleText(text, isError);
      });
    }

  }
}
