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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

/**
 * A class that uploads files to (<a href="https://file.io">file.io</a>). Files are deleted after one download
 */
public class FileIoService implements FileSharingService {

  // The API endpoint for uploading files
  private static final String WEB_URL = "https://file.io";
  private static final String TOS_URL = WEB_URL + "/tos";
  private static final String PRIVACY_URL = WEB_URL + "/privacy";

  @Override
  public String getName() {
    return "file.io";
  }

  @Override
  public String getWebpage() {
    return WEB_URL;
  }

  @Override
  public String getTosUrl() {
    return TOS_URL;
  }

  @Override
  public String getPrivacyUrl() {
    return PRIVACY_URL;
  }

  @Override
  public UploadResponse uploadFile(String filename, InputStream inputStream) throws IOException {
    Connection.Response response = Jsoup.connect(WEB_URL)
        .header("Accept", "application/json")
        .header("Content-Type", "multipart/form-data")
        .data("file", filename, inputStream)
        .method(Connection.Method.POST)
        .ignoreContentType(true)
        .execute();

    String body = response.body();
    JsonObject jsonObject = new Gson().fromJson(body, JsonObject.class);
    return new UploadResponse(response.statusCode(), response.statusMessage(), jsonObject.get("link").getAsString());
  }

}
