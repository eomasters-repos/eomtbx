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

package org.eomasters.eomtbx.s2norm;

import org.eomasters.eomtbx.s2geom.S2DataFormat;
import org.eomasters.eomtbx.s2geom.StdS2L2AFormat;
import org.eomasters.eomtbx.s2geom.TheiaS2L2AFormat;

abstract class NormBands {

  public static NormBands create(S2DataFormat format) {
    if (format instanceof StdS2L2AFormat) {
      return new StdS2NormBands();
    }

    if (format instanceof TheiaS2L2AFormat) {
      return new TheiaS2NormBands();
    }

    throw new IllegalArgumentException(String.format("Format %s not supported", format.getName()));
  }

  abstract boolean isSupportedBand(String bandName);

}
