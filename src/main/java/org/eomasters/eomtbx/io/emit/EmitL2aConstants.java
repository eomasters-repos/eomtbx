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

public class EmitL2aConstants extends EmitConstants {

  static final String REFLECTANCE_VARIABLE = "reflectance";
  static final String MASK_VARIABLE = "mask";
  static final String REFL_UNC_VARIABLE = "reflectance_uncertainty";
  static final FlagInfo[] MASK_FLAGS = {
      new FlagInfo("Cloud", 1, "Probability this pixel is cloud"),
      new FlagInfo("Cirrus", 2, "Probability this pixel is cirrus"),
      new FlagInfo("Water", 4, "Probability this pixel is standing water"),
      new FlagInfo("Spacecraft", 8, "Indicating spacecraft issue"),
      new FlagInfo("Dilated_cloud", 16, "Dilated cloud mask"),
      null, null, // these are actually ancillary values AOD550 and water_vapour
      new FlagInfo("Aggregated", 32, "Aggregate bad data flag"),};
  static final int[] FLAG_INDICES = {0, 1, 2, 3, 4, 7};

  static class FlagInfo {

    String name;
    int bitMask;
    String description;

    public FlagInfo(String name, int bitMask, String description) {
      this.name = name;
      this.bitMask = bitMask;
      this.description = description;
    }
  }
}
