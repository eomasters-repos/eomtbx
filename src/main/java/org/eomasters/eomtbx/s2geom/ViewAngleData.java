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

import java.util.Map;

/**
 * The ViewAngleData class represents the view angles for a S2 product. It provides methods to retrieve the average view
 * angles for different bands.
 */
public class ViewAngleData {

  private final Map<Integer, Map<Integer, double[][]>> viewZenithBandDetectorGrids;
  private final Map<Integer, Map<Integer, double[][]>> viewAzimuthBandDetectorGrids;

  public ViewAngleData(Map<Integer, Map<Integer, double[][]>> viewZenithBandDetectorGrids,
      Map<Integer, Map<Integer, double[][]>> viewAzimuthBandDetectorGrids) {
    this.viewZenithBandDetectorGrids = viewZenithBandDetectorGrids;
    this.viewAzimuthBandDetectorGrids = viewAzimuthBandDetectorGrids;
  }

  public Map<Integer, Map<Integer, double[][]>> getViewZenithBandDetectorGrids() {
    return viewZenithBandDetectorGrids;
  }

  public Map<Integer, Map<Integer, double[][]>> getViewAzimuthBandDetectorGrids() {
    return viewAzimuthBandDetectorGrids;
  }

  public Map<Integer, double[][]> getViewZenithDetectorGrids(int bandIndex) {
    return viewZenithBandDetectorGrids.getOrDefault(bandIndex, null);
  }

  public Map<Integer, double[][]> getViewAzimuthDetectorGrids(int bandIndex) {
    return viewAzimuthBandDetectorGrids.getOrDefault(bandIndex, null);
  }

}
