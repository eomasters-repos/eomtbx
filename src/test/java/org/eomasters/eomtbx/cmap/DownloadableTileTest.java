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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;

class DownloadableTileTest {

  @Test
  void getMd5Checksum() throws URISyntaxException, NoSuchAlgorithmException, IOException {
    String expectedMd5 = "234D75EB63802EC4CDFFB6D910490097";
    String md5Checksum = DownloadableTile.getMd5Checksum(Paths.get(getClass().getResource("EOM_CoastalMap_Flags_N06E093.znap.zip").toURI()));
    assertEquals(expectedMd5, md5Checksum);
  }

  @Test
  void getEtag() throws IOException {
    String expectedEtag = "234D75EB63802EC4CDFFB6D910490097";
    URL url;
    try {
      var urlStr = "https://s3-eu-central-1.ionoscloud.com/coastalmap/EOM_CoastalMap_Flags_N06E093.znap.zip";
      url = new URI(urlStr).toURL();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    String etag = DownloadableTile.getEtag(url);
    assertEquals(expectedEtag, etag);
  }
}
