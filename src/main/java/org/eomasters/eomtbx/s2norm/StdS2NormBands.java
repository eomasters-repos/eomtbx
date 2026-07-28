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

import java.util.Map;

public class StdS2NormBands extends NormBands {
  public static final Map<String, Integer> BRDF_NAME_INDEX_MAP = Map.of(
      "B2", 1, "B3", 2, "B4", 3,
      "B5", 4, "B6", 5, "B7", 6,
      "B8", 7, "B11", 11, "B12", 12);

  @Override
  boolean isSupportedBand(String bandName) {
    return BRDF_NAME_INDEX_MAP.containsKey(bandName);
  }


}
