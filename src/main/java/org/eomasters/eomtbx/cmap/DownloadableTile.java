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

package org.eomasters.eomtbx.cmap;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;
import org.eomasters.eomtbx.EomToolbox;

final class DownloadableTile implements Callable<Boolean> {
  private static final double MIN_FILE_SIZE = 1.5 * 1024 * 1024;

  private final String fileName;
  private final Path downloadDir;
  String[] remoteLocations;

  public DownloadableTile(Path downloadDir, String fileName, String... remoteLocations) {
    this.remoteLocations = remoteLocations;
    this.fileName = fileName;
    this.downloadDir = downloadDir;
  }

  @Override
  public Boolean call() throws Exception {
    Path downloadedZipFile = downloadDir.resolve(fileName);
    for (String remoteLocation : remoteLocations) {
      URL url = new URL(remoteLocation + "/" + fileName);
      if (mustDownload(url, downloadedZipFile)) {
        try {
          Files.createFile(downloadedZipFile);
          Files.copy(url.openStream(), downloadedZipFile, StandardCopyOption.REPLACE_EXISTING);
          return true;
        } catch (IOException e) {
          String msg = String.format("Could not download file %s from %s", fileName, remoteLocation);
          EomToolbox.LOG.log(Level.WARNING, msg, e);
          throw e;
        }
      }
    }
    return false;
  }

  private boolean mustDownload(URL url, Path downloadedZipFile) {
    try {
      // does not exist or is too small
      if (!Files.exists(downloadedZipFile) || Files.size(downloadedZipFile) < MIN_FILE_SIZE) {
        return true;
      }
      String md5 = getMd5Checksum(downloadedZipFile);
      String etag = getEtag(url);
      return !md5.equals(etag);
    } catch (Exception e) {
      return true;
    }
  }

  static String getEtag(URL url) throws IOException {
    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    urlConnection.setRequestMethod("HEAD");
    String etag = urlConnection.getHeaderField("etag").toUpperCase();
    urlConnection.disconnect();
    return etag.replace("\"","");
  }

  static String getMd5Checksum(Path downloadedZipFile) throws NoSuchAlgorithmException, IOException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(Files.readAllBytes(downloadedZipFile));
    byte[] digest = md.digest();
    return DatatypeConverter.printHexBinary(digest).toUpperCase();
  }

}
