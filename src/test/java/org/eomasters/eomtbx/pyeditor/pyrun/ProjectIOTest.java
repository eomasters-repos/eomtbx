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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.eomasters.eomtbx.pyeditor.pyrun.PackageManager.Package;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectIOTest {

  @TempDir
  Path tempDir;

  @Test
  void testSaveAndLoadProject() throws IOException {
    // Create a test project
    Project project = new Project();
    project.setName("Test Project");
    project.setSrcDirectory(Path.of("C:\\projects\\test\\src"));
    project.setVenvDirectory(Path.of("C:\\projects\\test\\venv"));
    project.setPackages(List.of(new Package("numpy", "1.3.1"), new Package("pandas", "1.5.1")));

    // Save the project to a temporary file
    Path projectFile = tempDir.resolve("test-project.json");
    ProjectIO.saveProject(project, projectFile);

    var jsonString = Files.readString(projectFile);
    System.out.println(jsonString);

    // Verify the file exists
    assertTrue(Files.exists(projectFile), "Project file should exist");

    // Load the project from the file
    Project loadedProject = ProjectIO.loadProject(projectFile);

    // Verify the loaded project matches the original
    assertEquals(project.getModelVersion(), loadedProject.getModelVersion(), "Project model version should match");
    assertEquals(project.getName(), loadedProject.getName(), "Project name should match");
    assertEquals(project.getSrcDirectory().toString(), loadedProject.getSrcDirectory().toString(),
                 "Source directory should match");
    assertEquals(project.getVenvDirectory().toString(), loadedProject.getVenvDirectory().toString(),
                 "Virtual environment directory should match");
    assertEquals(project.getPackages(), loadedProject.getPackages(), "Packages should match");
  }

  @Test
  void testLoadNonExistentFile() {
    Path nonExistentFile = tempDir.resolve("non-existent.json");
    assertThrows(IOException.class, () -> ProjectIO.loadProject(nonExistentFile),
                 "Loading a non-existent file should throw IOException");
  }

  @Test
  void testNullValues() throws IOException {
    // Create a project with null values
    Project project = new Project();
    project.setName("Null Test");
    project.setSrcDirectory(null);
    project.setVenvDirectory(null);

    // Save and load the project
    Path projectFile = tempDir.resolve("null-test.json");
    ProjectIO.saveProject(project, projectFile);
    Project loadedProject = ProjectIO.loadProject(projectFile);

    assertTrue(loadedProject.getPackages().isEmpty(), "Packages should be empty");
    assertEquals(1, loadedProject.getModelVersion(), "Project model version should match");
    assertEquals("Null Test", loadedProject.getName(), "Project name should match");
    // null values should be preserved
    assertNull(loadedProject.getSrcDirectory(), "Source directory should be null");
    assertNull(loadedProject.getVenvDirectory(), "Virtual environment directory should be null");
  }

  @Test
  void testTransientPropertiesNotSerialized() throws IOException {
    // Create a project with transient properties set
    Project project = new Project();
    project.setName("Transient Test");
    project.setSrcDirectory(Path.of("C:\\projects\\test\\src"));

    // Save and load the project
    Path projectFile = tempDir.resolve("transient-test.json");
    ProjectIO.saveProject(project, projectFile);

    // Read the JSON content to verify transient properties are not included
    String jsonContent = Files.readString(projectFile);
    assertFalse(jsonContent.contains("isDirty"), "JSON should not contain isDirty property");
    assertFalse(jsonContent.contains("editedFiles"), "JSON should not contain editedFiles property");
  }
}
