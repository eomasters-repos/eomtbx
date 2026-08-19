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

package org.eomasters.eomtbx.spex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import org.eomasters.eomtbx.spex.formula.SpexBand;

// not used yet
public enum Platform {
  LANDSAT4("Landsat-4", SpexBand.B, SpexBand.G, SpexBand.N, SpexBand.R, SpexBand.S1, SpexBand.S2, SpexBand.T),
  LANDSAT5("Landsat-5", SpexBand.B, SpexBand.G, SpexBand.N, SpexBand.R, SpexBand.S1, SpexBand.S2, SpexBand.T),
  LANDSAT7("Landsat-7", SpexBand.B, SpexBand.G, SpexBand.N, SpexBand.R, SpexBand.S1, SpexBand.S2, SpexBand.T),
  LANDSAT8("Landsat-8", SpexBand.A, SpexBand.B, SpexBand.G, SpexBand.N, SpexBand.R, SpexBand.S1, SpexBand.S2,
      SpexBand.T1, SpexBand.T2),
  LANDSAT9("Landsat-9", SpexBand.A, SpexBand.B, SpexBand.G, SpexBand.N, SpexBand.R, SpexBand.S1, SpexBand.S2,
      SpexBand.T1, SpexBand.T2),
  MODIS("Modis", SpexBand.B, SpexBand.G, SpexBand.G1, SpexBand.N, SpexBand.R, SpexBand.S1, SpexBand.S2),
  PLANET("Planet", SpexBand.A, SpexBand.B, SpexBand.G, SpexBand.G1, SpexBand.N, SpexBand.R, SpexBand.RE1,
      SpexBand.Y),
  SENTINEL1("Sentinel-1", SpexBand.HV, SpexBand.VH, SpexBand.HH, SpexBand.VV),
  SENTINEL2A("Sentinel-2", SpexBand.A, SpexBand.B, SpexBand.G, SpexBand.N, SpexBand.N2, SpexBand.R, SpexBand.RE1,
      SpexBand.RE2, SpexBand.RE3, SpexBand.S1, SpexBand.S2, SpexBand.WV),
  WORLDVIEW2("WorldView-2", SpexBand.A, SpexBand.B, SpexBand.G, SpexBand.N, SpexBand.R, SpexBand.Y),
  WORLDVIEW3("WorldView-3", SpexBand.A, SpexBand.B, SpexBand.G, SpexBand.N, SpexBand.R, SpexBand.Y);

  private final String name;
  private final List<SpexBand> bands;

  Platform(String name, SpexBand... bands) {
    this.name = name;
    this.bands = List.of(bands);
  }


  public List<SpexBand> getBands() {
    return bands;
  }

  public String getName() {
    return name;
  }

  public static List<Platform> getSupportedPlatforms(AbstractSpex spex) {
    List<Platform> platforms = new ArrayList<>();
    List<SpexBand> bandSymbols = SpexBand.getBandsUsedInFormula(spex.getFormula());
    for (Platform platform : Platform.values()) {
      if (new HashSet<>(platform.getBands()).containsAll(bandSymbols)) {
        platforms.add(platform);
      }
    }
    platforms.sort(Comparator.naturalOrder());
    return platforms;
  }

}
