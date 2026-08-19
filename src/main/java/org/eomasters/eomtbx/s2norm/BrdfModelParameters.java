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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the predefined MODIS BRDF spectral model parameters for the Sentinel-2 bands. F values from <a
 * href="https://arxiv.org/html/2404.15812v2">https://arxiv.org/html/2404.15812v2</a>
 */
public class BrdfModelParameters {
  public static final List<Integer> SUPPORTED_BAND_INDICES = List.of(1, 2, 3, 4, 5, 6, 7, 11, 12);

  // BRDF spectral parameters for the Sentinel-2 bands
  static final Map<Integer, Double> F_ISO = new LinkedHashMap<>();
  static final Map<Integer, Double> F_GEO = new LinkedHashMap<>();
  static final Map<Integer, Double> F_VOL = new LinkedHashMap<>();

  static {
    F_ISO.put(1, 0.0774);
    F_ISO.put(2, 0.1306);
    F_ISO.put(3, 0.1690);
    F_ISO.put(4, 0.2085);
    F_ISO.put(5, 0.2316);
    F_ISO.put(6, 0.2599);
    F_ISO.put(7, 0.3093);
    F_ISO.put(11, 0.3430);
    F_ISO.put(12, 0.2658);

    F_GEO.put(1, 0.0079);
    F_GEO.put(2, 0.0178);
    F_GEO.put(3, 0.0227);
    F_GEO.put(4, 0.0256);
    F_GEO.put(5, 0.0273);
    F_GEO.put(6, 0.0294);
    F_GEO.put(7, 0.0330);
    F_GEO.put(11, 0.0453);
    F_GEO.put(12, 0.0387);

    F_VOL.put(1, 0.0372);
    F_VOL.put(2, 0.0580);
    F_VOL.put(3, 0.0574);
    F_VOL.put(4, 0.0845);
    F_VOL.put(5, 0.1003);
    F_VOL.put(6, 0.1197);
    F_VOL.put(7, 0.1535);
    F_VOL.put(11, 0.1154);
    F_VOL.put(12, 0.0639);
  }

}
