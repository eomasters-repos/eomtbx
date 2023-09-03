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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

/**
 * A class that uploads files to (<a href="https://file.io">file.io</a>). Files are deleted after one download
 */
public class FileIoService implements FileSharingService {

  // The API endpoint for uploading files
  private static final String FILE_IO_URL = "https://file.io";

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getWebpage() {
    return FILE_IO_URL;
  }

  @Override
  public int getPriority() {
    return 0;
  }

  @Override
  public UploadResponse uploadFile(String filename, InputStream inputStream) throws IOException {
    Connection.Response response = Jsoup.connect(FileIoService.FILE_IO_URL)
        .header("Accept", "application/json")
        .header("Content-Type", "multipart/form-data")
        .data("file", filename, inputStream)
        .method(Connection.Method.POST)
        .ignoreContentType(true)
        .execute();

    String body = response.body();
    JsonObject jsonObject = new Gson().fromJson(body, JsonObject.class);
    return new UploadResponse(response.statusCode(), jsonObject.get("link").getAsString());
  }


  /**
   * Uploads a data from the provided input stream to file.io and puts it into a file using the provided name.
   *
   * @param filename    The name of the file to upload
   * @param inputStream The input stream of the file to upload
   * @return The response from the API
   * @throws IOException if an I/O error occurs
   */
  static UploadResponse _uploadFile(String filename, InputStream inputStream) throws IOException {
    Connection.Response response = Jsoup.connect(FileIoService.FILE_IO_URL)
        .header("Accept", "application/json")
        .header("Content-Type", "multipart/form-data")
        .data("file", filename, inputStream)
        .method(Connection.Method.POST)
        .ignoreContentType(true)
        .execute();

    String body = response.body();
    JsonObject jsonObject = new Gson().fromJson(body, JsonObject.class);
    return new UploadResponse(response.statusCode(), jsonObject.get("link").getAsString());
  }

  // A main method for testing
  public static void main(String[] args) throws IOException {
    // Call the uploadFile method and get the response as a string
    Path path = Path.of("C:\\Users\\marco\\OneDrive\\Dokumente\\EOMTBX_Error_Report_20230903_084531_2.txt");
    UploadResponse response = new FileIoService().uploadFile(path);
    // Print the response
    System.out.println(response);
  }
}
