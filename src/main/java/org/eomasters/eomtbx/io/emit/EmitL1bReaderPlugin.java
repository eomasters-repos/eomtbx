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

import java.nio.file.Path;
import org.eomasters.eomtbx.io.AbstractReaderPlugin;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;

public class EmitL1bReaderPlugin extends AbstractReaderPlugin {
  static final String FILENAME_REGEX = "EMIT_L1B_RAD_\\d{3}_\\d{8}T\\d{6}_\\d{7}_\\d{3}.NC";
  static final String EMIT_L1B_FORMAT = "EMIT_L1B";

  public EmitL1bReaderPlugin() {
    super(EMIT_L1B_FORMAT, "EMIT L1B - Calibrated Radiance at Sensor", new String[]{".nc"});
  }

  @Override
  public DecodeQualification getDecodeQualification(Object input) {
    Path path = getAsPath(input);
    Path fileName = path.getFileName();
    if (fileName.toString().toUpperCase().matches(FILENAME_REGEX)) {
      return DecodeQualification.INTENDED;
    }
    return DecodeQualification.UNABLE;
  }

  @Override
  public ProductReader createReaderInstance() {return new EmitL1bReader(this);}

}
