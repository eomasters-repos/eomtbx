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

package org.eomasters.eomtbx.pyeditor.graalpy;

import com.bc.ceres.core.ProgressMonitor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eomasters.eomtbx.EomtbxRuntime;
import org.eomasters.eomtbx.pyeditor.Properties;

public class GraalPy {

  private static final String TAG_PREFIX = "graal-";

  private GraalPy() {}

  public static boolean isInstalled() {
    var pythonHome = Properties.getPythonHome();
    if (pythonHome == null) {
      return false;
    }
    return isGraalPyHome(pythonHome);
  }

  public static boolean isGraalPyHome(Path home) {
    if (home == null) {
      return false;
    }
    return Files.exists(getGraalpyExecutable(home));
  }

  public static Path getGraalpyExecutable(Path home) {
    return home.resolve("bin").resolve("graalpy" + (EomtbxRuntime.IS_WINDOWS ? ".exe" : ""));
  }

  public static Path getPythonExecutable(Path home) {
    return home.resolve("bin").resolve("python" + (EomtbxRuntime.IS_WINDOWS ? ".exe" : ""));
  }

  /**
   * Retrieves available GraalPython releases from GitHub. Only releases from version 23.0.0 onwards are included.
   *
   * @return List of release tags (e.g., "graal-24.0.0")
   * @throws IOException if the GitHub API request fails
   */
  public static List<String> getReleaseTags() throws IOException {
    Installer installer = new Installer();
    List<String> releases = new ArrayList<>();

    try {
      String releasesJson = installer.fetchGitHubReleases();
      List<String> tagNames = installer.parseReleaseTagsFromJson(releasesJson);

      for (String tagName : tagNames) {
        if (tagName.startsWith(TAG_PREFIX)) {
          String version = Installer.extractVersion(tagName);
          if (installer.isVersionSupported(version)) {
            releases.add(tagName);
          }
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }

    releases.sort(Collections.reverseOrder()); // Latest first
    return releases;
  }

  public static List<String> getReleaseVersions() throws IOException {
    return getReleaseTags().stream().map(Installer::extractVersion).toList();
  }

  public static void install(String releaseVersion, Path targetPath, ProgressMonitor pm) throws IOException {
    if (!isInstalled()) {
      new Installer().downloadAndInstall(toReleaseTag(releaseVersion), targetPath, pm);
    }
    Properties.setPythonHome(targetPath);
  }

  private static String toReleaseTag(String releaseVersion) {
    return TAG_PREFIX + releaseVersion;
  }

}
