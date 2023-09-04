/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * --> https://www.eomasters.org/sw/EOMTBX
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
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eomasters.eomtbx.utils.FileSharingService.UploadResponse;

/**
 * A class that uploads files to a file sharing service.
 */
public final class FileSharing {

  private static FileSharingService service = new GoFileService();

  // this one is currently not used. Files are deleted after one download which is usually done by the user himself.
  // private static FileSharingService service = new FileIoService();

  private FileSharing() {
  }

  /**
   * Returns the currently used file sharing service.
   *
   * @return the currently used file sharing service
   */
  public static FileSharingService getService() {
    return service;
  }

  /**
   * Sets the file sharing service to use.
   *
   * @param service the file sharing service to use
   */
  public static void setService(FileSharingService service) {
    FileSharing.service = service;
  }

  /**
   * Uploads a file the file sharing service.
   *
   * @param file The file to upload
   * @return The response from the API
   * @throws IOException if an I/O error occurs
   */
  public static UploadResponse uploadFile(Path file) throws IOException {
    return FileSharing.uploadFile(file.getFileName().toString(), Files.newInputStream(file));
  }

  /**
   * Uploads a data from the provided input stream to the used file sharing service and puts it into a file using the
   * provided name.
   *
   * @param filename    The name of the file to upload
   * @param inputStream The input stream of the file to upload
   * @return The response from the API
   * @throws IOException if an I/O error occurs
   */
  public static UploadResponse uploadFile(String filename, InputStream inputStream) throws IOException {
    return service.uploadFile(filename, inputStream);
  }


  /**
   * A main method for testing.
   */
  public static void main(String[] args) throws IOException {
    // Call the uploadFile method and get the response as a string
    Path path = Path.of("src/main/resources/org/eomasters/eomtbx/docs/toc.xml").toAbsolutePath();
    UploadResponse response = FileSharing.uploadFile(path);
    // Print the response
    System.out.println(response);
  }
}
