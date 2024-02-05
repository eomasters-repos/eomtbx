/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2024 Marco Peters
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

package org.eomasters.eomtbx.loopgpt;

import com.bc.ceres.jai.operator.ReinterpretDescriptor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.esa.snap.core.gpf.main.CommandLineTool;
import org.esa.snap.core.util.SystemUtils;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionGroups;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.NbBundle;

/**
 * Option processor for the --loopgpt option.
 * <p>With this option, a file with GPT commands can be run in batch mode.
 * It loops over each line and executes the gpt command</p>
 * <p>Example: <code>snap --loopgpt C:\temp\commands.txt</code></p>
 * <p>To prevent the GUI and splash screen from showing, add also {@code --nogui} {@code --nosplash} <br>
 * The GPT commands file should contain one command per line. Empty lines and comments (starting with #) are allowed and
 * will be ignored. File paths containing spaces shall be put inside double quotes.</p>
 */
@org.openide.util.lookup.ServiceProvider(service = OptionProcessor.class)
@NbBundle.Messages({
    "DSC_LoopGpt=Run GPT commands in loop: snap --loopgpt <file-path>; "
        + "Add also --nogui --nosplash to prevent GUI and splash screen from showing."
})
public class LoopGptOptionProcessor extends OptionProcessor {

  private static final String PROP_PLUGIN_MANAGER_CHECK_INTERVAL = "plugin.manager.check.interval";
  private static final Option loopGpt;
  private static final Set<Option> optionSet;

  static {
    String b = LoopGptOptionProcessor.class.getPackageName() + ".Bundle";
    loopGpt = Option.shortDescription(Option.requiredArgument(Option.NO_SHORT_NAME, "loopgpt"), b, "DSC_LoopGpt");
    optionSet = Collections.singleton(OptionGroups.allOf(loopGpt));
  }

  private static List<String> readCommands(Path commandsFilePath) throws CommandException {
    List<String> commands;
    try {
      commands = Files.readAllLines(commandsFilePath);
    } catch (IOException e) {
      CommandException commandException = new CommandException(90004, "Could not read GPT commands file");
      commandException.initCause(e);
      throw commandException;
    }
    return commands;
  }

  // Splits a command line into an array of strings. As separator serves a space character but if the space is inside
  // a pair of double quotes, it is not used as separator. The double quotes are removed from the result.
  static String[] parseCommandline(String cmd) {
    String[] split = cmd.trim().split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
    for (int i = 0; i < split.length; i++) {
      split[i] = split[i].replace("\"", "");
    }
    return split;
  }

  @Override
  protected Set<Option> getOptions() {
    return optionSet;
  }

  @Override
  protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {

    if (optionValues.containsKey(loopGpt)) {
      String actualUpdateIternal = System.getProperty(PROP_PLUGIN_MANAGER_CHECK_INTERVAL);
      try {
        System.setProperty(PROP_PLUGIN_MANAGER_CHECK_INTERVAL, "NEVER");
        String[] args = optionValues.get(loopGpt);
        if (args.length < 1) {
          throw new CommandException(90001, "No file path given");
        }
        Path commandsFilePath = Paths.get(args[0]);
        if (!Files.exists(commandsFilePath) || Files.isReadable(commandsFilePath)) {
          throw new CommandException(90002, "GPT commands file does not exist or is not readable");
        }
        List<String> commands = readCommands(commandsFilePath);

        commands.stream().filter(s -> s.startsWith("#") || s.isBlank()).forEach(commands::remove);
        if (commands.isEmpty()) {
          throw new CommandException(90003, "GPT commands file is empty");
        }

        runLoopGpt(env, commands);
      } finally {
        System.setProperty(PROP_PLUGIN_MANAGER_CHECK_INTERVAL, actualUpdateIternal);
      }

    }
  }

  private void runLoopGpt(Env env, List<String> commands) {
    env.getOutputStream().println("LoopGpt");
    Locale.setDefault(Locale.ENGLISH); // Force usage of english locale
    // need to use a class from ceres-jai in order to get the defined JAI descriptors loaded
    SystemUtils.init3rdPartyLibs(ReinterpretDescriptor.class);

    CommandLineTool commandLineTool = new CommandLineTool();
    for (String cmd : commands) {
      env.getOutputStream().println("**********************************************************************");
      env.getOutputStream().println("Starting GPT command: " + cmd);
      try {
        String[] s = parseCommandline(cmd);
        commandLineTool.run(s);
      } catch (Exception e) {
        env.getOutputStream().println("Could not run GPT command: " + cmd);
        env.getOutputStream().println("Reason: " + e.getMessage());
      }
      System.gc();
      env.getOutputStream().println("**********************************************************************");
      env.getOutputStream().println();
    }

    System.exit(0);
  }
}
