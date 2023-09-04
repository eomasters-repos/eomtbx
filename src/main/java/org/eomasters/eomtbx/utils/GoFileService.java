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
import org.jsoup.Connection;
import org.jsoup.Jsoup;

/**
 * A class that uploads files to (<a href="https://gofile.io">gofile.io</a>). See the <a
 * href="https://gofile.io/api">API</a>
 */
public class GoFileService implements FileSharingService {

  private static final String WEB_URL = "https://gofile.io";

  @Override
  public String getName() {
    return "Gofile";
  }

  @Override
  public String getWebpage() {
    return WEB_URL;
  }

  @Override
  public String getTosUrl() {
    return WEB_URL + "/terms";
  }

  @Override
  public String getPrivacyUrl() {
    return WEB_URL + "/privacy";
  }

  @Override
  public UploadResponse uploadFile(String filename, InputStream inputStream) throws IOException {
    Connection.Response response;
    // get the best server first
    response = Jsoup.connect("https://api.gofile.io/getServer")
        .method(Connection.Method.GET)
        .ignoreContentType(true)
        .execute();

    JsonObject serverJson = new Gson().fromJson(response.body(), JsonObject.class);
    if (!serverJson.get("status").getAsString().equalsIgnoreCase("ok")) {
      throw new IOException("Could not get server");
    }
    JsonObject serverData = serverJson.getAsJsonObject("data");
    String serverName = serverData.get("server").getAsString();

    response = Jsoup.connect("https://" + serverName + ".gofile.io/uploadFile")
        .data("file", filename, inputStream)
        .method(org.jsoup.Connection.Method.POST)
        .ignoreContentType(true)
        .execute();

    JsonObject uploadJson = new Gson().fromJson(response.body(), JsonObject.class);
    if (!uploadJson.get("status").getAsString().equalsIgnoreCase("ok")) {
      throw new IOException("Could not upload file");
    }
    JsonObject uploadData = uploadJson.getAsJsonObject("data");
    String downloadPage = uploadData.get("downloadPage").getAsString();
    return new UploadResponse(200, downloadPage);

  }
}
