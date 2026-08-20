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

import static org.eomasters.eomtbx.pyeditor.pyrun.PythonEnvironment.getPythonExecutableFromVenv;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.eomasters.eomtbx.pyeditor.graalpy.GraalPy;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Context.Builder;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.io.IOAccess;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
public abstract class PythonTests {

  @TempDir
  static Path apiDir;

  static File getPythonFilePath(String name) throws URISyntaxException {
    return new File(PythonTests.class.getResource(name).toURI());
  }

  static Context createContext(Path pyHome, Path venvDir) throws PyRunnerException {
    return createContext(pyHome, venvDir, apiDir);
  }

  public static Context createContext(Path pyHome, Path venvDir, Path apiDir) throws PyRunnerException {
    try {
      Builder builder = Context.newBuilder("python")
                               .out(new PrintStream(System.out))
                               .err(new PrintStream(System.err))
                               // allows everything - actually allows importing of java packages
                               .allowAllAccess(true)
                               // allows python to access the java language
                               .allowHostAccess(HostAccess.ALL)
                               // allow class loading from the host
                               .allowHostClassLoading(true)
                               .allowHostClassLookup(s -> true)
                               // allow creating python threads
                               .allowCreateThread(true)
                               // allow running Python native extensions
                               .allowNativeAccess(true)
                               .allowPolyglotAccess(PolyglotAccess.ALL)
                               // allow all IO access
                               .allowIO(IOAccess.ALL);

      // Where are the API sources
      PythonEnvironment.unpackAPI(apiDir);
      // Set the python home;
      builder.option("python.PythonHome", pyHome.toString());
      if (venvDir != null) {
        builder.option("python.Executable", getPythonExecutableFromVenv(venvDir).toString());
        builder.option("python.ForceImportSite", "true");
      }
      builder.option("python.PythonPath", apiDir.toString());
      // Emulate some Jython features - e.g., allow importing of not 'java' package
      builder.option("python.EmulateJython", "true");
      // suppress the "JVMCI is not enabled for this JVM" warning
      builder.option("python.WarnExperimentalFeatures", "false");
      builder.option("engine.WarnInterpreterOnly", "false");

      return builder.build();
    } catch (Throwable e) {
      throw new PyRunnerException(e);
    }
  }

  public static Path getPyHome() {
    var property = System.getProperty("junit.graalpy.home");
    Assumptions.assumeTrue(property != null, "junit.graalpy.home system property must be set");
    var pyHome = Path.of(property);
    Assumptions.assumeTrue(GraalPy.isGraalPyHome(pyHome), "Path must point to a GraalPy home");
    return pyHome;
  }

  protected static void executeTests(Context context, File pythonFile) throws IOException {
    Source source = Source.newBuilder("python", pythonFile).build();
    try {
      System.out.println("++++ Running " + pythonFile.getName());
      context.eval(source);
      System.out.println("++++ Passed " + pythonFile.getName());
    } catch (PolyglotException e) {
      System.err.println(PolyglotUtils.formatPolyglotException(e));
      fail("++++ Failed " + pythonFile.getName(), e);
    } finally {
      System.out.println();
    }
  }
}
