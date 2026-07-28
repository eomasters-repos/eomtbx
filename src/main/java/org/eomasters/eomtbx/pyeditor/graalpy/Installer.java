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
import com.bc.ceres.core.SubProgressMonitor;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.eomasters.eomtbx.EomtbxRuntime;


/**
 * GraalPythonInstaller provides functionality to download and install GraalPython from GitHub releases. It can fetch
 * available releases, download specific versions, and extract them to target directories. Only releases from version
 * 23.0.0 onwards are supported.
 */
class Installer {

  private static final String GITHUB_REPO_NAME = "oracle/graalpython";
  private static final String MINIMUM_VERSION = "23.0.0";
  private static final String GITHUB_API_BASE = "https://api.github.com/repos/" + GITHUB_REPO_NAME;
  private static final String GRAALPY_COMMUNITY_JVM = "graalpy-community-jvm";
  private static final String GRAALPY_TYPE = GRAALPY_COMMUNITY_JVM;
  private static final int ONE_MB = 1024 * 1024;

  private final HttpClient httpClient;

  /**
   * Creates a new GraalPythonInstaller instance.
   */
  public Installer() {
    this.httpClient = HttpClient.newBuilder()
                                .connectTimeout(Duration.ofSeconds(30))
                                .followRedirects(Redirect.ALWAYS)
                                .version(HttpClient.Version.HTTP_1_1)
                                .build();
  }

  /**
   * Downloads and installs a specific GraalPython release to the target directory with progress monitoring.
   *
   * @param releaseTag the release tag (e.g., "graal-24.0.0")
   * @param targetPath the target directory for installation
   * @param pm         progress monitor to monitor progress, can be null
   * @throws IOException if the download or extraction fails
   */
  public void downloadAndInstall(String releaseTag, Path targetPath,
                                 ProgressMonitor pm) throws IOException {
    pm.beginTask("Installing GraalPython ...", 100);
    try {
      Path downloadedFile = download(releaseTag, SubProgressMonitor.create(pm, 60));

      try {
        Files.createDirectories(targetPath);

        if (EomtbxRuntime.IS_WINDOWS) {
          extractZip(downloadedFile, targetPath, SubProgressMonitor.create(pm, 40));
        } else {
          extractTarGz(downloadedFile, targetPath, SubProgressMonitor.create(pm, 40));
        }

      } finally {
        // Clean up temporary file
        try {
          Files.deleteIfExists(downloadedFile);
        } catch (IOException e) {
          // Log but don't fail the operation
          System.err.println("Warning: Could not delete temporary file: " + downloadedFile);
        }
      }
    } finally {
      pm.done();
    }
  }

  /**
   * Downloads a specific GraalPython release for the current OS and architecture with progress monitoring. The file
   * follows the pattern: graalpy-community-jvm-{version}-{os}-{arch}.{ext}
   *
   * @param releaseTag the release tag (e.g., "graal-24.0.0")
   * @param pm         progress monitor to monitor progress
   * @return Path to the downloaded file
   * @throws IOException if the download fails
   */
  Path download(String releaseTag, ProgressMonitor pm) throws IOException {
    String version = extractVersion(releaseTag);
    if (!isVersionSupported(version)) {
      throw new IllegalArgumentException(
          "Version " + version + " is not supported. Minimum version is " + MINIMUM_VERSION);
    }

    String filename = buildFilename(version);

    try {
      String downloadUrl = getAssetDownloadUrl(releaseTag, filename);
      Path tempFile = Files.createTempFile("graalpy-" + version + "-", getFileExtension());
      downloadFile(downloadUrl, tempFile, pm);
      return tempFile;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Download interrupted", e);
    }
  }

  String fetchGitHubReleases() throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
                                     .uri(URI.create(GITHUB_API_BASE + "/releases"))
                                     .timeout(Duration.ofSeconds(30))
                                     .header("Accept", "application/vnd.github.v3+json")
                                     .header("User-Agent", "GraalPythonInstaller/1.0")
                                     .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new IOException("GitHub API request failed with status: " + response.statusCode());
    }

    return response.body();
  }

  List<String> parseReleaseTagsFromJson(String json) {
    List<String> tagNames = new ArrayList<>();

    // Simple regex-based JSON parsing for tag_name field
    Pattern pattern = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
    Matcher matcher = pattern.matcher(json);

    while (matcher.find()) {
      tagNames.add(matcher.group(1));
    }

    return tagNames;
  }

  String getAssetDownloadUrl(String releaseTag, String filename) throws IOException, InterruptedException {
    String releaseUrl = GITHUB_API_BASE + "/releases/tags/" + releaseTag;
    HttpRequest request = HttpRequest.newBuilder()
                                     .uri(URI.create(releaseUrl))
                                     .timeout(Duration.ofSeconds(30))
                                     .header("Accept", "application/vnd.github.v3+json")
                                     .header("User-Agent", "GraalPythonInstaller/1.0")
                                     .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new IOException("GitHub API request failed with status: " + response.statusCode() +
                                " for release: " + releaseTag);
    }

    return parseAssetDownloadUrl(response.body(), filename);
  }

  private String parseAssetDownloadUrl(String json, String filename) throws IOException {
    try {
      JsonElement jsonElement = JsonParser.parseString(json);

      // The response should be a JSON object with an "assets" array
      if (!jsonElement.isJsonObject()) {
        throw new IOException("Invalid JSON response: not a JSON object");
      }

      var jsonObject = jsonElement.getAsJsonObject();
      var assetsElement = jsonObject.get("assets");

      if (assetsElement == null || !assetsElement.isJsonArray()) {
        throw new IOException("No assets array found in release data");
      }

      var assetsArray = assetsElement.getAsJsonArray();

      // Find the asset with matching filename
      for (JsonElement assetElement : assetsArray) {
        if (!assetElement.isJsonObject()) {
          continue;
        }

        var assetObject = assetElement.getAsJsonObject();
        var nameElement = assetObject.get("name");

        if (nameElement != null && nameElement.isJsonPrimitive() &&
            filename.equals(nameElement.getAsString())) {

          var downloadUrlElement = assetObject.get("browser_download_url");
          if (downloadUrlElement != null && downloadUrlElement.isJsonPrimitive()) {
            return downloadUrlElement.getAsString();
          }
        }
      }

      throw new IOException("Asset not found: " + filename);

    } catch (Exception e) {
      if (e instanceof IOException) {
        throw e;
      }
      throw new IOException("Failed to parse JSON response", e);
    }
  }


  private void downloadFile(String downloadUrl, Path tempFile, ProgressMonitor pm)
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
                                     .uri(URI.create(downloadUrl))
                                     .timeout(Duration.ofMinutes(10))
                                     .header("User-Agent", "GraalPythonInstaller/1.0")
                                     .build();

    HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
    if (response.statusCode() != 200) {
      throw new IOException("Download failed with status: " + response.statusCode() +
                                " for URL: " + downloadUrl);
    }

    // Get content length from headers
    long contentLength = response.headers()
                                 .firstValueAsLong("content-length")
                                 .orElse(-1);

    var workScaleFactor = 1000;
    int totalWork = contentLength > 0 ? (int) (contentLength / workScaleFactor) : -1;
    pm.beginTask("Downloading GraalPython", totalWork);
    try (InputStream inputStream = response.body();
        var outputStream = Files.newOutputStream(tempFile);
        var bufferedOutputStream = new java.io.BufferedOutputStream(outputStream, 4 * ONE_MB)) {

      byte[] buffer = new byte[2 * ONE_MB];
      int bytesRead;
      long totalBytesRead = 0;
      long lastReportedProgress = 0;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        bufferedOutputStream.write(buffer, 0, bytesRead);
        if (pm.isCanceled()) {
          return;
        }

        totalBytesRead += bytesRead;
        // Only update progress periodically to reduce overhead
        long currentProgressUnits = totalBytesRead / workScaleFactor;
        long progressDelta = currentProgressUnits - lastReportedProgress;

        if (progressDelta > 10000) {
          pm.worked((int) progressDelta);
          lastReportedProgress = currentProgressUnits;
        }
      }
      bufferedOutputStream.flush();
    } finally {
      pm.done();
    }
  }

  static String extractVersion(String releaseTag) {
    if (releaseTag.startsWith("graal-")) {
      return releaseTag.substring(6); // Remove "graal-" prefix
    }
    return releaseTag;
  }

  boolean isVersionSupported(String version) {
    return compareVersions(version, MINIMUM_VERSION) >= 0;
  }

  private int compareVersions(String version1, String version2) {
    String[] parts1 = version1.split("\\.");
    String[] parts2 = version2.split("\\.");

    int maxLength = Math.max(parts1.length, parts2.length);

    for (int i = 0; i < maxLength; i++) {
      int v1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
      int v2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

      if (v1 != v2) {
        return Integer.compare(v1, v2);
      }
    }

    return 0;
  }

  private String buildFilename(String version) {
    String os = getOsName();
    String arch = getArchitecture();
    String ext = getFileExtension();

    return String.format("%s-%s-%s-%s.%s", GRAALPY_TYPE, version, os, arch, ext);
  }

  private String getOsName() {
    String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    if (osName.contains("windows")) {
      return "windows";
    } else if (osName.contains("mac") || osName.contains("darwin")) {
      return "macos";
    } else {
      return "linux";
    }
  }

  private String getArchitecture() {
    String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
    if (arch.contains("aarch64") || arch.contains("arm64")) {
      return "aarch64";
    } else {
      return "amd64";
    }
  }

  private String getFileExtension() {
    return EomtbxRuntime.IS_WINDOWS ? "zip" : "tar.gz";
  }

  private void extractZip(Path zipFile, Path targetDir, ProgressMonitor pm) throws IOException {
    // First pass: count total entries and find root folder
    var rootFolder = getZipRootFolder(zipFile);
    var totalEntries = getZipTotalEntries(zipFile);

    pm.beginTask("Installing files ...", totalEntries);
    // Second pass: extract files with progress reporting
    try (ZipArchiveInputStream zis = new ZipArchiveInputStream(Files.newInputStream(zipFile))) {
      ZipArchiveEntry entry;
      int worked = 0;
      while ((entry = zis.getNextEntry()) != null) {
        if (pm.isCanceled()) {
          return;
        }
        var entryName = entry.getName();
        // Skip the root folder by removing it from the path
        entryName = removeRoot(rootFolder, entryName);
        if (entryName.isEmpty()) {
          // Skip if this was just the root folder itself
          worked++;
          continue;
        }

        Path targetFile = targetDir.resolve(entryName);

        // Security check to prevent zip slip
        if (!targetFile.normalize().startsWith(targetDir.normalize())) {
          throw new IOException("Entry is outside target directory: " + entryName);
        }

        if (entry.isDirectory()) {
          Files.createDirectories(targetFile);
        } else {
          Files.createDirectories(targetFile.getParent());
          Files.copy(zis, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }

        worked++;
        if (worked >= 100) {
          pm.worked(worked);
          worked = 0;
        }
      }
    } finally {
      pm.done();
    }
  }

  private static String removeRoot(String rootFolder, String entryName) {
    if (rootFolder != null && entryName.startsWith(rootFolder)) {
      entryName = entryName.substring(rootFolder.length());
    }
    return entryName;
  }

  private static int getZipTotalEntries(Path zipFile) throws IOException {
    int totalEntries = 0;
    try (ZipArchiveInputStream zis = new ZipArchiveInputStream(Files.newInputStream(zipFile))) {
      while (zis.getNextEntry() != null) {
        totalEntries++;
      }
    }
    return totalEntries;
  }

  private static String getZipRootFolder(Path zipFile) throws IOException {
    String rootFolder = null;
    try (ZipArchiveInputStream zis = new ZipArchiveInputStream(Files.newInputStream(zipFile))) {
      ZipArchiveEntry firstEntry = zis.getNextEntry();
      if (firstEntry != null) {
        // Determine the root folder from the first entry
        String firstPath = firstEntry.getName();
        int firstSlash = firstPath.indexOf('/');
        if (firstSlash > 0) {
          rootFolder = firstPath.substring(0, firstSlash + 1);
        }

      }
    }
    return rootFolder;
  }

  private void extractTarGz(Path tarGzFile, Path targetDir, ProgressMonitor pm) throws IOException {
    // First pass: count total entries and find root folder
    var rootFolder = getTarGzRootFolder(tarGzFile);
    var totalEntries = getTarGzTotalEntries(tarGzFile);

    pm.beginTask("Installing files ...", totalEntries);
    // Second pass: extract files with progress reporting
    try (GzipCompressorInputStream gzis = new GzipCompressorInputStream(Files.newInputStream(tarGzFile));
        TarArchiveInputStream tis = new TarArchiveInputStream(gzis)) {

      TarArchiveEntry entry;
      int worked = 0;
      while ((entry = tis.getNextEntry()) != null) {
        if (pm.isCanceled()) {
          return;
        }

        var entryName = entry.getName();
        // Skip the root folder by removing it from the path
        entryName = removeRoot(rootFolder, entryName);
        if (entryName.isEmpty()) {
          // Skip if this was just the root folder itself
          worked++;
          continue;
        }

        Path targetFile = targetDir.resolve(entryName);

        // Security check to prevent tar slip
        if (!targetFile.normalize().startsWith(targetDir.normalize())) {
          throw new IOException("Entry is outside target directory: " + entryName);
        }

        if (entry.isDirectory()) {
          Files.createDirectories(targetFile);
        } else {
          Files.createDirectories(targetFile.getParent());
          Files.copy(tis, targetFile, StandardCopyOption.REPLACE_EXISTING);

          // Set executable permissions on Unix systems if executable bit is set
          // noinspection OctalInteger
          if (!EomtbxRuntime.IS_WINDOWS && (entry.getMode() & 0100) != 0) {
            targetFile.toFile().setExecutable(true);
          }
        }
        worked++;
        if (worked >= 100) {
          pm.worked(worked);
          worked = 0;
        }
      }
    } finally {
      pm.done();
    }
  }

  private static int getTarGzTotalEntries(Path tarGzFile) throws IOException {
    int totalEntries = 0;
    try (GzipCompressorInputStream gzis = new GzipCompressorInputStream(Files.newInputStream(tarGzFile));
        TarArchiveInputStream tis = new TarArchiveInputStream(gzis)) {
      while (tis.getNextEntry() != null) {
        totalEntries++;
      }
    }
    return totalEntries;
  }

  private static String getTarGzRootFolder(Path tarGzFile) throws IOException {
    String rootFolder = null;
    try (GzipCompressorInputStream gzis = new GzipCompressorInputStream(Files.newInputStream(tarGzFile));
        TarArchiveInputStream tis = new TarArchiveInputStream(gzis)) {
      TarArchiveEntry firstEntry = tis.getNextEntry();
      if (firstEntry != null) {
        // Determine the root folder from the first entry
        String firstPath = firstEntry.getName();
        int firstSlash = firstPath.indexOf('/');
        if (firstSlash > 0) {
          rootFolder = firstPath.substring(0, firstSlash + 1);
        }
      }
    }
    return rootFolder;
  }
}
