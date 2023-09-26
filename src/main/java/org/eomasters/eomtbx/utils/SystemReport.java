/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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

package org.eomasters.eomtbx.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductManager;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.rcp.SnapApp;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;


/**
 * Generates an error report.
 *
 * <p>The report contains the following information:
 * <ul>
 *   <li>Basic System Information/li>
 *   <li>Stacktrace (optionally, if provided)</li>
 *   <li>Installed Modules</li>
 *   <li>Open Products</li>
 *   <li>Preferences</li>
 *   <li>System Properties</li>
 *   <li>Environment Variables</li>
 *   <li>System Log Tail (optionally, if number of lines has been set)</li>
 * </ul>
 */
public class SystemReport {

  private String reportName;
  private String title;
  private String message;
  private Throwable throwable;
  private final Instant created;
  private int numLogTailLines = -1;

  /**
   * Generates an error report instance.
   */
  public SystemReport() {
    Clock utcClock = Clock.systemUTC();
    created = Instant.now(utcClock);
  }

  /**
   * Set the name of this report.
   *
   * @param name the name of the report
   * @return this report
   */
  public SystemReport name(String name) {
    this.reportName = name;
    return this;
  }

  /**
   * Set the title of this report.
   *
   * @param title the title of the report
   * @return this report
   */
  public SystemReport title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Set the message of this report.
   *
   * @param message the message of the report
   * @return this report
   */
  public SystemReport message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Set the throwable of this report.
   *
   * @param throwable the throwable of the report
   * @return this report
   */
  public SystemReport throwable(Throwable throwable) {
    this.throwable = throwable;
    return this;
  }

  /**
   * Includes the last {@code numLines} of the log file.
   *
   * @param numLines the number of lines to include
   * @return this report
   */
  public SystemReport logTail(int numLines) {
    this.numLogTailLines = numLines;
    return this;
  }


  /**
   * Returns the name of the report.
   *
   * @return the name
   */
  public String getName() {
    return reportName;
  }

  /**
   * Returns the title of the report. If no title is set, the title is "Error Report" if a throwable is set, otherwise
   * it is "System Report".
   *
   * @return the title
   */
  public String getTitle() {
    if (title == null) {
      title = throwable != null ? "Error Report" : "System Report";
    }
    return title;
  }

  /**
   * Returns the message of the report.
   *
   * @return the message
   */
  public Instant getCreatedAt() {
    return created;
  }

  /**
   * Generates the error report as String.
   *
   * @return the error report
   */
  public String generate() {
    getTitle();
    StringBuilder report = new StringBuilder(title + ": ");
    addBasicInformation(report);
    addStackTrace(report);
    addProductList(report);
    addInstalledModules(report);
    addPreferences(report);
    addSystemProperties(report);
    addEnvironmentVariables(report);
    addLogTail(report);
    return report.toString();
  }

  private void addLogTail(StringBuilder report) {
    if (numLogTailLines > 0) {
      report.append("System Log Tail:\n");

      Path logFile = getCurrentLogFile();
      try (ReversedLinesFileReader reader = new ReversedLinesFileReader(logFile.toFile(), StandardCharsets.UTF_8)) {
        List<String> lines = reader.readLines(numLogTailLines);
        lines.forEach(line -> report.append(line).append("\n"));
      } catch (IOException e) {
        report.append("Error while reading log file: ").append(e.getMessage()).append("\n");
      }
      report.append("\n\n");
    }

  }

  private static Path getCurrentLogFile() {
    Path userHomeDir = SystemUtils.getUserHomeDir().toPath();
    Path winLogDir = userHomeDir.resolve("AppData/Roaming/SNAP/var/log");
    Path unixLogDir = userHomeDir.resolve(".snap/system/var/log");
    Path logDir = Files.exists(winLogDir) ? winLogDir : unixLogDir;
    return logDir.resolve("messages.log");
  }


  private static void addEnvironmentVariables(StringBuilder report) {
    report.append("Environment Variables:\n");
    Map<String, String> getenv = System.getenv();
    for (String name : getenv.keySet()) {
      report.append(name).append(" = ").append(getenv.get(name)).append("\n");
    }
    report.append("\n\n");
  }

  private static void addSystemProperties(StringBuilder report) {
    report.append("System Properties:\n");
    Properties properties = System.getProperties();
    properties.stringPropertyNames().stream().sorted()
        .forEach(name -> report.append(name).append(" = ").append(properties.getProperty(name)).append("\n"));
    report.append("\n\n");
  }

  private static void addPreferences(StringBuilder report) {
    report.append("Preferences:\n");
    try {
      Preferences preferences = SnapApp.getDefault().getPreferences();
      String[] keys = Arrays.stream(preferences.keys()).sorted().toArray(String[]::new);
      if (keys.length == 0) {
        report.append("No preferences found.\n");
      }
      for (String key : keys) {
        report.append(key).append(" = ").append(preferences.get(key, null)).append("\n");
      }
    } catch (Exception e) {
      report.append("Error while reading preferences: ").append(e.getMessage()).append("\n");
    }
    report.append("\n\n");
  }

  private static void addInstalledModules(StringBuilder report) {
    report.append("Installed Modules:\n");
    try {
      Collection<? extends ModuleInfo> modules = Lookup.getDefault().lookupAll(ModuleInfo.class);
      modules.stream().sorted(Comparator.comparing((ModuleInfo o) -> o.getCodeNameBase())).forEach(
          info -> report.append(info.getDisplayName()).append("\n").append("\tcode name: ")
              .append(info.getCodeNameBase()).append("\n").append("\tversion: ").append(info.getImplementationVersion())
              .append("\n").append("\tenabled: ").append(info.isEnabled()).append("\n"));
    } catch (Exception e) {
      report.append("Error while while retrieving module information:\n").append("\t").append(e.getMessage())
          .append("\n");
    }
    report.append("\n\n");
  }

  private static void addProductList(StringBuilder report) {
    ProductManager productManager = SnapApp.getDefault().getProductManager();
    Product[] products = productManager.getProducts();
    report.append("Open Products:\n");
    if (products.length > 0) {
      for (int i = 0; i < products.length; i++) {
        String productName = products[i].getDisplayName();
        report.append(String.format("(%d) %s%n", i, productName));
        report.append(String.format("\t%s%n", products[i].getFileLocation().toPath().toAbsolutePath()));
      }
    } else {
      report.append("No products open.\n");
    }
    report.append("\n\n");
  }

  private void addStackTrace(StringBuilder report) {
    if (throwable != null) {
      report.append("Stacktrace:\n");
      for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
        report.append(stackTraceElement.toString()).append("\n");
      }
      Throwable cause = throwable.getCause();
      if (cause != null) {
        report.append("Caused by: ").append(cause.getMessage()).append("\n");
        for (StackTraceElement stackTraceElement : cause.getStackTrace()) {
          report.append(stackTraceElement.toString()).append("\n");
        }
      }
      report.append("\n\n");
    }
  }

  private void addBasicInformation(StringBuilder report) {
    report.append(message).append("\n");
    report.append("Time: ").append(Instant.now(Clock.systemUTC())).append("\n");
    String applicationName = SnapApp.getDefault().getAppContext().getApplicationName();
    report.append("Application: ").append(applicationName).append(" v").append(SystemUtils.getReleaseVersion())
        .append("\n");
    report.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
    report.append("Java Vendor: ").append(System.getProperty("java.vendor")).append("\n");
    report.append("OS: ").append(System.getProperty("os.name")).append("\n");
    report.append("OS Version: ").append(System.getProperty("os.version")).append("\n");
    report.append("Used Memory: ").append(toMib(Runtime.getRuntime().totalMemory())).append(" MiB\n");
    report.append("Max JVM Memory: ").append(toMib(Runtime.getRuntime().maxMemory())).append(" MiB\n");
    report.append("\n");
    File[] roots = File.listRoots();
    report.append("File systems: \n");
    for (File root : roots) {
      report.append(root.getAbsolutePath()).append(" - ").append(toGib(root.getFreeSpace())).append("/")
          .append(toGib(root.getTotalSpace())).append(" Free/Total GiB\n");
    }
    report.append("\n\n");
  }

  private static long toMib(long memory) {
    return memory / 1024 / 1024;
  }

  private static long toGib(long memory) {
    return memory / 1024 / 1024 / 1024;
  }
}
