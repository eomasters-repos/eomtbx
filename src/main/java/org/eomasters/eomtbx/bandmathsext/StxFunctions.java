/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Basic for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2025 Marco Peters
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

package org.eomasters.eomtbx.bandmathsext;

import com.bc.ceres.core.ProgressMonitor;
import java.util.Arrays;
import org.eomasters.utils.Exceptions;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.datamodel.Stx;
import org.esa.snap.core.jexp.EvalEnv;
import org.esa.snap.core.jexp.EvalException;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.jexp.impl.AbstractFunction.D;

/**
 * Accessor to statistical values of raster.
 *
 * <p>The following statistical values are available:
 *  <ul>
 *    <li>min: Minimum value of the raster</li>
 *    <li>max: Maximum value of the raster</li>
 *    <li>mean: Mean value of the raster</li>
 *    <li>median: Median value of the raster</li>
 *    <li>sigma: Standard deviation value of the raster</li>
 *    <li>rsd: Coefficient of variation or relative standard deviation (RSD) value of the raster</li>
 *    <li>enl: Equivalent number of looks (ENL) value of the raster</li>
 * </ul>
 * </p>
 *   Optionally the functions allow to define if the accurate or the fast statistics shall be computed.
 */
class StxFunctions extends D {

  private static final String FUNC_MIN = "min";
  private static final String FUNC_MAX = "max";
  private static final String FUNC_MEAN = "mean";
  private static final String FUNC_MEDIAN = "median";
  private static final String FUNC_SIGMA = "sigma"; // Standard deviation
  private static final String FUNC_RSD = "rsd"; // Coefficient of variation or relative standard deviation
  private static final String FUNC_ENL = "enl"; // Equivalent Number Of Looks
  private static final String[] FUNCTION_NAMES = new String[]{FUNC_MIN, FUNC_MAX, FUNC_MEAN, FUNC_MEDIAN, FUNC_SIGMA,
      FUNC_RSD, FUNC_ENL};

  public StxFunctions() {
    super("stx", -1, new int[]{Term.TYPE_D, Term.TYPE_S, Term.TYPE_B});
  }

  @Override
  public double evalD(EvalEnv env, Term[] args) throws EvalException {
    Exceptions.throwIf((args.length != 2) && (args.length != 3),
        new EvalException("The stx function takes either 2 or three parameters"));
    RasterDataNode raster = getRaster(args);
    String stxFunction = getStxFunction(env, args);
    Stx stx = raster.getStx(getAccurate(env, args), ProgressMonitor.NULL);
    switch (stxFunction) {
      case FUNC_MIN:
        return stx.getMinimum();
      case FUNC_MAX:
        return stx.getMaximum();
      case FUNC_MEAN:
        return stx.getMean();
      case FUNC_MEDIAN:
        return stx.getMedian();
      case FUNC_SIGMA:
        return stx.getStandardDeviation();
      case FUNC_RSD:
        return stx.getCoefficientOfVariation();
      case FUNC_ENL:
        return stx.getEquivalentNumberOfLooks();
      default:
        throw new EvalException("Second argument of stx() must be one of +" + Arrays.toString(FUNCTION_NAMES));
    }
  }

  private static RasterDataNode getRaster(Term[] args) {
    RasterDataNode raster = TermUtils.getRaster(args[0]);
    if (raster == null) {
      throw new EvalException("First argument of stx() must reference a raster");
    }
    return raster;
  }

  private static String getStxFunction(EvalEnv env, Term[] args) {
    if (!args[1].isS()) {
      throw new EvalException("Second argument of stx() must specify the statistical value");
    }
    return args[1].evalS(env).toLowerCase();
  }

  private static boolean getAccurate(EvalEnv env, Term[] args) {
    if (args.length > 2) {
      if (!args[2].isB()) {
        throw new EvalException(
            "If provided, third argument of stx() must be a boolean specifying if accurate statistics shall be computed");
      }
      return args[2].evalB(env);
    }
    return false; // not specified - default is false
  }


}
