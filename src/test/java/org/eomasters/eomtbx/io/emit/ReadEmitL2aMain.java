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

package org.eomasters.eomtbx.io.emit;

import java.io.IOException;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;

public class ReadEmitL2aMain {

  private static final String FILE_L2A_PATH = "D:\\EOData\\EMIT\\Level2A\\EMIT_L2A_RFL_001_20241011T131626_2428509_004.nc";

  public static void main(String[] args) throws IOException {
    String filePath = FILE_L2A_PATH;
    try (NetcdfFile ncFile = NetcdfFiles.open(filePath)) {
      Group rootGroup = ncFile.getRootGroup();
      System.out.println(rootGroup);
      EmitL2aReader reader = new EmitL2aReader(new EmitL2aReaderPlugin());
      Product product = reader.readProductNodes(filePath, null);
      String name = product.getName();
      Band flags = product.getBand("flags");
      int sampleInt = flags.getSampleInt(1230, 250);

    }

  }

}
