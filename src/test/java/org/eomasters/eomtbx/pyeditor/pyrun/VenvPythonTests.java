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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.io.file.PathUtils;
import org.eomasters.eomtbx.EomtbxRuntime;
import org.eomasters.eomtbx.pyeditor.graalpy.GraalPy;
import org.eomasters.eomtbx.pyeditor.snapkit.DataTransfer;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@EnabledIfSystemProperty(named = "junit.runPythonTests", matches = "true")
public class VenvPythonTests extends PythonTests {

  @TempDir
  private static Path venvDir;
  private static Context context;

  @BeforeAll
  public static void setup() throws PyRunnerException, IOException, InterruptedException {
    var pyHome = getPyHome();
    var pyExe = GraalPy.getPythonExecutable(pyHome);
    PythonEnvironment.runInitVenvProcess(pyExe, venvDir);
    var pma = PackageManager.create(venvDir);
    if (EomtbxRuntime.IS_LINUX) {
      pma.installPackage("art", "numpy");
    } else {
      pma.installPackage("art");
    }
    context = createContext(pyHome, venvDir);
  }

  static Stream<String> provideRelTestFilePaths() {
    List<String> list = new ArrayList<>();
    List<String> generalTests = List.of(
        "tests/venv/AsciiArtTest.py"
    );
    List<String> linuxTests = List.of(
        "tests/venv/AddNumpyBandToProduct.py",
        "tests/venv/DataTypeTransferNumpyTests.py"
    );
    list.addAll(generalTests);
    if (EomtbxRuntime.IS_LINUX) {
      list.addAll(linuxTests);
    }
    return list.stream();
  }

  @ParameterizedTest
  @MethodSource("provideRelTestFilePaths")
  void testPythonScripts(String relFilePath) throws IOException, URISyntaxException {
    executeTests(context, getPythonFilePath(relFilePath));
  }

  @Test
  void test_DataTransfer_validateNumElements() throws IOException {
    Path tempDirectory = Files.createTempDirectory("testValidation");
    try {
      Value arr;
      String language = "python";

      if (EomtbxRuntime.IS_LINUX) {
        context.eval(language, """
                               import numpy as np
                               a = np.array([1, 2, 3])
                               """);
        arr = context.getBindings(language).getMember("a");
        DataTransfer.validateNumElements(arr, 3);
      }

      context.eval(language, """
                             import array
                             a = array.array('i', [1, 2, 3])
                             """);
      arr = context.getBindings(language).getMember("a");
      DataTransfer.validateNumElements(arr, 3);

      context.eval(language, """
                             a = [1, 2, 3]
                             """);
      arr = context.getBindings(language).getMember("a");
      DataTransfer.validateNumElements(arr, 3);
    } finally {
      PathUtils.deleteDirectory(tempDirectory, PathUtils.noFollowLinkOptionArray());
    }
  }

}
