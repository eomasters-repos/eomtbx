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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eomasters.eomtbx.pyeditor.pyrun.Project;
import org.eomasters.eomtbx.pyeditor.pyrun.ProjectBuilder;
import org.junit.jupiter.api.Test;

/**
 * Tests for the NewProjectDialog class. Note: These tests don't open the actual dialog since it requires user
 * interaction. Instead, they test the underlying functionality.
 */
class NewProjectDialogTest {

  /**
   * Test project path generation.
   */
  @Test
  void testProjectPathGeneration() throws IOException {
    String projectName = "TestProject";
    Path baseDir = Paths.get(System.getProperty("user.home"), "snap_code");

    // Expected paths
    Path expectedProjectDir = baseDir.resolve(projectName);
    Path expectedSrcDir = expectedProjectDir.resolve("src");
    Path expectedVenvDir = expectedProjectDir.resolve("venv");

    // Create a project with the test name
    var projectBuilder = new ProjectBuilder();
    projectBuilder.name(projectName);
    projectBuilder.srcDirectory(expectedSrcDir);
    projectBuilder.venvDirectory(expectedVenvDir);
    Project project = projectBuilder.build();

    assertEquals(expectedSrcDir.toString(),
                 project.getSrcDirectory().toString(),
                 "Source directory should be {baseDir}/{projectName}/src");
    assertEquals(expectedVenvDir.toString(),
                 project.getVenvDirectory().toString(),
                 "Venv directory should be {baseDir}/{projectName}/venv");
  }

}
