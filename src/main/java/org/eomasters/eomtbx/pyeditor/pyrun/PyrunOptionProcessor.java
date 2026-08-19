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

import com.bc.ceres.jai.operator.ReinterpretDescriptor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eomasters.eomtbx.EomtbxRuntime;
import org.eomasters.eomtbx.utils.CaptureStreamCallback;
import org.esa.snap.core.util.SystemUtils;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionGroups;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.NbBundle;

@org.openide.util.lookup.ServiceProvider(service = OptionProcessor.class)
@NbBundle.Messages({
    "DSC_Pyrun=Run a project's Python file from the command line: snap --pyrun <projectFile> [-F=<pythonFile>}; "
        + "Add also --nogui --nosplash to prevent GUI and splash screen from showing.",
    "DSC_pythonFile=The file to be executed. Path mus be relative to source directory of the project.",

})
public class PyrunOptionProcessor extends OptionProcessor {

  private static final String PROP_PLUGIN_MANAGER_CHECK_INTERVAL = "plugin.manager.check.interval";

  private static final Option pyrun;
  private static final Set<Option> optionSet;
  private static final Option fileOpt;

  static {
    String b = PyrunOptionProcessor.class.getPackageName() + ".Bundle";
    pyrun = Option.shortDescription(Option.requiredArgument(Option.NO_SHORT_NAME, "pyrun"), b, "DSC_Pyrun");
    fileOpt = Option.shortDescription(Option.requiredArgument('F', null), b, "DSC_pythonFile");

    optionSet = Set.of(OptionGroups.allOf(pyrun), OptionGroups.allOf(fileOpt));
  }

  @Override
  protected Set<Option> getOptions() {
    return optionSet;
  }

  @Override
  protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {

    if (optionValues.containsKey(pyrun)) {
      new DisablePluginsWrapper().run(() -> {
        validateOptions(optionValues);

        var project = loadProject(optionValues);
        var pythonFile = getPythonFile(optionValues, project);

        runPython(env, project, pythonFile);
      });
    }

    System.exit(0);
  }

  private void runPython(Env env, Project project, Path pythonFile) throws CommandException {
    Locale.setDefault(Locale.ENGLISH); // Force usage of English locale
    // need to use a class from ceres-jai in order to get the defined JAI descriptors loaded
    SystemUtils.init3rdPartyLibs(ReinterpretDescriptor.class);

    try {
      CaptureStreamCallback callback = (line, isError) -> {
        if (isError) {
          env.getErrorStream().print(line);
          env.getErrorStream().flush();
        } else {
          env.getOutputStream().print(line);
          env.getOutputStream().flush();
        }
      };

      new PyRunner(project).run(pythonFile, callback);
    } catch (PyRunnerException | PythonException e) {
      EomtbxRuntime.LOGGER.log(Level.FINER, "Failed to run Python script", e);
      throw new CommandException(9006, e.getMessage());
    }
  }

  private static Project loadProject(Map<Option, String[]> optionValues) throws CommandException {
    var projectFile = Path.of(getArgument(optionValues, pyrun));
    if (!Files.isRegularFile(projectFile) || !Files.isReadable(projectFile)) {
      throw new CommandException(90003, "The project path must be a readable file");
    }
    try {
      var project = ProjectIO.loadProject(projectFile);
      var srcDirectory = project.getSrcDirectory();
      if (!Files.isDirectory(srcDirectory)) {
        throw new CommandException(90004, "The source directory does not exist");
      }
      return project;
    } catch (IOException e) {
      throw new CommandException(9005, "Failed to load project file: " + e.getMessage());
    }
  }

  private static Path getPythonFile(Map<Option, String[]> optionValues, Project project) throws CommandException {
    var argument = getArgument(optionValues, fileOpt);
    var pythonFile = Path.of(argument);
    var absPythonFile = project.getSrcDirectory().resolve(pythonFile);
    if (!Files.isReadable(absPythonFile) && !absPythonFile.toString().toLowerCase().endsWith(".py")) {
      throw new CommandException(90003, "The python file must be a readable file and must be a Python script [*.py]");
    }
    return absPythonFile;
  }

  private static void validateOptions(Map<Option, String[]> optionValues) throws CommandException {
    String[] pyrunArgs = optionValues.get(pyrun);
    if (pyrunArgs == null || pyrunArgs.length < 1) {
      throw new CommandException(90001, "Project path must be specified");
    }
    String[] fileArgs = optionValues.get(fileOpt);
    if (fileArgs == null || fileArgs.length < 1) {
      throw new CommandException(90002, "Python source file must be specified");
    }
  }

  private static String getArgument(Map<Option, String[]> optionValues, Option option) throws CommandException {
    String[] args = optionValues.get(option);
    if (args.length < 1) {
      throw new CommandException(80001, "Missing argument for option " + option);
    }
    if (args[0].startsWith("=")) {
      return args[0].substring(1);
    }
    return args[0];
  }

  private static class DisablePluginsWrapper {

    void run(RunOptionProcessor run) throws CommandException {
      String actualUpdateIternal = System.getProperty(PROP_PLUGIN_MANAGER_CHECK_INTERVAL);
      System.setProperty(PROP_PLUGIN_MANAGER_CHECK_INTERVAL, "NEVER");
      try {
        run.run();
      } finally {
        if (actualUpdateIternal != null) {
          System.setProperty(PROP_PLUGIN_MANAGER_CHECK_INTERVAL, actualUpdateIternal);
        }

      }
    }

  }

  @FunctionalInterface
  private interface RunOptionProcessor {

    void run() throws CommandException;
  }
}
