/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Basic for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2024 Marco Peters
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

import java.io.IOException;
import java.util.Arrays;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.dataop.barithm.RasterDataEvalEnv;
import org.esa.snap.core.jexp.EvalEnv;
import org.esa.snap.core.jexp.EvalException;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.jexp.impl.AbstractFunction.D;

/**
 * Window functions for band math expressions.
 *
 * <p>The following functions are available:
 *  <ul>
 *    <li>sum: Sum of all valid pixels in the window</li>
 *    <li>min: Minimum of all valid pixels in the window</li>
 *    <li>max: Maximum of all valid pixels in the window</li>
 *    <li>mean: Mean of all valid pixels in the window</li>
 *    <li>median: Median of all valid pixels in the window</li>
 * </ul>
 * </p>
 *   The window is centered on the current pixel and the window size must be 3, 5 or 7.
 */
class WindowFunctions extends D {

  private static final int[] ALLOWED_WND_SIZES = new int[]{3, 5, 7};
  private static final String FUNC_SUM = "sum";
  private static final String FUNC_MIN = "min";
  private static final String FUNC_MAX = "max";
  private static final String FUNC_MEAN = "mean";
  private static final String FUNC_MEDIAN = "median";
  private static final String[] FUNCTION_NAMES = new String[]{FUNC_SUM, FUNC_MIN, FUNC_MAX, FUNC_MEAN, FUNC_MEDIAN};

  public WindowFunctions() {
    super("wnd", 3, new int[]{Term.TYPE_D, Term.TYPE_I, Term.TYPE_S});
  }

  @Override
  public double evalD(EvalEnv env, Term[] args) throws EvalException {
    RasterDataNode raster = TermUtils.getRaster(args[0]);
    if (raster == null) {
      throw new EvalException("First argument of wnd() must reference a raster");
    }

    int wndSize = getWndSize(env, args);
    String wndFunction = getWndFunction(env, args);

    switch (wndFunction) {
      case FUNC_SUM:
        return sum(raster, wndSize, env);
      case FUNC_MIN:
        return min(raster, wndSize, env);
      case FUNC_MAX:
        return max(raster, wndSize, env);
      case FUNC_MEAN:
        return mean(raster, wndSize, env);
      case FUNC_MEDIAN:
        return median(raster, wndSize, env);
      default:
        throw new EvalException("Third argument of wnd() must be one of +" + Arrays.toString(FUNCTION_NAMES));
    }
  }

  private static int getWndSize(EvalEnv env, Term[] args) {
    int wndSize = args[1].evalI(env);
    if (!isValidWndSize(wndSize)) {
      throw new EvalException("Second argument of wnd() must be one of " + Arrays.toString(ALLOWED_WND_SIZES));
    }
    return wndSize;
  }

  private static String getWndFunction(EvalEnv env, Term[] args) {
    if (!args[2].isS()) {
      throw new EvalException("Third argument of wnd() must be a string");
    }
    return args[2].evalS(env);
  }

  private static boolean isValidWndSize(int wndSize) {
    boolean found = false;
    for (int allowedSize : ALLOWED_WND_SIZES) {
      if (wndSize == allowedSize) {
        found = true;
        break;
      }
    }
    return found;
  }

  private double sum(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = getFilteredData(raster, wndSize, env);
    return data.length == 0 ? Double.NaN : Arrays.stream(data).sum();
  }

  private double mean(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = getFilteredData(raster, wndSize, env);
    return data.length == 0 ? Double.NaN : Arrays.stream(data).sum() / data.length;
  }

  private double median(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = getFilteredData(raster, wndSize, env);
    if (data.length == 0) {
      return Double.NaN;
    }
    Arrays.sort(data);
    long count = data.length;
    if (count % 2 == 0) {
      return (data[(int) (count / 2)] + data[(int) (count / 2 - 1)]) / 2;
    }
    return data[(int) Math.ceil(count / 2.)];
  }

  private double min(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = getFilteredData(raster, wndSize, env);
    return Arrays.stream(data).min().orElse(Double.NaN);
  }

  private double max(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = getFilteredData(raster, wndSize, env);
    return Arrays.stream(data).max().orElse(Double.NaN);
  }

  // returns only valid and not NaN value scaled to geophysical values
  private double[] getFilteredData(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = new double[wndSize * wndSize];
    Arrays.fill(data, Double.NaN);
    RasterDataEvalEnv dataEvalEnv = (RasterDataEvalEnv) env;
    int centerX = dataEvalEnv.getPixelX();
    int centerY = dataEvalEnv.getPixelY();
    try {
      for (int i = 0; i < wndSize; i++) {
        int offsetI = i * wndSize;
        int x = centerX - wndSize / 2 + i;
        for (int j = 0; j < wndSize; j++) {
          int y = centerY - wndSize / 2 + j;
          if (raster.isPixelWithinImageBounds(x, y) && raster.isPixelValid(x, y)) {
            data[offsetI + j] = raster.readPixels(x, y, 1, 1, new double[1])[0];
          }
        }
      }
    } catch (IOException e) {
      throw new EvalException("Error reading raster data", e);
    }
    return Arrays.stream(data).filter(d -> !Double.isNaN(d)).toArray();
  }

}
