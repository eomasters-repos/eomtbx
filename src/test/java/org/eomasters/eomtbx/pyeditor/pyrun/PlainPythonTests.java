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
import java.util.stream.Stream;
import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@EnabledIfSystemProperty(named = "junit.runPythonTests", matches = "true")
public class PlainPythonTests extends PythonTests {

  private static Context context;

  @BeforeAll
  public static void setup() throws PyRunnerException {
    context = createContext(getPyHome(), apiDir);
    context.getBindings("python").putMember("passed_java_array", new int[]{1, 2, 3});
  }

  static Stream<String> provideRelTestFilePaths() {
    return Stream.of(
        "tests/plain/SpeedTests.py",
        "tests/plain/PythonBehaviorTests.py",
        "tests/plain/UtilsTests.py",
        "tests/plain/ProductIOTests.py",
        "tests/plain/OperatorDescriptionTests.py",
        "tests/plain/RasterDataTransferTests.py",
        "tests/plain/RasterTests.py",
        "tests/plain/AddBandToProduct.py",
        "tests/plain/DataTypeTransferTests.py",
        "tests/plain/SnapTests.py"
        );
  }

  @ParameterizedTest
  @MethodSource("provideRelTestFilePaths")
  void testPython(String relFilePath) throws IOException, URISyntaxException {
    executeTests(context, getPythonFilePath(relFilePath));
  }

}
