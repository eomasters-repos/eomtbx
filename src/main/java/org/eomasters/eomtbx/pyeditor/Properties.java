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

package org.eomasters.eomtbx.pyeditor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;
import org.eomasters.eomtbx.EomToolbox;
import org.eomasters.eomtbx.pyeditor.pyrun.EditorTheme;

public class Properties {

  public final static Preferences PYEDITOR_PREFERENCES = EomToolbox.getPreferences().node("pyeditor");

  // GUI Options
  public static final String PROP_EDITOR_THEME = "editorTheme";
  public static final String PROP_PYTHON_HOME = "pythonHome";
  public static final String PROP_PROJECTS_DIRECTORY = "projectsDir";
  private static final String USER_PROJECTS_DIR_DEFAULT_VALUE = System.getProperty(PROP_PROJECTS_DIRECTORY,
                                                                                   System.getProperty("user.home"));
  public static final Path PROP_USER_PROJECTS_DIRECTORY = Paths.get(USER_PROJECTS_DIR_DEFAULT_VALUE, "snap_code");

  // VM Options
  private static final String PYCODE_APIDIR_PROPERTY = "eomasters.pyEditor.apiDir";
  private static final String PYEDITOR_PROJECTS_ALL_PACKAGES = "eomasters.pyEditor.allowAllJavaPackages";
  private static final String PYTHON_VERBOSE_FLAG = "eomasters.pyEditor.verbose";

  private Properties() {}

  public static Path getPythonHome() {
    var pythonPath = PYEDITOR_PREFERENCES.get(PROP_PYTHON_HOME, "");
    if (!pythonPath.isBlank()) {
      return Path.of(pythonPath);
    } else {
      return null;
    }
  }

  public static void setPythonHome(Path pyhome) {
    String pathString = (pyhome != null && Files.isDirectory(pyhome)) ? pyhome.toAbsolutePath().toString() : "";
    PYEDITOR_PREFERENCES.put(PROP_PYTHON_HOME, pathString);
  }

  public static Path getUserProjectsDirectory() {
    return Path.of(PYEDITOR_PREFERENCES.get(PROP_PROJECTS_DIRECTORY, PROP_USER_PROJECTS_DIRECTORY.toString()));
  }

  public static void setUserProjectsDirectory(Path dir) {
    PYEDITOR_PREFERENCES.put(PROP_PROJECTS_DIRECTORY, dir.toAbsolutePath().toString());
  }

  public static EditorTheme getTheme() {
    return EditorTheme.valueOf(PYEDITOR_PREFERENCES.get(PROP_EDITOR_THEME, EditorTheme.DARK.name()));
  }

  public static void setTheme(EditorTheme theme) {
    PYEDITOR_PREFERENCES.put(PROP_EDITOR_THEME, theme.name());
  }

  public static boolean isAllPackagesEnabled() {
    return PYEDITOR_PREFERENCES.get(PYEDITOR_PROJECTS_ALL_PACKAGES, "true").equals("true");
  }

  public static Path getApiDir() {
    var stringPath = System.getProperty(PYCODE_APIDIR_PROPERTY, "");
    if (stringPath.isBlank()) {
      return null;
    }
    return Path.of(stringPath);
  }

  public static void setApiDir(Path path) {
    System.setProperty(PYCODE_APIDIR_PROPERTY, path.toAbsolutePath().toString());
  }

  public static boolean isVerbose() {
    return Boolean.getBoolean(PYTHON_VERBOSE_FLAG);
  }

  public static void setVerbose(boolean verbose) {
    System.setProperty(PYTHON_VERBOSE_FLAG, String.valueOf(verbose));
  }
}
