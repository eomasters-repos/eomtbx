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

public class InitVenvTestMain {

  public static void main(String[] args) throws IOException {
    var directory = Files.createTempDirectory("venvTest");
    System.out.println("Directory: " + directory);

    try {
      var pythonVenv = directory.resolve("pythonVenv");
      Files.createDirectory(pythonVenv);
      PythonEnvironment.runInitVenvProcess(
          Path.of("C:\\Users\\marco\\AppData\\Local\\Programs\\Python\\Python39\\python.exe"), pythonVenv);

      var graalVenv = directory.resolve("graalVenv");
      Files.createDirectory(graalVenv);
      PythonEnvironment.runInitVenvProcess(
          Path.of("C:\\Users\\marco\\.snap\\auxdata\\eomtbx\\graalpy\\bin\\python.exe"), graalVenv);

      System.out.println("STOP");
    } finally {
      PathUtils.deleteDirectory(directory);
    }

  }

}
