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

package org.eomasters.eomtbx.s2geom;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eomasters.eomtbx.utils.ArrayHelper;

public class S2GeometryAngles {

  public static final int GRID_WIDTH = 23;
  public static final int GRID_HEIGHT = 23;
  public static final int[] IMAGE_RESOLUTIONS = new int[]{10, 20, 60};
  public static Map<Integer, Integer> RESOLUTION_IMAGE_SIZE_MAP = Map.of(10, 10980, 20, 5490, 60, 1830);
  public static final int NUM_S2_BANDS = 13;


  private final SunAngleData sunAngles;
  private final ViewAngleData viewAngles;
  private Map<Integer, Map<Integer, double[][]>> extrapolatedViewZenithBandDetectorGrids;
  private Map<Integer, Map<Integer, double[][]>> extrapolatedViewAzimuthBandDetectorGrids;
  private Map<Integer, Map<Integer, double[][]>> relativeAzimuthAngles;

  S2GeometryAngles(SunAngleData sunAngleData, ViewAngleData viewAngleData) {
    sunAngles = sunAngleData;
    viewAngles = viewAngleData;
  }

  public double[][] getSunZenithAnglGrids() {
    return sunAngles.getZenith();
  }


  public double[][] getSunAzimuthAngles() {
    return sunAngles.getAzimuth();
  }

  public Map<Integer, Map<Integer, double[][]>> getViewZenithBandDetectorGrids() {
    return viewAngles.getViewZenithBandDetectorGrids();
  }

  public Map<Integer, Map<Integer, double[][]>> getViewAzimuthBandDetectorGrids() {
    return viewAngles.getViewAzimuthBandDetectorGrids();
  }

  public Map<Integer, Map<Integer, double[][]>> getExtrpolatedViewZenithAngleGrids() {
    if (extrapolatedViewZenithBandDetectorGrids == null) {
      extrapolatedViewZenithBandDetectorGrids = new HashMap<>();
      Map<Integer, Map<Integer, double[][]>> viewZenithBandDetectorGrids = getViewZenithBandDetectorGrids();
      extrapolateBandDetectorAngleGrids(viewZenithBandDetectorGrids, extrapolatedViewZenithBandDetectorGrids);
    }
    return extrapolatedViewZenithBandDetectorGrids;
  }


  public Map<Integer, Map<Integer, double[][]>> getExtrapolatedViewAzimuthAngleGrids() {
    if (extrapolatedViewAzimuthBandDetectorGrids == null) {
      extrapolatedViewAzimuthBandDetectorGrids = new HashMap<>();
      Map<Integer, Map<Integer, double[][]>> viewAzimuthBandDetectorGrids = getViewAzimuthBandDetectorGrids();
      extrapolateBandDetectorAngleGrids(viewAzimuthBandDetectorGrids, extrapolatedViewAzimuthBandDetectorGrids);
    }
    return extrapolatedViewAzimuthBandDetectorGrids;
  }

  public Map<Integer, Map<Integer, double[][]>> getRelativeAzimuthGrids() {
    if (relativeAzimuthAngles == null) {
      relativeAzimuthAngles = new HashMap<>();
      Map<Integer, Map<Integer, double[][]>> bandDetectorViewAzimuthAngles = getExtrapolatedViewAzimuthAngleGrids();
      for (Entry<Integer, Map<Integer, double[][]>> bandDetectorEntry : bandDetectorViewAzimuthAngles.entrySet()) {
        Map<Integer, double[][]> viewAzimuthDetectorGrids = bandDetectorEntry.getValue();
        Integer detectorIndex = bandDetectorEntry.getKey();
        Map<Integer, double[][]> relativeAzimuthDetectorGrids = relativeAzimuthAngles.getOrDefault(detectorIndex,
            new HashMap<>());
        for (Entry<Integer, double[][]> detectorEntry : viewAzimuthDetectorGrids.entrySet()) {
          double[][] viewAzimuthAngles = detectorEntry.getValue();
          double[][] sunAzimuthAngles = getSunAzimuthAngles();
          double[][] destGrid = new double[viewAzimuthAngles.length][];
          for (int i = 0; i < viewAzimuthAngles.length; i++) {
            destGrid[i] = ArrayHelper.subtract(viewAzimuthAngles[i], sunAzimuthAngles[i]);
          }
          relativeAzimuthDetectorGrids.put(detectorEntry.getKey(), destGrid);
        }
        relativeAzimuthAngles.put(detectorIndex, relativeAzimuthDetectorGrids);
      }
    }
    return relativeAzimuthAngles;
  }


  private void extrapolateBandDetectorAngleGrids(Map<Integer, Map<Integer, double[][]>> srcGrids,
      Map<Integer, Map<Integer, double[][]>> destGrids) {
    for (Entry<Integer, Map<Integer, double[][]>> bandDetectorEntry : srcGrids.entrySet()) {
      Map<Integer, double[][]> value = bandDetectorEntry.getValue();
      Integer detectorIndex = bandDetectorEntry.getKey();
      Map<Integer, double[][]> extrapolatedBandDetectorEntry = destGrids.getOrDefault(
          detectorIndex, new HashMap<>());
      for (Entry<Integer, double[][]> detectorEntry : value.entrySet()) {
        double[][] srcGrid = detectorEntry.getValue();
        double[][] destGrid = new double[srcGrid.length][];
        for (int i = 0; i < srcGrid.length; i++) {
          destGrid[i] = Arrays.copyOf(srcGrid[i], srcGrid[i].length);
          ArrayHelper.extrapolateNaN(destGrid[i]);
        }
        extrapolatedBandDetectorEntry.put(detectorEntry.getKey(), destGrid);
      }
      destGrids.put(detectorIndex, extrapolatedBandDetectorEntry);
    }
  }


}
