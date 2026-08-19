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

import java.awt.image.RenderedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eomasters.eomtbx.s2geom.S2DataFormat;
import org.eomasters.eomtbx.s2geom.S2GeometryImages;
import org.eomasters.eomtbx.s2geom.S2GeometryAngles;


public class NormCFactors {

  private final Map<Integer, RenderedImage> cFactorImageMap;

  public static NormCFactors create(S2DataFormat format) {
    S2GeometryAngles s2GeometryAngles = format.getGeometryAngles();
    S2GeometryImages s2GeometryImages = format.getS2GeometryImages();
    double[][] sunZenithAngles = s2GeometryAngles.getSunZenithAnglGrids();
    Map<Integer, Map<Integer, double[][]>> viewZenithAngles = s2GeometryAngles.getExtrpolatedViewZenithAngleGrids();
    Map<Integer, Map<Integer, double[][]>> relativeAzimuthAngles = s2GeometryAngles.getRelativeAzimuthGrids();

    List<Integer> bandIndices = BrdfModelParameters.SUPPORTED_BAND_INDICES;
    Map<Integer, RenderedImage> cfImageMap = new HashMap<>();
    for (int bandIndex : bandIndices) {
      cfImageMap.computeIfAbsent(bandIndex, integer -> {
        Map<Integer, double[][]> detectorViewZenAngles = viewZenithAngles.get(bandIndex);
        Map<Integer, double[][]> detectorRelAziAngles = relativeAzimuthAngles.get(bandIndex);
        Map<Integer, double[][]> detectorCFactorsMap = new HashMap<>();
        for (int detIndex : detectorViewZenAngles.keySet()) {
          double[][] viewZenAngles = detectorViewZenAngles.get(detIndex);
          double[][] relAziAngles = detectorRelAziAngles.get(detIndex);
          double[][] detectorCFactors = detectorCFactors(bandIndex, sunZenithAngles, viewZenAngles, relAziAngles);
          detectorCFactorsMap.put(detIndex, detectorCFactors);
        }
        return s2GeometryImages.createCombinedAndMaskedImage(bandIndex,
            detectorCFactorsMap, s2GeometryImages.getBandDetectorMaskImages());
      });
    }

    return new NormCFactors(cfImageMap);
  }

  private NormCFactors(Map<Integer, RenderedImage> cFactorImageMap) {
    this.cFactorImageMap = cFactorImageMap;
  }

  public RenderedImage getScaledCFactorImage(int bandIndex) {
    return cFactorImageMap.get(bandIndex);
  }

  /**
   * Computes the Ross-Thick/Li-Sparse Reciprocal Bidirectional Reflectance Distribution Function (BRDF) Model.
   * References:
   * <a href="https://doi.org/10.1016/j.rse.2016.01.023">A general method to normalize Landsat reflectance data to
   * nadir BRDF adjusted reflectance</a>
   * <a href="https://doi.org/10.1016/j.rse.2008.03.009">Multi-temporal MODIS–Landsat data fusion for relative
   * radiometric normalization, gap filling, and prediction of Landsat data</a>
   */
  private static double[][] detectorCFactors(int bandIndex, double[][] sun_zenith, double[][] view_zenith,
      double[][] relative_azimuth) {
    double[][] brdf = new double[sun_zenith.length][sun_zenith[0].length];
    Double fVol = BrdfModelParameters.F_VOL.get(bandIndex);
    Double fGeo = BrdfModelParameters.F_GEO.get(bandIndex);
    Double fIso = BrdfModelParameters.F_ISO.get(bandIndex);
    for (int y = 0; y < brdf.length; y++) {
      double[] line = brdf[y];
      for (int x = 0; x < line.length; x++) {
        double kVolActual = fVol * BrdfKernel.kvolSingle(sun_zenith[y][x], view_zenith[y][x], relative_azimuth[y][x]);
        double kGeoActual = fGeo * BrdfKernel.kgeoSingle(sun_zenith[y][x], view_zenith[y][x], relative_azimuth[y][x]);
        double brdfActual = kVolActual + kGeoActual + fIso;

        double kVolNadir = fVol * BrdfKernel.kvolSingle(sun_zenith[y][x], 0, relative_azimuth[y][x]);
        double kGeoNadir = fGeo * BrdfKernel.kgeoSingle(sun_zenith[y][x], 0, relative_azimuth[y][x]);
        double brdfNadir = kVolNadir + kGeoNadir + fIso;

        line[x] = brdfNadir / brdfActual;
      }
    }
    return brdf;
  }

}
