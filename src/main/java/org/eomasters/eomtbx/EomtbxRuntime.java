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

package org.eomasters.eomtbx;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.core.util.io.FileUtils;

/**
 * The EomtbxRuntime class provides utility methods and configurations specific to the EOMTBX toolbox. It includes
 * methods for retrieving temporary and cache directories, checking debug mode status, and accessing toolbox
 * preferences.
 */
public class EomtbxRuntime {

  public static final Logger LOGGER;
  public static final String TOOLBOX_ID = EomToolbox.TOOLBOX_ID;

  private static final boolean debugMode = ManagementFactory.getRuntimeMXBean()
                                                            .getInputArguments()
                                                            .stream()
                                                            .anyMatch(s -> s.contains("jdwp"));
  private static final File tempDir = new File(org.apache.commons.lang3.SystemUtils.getJavaIoTmpDir(),
      EomToolbox.TOOLBOX_ID);
  private static final Preferences preferences = EomToolbox.getPreferences();


  private static final String DEFAULT_LOG_LEVEL = "INFO";
  private static final String EOMTBX_LOG_LEVEL_PROPERTY = "eomasters.eomtbx.loglevel";
  public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
  public static final boolean IS_LINUX = System.getProperty("os.name").toLowerCase().contains("linux");

  static {
    LOGGER = Logger.getLogger("EOMTBX");
    LOGGER.setLevel(debugMode ? Level.ALL : Level.parse(System.getProperty(EOMTBX_LOG_LEVEL_PROPERTY, DEFAULT_LOG_LEVEL)));
  }

  /**
   * Retrieves the auxiliary data directory path for a specific module and version within the EOMTBX toolbox.
   *
   * @param moduleName the name of the module for which the auxiliary data directory is being retrieved
   * @return the path to the auxiliary data directory for the specified module and version
   */
  public static Path getModuleAuxdataDir(String moduleName) {
    Path moduleAuxDataPath = SystemUtils.getAuxDataPath().resolve(EomToolbox.TOOLBOX_ID).resolve(moduleName);
    try {
      Files.createDirectories(moduleAuxDataPath);
    } catch (IOException e) {
      LOGGER.severe(moduleAuxDataPath.toAbsolutePath() + " could not be created");
    }
    return moduleAuxDataPath;
  }

  /**
   * Retrieves the auxiliary data directory path for a specific module and version within the EOMTBX toolbox.
   *
   * @param moduleName the name of the module for which the auxiliary data directory is being retrieved
   * @param version the version of the module for which the auxiliary data directory is being retrieved
   * @return the path to the auxiliary data directory for the specified module and version
   */
  public static Path getModuleAuxdataDir(String moduleName, String version) {
    Path moduleAuxDataPath = SystemUtils.getAuxDataPath().resolve(EomToolbox.TOOLBOX_ID).resolve(moduleName)
                                            .resolve(version);
    try {
      Files.createDirectories(moduleAuxDataPath);
    } catch (IOException e) {
      LOGGER.severe(moduleAuxDataPath.toAbsolutePath() + " could not be created");
    }
    return moduleAuxDataPath;
  }

  /**
   * Retrieves the temporary directory path used by EOMTBX.
   *
   * @return the path to the temporary directory
   */
  public static Path getTempDir() {
    return tempDir.toPath();
  }


  /**
   * Retrieves the preferences node specific to the EOMTBX toolbox.
   *
   * @return the preferences associated with EOMTBX toolbox
   */
  public static Preferences getPreferences() {
    return preferences;
  }


  /**
   * Checks if the application is currently running in debug mode.
   *
   * @return true if the application is running in debug mode, false otherwise.
   */
  public static boolean isRunningDebugMode() {
    return debugMode;
  }

  static void doTempFileCleaning() {
    FileUtils.deleteTree(getTempDir().toFile());
  }

}
