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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.eomasters.eomtbx.EomtbxRuntime;
import org.eomasters.eomtbx.utils.CaptureStreamCallback;

/**
 * A utility class for managing Python packages using pip. This class provides methods for listing, installing, and
 * uninstalling packages.
 */
public class PackageManager {

  public record Package(String name, String version) {

  }

  private final Path pipPath;

  /**
   * Constructs a new PackageManager instance with the specified path to the pip executable.
   *
   * @param pipPath the path to the pip executable
   */
  public PackageManager(Path pipPath) {
    this.pipPath = pipPath;
  }

  public static PackageManager create(Path venvDir) {
    var pipPath = getPipPath(venvDir);
    return new PackageManager(pipPath);
  }

  public static boolean isPipInstalled(Path venvDir) throws IOException {
    return Files.isRegularFile(getPipPath(venvDir));
  }

  /**
   * Lists the installed Python packages.
   *
   * @return a list of installed packages with their versions
   * @throws IOException          if an I/O error occurs
   * @throws InterruptedException if the process is interrupted
   */
  public List<Package> listInstalledPackages() throws IOException, InterruptedException {
    List<Package> packages = new ArrayList<>();
    ProcessBuilder pb = new ProcessBuilder(pipPath.toString(), "list");
    CaptureStreamCallback callback = (line, isError) -> SwingUtilities.invokeLater(() -> {
      if (isError || line.startsWith("Package") || line.startsWith("--")) {
        return;
      }
      var split = line.split(" +");
      if (split.length == 2) {
        String packageName = split[0];
        String version = split[1];
        packages.add(new Package(packageName, version));
      }
    });

    int exitCode = runProcess(pb, callback);
    if (exitCode != 0) {
      throw new IOException("pip list command failed with exit code " + exitCode);
    }

    return packages;
  }


  /**
   * Installs Python packages with live output streaming.
   *
   * @param packageNames the packages to install
   * @throws IOException          if an I/O error occurs
   * @throws InterruptedException if the process is interrupted
   */
  public void installPackage(String... packageNames) throws IOException, InterruptedException {
    installPackage(null, packageNames);
  }

  /**
   * Installs Python packages with live output streaming.
   *
   * @param callback     the callback for receiving output lines, can be null
   * @param packageNames the packages to install
   * @throws IOException          if an I/O error occurs
   * @throws InterruptedException if the process is interrupted
   */
  public void installPackage(CaptureStreamCallback callback, String... packageNames)
      throws IOException, InterruptedException {
    if (packageNames.length == 0) {
      return;
    }
    List<String> command = new ArrayList<>();
    command.addAll(List.of(pipPath.toString(), "install"));
    command.addAll(List.of(packageNames));
    ProcessBuilder pb = new ProcessBuilder(command);
    int exitCode = runProcess(pb, callback);
    if (exitCode != 0) {
      throw new IOException("pip install command failed with exit code " + exitCode);
    }
  }

  /**
   * Uninstalls a Python package with live output streaming.
   *
   * @param callback    the callback for receiving output lines, can be null
   * @param packageName the name of the package to uninstall
   * @throws IOException          if an I/O error occurs
   * @throws InterruptedException if the process is interrupted
   */
  public void uninstallPackage(CaptureStreamCallback callback, String packageName)
      throws IOException, InterruptedException {
    ProcessBuilder pb = new ProcessBuilder(pipPath.toString(), "uninstall", "-y", packageName);
    int exitCode = runProcess(pb, callback);
    if (exitCode != 0) {
      throw new IOException("pip uninstall command failed with exit code " + exitCode);
    }
  }

  private static int runProcess(ProcessBuilder pb, CaptureStreamCallback callback)
      throws IOException, InterruptedException {
    pb.redirectErrorStream(true);
    Process process = pb.start();

    // Read output line by line and call callback for each line
    if (callback != null) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          boolean isError = line.toLowerCase().contains("error") || line.toLowerCase().contains("failed");
          callback.onCapture(line, isError);
        }
      }
    }
    return process.waitFor();
  }

  private static Path getPipPath(Path venvDir) {
    Path pipPath;
    if (EomtbxRuntime.IS_WINDOWS) {
      pipPath = venvDir.resolve("Scripts").resolve("pip.exe");
    } else {
      pipPath = venvDir.resolve("bin").resolve("pip");
    }
    return pipPath;
  }
}
