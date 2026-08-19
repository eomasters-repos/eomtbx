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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "junit.runPythonTests", matches = "true")
class PolyglotValueTest extends PythonTests {

  private static Context context;

  @BeforeAll
  public static void setup() throws PyRunnerException {
    context = createContext(getPyHome(), null);
  }

  @Test
  void testPython() throws IOException {
    var script = """
                 import array
                 # python_array = [1, 2, 300, 400, 500, 10000, 100_000]
                 python_array = array.array('i', [1, 2, 300, 400, 500, 10000, 100_000])
                 """;

    context.eval(Source.newBuilder("python", script, "test").build());

    Value pythonArray = context.getBindings("python").getMember("python_array");
    assertTrue(pythonArray.hasArrayElements());
    assertEquals(28, pythonArray.getArraySize());
    String typeCode = pythonArray.getMember("typecode").asString();
    System.out.println("Array typecode: " + typeCode);

    byte[] rawBytes = pythonArray.as(byte[].class);

    // Wrap in ByteBuffer and set correct byte order
    ByteBuffer buffer = ByteBuffer.wrap(rawBytes).order(ByteOrder.nativeOrder());
    assertEquals(1, buffer.getInt());
    assertEquals(2, buffer.getInt());
    assertEquals(300, buffer.getInt());
    assertEquals(400, buffer.getInt());
    assertEquals(500, buffer.getInt());
    assertEquals(10000, buffer.getInt());
    assertEquals(100_000, buffer.getInt());
  }

}
