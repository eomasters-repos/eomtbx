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

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;
import javax.swing.event.EventListenerList;
import org.eomasters.eomtbx.pyeditor.pyrun.PackageManager.Package;


/**
 * Represents a software project, encapsulating properties such as the project's name, type, source directory, virtual
 * environment directory, Python home, and library directory. This class provides getter and setter methods for each
 * property, enforcing validation rules where applicable.
 * <p>
 * The class is designed to serve as the primary data structure for working with Python-based projects in a software
 * ecosystem, allowing manipulation of project metadata and environment configurations.
 */
public class Project {

  @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
  private int modelVersion = 1;
  private String name;
  private Path srcDirectory;
  private Path venvDirectory;
  private Path workingDirectory;
  private LocalDateTime lastUsed;
  private EditorTheme theme = EditorTheme.DARK;
  private List<Package> packages = List.of();

  // Transient properties that should not be serialized
  private final transient EventListenerList eventListenerList = new EventListenerList();

  private transient Path projectFile;
  private transient PythonEnvironment pyEnvironment;

  Project() {}

  /**
   * Retrieves the version of the project model.
   *
   * @return the version of the project model
   */
  public int getModelVersion() {
    return modelVersion;
  }

  /**
   * Retrieves the name of the project.
   *
   * @return the name of the project as a String
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the project. The provided name cannot be null or blank.
   *
   * @param name the name to set for the project
   * @throws IllegalArgumentException if the name is null or blank
   */
  public void setName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Project name cannot be null or blank");
    }
    this.name = name;
  }

  public void setPackages(List<Package> packages) {
    this.packages = packages;
  }

  public List<Package> getPackages() {
    return packages;
  }

  public Path getProjectFile() {
    return projectFile;
  }

  public void setProjectFile(Path filePath) {
    this.projectFile = filePath;
  }

  public PythonEnvironment getPyEnvironment() {
    if (pyEnvironment == null) {
      pyEnvironment = new PythonEnvironment(this);
      try {
        pyEnvironment.init();
      } catch (PyRunnerException e) {
        throw new RuntimeException(e);
      }
    }
    return pyEnvironment;
  }

  public Path getSrcDirectory() {return srcDirectory;}

  public void setSrcDirectory(Path srcDirectory) {
    this.srcDirectory = srcDirectory;
  }

  public Path getVenvDirectory() {
    return venvDirectory;
  }

  public void setVenvDirectory(Path venvDirectory) {
    this.venvDirectory = venvDirectory;
  }

  public Path getWorkingDirectory() {
    return workingDirectory == null ? getSrcDirectory() : workingDirectory;
  }

  public void setWorkingDirectory(Path workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  /**
   * Retrieves the timestamp when the project was last used.
   *
   * @return the timestamp when the project was last used
   */
  public LocalDateTime getLastUsed() {
    return lastUsed;
  }

  /**
   * Updates the lastUsed timestamp to the current time.
   */
  public void updateLastUsed() {
    this.lastUsed = LocalDateTime.now();
  }

  public EditorTheme getTheme() {
    return theme;
  }

  public void setTheme(EditorTheme theme) {
    if (theme == this.theme) {
      return;
    }
    this.theme = Objects.requireNonNullElse(theme, EditorTheme.DARK);
    fireThemeChanged();
  }

  public void addChangeListener(ChangeListener listener) {
    eventListenerList.add(ChangeListener.class, listener);
  }

  public void removeChangeListener(ChangeListener listener) {
    eventListenerList.remove(ChangeListener.class, listener);
  }

  private void fireThemeChanged() {
    var listeners = eventListenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ChangeListener.class) {
        ((ChangeListener) listeners[i + 1]).themeChanged(this.theme);
      }
    }
  }


  public static class ChangeListener implements EventListener {

    public void themeChanged(EditorTheme theme) {}

  }


}
