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
import org.apache.commons.io.file.PathUtils;
import org.eomasters.eomtbx.pyeditor.Properties;
import org.eomasters.eomtbx.pyeditor.graalpy.GraalPy;

public class ProjectBuilder {

  private boolean initVenv = false;
  private Path pythonHome;
  private String name;
  private Path srcDir;
  private Path workingDir;
  private Path venvDir;
  private EditorTheme theme;

  public ProjectBuilder() {
  }

  private static Path getPythonExecutableFromPyHome(Path pythonHome) {
    if (!GraalPy.isGraalPyHome(pythonHome)) {
      throw new IllegalArgumentException("Not a valid GraalPython home: " + pythonHome);
    }
    return GraalPy.getPythonExecutable(pythonHome);
  }

  public ProjectBuilder name(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Project name cannot be null or empty");
    }
    this.name = name;
    return this;
  }

  public void theme(EditorTheme theme) {
    this.theme = theme;
  }

  public ProjectBuilder srcDirectory(Path srcDir) {
    this.srcDir = srcDir;
    return this;
  }

  public ProjectBuilder workingDirectory(Path workingDir) {
    this.workingDir = workingDir;
    return this;
  }

  public ProjectBuilder venvDirectory(Path venvDir) {
    this.venvDir = venvDir;
    return this;
  }

  public ProjectBuilder initVenv(boolean init) {
    this.initVenv = init;
    return this;
  }

  public ProjectBuilder pythonHome(Path pythonHome) {
    this.pythonHome = pythonHome;
    return this;
  }

  public Project build() throws IOException {
    Project project = new Project();
    initName(project);
    initTheme(project);

    // Create base directory if it doesn't exist
    var userProjectsDirectory = Properties.getUserProjectsDirectory();
    if (!Files.exists(userProjectsDirectory)) {
      Files.createDirectories(userProjectsDirectory);
    }
    try {
      initSrcDirectory(project);
      initVirtEnvironment(project);
      initWorkingDirectory(project);
    } catch (IOException e) {
      Path srcDir = project.getSrcDirectory();
      if (srcDir != null && Files.exists(srcDir)) {
        PathUtils.deleteDirectory(srcDir);
      }
      Path venvDir = project.getVenvDirectory();
      if (venvDir == null && Files.exists(venvDir)) {
        PathUtils.deleteDirectory(venvDir);
      }
      throw new IOException("Not able to setup project structure", e);
    }
    return project;
  }

  private void initName(Project project) {
    if (name == null) {
      throw new IllegalArgumentException("Project name must be given");
    }
    project.setName(name);
  }


  private void initTheme(Project project) {
    if (theme == null) {
      theme = EditorTheme.BRIGHT;
    }
    project.setTheme(theme);
  }

  private void initWorkingDirectory(Project project) throws IOException {
    if (workingDir != null && !Files.exists(workingDir)) {
      Files.createDirectories(workingDir);
      project.setWorkingDirectory(workingDir);
    } else {
      project.setWorkingDirectory(srcDir);
    }
  }

  private void initVirtEnvironment(Project project) throws IOException {
    if (venvDir == null) {
      throw new IllegalArgumentException("Virtual environment directory must be given");
    }
    if (initVenv) {
      if (!Files.exists(venvDir)) {
        Files.createDirectories(venvDir);
      }
      if (GraalPy.isGraalPyHome(pythonHome)) {
        // Clear the directory if it exists
        if (Files.exists(venvDir)) {
          PathUtils.deleteDirectory(venvDir);
          Files.createDirectories(venvDir);
        }

        Path pythonExe = getPythonExecutableFromPyHome(pythonHome);
        PythonEnvironment.runInitVenvProcess(pythonExe, venvDir);
        // installDefaultPackages(venvDir, handle);
      } else {
        throw new IllegalStateException(
            "Cannot initialize the virtual environment. Path '%s' is not a Graal Python home".formatted(pythonHome));
      }
    }
    project.setVenvDirectory(venvDir);
  }

  private void initSrcDirectory(Project project) throws IOException {
    if (srcDir == null) {
      throw new IllegalArgumentException("Source directory must be given");
    }
    if (!Files.exists(srcDir)) {
      Files.createDirectories(srcDir);
    }
    project.setSrcDirectory(srcDir);
  }

}
