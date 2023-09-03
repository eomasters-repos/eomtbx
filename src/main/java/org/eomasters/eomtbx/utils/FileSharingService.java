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

public interface FileSharingService {

  String getName();
  String getWebpage();

  int getPriority();

  /**
   * Uploads a file the file sharing service.
   *
   * @param file The file to upload
   * @return The response from the API
   * @throws IOException if an I/O error occurs
   */
  default UploadResponse uploadFile(Path file) throws IOException {
    return this.uploadFile(file.getFileName().toString(), Files.newInputStream(file));
  }

  /**
   * Uploads data from the provided input stream to the file sharing service and puts it into a file using the provided name.
   *
   * @param filename    The name of the file to upload
   * @param inputStream The input stream of the file to upload
   * @return The response from the API
   * @throws IOException if an I/O error occurs
   */
  UploadResponse uploadFile(String filename, InputStream inputStream) throws IOException;

  /**
   * A class that represents the response from the file sharing service.
    */
  class UploadResponse {

    // The properties of the response
    private final int status;
    private final String url;

    /**
     * Creates a new UploadResponse with the given status and url.
     * @param status
     * @param url
     */
    public UploadResponse(int status, String url) {
      this.status = status;
      this.url = url;
    }

    /**
     * Returns the status of the response.
     * @return the status
     */
    public int getStatus() {
      return status;
    }


    /**
     * Returns the url of the response.
     * @return the url under which the uploaded file can be accessed
     */
    public String getUrl() {
      return url;
    }

    @Override
    public String toString() {
      return "UploadResponse{" +
          "status=" + status +
          ", url='" + url + '\'' +
          '}';
    }
  }
}
