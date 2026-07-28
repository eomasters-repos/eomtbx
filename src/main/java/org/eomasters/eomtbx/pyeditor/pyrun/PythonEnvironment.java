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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import org.apache.commons.io.file.PathUtils;
import org.eomasters.eomtbx.EomtbxRuntime;
import org.eomasters.eomtbx.pyeditor.Properties;
import org.eomasters.eomtbx.utils.CapturePrintStream;
import org.eomasters.eomtbx.utils.CaptureStreamCallback;
import org.eomasters.eomtbx.utils.FileUtils;
import org.graalvm.options.OptionDescriptor;
import org.graalvm.options.OptionDescriptors;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Context.Builder;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.io.IOAccess;
import org.openide.modules.Modules;

public class PythonEnvironment {

  private static final String PYVENV_CFG_FILE = "pyvenv.cfg";
  private static final String SNAPKIT_PACKAGE_NAME = "snapkit";
  private static final ConcurrentMap<Path, Engine> ENGINES_BY_VENV = new ConcurrentHashMap<>();
  private static final String PYEDITOR = "PyEditor";

  private final Project project;

  public PythonEnvironment(Project project) {
    this.project = project;
  }

  public static void releaseResources() {
    ENGINES_BY_VENV.values().forEach(e -> {
      try {
        e.close();
      } catch (Exception ignore) {
      }
    });
    ENGINES_BY_VENV.clear();
  }

  @SuppressWarnings("unused")
  private static void dumpOptions(OptionDescriptors engineOptions) {
    for (OptionDescriptor next : engineOptions) {
      System.out.println("Name: " + next.getName());
      System.out.println("Default: " + next.getKey().getDefaultValue());
      System.out.println("Category: " + next.getCategory());
      System.out.println("Usage: " + next.getUsageSyntax());
      System.out.println("Help: " + next.getHelp());
      System.out.println("Stability: " + next.getStability().toString());
      System.out.println("Deprecation: " + next.getDeprecationMessage());
      System.out.println("==========================");
    }
  }

  public static Map<String, String> getDefaultOptions() {
    Map<String, String> options = new HashMap<>();
    // Force to automatically import site.py module, to make Python packages available
    options.put("python.ForceImportSite", "true");
    // Emulate some Jython features - e.g., allow importing of not 'java' package
    options.put("python.EmulateJython", "true");
    // choose the backend for the POSIX module
    options.put("python.PosixModuleBackend", "java");
    // equivalent to the Python -B flag
    options.put("python.DontWriteBytecodeFlag", "true");
    // equivalent to the Python -v flag
    var verbosePython = Properties.isVerbose();
    options.put("python.VerboseFlag", String.valueOf(verbosePython));
    // log level
    options.put("log.python.level", verbosePython ? "FINE" : "SEVERE");
    // log level for engine
    options.put("log.engine.level", verbosePython ? "FINE" : "SEVERE");
    // log warning when using capi
    options.put("log.python.capi.level", "WARNING");
    options.put("python.WarnExperimentalFeatures", "false");
    // print Python exceptions directly
    options.put("python.AlwaysRunExcepthook", "true");
    // Eliminates debugging-related function call overhead during array element iteration
    // Memory efficiency: Saves memory by not loading debugging infrastructure
    // Faster execution: Removes debugging checks that occur during Python loops (like in _convert_data())
    // options.put("python.EnableDebuggingBuiltins", "false"); // by default, false
    // causes the interpreter to always assume hash-based pycs are valid
    options.put("python.CheckHashPycsMode", "never");
    return options;
  }

  private static Engine getOrCreateEngine(Project project) {
    Path venvDir = project.getVenvDirectory();
    Path key = venvDir.toAbsolutePath().normalize();
    return ENGINES_BY_VENV.computeIfAbsent(key, k -> Engine.newBuilder("python")
                                                           // suppress the "JVMCI is not enabled for this JVM" warning
                                                           .option("engine.WarnInterpreterOnly", "false")
                                                           // Set the python executable
                                                           .option("python.Executable",
                                                                   getPythonExecutableFromVenv(venvDir).toString())
                                                           // Set the python home;
                                                           .option("python.PythonHome",
                                                                   getPythonHome(project).toString())
                                                           .build());
  }

  public static Path getPythonExecutableFromVenv(Path venvDir) {
    return venvDir.resolve((EomtbxRuntime.IS_WINDOWS ? "Scripts\\python.exe" : "bin/python"));
  }

  private static Path getPythonHome(Project project) {
    Path pythonHome = getPythonHomeFromVenvConfig(project);
    if (pythonHome == null) {
      pythonHome = Properties.getPythonHome();
    }
    return pythonHome;
  }

  private static Path getPythonHomeFromVenvConfig(Project project) {
    Path pyEnvConfig = project.getVenvDirectory().resolve(PYVENV_CFG_FILE);
    if (!Files.exists(pyEnvConfig)) {
      return null;
    }
    try {
      List<String> lines = Files.readAllLines(pyEnvConfig);
      for (String line : lines) {
        if (line.startsWith("home =")) {
          var path = Path.of(line.substring("home =".length()).trim());
          // virtual envs initialized with graalpython the home points to the bin directory
          if (path.getFileName().toString().equalsIgnoreCase("bin")) {
            return path.getParent();
          }
          return path;
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read " + PYVENV_CFG_FILE, e);
    }
    return null;
  }

  static void runInitVenvProcess(Path pythonExe, Path venvDir) throws IOException {
    // Run the Python command to create a virtual environment
    ProcessBuilder processBuilder = new ProcessBuilder(
        pythonExe.toString(),
        "-m",
        "venv",
        venvDir.toString()
    );

    Process process = processBuilder.start();
    try {
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new IOException("Failed to create virtual environment. Exit code: " + exitCode);
      }
      if (!PackageManager.isPipInstalled(venvDir)) {
        throw new IOException("Failed to create virtual environment. Pip not found at: " + venvDir);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while creating virtual environment", e);
    }
  }

  public void init() throws PyRunnerException {
    // init the engine for the project
    getOrCreateEngine(project);
  }

  public Context createContext(Map<String, String> options) throws PyRunnerException {
    return createContext(options, null);
  }

  public Context createContext(Map<String, String> options, CaptureStreamCallback outputCallback)
      throws PyRunnerException {
    var engine = getOrCreateEngine(project);
    // System.out.println("=== Available Engine Options ===");
    // dumpOptions(engine.getOptions());
    // System.out.println("=== Available Python Language Options ===");
    // dumpOptions(engine.getLanguages().get("python").getOptions());

    // for debugging purposes
    // var pythonHome = Properties.getPythonHome();
    // System.out.printf("Python home: %s%n", pythonHome);
    // System.out.printf("Python executable from VEnv: %s%n", getPythonExecutableFromVenv(project.getVenvDirectory()));
    // System.out.printf("Source directory: %s%n", project.getSrcDirectory());
    // System.out.printf("VEnv directory: %s%n", project.getVenvDirectory());

    var env = System.getenv();
    try {
      var workingDirectory = project.getWorkingDirectory();
      // See org.graalvm.python.embedding.GraalPyResources.contextBuilder(java.nio.file.Path) for example
      // in org.graalvm.python:python-embedding
      Builder builder = Context.newBuilder().engine(engine) // permitted languages of engine
                               .environment(env)
                               // Set working directory for easy file access
                               // Affects: Where relative file paths resolve to when Python code opens files
                               // **Scope**: All file I/O operations in Python (`open()`, `Path()`, etc.)
                               // **Purpose**: Sets the "base directory" for relative paths
                               .currentWorkingDirectory(workingDirectory)
                               // allows everything - actually allows importing of java packages
                               .allowAllAccess(true)
                               // allows python to access the java language
                               .allowHostAccess(HostAccess.ALL)
                               // allow class loading from the host
                               .allowHostClassLoading(true)
                               // allow creating python threads
                               .allowCreateThread(true)
                               // allow running Python native extensions
                               .allowNativeAccess(true)
                               .allowPolyglotAccess(PolyglotAccess.ALL)
                               // allow all IO access
                               .allowIO(IOAccess.ALL);

      if (outputCallback != null) {
        builder.out(new CapturePrintStream(false, outputCallback))
               .err(new CapturePrintStream(true, outputCallback));
      }

      // Where are the sources
      options.putIfAbsent("python.PythonPath", buildPythonPath());
      options.putIfAbsent("python.InputFilePath", project.getSrcDirectory().toString());
      builder.options(options);

      if (Properties.isAllPackagesEnabled()) {
        builder.allowHostClassLookup(className -> true);
      } else {
        // more control which classes can be loaded -- at execution time
        final List<String> allowedPackages = new ArrayList<>();
        allowedPackages.add("java");
        allowedPackages.add("org.esa.snap");
        allowedPackages.add("eu.esa.snap");
        allowedPackages.add("com.bc.ceres");
        allowedPackages.add("org.eomasters");

        // adding the Microwave Toolbox to allowed packages the first eval takes a lot longer
        // so disabling both
        // allowedPackages.add("eu.esa.opt");
        // allowedPackages.add("eu.esa.sar");
        builder.allowHostClassLookup(className -> allowedPackages.stream().anyMatch(className::startsWith));
      }
      return builder.build();
    } catch (Throwable e) {
      throw new PyRunnerException(e);
    }
  }

  public PackageManager getPackageManager() {
    return PackageManager.create(project.getVenvDirectory());
  }

  private String buildPythonPath() throws PyRunnerException {
    try {
      var apiDir = getApiDir();
      unpackAPI(apiDir);
      // Combine the source directory and resources directory
      return project.getSrcDirectory() + File.pathSeparator + apiDir;
    } catch (IOException e) {
      throw new PyRunnerException("Could not initialise packages from resources", e);
    }
  }

  public static void unpackAPI(Path targetDir) throws IOException {
    unpackPackageCode(SNAPKIT_PACKAGE_NAME, targetDir);
  }

  private static Path getApiDir() throws IOException {
    Path apiDir = Properties.getApiDir();
    final var moduleInfo = Modules.getDefault().ownerOf(PythonEnvironment.class);
    if (apiDir == null) {
      var version = moduleInfo.getSpecificationVersion().toString();
      var auxDataPath = EomtbxRuntime.getModuleAuxdataDir(PYEDITOR, version);
      apiDir = auxDataPath.resolve("PyEditor_api");
      Files.createDirectories(apiDir);
    } else {
      if (!Files.exists(apiDir)) {
        throw new IOException("Python code path does not exist: " + apiDir);
      }
    }

    if (moduleInfo != null) {
      var isDevVersion = moduleInfo.getImplementationVersion().contains("-");
      if (isDevVersion) {
        PathUtils.cleanDirectory(apiDir);
      }
    }
    return apiDir;
  }

  private static void unpackPackageCode(String packageName, Path apiDir) throws IOException {
    if (!apiDir.resolve("__init__.py").toFile().exists()) {
      extractCode(getPackageResourcePath(packageName), apiDir.resolve(packageName));
    }
  }

  private static void extractCode(Path resourcesPath, Path targetDir) throws IOException {
    Files.createDirectories(targetDir);

    try (Stream<Path> walk = Files.walk(resourcesPath)) {
      walk.forEach(source -> {
        Path relative = resourcesPath.relativize(source);
        Path target = targetDir.resolve(relative.toString());
        try {
          if (Files.isDirectory(source)) {
            Files.createDirectories(target);
          } else {
            Files.copy(source, target, REPLACE_EXISTING);
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }
  }

  private static Path getPackageResourcePath(String packageName) throws IllegalStateException {
    URL entryResource = PythonEnvironment.class.getResource(
        "/org/eomasters/eomtbx/pyeditor/pycode/" + packageName + "/__init__.py");

    if (entryResource == null) {
      throw new IllegalStateException("Could not find " + packageName + " package in resources");
    }

    try {
      return FileUtils.getPath(entryResource.toURI()).getParent();
    } catch (IOException | URISyntaxException e) {
      throw new IllegalStateException("Could not find " + packageName + " package in resources", e);
    }
  }

}
