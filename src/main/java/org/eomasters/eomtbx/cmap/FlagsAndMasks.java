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

package org.eomasters.eomtbx.cmap;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.Mask;
import org.esa.snap.core.datamodel.ProductData;

public final class FlagsAndMasks {

  private static final String BAND_NAME_FLAGS = "eom_cm_flags";
  private static final String BAND_DESCRIPTION_FLAGS = "A coastal map providing a coastline, land-water and intertidal flag as well as coastal land/water vicinity score flags.";
  static final int LW_MASK = 0b00000001;
  static final int COASTLINE_MASK = 0b00000010;
  static final int INTERTIDAL_MASK = 0b00000100;
  static final int WATER_VIC_MASK = 0b00011000;
  static final int LAND_VIC_MASK = 0b01100000;
  public static final int ALL_FLAGS = LW_MASK & COASTLINE_MASK & INTERTIDAL_MASK & WATER_VIC_MASK & LAND_VIC_MASK;
  public static final int LAND_FLAG_VALUE = 0b00000001;
  public static final int WATER_FLAG_VALUE = 0b00000000;
  public static final int COASTLINE_FLAG_VALUE = 0b00000010;
  public static final int INTERTIDAL_FLAG_VALUE = 0b00000100;
  public static final int WATER_VICINITY_LOW_FLAG_VALUE = 0b00001000;
  public static final int WATER_VICINITY_MID_FLAG_VALUE = 0b00010000;
  public static final int WATER_VICINITY_HIGH_FLAG_VALUE = 0b00011000;
  public static final int LAND_VICINITY_LOW_FLAG_VALUE = 0b00100000;
  public static final int LAND_VICINITY_MID_FLAG_VALUE = 0b01000000;
  public static final int LAND_VICINITY_HIGH_FLAG_VALUE = 0b01100000;
  public static final FlagMask[] FLAG_MASKS = new FlagMask[]{
      new FlagMask(BAND_NAME_FLAGS, "LAND", "Land pixels", LW_MASK, LAND_FLAG_VALUE,
          Color.GREEN, 0.5),
      new FlagMask(BAND_NAME_FLAGS, "WATER", "Water pixels", LW_MASK, WATER_FLAG_VALUE,
          Color.BLUE, 0.5),
      new FlagMask(BAND_NAME_FLAGS, "COASTLINE", "Coastline pixels", COASTLINE_MASK, COASTLINE_FLAG_VALUE,
          Color.WHITE, 0.5),
      new FlagMask(BAND_NAME_FLAGS, "INTERTIDAL", "Intertidal pixels", INTERTIDAL_MASK, INTERTIDAL_FLAG_VALUE,
          Color.CYAN, 0.5),
      new FlagMask(BAND_NAME_FLAGS, "COASTAL_WATER_VICINITY_LOW",
          "Low vicinity score for water pixels", WATER_VIC_MASK, WATER_VICINITY_LOW_FLAG_VALUE,
          Color.BLUE, 0.5),
      new FlagMask(BAND_NAME_FLAGS, "COASTAL_WATER_VICINITY_MID",
          "Intermediate vicinity score for water pixels", WATER_VIC_MASK, WATER_VICINITY_MID_FLAG_VALUE,
          Color.YELLOW, 0.5),
      new FlagMask(BAND_NAME_FLAGS, "COASTAL_WATER_VICINITY_HIGH",
          "High vicinity score for water pixels", WATER_VIC_MASK, WATER_VICINITY_HIGH_FLAG_VALUE,
          Color.RED, 0.5),
      new FlagMask(BAND_NAME_FLAGS, "COASTAL_LAND_VICINITY_LOW",
          "Low vicinity score for land pixels", LAND_VIC_MASK, LAND_VICINITY_LOW_FLAG_VALUE,
          Color.GREEN, 0.5),
      new FlagMask(BAND_NAME_FLAGS, "COASTAL_LAND_VICINITY_MID",
          "Intermediate vicinity score for land pixels", LAND_VIC_MASK, LAND_VICINITY_MID_FLAG_VALUE,
          Color.YELLOW, 0.5),
      new FlagMask(BAND_NAME_FLAGS, "COASTAL_LAND_VICINITY_HIGH",
          "High vicinity score for land pixels", LAND_VIC_MASK, LAND_VICINITY_HIGH_FLAG_VALUE,
          Color.RED, 0.5)
  };

  private FlagsAndMasks() {
  }

  public static String getFlagBandName() {
    return BAND_NAME_FLAGS;
  }

  public static int getFlagsDataType() {
    return ProductData.TYPE_INT8;
  }

  public static String getFlagBandDescription() {
    return BAND_DESCRIPTION_FLAGS;
  }

  public static FlagCoding getFlagCoding() {
    FlagCoding fc = new FlagCoding("EOM_CoastalFlags");
    for (FlagMask flagMask : FLAG_MASKS) {
      flagMask.addTo(fc);
    }
    return fc;
  }

  public static Mask[] getMasks(Dimension size) {
    return Arrays.stream(FLAG_MASKS)
                 .map(flagMask -> flagMask.createMask(size.width, size.height))
                 .toArray(Mask[]::new);
  }

}
