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

import java.util.Arrays;

// Converted from https://github.com/ESDS-Leipzig/sen2nbar/blob/dc6afc7e9281dd0e58ee2d1459836a0e520ab831/sen2nbar/kernels.py
public class BrdfKernel {


  private static final double BR_DEFAULT = 1.0;
  private static final double HB_DEFAULT = 2.0;
  private static final double HALF_PI = Math.PI / 2.0;
  private static final double QUARTER_PI = Math.PI / 4.0;
  private static final double INV_PI = 1.0 / Math.PI;

  public static double[][] kgeo(double[] sunZenith, double[][] viewZenith, double[][] relativeAzimuth) {
    return kgeo(sunZenith, viewZenith, relativeAzimuth, BR_DEFAULT, HB_DEFAULT);
  }

  public static double[][] kgeo(double[] sunZenith, double[][] viewZenith, double[][] relativeAzimuth, double br,
      double hb) {
    double[][] result = new double[viewZenith.length][sunZenith.length];
    for (int i = 0; i < viewZenith.length; i++) {
      double[] bandViewZenith = viewZenith[i];
      if (bandViewZenith == null) {
        result[i] = null;
      } else {
        double[] bandRelAzimuth = relativeAzimuth[i];
        for (int j = 0; j < sunZenith.length; j++) {
          result[i][j] = kgeoSingle(sunZenith[j], bandViewZenith[j], bandRelAzimuth[j], br, hb);
        }
      }
    }
    return result;
  }

  public static double[][] kvol(double[] sunZenith, double[][] viewZenith, double[][] relativeAzimuth) {
    double[][] result = new double[viewZenith.length][sunZenith.length];
    for (int i = 0; i < viewZenith.length; i++) {
      double[] bandViewZenith = viewZenith[i];
      if (bandViewZenith == null) {
        result[i] = null;
      } else {
        double[] bandRelAzimuth = relativeAzimuth[i];
        for (int j = 0; j < sunZenith.length; j++) {
          result[i][j] = kvolSingle(sunZenith[j], bandViewZenith[j], bandRelAzimuth[j]);
        }
      }
    }
    return result;
  }

  static double kgeoSingle(double sunZenith, double viewZenith, double relativeAzimuth) {
    return kgeoSingle(sunZenith, viewZenith, relativeAzimuth, BR_DEFAULT, HB_DEFAULT);
  }

  static double kgeoSingle(double sunZenith, double viewZenith, double relativeAzimuth, double br, double hb) {
    // Convert to radians
    double thetaI = deg2rad(sunZenith);
    double thetaV = deg2rad(viewZenith);
    double phi = deg2rad(relativeAzimuth);

    // Equation 44 in Lucht et al., 2000
    double thetaIDev = arctan(br * Math.tan(thetaI));
    double thetaVDev = arctan(br * Math.tan(thetaV));

    // Equation 43 in Lucht et al., 2000
    double cosThetaIDev = Math.cos(thetaIDev);
    double cosThetaVDev = Math.cos(thetaVDev);
    double cosXiDev = cosThetaIDev * cosThetaVDev +
        Math.sin(thetaIDev) * Math.sin(thetaVDev) * Math.cos(phi);

    // Equation 42 in Lucht et al., 2000
    double tanThetaVDev = Math.tan(thetaVDev);
    double tanThetaIDev = Math.tan(thetaIDev);
    double D = Math.sqrt(
        Math.pow(tanThetaIDev, 2) + Math.pow(tanThetaVDev, 2) - 2 * tanThetaIDev * tanThetaVDev * Math.cos(phi));

    // Compute the cost term cos(t)
    // Equation 41 in Lucht et al., 2000
    double invCosThetaVDev = 1 / cosThetaVDev;
    double invCosVDev = 1 / cosThetaIDev;
    double cosT = hb * Math.sqrt(Math.pow(D, 2) +
        Math.pow(tanThetaIDev * tanThetaVDev * Math.sin(phi), 2)) / (invCosVDev + invCosThetaVDev);

    // cos(t) should be constrained to [-1,1]
    // Page 981 in Lucht et al., 2000
    cosT = Math.min(1, Math.max(-1, cosT));
    double t = arccos(cosT);

    // Compute the overlap area between the view and solar shadows (O)
    // Equation 40 in Lucht et al., 2000
    double O = INV_PI * (t - Math.sin(t) * cosT) * (invCosVDev + invCosThetaVDev);

    return O - invCosVDev - invCosThetaVDev + 0.5 * (1 + cosXiDev) * invCosVDev * invCosThetaVDev;
  }


  static double kvolSingle(double sunZenith, double viewZenith, double relativeAzimuth) {
    // Convert to radians
    double thetaI = deg2rad(sunZenith);
    double thetaV = deg2rad(viewZenith);
    double phi = deg2rad(relativeAzimuth);

    // Compute the phase angle
    double cosThetaI = Math.cos(thetaI);
    double cosThetaV = Math.cos(thetaV);
    double cosXi = cosThetaI * cosThetaV + Math.sin(thetaI) * Math.sin(thetaV) * Math.cos(phi);
    double xi = arccos(cosXi);

    return ((HALF_PI - xi) * cosXi + Math.sin(xi)) / (cosThetaI + cosThetaV) - QUARTER_PI;
  }

  private static double deg2rad(double degrees) {
    return Math.toRadians(degrees);
  }

  private static double arccos(double x) {
    return Math.acos(x);
  }

  private static double arctan(double x) {
    return Math.atan(x);
  }

  private static double[] kgeoArray(double[] sunZenith, double[] viewZenith, double[] relativeAzimuth, double br,
      double hb) {
    // Convert to radians
    double[] thetaI = deg2rad(sunZenith);
    double[] thetaV = deg2rad(viewZenith);
    double[] phi = deg2rad(relativeAzimuth);

    // Equation 44 in Lucht et al., 2000
    double[] thetaIDev = arctan(Arrays.stream(thetaI).map(t -> br * Math.tan(t)).toArray());
    double[] thetaVDev = arctan(Arrays.stream(thetaV).map(t -> br * Math.tan(t)).toArray());

    // Equation 43 in Lucht et al., 2000
    double[] cosXiDev = new double[thetaIDev.length];
    for (int i = 0; i < thetaIDev.length; i++) {
      cosXiDev[i] =
          Math.cos(thetaIDev[i]) * Math.cos(thetaVDev[i]) + Math.sin(thetaIDev[i]) * Math.sin(thetaVDev[i]) * Math.cos(
              phi[i]);
    }

    // Equation 42 in Lucht et al., 2000
    double[] D = new double[thetaIDev.length];
    for (int i = 0; i < thetaIDev.length; i++) {
      D[i] = Math.sqrt(Math.pow(Math.tan(thetaIDev[i]), 2)
          + Math.pow(Math.tan(thetaVDev[i]), 2)
          - 2 * Math.tan(thetaIDev[i]) * Math.tan(thetaVDev[i]) * Math.cos(phi[i]));
    }

    // Compute the cost term cos(t)
    // Equation 41 in Lucht et al., 2000
    double[] cosT = new double[D.length];
    for (int i = 0; i < D.length; i++) {
      cosT[i] = hb * Math.sqrt(Math.pow(D[i], 2)
          + Math.pow(Math.tan(thetaIDev[i]) * Math.tan(thetaVDev[i]) * Math.sin(phi[i]), 2))
          / ((1 / Math.cos(thetaIDev[i])) + (1 / Math.cos(thetaVDev[i])));
    }

    // cos(t) should be constrained to [-1,1]
    // Page 981 in Lucht et al., 2000
    cosT = Arrays.stream(cosT).map(x -> Math.min(1, Math.max(-1, x))).toArray();
    double[] t = arccos(cosT);

    // Compute the overlap area between the view and solar shadows (O)
    // Equation 40 in Lucht et al., 2000
    double[] O = new double[t.length];
    for (int i = 0; i < t.length; i++) {
      O[i] = INV_PI
          * (t[i] - Math.sin(t[i]) * cosT[i])
          * ((1 / Math.cos(thetaIDev[i])) + (1 / Math.cos(thetaVDev[i])));
    }

    double[] result = new double[O.length];
    for (int i = 0; i < O.length; i++) {
      result[i] = O[i]
          - (1 / Math.cos(thetaIDev[i]))
          - (1 / Math.cos(thetaVDev[i]))
          + 0.5
          * (1 + cosXiDev[i])
          * (1 / Math.cos(thetaIDev[i]))
          * (1 / Math.cos(thetaVDev[i]));
    }
    return result;
  }

  private static double[] kvolArray(double[] sunZenith, double[] viewZenith, double[] relativeAzimuth) {
    // Convert to radians
    double[] thetaI = deg2rad(sunZenith);
    double[] thetaV = deg2rad(viewZenith);
    double[] phi = deg2rad(relativeAzimuth);

    // Compute the phase angle
    double[] cosXi = new double[thetaI.length];
    for (int i = 0; i < thetaI.length; i++) {
      cosXi[i] =
          Math.cos(thetaI[i]) * Math.cos(thetaV[i]) + Math.sin(thetaI[i]) * Math.sin(thetaV[i]) * Math.cos(phi[i]);
    }

    double[] xi = arccos(cosXi);

    double[] result = new double[xi.length];
    for (int i = 0; i < xi.length; i++) {
      result[i] = ((HALF_PI - xi[i]) * cosXi[i] + Math.sin(xi[i]))
          / (Math.cos(thetaI[i]) + Math.cos(thetaV[i])) - QUARTER_PI;
    }
    return result;
  }

  private static double[] deg2rad(double[] degrees) {
    return Arrays.stream(degrees).map(Math::toRadians).toArray();
  }

  private static double[] arccos(double[] x) {
    return Arrays.stream(x).map(Math::acos).toArray();
  }

  private static double[] arctan(double[] x) {
    return Arrays.stream(x).map(Math::atan).toArray();
  }

}
