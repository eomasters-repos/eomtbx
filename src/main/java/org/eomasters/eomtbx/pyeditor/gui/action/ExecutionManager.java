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

import java.nio.file.Path;
import java.util.HashMap;
import org.eomasters.eomtbx.pyeditor.pyrun.Project;
import org.eomasters.eomtbx.pyeditor.pyrun.PyRunner;
import org.eomasters.eomtbx.pyeditor.pyrun.PyRunnerException;
import org.eomasters.eomtbx.pyeditor.pyrun.PythonException;
import org.eomasters.eomtbx.utils.CaptureStreamCallback;

class ExecutionManager {

  private static final HashMap<Project, PyRunner> PROJECT_RUNNERS = new HashMap<>();

  public static boolean isExecuting(Project project) {
    return PROJECT_RUNNERS.containsKey(project) && PROJECT_RUNNERS.get(project).isRunning();
  }

  public static boolean cancel(Project project) {
    if (PROJECT_RUNNERS.containsKey(project)) {
      PROJECT_RUNNERS.get(project).stop();
      return true;
    }
    return false;
  }

  public static void execute(Project project, Path scriptFile) throws PyRunnerException, PythonException {
    ExecutionManager.execute(project, scriptFile, null);
  }

  public static void execute(Project project, Path scriptFile, CaptureStreamCallback callback) throws PyRunnerException, PythonException {
    var pyRunner = PROJECT_RUNNERS.computeIfAbsent(project, PyRunner::new);
    pyRunner.run(scriptFile, callback);
  }

  public static void remove(Project project) {
    if (cancel(project)) {
      PROJECT_RUNNERS.remove(project);
    }
  }


}

