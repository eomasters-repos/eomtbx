/*-
 * ========================LICENSE_START=================================
 * EOMTBXP Toolbox Module - Toolbox Module for the EOMasters Pro Toolbox for SNAP
 * -> https://www.eomasters.org/eomtbx/modules/eomtbxp-toolbox
 * ======================================================================
 * Copyright (C) 2023 - 2025 Marco Peters
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

package org.eomasters.eomtbx.io.s2l2a;

import eu.esa.opt.dataio.s2.VirtualPath;
import eu.esa.opt.dataio.s2.ortho.S2CRSHelper;
import java.io.IOException;
import java.nio.file.Path;
import org.esa.snap.core.util.io.FileUtils;
import org.esa.snap.engine_utilities.commons.FilePath;

public class EomS2L2AUtils {

  private static final int TILE_ID_BEGIN_INDEX = 38;
  private static final int TILE_ID_END_INDEX = 44;
  private static final String XML_EXTENSION = ".xml";

  public static String getProductName(Path inputPath) {
    String fileName = inputPath.getFileName().toString();
    if (fileName.toLowerCase().endsWith(XML_EXTENSION)) {
      inputPath = inputPath.getParent();
      fileName = inputPath.getFileName().toString();
    }
    return FileUtils.getFilenameWithoutExtension(fileName);
  }

  static String getTileId(String productName) {
      return productName.substring(TILE_ID_BEGIN_INDEX, TILE_ID_END_INDEX);
  }

  static String getEpsgCode(VirtualPath virtualPath) throws IOException {
      FilePath localFile = virtualPath.getFilePath();
      String productName = getProductName(localFile.getPath());
      String tileId = getTileId(productName);
      String epsgCode = S2CRSHelper.tileIdentifierToEPSG(tileId);
      return epsgCode;
  }
}
