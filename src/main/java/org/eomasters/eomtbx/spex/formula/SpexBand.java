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

package org.eomasters.eomtbx.spex.formula;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum SpexBand {
  A("coastal", "Aerosols", 400, 455,
      new String[]{"landsat8", "landsat9", "planetscope", "sentinel2a", "sentinel2b", "wv2", "wv3"}),
  B("blue", "Blue", 450, 530,
      new String[]{"landsat4", "landsat5", "landsat7", "landsat8", "landsat9", "modis", "planetscope", "sentinel2a",
          "sentinel2b", "wv2", "wv3"}),
  G("green", "Green", 510, 600,
      new String[]{"landsat4", "landsat5", "landsat7", "landsat8", "landsat9", "modis", "planetscope", "sentinel2a",
          "sentinel2b", "wv2", "wv3"}),
  G1("green", "Green 1", 510, 550, new String[]{"modis", "planetscope"}),
  N("nir", "Near-Infrared (NIR)", 760, 900,
      new String[]{"landsat4", "landsat5", "landsat7", "landsat8", "landsat9", "modis", "planetscope", "sentinel2a",
          "sentinel2b", "wv2", "wv3"}),
  N2("nir08", "Near-Infrared (NIR) 2", 850, 880, new String[]{"sentinel2a", "sentinel2b"}),
  R("red", "Red", 620, 690,
      new String[]{"landsat4", "landsat5", "landsat7", "landsat8", "landsat9", "modis", "planetscope", "sentinel2a",
          "sentinel2b", "wv2", "wv3"}),
  RE1("rededge", "Red Edge 1", 695, 715, new String[]{"planetscope", "sentinel2a", "sentinel2b"}),
  RE2("rededge", "Red Edge 2", 730, 750, new String[]{"sentinel2a", "sentinel2b"}),
  RE3("rededge", "Red Edge 3", 765, 795, new String[]{"sentinel2a", "sentinel2b"}),
  S1("swir16", "Short-wave Infrared (SWIR) 1", 1550, 1750,
      new String[]{"landsat4", "landsat5", "landsat7", "landsat8", "landsat9", "modis", "sentinel2a", "sentinel2b"}),
  S2("swir22", "Short-wave Infrared (SWIR) 2", 2080, 2350,
      new String[]{"landsat4", "landsat5", "landsat7", "landsat8", "landsat9", "modis", "sentinel2a", "sentinel2b"}),
  T("lwir", "Thermal Infrared", 10400, 12500, new String[]{"landsat4", "landsat5", "landsat7"}),
  T1("lwir11", "Thermal Infrared 1", 10600, 11190, new String[]{"landsat8", "landsat9"}),
  T2("lwir12", "Thermal Infrared 2", 11500, 12510, new String[]{"landsat8", "landsat9"}),
  WV("nir09", "Water Vapour", 930, 960, new String[]{"sentinel2a", "sentinel2b"}),
  Y("yellow", "Yellow", 585, 625, new String[]{"planetscope", "wv2", "wv3"}),
  HV("HV", "Backscattering Coefficient HV", null, null, new String[]{"sentinel1"}),
  VH("VH", "Backscattering Coefficient VH", null, null, new String[]{"sentinel1"}),
  HH("HH", "Backscattering Coefficient HH", null, null, new String[]{"sentinel1"}),
  VV("VV", "Backscattering Coefficient VV", null, null, new String[]{"sentinel1"});

  private final String commonName;
  private final String longName;
  private final Integer maxWavelength;
  private final Integer minWavelength;
  private final String[] platforms;

  SpexBand(String commonName, String longName, Integer minWavelength, Integer maxWavelength, String[] platforms) {
    this.commonName = commonName;
    this.longName = longName;
    this.minWavelength = minWavelength;
    this.maxWavelength = maxWavelength;
    this.platforms = platforms;
  }

  public String getCommonName() {
    return commonName;
  }

  public String getLongName() {
    return longName;
  }

  public Integer getMaxWavelength() {
    return maxWavelength;
  }

  public Integer getMinWavelength() {
    return minWavelength;
  }

  public String[] getPlatforms() {
    return platforms;
  }

  public static boolean contains(String symbol) {
    return Arrays.stream(SpexBand.values()).anyMatch(value -> value.name().equals(symbol));
  }

  public static List<SpexBand> getBandsUsedInFormula(String formula) {
    Stream<String> symbolNames = Stream.of(SpexBand.values()).map(SpexBand::name);
    // Find all SpexBand.name considering word boundaries in formula
    return symbolNames.filter(name -> formula.matches(".*\\b" + name + "\\b.*")).map(SpexBand::valueOf).collect(Collectors.toList());
  }
}
