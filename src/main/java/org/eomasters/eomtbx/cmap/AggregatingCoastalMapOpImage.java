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

import static org.eomasters.eomtbx.cmap.FlagsAndMasks.COASTLINE_FLAG_VALUE;
import static org.eomasters.eomtbx.cmap.FlagsAndMasks.COASTLINE_MASK;
import static org.eomasters.eomtbx.cmap.FlagsAndMasks.INTERTIDAL_FLAG_VALUE;
import static org.eomasters.eomtbx.cmap.FlagsAndMasks.INTERTIDAL_MASK;
import static org.eomasters.eomtbx.cmap.FlagsAndMasks.LAND_FLAG_VALUE;
import static org.eomasters.eomtbx.cmap.FlagsAndMasks.LAND_VICINITY_HIGH_FLAG_VALUE;
import static org.eomasters.eomtbx.cmap.FlagsAndMasks.LAND_VICINITY_LOW_FLAG_VALUE;
import static org.eomasters.eomtbx.cmap.FlagsAndMasks.LAND_VICINITY_MID_FLAG_VALUE;
import static org.eomasters.eomtbx.cmap.FlagsAndMasks.LAND_VIC_MASK;
import static org.eomasters.eomtbx.cmap.FlagsAndMasks.LW_MASK;
import static org.eomasters.eomtbx.cmap.FlagsAndMasks.WATER_VICINITY_HIGH_FLAG_VALUE;
import static org.eomasters.eomtbx.cmap.FlagsAndMasks.WATER_VICINITY_LOW_FLAG_VALUE;
import static org.eomasters.eomtbx.cmap.FlagsAndMasks.WATER_VICINITY_MID_FLAG_VALUE;
import static org.eomasters.eomtbx.cmap.FlagsAndMasks.WATER_VIC_MASK;

import java.io.IOException;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.datamodel.SceneFactory;
import org.esa.snap.core.image.RasterDataNodeSampleOpImage;
import org.esa.snap.core.image.ResolutionLevel;

class AggregatingCoastalMapOpImage extends RasterDataNodeSampleOpImage {

  private final GeoCoding geoCoding;
  private final CoastalDataProvider provider;


  public AggregatingCoastalMapOpImage(RasterDataNode rasterDataNode) throws IOException {
    super(rasterDataNode, ResolutionLevel.MAXRES);
    CoastalMap map = CoastalMap.getInstance();
    provider = new CoastalDataProvider(map);
    provider.init(SceneFactory.createScene(rasterDataNode));
    geoCoding = rasterDataNode.getGeoCoding();
  }

  @Override
  protected double computeSample(int x, int y) {
    GeoPos geoPos0 = geoCoding.getGeoPos(new PixelPos(x, y), null);
    GeoPos geoPos1 = geoCoding.getGeoPos(new PixelPos(x + 1, y + 1), null);
    try {
      int[] mapValues = provider.getMapValues(geoPos0.lon, geoPos0.lat, geoPos1.lon, geoPos1.lat);
      return doAggregation(mapValues);
    } catch (IOException e) {
      throw new RuntimeException("Error reading coastal map values", e);
    }
  }

  private static final int LAND_INDEX = 0;
  private static final int COASTLINE_INDEX = 1;
  private static final int INTERTIDAL_INDEX = 2;
  private static final int WATER_VIC_LOW_INDEX = 3;
  private static final int WATER_VIC_MID_INDEX = 4;
  private static final int WATER_VIC_HIGH_INDEX = 5;
  private static final int LAND_VIC_LOW_INDEX = 6;
  private static final int LAND_VIC_MID_INDEX = 7;
  private static final int LAND_VIC_HIGH_INDEX = 8;

  static int doAggregation(int[] values) {

    int[] flagCounts = new int[9];
    for (int mapValue : values) {
        countFlag(LAND_INDEX, LAND_FLAG_VALUE, LW_MASK, mapValue, flagCounts);
        countFlag(COASTLINE_INDEX, COASTLINE_FLAG_VALUE, COASTLINE_MASK, mapValue, flagCounts);
        countFlag(INTERTIDAL_INDEX, INTERTIDAL_FLAG_VALUE, INTERTIDAL_MASK, mapValue, flagCounts);
        countFlag(WATER_VIC_LOW_INDEX, WATER_VICINITY_LOW_FLAG_VALUE, WATER_VIC_MASK, mapValue, flagCounts);
        countFlag(WATER_VIC_MID_INDEX, WATER_VICINITY_MID_FLAG_VALUE, WATER_VIC_MASK, mapValue, flagCounts);
        countFlag(WATER_VIC_HIGH_INDEX, WATER_VICINITY_HIGH_FLAG_VALUE, WATER_VIC_MASK, mapValue, flagCounts);
        countFlag(LAND_VIC_LOW_INDEX, LAND_VICINITY_LOW_FLAG_VALUE, LAND_VIC_MASK, mapValue, flagCounts);
        countFlag(LAND_VIC_MID_INDEX, LAND_VICINITY_MID_FLAG_VALUE, LAND_VIC_MASK, mapValue, flagCounts);
        countFlag(LAND_VIC_HIGH_INDEX, LAND_VICINITY_HIGH_FLAG_VALUE, LAND_VIC_MASK, mapValue, flagCounts);
    }
    int mid = values.length / 2;
    int coastalValue = 0;
    boolean isLand = flagCounts[LAND_INDEX] > mid;
    if (isLand) {
      coastalValue |= LAND_FLAG_VALUE;
    }
    if (flagCounts[COASTLINE_INDEX] > 0) {
      coastalValue |= COASTLINE_FLAG_VALUE;
    }
    if (flagCounts[INTERTIDAL_INDEX] > mid) {
      coastalValue |= INTERTIDAL_FLAG_VALUE;
    }

    if (isLand) {
      int maxWaterVicIndex = WATER_VIC_LOW_INDEX;
      for (int i = WATER_VIC_LOW_INDEX + 1; i <= WATER_VIC_HIGH_INDEX; i++) {
        if (flagCounts[i] > flagCounts[maxWaterVicIndex]) {
          maxWaterVicIndex = i;
        }
      }
      if (flagCounts[maxWaterVicIndex] > 0) {
        switch (maxWaterVicIndex) {
          case WATER_VIC_LOW_INDEX:
            coastalValue |= WATER_VICINITY_LOW_FLAG_VALUE;
            break;
          case WATER_VIC_MID_INDEX:
            coastalValue |= WATER_VICINITY_MID_FLAG_VALUE;
            break;
          case WATER_VIC_HIGH_INDEX:
            coastalValue |= WATER_VICINITY_HIGH_FLAG_VALUE;
            break;
        }
      }
    }else {
      int maxLandVicIndex = LAND_VIC_LOW_INDEX;
      for (int i = LAND_VIC_LOW_INDEX + 1; i <= LAND_VIC_HIGH_INDEX; i++) {
        if (flagCounts[i] > flagCounts[maxLandVicIndex]) {
          maxLandVicIndex = i;
        }
      }
      if (flagCounts[maxLandVicIndex] > 0) {
        switch (maxLandVicIndex) {
          case LAND_VIC_LOW_INDEX:
            coastalValue |= LAND_VICINITY_LOW_FLAG_VALUE;
            break;
          case LAND_VIC_MID_INDEX:
            coastalValue |= LAND_VICINITY_MID_FLAG_VALUE;
            break;
          case LAND_VIC_HIGH_INDEX:
            coastalValue |= LAND_VICINITY_HIGH_FLAG_VALUE;
            break;
        }
      }
    }
    return coastalValue;
  }

  private static void countFlag(int flagIndex, int flagValue, int flagMask, int mapValue, int[] flagCounts) {
    flagCounts[flagIndex] += (mapValue & flagMask) == flagValue ? 1 : 0;
  }

}
