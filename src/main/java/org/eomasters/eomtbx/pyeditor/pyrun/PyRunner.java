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

package org.eomasters.eomtbx.pyeditor.pyrun;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eomasters.eomtbx.pyeditor.Properties;
import org.eomasters.eomtbx.utils.CaptureStreamCallback;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;

/**
 * PyRunner is responsible for managing the lifecycle and execution of a Python project's main entry point via a
 * configured Python environment. It handles executing the Python script, ensuring thread-safe synchronization during
 * the execution, and provides utilities to monitor or interrupt the running process.
 */
public class PyRunner {

  private final Project project;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private volatile Context currentContext;

  /**
   * Constructs a new PyRunner instance associated with a specific project.
   *
   * @param project the project to associate with this PyRunner; must not be null
   * @throws IllegalArgumentException if the provided project is null
   */
  public PyRunner(Project project) {
    if (project == null) {
      throw new IllegalArgumentException("Project cannot be null");
    }
    this.project = project;
  }

  public static boolean isExecutableScript(Path selectedFile) {
    return selectedFile != null && Files.isRegularFile(selectedFile) && selectedFile.getFileName()
                                                                                    .toString()
                                                                                    .toLowerCase()
                                                                                    .endsWith(".py");
  }

  /**
   * Executes a Python script using the configured Python environment. This method uses the default execution context
   * and does not capture standard output or error streams.
   *
   * @param executeFile the file path to the Python script to be executed; must not be null
   * @throws PyRunnerException if a concurrent execution is attempted or if any other runtime error occurs
   * @throws PythonException   if an error occurs during the execution of the Python code
   */
  public void run(Path executeFile) throws PyRunnerException, PythonException {
    run(executeFile, null);
  }

  /**
   * Executes a Python script using the configured Python environment and captures the script's output. This method
   * ensures that only one execution is active at a time by using an atomic flag. If an execution is already in
   * progress, a {@code PyRunnerException} is thrown. The method initializes a context for the Python environment, runs
   * the specified Python script, and invokes the provided callback to capture standard output and error streams.
   *
   * @param executeFile the file path to the Python script to be executed; must not be null
   * @param callback    the callback to capture standard output and error streams; can be null if no capturing is
   *                    needed
   * @throws PyRunnerException if a concurrent execution is attempted or if any other runtime error occurs
   * @throws PythonException   if an error occurs during the execution of the Python code
   */
  public void run(Path executeFile, CaptureStreamCallback callback) throws PyRunnerException, PythonException {
    // Check if already running and prevent concurrent execution
    if (!running.compareAndSet(false, true)) {
      throw new PyRunnerException(
          "PyRunner is already running. Cannot start another execution while one is in progress.");
    }

    var pyEnv = project.getPyEnvironment();
    try (Context context = pyEnv.createContext(PythonEnvironment.getDefaultOptions(), callback)) {
      this.currentContext = context;
      // only need to evaluate the main entry point.
      Source source = Source.newBuilder("python", executeFile.toFile()).build();
      context.eval(source);
    } catch (PolyglotException pe) {
      if (pe.isCancelled()) {
        var runnerException = new PyRunnerException("Execution canceled.");
        runnerException.setCanceled(true);
        throw runnerException;
      }
      throw new PythonException(pe);
    } catch (Throwable e) {
      throw new PyRunnerException(e);
    } finally {
      this.currentContext = null;
      running.set(false);
    }
  }

  /**
   * Stops the currently running execution context, if any.
   * <p>
   * This method attempts to interrupt the ongoing context execution immediately. Any ongoing execution should handle
   * the interruption appropriately.
   * <p>
   * This method is a no-op if there is no active execution.
   */
  public void stop() {
    Context context = currentContext;
    if (context != null) {
      context.close(true);
    }
    currentContext = null;
  }

  /**
   * Checks if the PyRunner is currently executing.
   *
   * @return true if the runner is currently executing, false otherwise
   */
  public boolean isRunning() {
    return running.get();
  }

  public static void main(String[] args) throws IOException, PyRunnerException, PythonException {
    Properties.setApiDir(Path.of("C:\\Users\\marco\\.snap\\auxdata\\eomtbx\\PyEditor\\1.5.0\\PyEditor_api"));
    Properties.setPythonHome(Path.of("C:\\Users\\marco\\.snap\\auxdata\\eomtbx\\graalpy"));
    var project = ProjectIO.loadProject(Path.of("C:\\Users\\marco\\snap_code\\AddBand\\AddBand.scp"));
    CaptureStreamCallback callback = (line, isError) -> {
      if (isError) {
        System.err.print(line);
        System.err.flush();
      } else {
        System.out.print(line);
        System.out.flush();
      }
    };
    var pyRunner = new PyRunner(project);
    pyRunner.run(Path.of("C:\\Users\\marco\\snap_code\\AddBand\\src\\asciiArt.py"), callback);
  }

}
