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

package org.eomasters.eomtbx.io.aster;

import java.nio.file.Path;
import org.eomasters.eomtbx.io.AbstractReaderPlugin;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.datamodel.RGBImageProfile;
import org.esa.snap.core.datamodel.RGBImageProfileManager;

// Product Specification L1T: https://lpdaac.usgs.gov/documents/300/ASTER_L1T_Product_Specification.pdf

public class AsterProductReaderPlugIn extends AbstractReaderPlugin {

  static {
    registerRGBProfiles();
    AsterWarning.addAsterWarningOnOpen();
  }

  /* AST_L1T_{VERSION}{START_DATE_TIME}_{PROCESS_DATE_TIME}_{IDENTIFIER}.hdf
   Variables are put into {}.
   VERSION is 3 digits long and can either be 003 or 031
   START_DATE_TIME is of format MMDDYYYYHHMMSS
   PROCESS_DATE_TIME is of format MMDDYYYYHHMMSS
   IDENTIFIER is 5 to 7 digits long
  */
  private static final String FILENAME_PATTERN = "AST_L1T_(003|031)\\d{14}_\\d{14}_\\d{4,7}\\.hdf";
  static final String FORMAT_NAME = "AST_L1T";
  static final String DESCRIPTION = "ASTER L1T Registered Radiance at the Sensor";
  private static final String FILE_EXTENSION = ".hdf";

  public AsterProductReaderPlugIn() {
    super(FORMAT_NAME, DESCRIPTION, new String[]{FILE_EXTENSION});
  }

  @Override
  public DecodeQualification getDecodeQualification(Object input) {
    Path path = getAsPath(input);
    if (isAsterL1TFilename(path)) {
      return DecodeQualification.INTENDED;
    }
    return DecodeQualification.UNABLE;
  }

  @Override
  public ProductReader createReaderInstance() {
    return new AsterProductReader(this);
  }

  private boolean isAsterL1TFilename(Path path) {
    String filename = path.getFileName().toString();
    return filename.matches(FILENAME_PATTERN);
  }

  private static void registerRGBProfiles() {
    final String[] vnirExpressions = {
        "VNIR_BAND_2",  // red channel band-maths expression
        "VNIR_BAND_3N",  // green channel band-maths expression
        "VNIR_BAND_1"   // blue channel band-maths expression
    };
    final String[] vnirSwirExpressions = {
        "SWIR_BAND_4",  // red channel band-maths expression
        "VNIR_BAND_3N",  // green channel band-maths expression
        "VNIR_BAND_2"   // blue channel band-maths expression
    };
    final String[] tirExpressions = {
        "TIR_BAND_14",  // red channel band-maths expression
        "TIR_BAND_12",  // green channel band-maths expression
        "TIR_BAND_10",  // blue channel band-maths expression
    };

    final RGBImageProfileManager manager = RGBImageProfileManager.getInstance();
    String[] pattern = {AsterProductReaderPlugIn.FORMAT_NAME, "", ""};
    manager.addProfile(new RGBImageProfile("ASTER_L1T VNIR", vnirExpressions, pattern));
    manager.addProfile(new RGBImageProfile("ASTER_L1T VNIR/SWIR", vnirSwirExpressions, pattern));
    manager.addProfile(new RGBImageProfile("ASTER_L1T TIR", tirExpressions, pattern));
  }

}
