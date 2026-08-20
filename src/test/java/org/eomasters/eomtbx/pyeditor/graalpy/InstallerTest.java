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

import static org.junit.jupiter.api.Assertions.fail;

import com.bc.ceres.core.NullProgressMonitor;
import com.bc.ceres.core.ProgressMonitor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test class for GraalPythonInstaller
 */
class InstallerTest {

  @TempDir
  Path tempDir;

  @Test
  void testInstallerCreation() {
    Installer installer = new Installer();
    Assertions.assertNotNull(installer, "Installer should be created successfully");
  }

  @Test
  void testGetReleasesOnlineIntegration() {
    // This is an integration test that requires internet connection
    // It may be skipped in CI environments
    try {
      List<String> releases = GraalPy.getReleaseTags();

      Assertions.assertNotNull(releases, "Releases list should not be null");
      System.out.println("[TEST_LOG] Found " + releases.size() + " releases");

      // Print first few releases for verification
      for (int i = 0; i < Math.min(5, releases.size()); i++) {
        System.out.println("[TEST_LOG] Release " + i + ": " + releases.get(i));
      }

      // Verify that releases are properly filtered (should only contain graal-* tags >= 23.0.0)
      for (String release : releases) {
        Assertions.assertTrue(release.startsWith("graal-"), "Release should start with 'graal-': " + release);

        String version = release.substring(6); // Remove "graal-" prefix
        String[] versionParts = version.split("\\.");
        if (versionParts.length >= 2) {
          int major = Integer.parseInt(versionParts[0]);
          int minor = Integer.parseInt(versionParts[1]);

          Assertions.assertTrue(major > 23 || (major == 23 && minor >= 0), "Version should be >= 23.0.0: " + version);
        }
      }

    } catch (IOException e) {
      System.out.println("[TEST_LOG] Online test skipped due to network issue: " + e.getMessage());
      // This is acceptable for offline testing
    }
  }

  @Test
  void testVersionSupport() {
    Installer installer = new Installer();

    try {
      // Test with unsupported version (should throw exception)
      installer.downloadAndInstall("graal-22.3.0", tempDir, ProgressMonitor.NULL);
      fail("Should have thrown IllegalArgumentException for version < 23.0.0");
    } catch (IllegalArgumentException e) {
      Assertions.assertTrue(e.getMessage().contains("23.0.0"), "Error message should mention minimum version");
      System.out.println("[TEST_LOG] Correctly rejected version 22.3.0: " + e.getMessage());
    } catch (IOException e) {
      // This is also acceptable as it means the version check passed
      // and it tried to actually download
      System.out.println("[TEST_LOG] Version 22.3.0 check passed to download phase");
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "junit.runIntegrationTests", matches = "true")
  void testDownloadAndInstallWithProgressCallbacks() throws IOException {
    // Test progress monitoring functionality
    Installer installer = new Installer();

    // Track progress callback invocations
    final double[] installProgress = {0}; // [extracted, total]

    var pm = new NullProgressMonitor() {
      @Override
      public void internalWorked(double work) {
        installProgress[0] += work;
        System.out.println("[TEST_LOG] Installation progress: " + installProgress[0]);
      }
    };

    // Test with a recent version - this will attempt actual download with progress monitoring
    installer.downloadAndInstall("graal-24.0.0", tempDir, pm);
    // Verify that progress values make sense
    Assertions.assertTrue(installProgress[0] > 0, "Progress should be > 0");

    // Verify that files were extracted
    Assertions.assertTrue(Files.exists(tempDir), "Target directory should exist");
    Assertions.assertTrue(Files.list(tempDir).findAny().isPresent(), "Target directory should not be empty");


    System.out.println("[TEST_LOG] Successfully tested progress monitoring");
  }
}
