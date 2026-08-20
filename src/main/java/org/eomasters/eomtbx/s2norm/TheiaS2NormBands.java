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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TheiaS2NormBands extends NormBands {
  public static final Map<String, Integer> BRDF_NAME_INDEX_MAP;

  static {
    HashMap<String, Integer> map = new HashMap<>(Map.of("Surface_Reflectance_B2", 1,
        "Surface_Reflectance_B3", 2, "Surface_Reflectance_B4", 3,
        "Surface_Reflectance_B5", 4, "Surface_Reflectance_B6", 5,
        "Surface_Reflectance_B7", 6, "Surface_Reflectance_B8", 7,
        "Surface_Reflectance_B11", 11, "Surface_Reflectance_B12", 12));
    map.putAll(Map.of("Flat_Reflectance_B2", 1, "Flat_Reflectance_B3", 2,
        "Flat_Reflectance_B4", 3, "Flat_Reflectance_B5", 4,
        "Flat_Reflectance_B6", 5, "Flat_Reflectance_B7", 6,
        "Flat_Reflectance_B8", 7, "Flat_Reflectance_B11", 11,
        "Flat_Reflectance_B12", 12));
    BRDF_NAME_INDEX_MAP = Collections.unmodifiableMap(map);
  }


  @Override
  boolean isSupportedBand(String bandName) {
    return BRDF_NAME_INDEX_MAP.containsKey(bandName);
  }

}
