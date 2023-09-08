/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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

import java.awt.Dimension;
import java.io.IOException;
import java.util.Arrays;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.dataop.barithm.RasterDataEvalEnv;
import org.esa.snap.core.dataop.barithm.RasterDataSymbol;
import org.esa.snap.core.jexp.EvalEnv;
import org.esa.snap.core.jexp.EvalException;
import org.esa.snap.core.jexp.Symbol;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.jexp.Term.Ref;
import org.esa.snap.core.jexp.impl.AbstractFunction.D;

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

  private static String getWndFunction(EvalEnv env, Term[] args) {
    if (!args[2].isS()) {
      throw new EvalException("Third argument must be a string");
    }
    return args[2].evalS(env);
  }

  private static int getWndSize(EvalEnv env, Term[] args) {
    int wndSize = args[1].evalI(env);
    if (!isValidWndSize(wndSize)) {
      throw new EvalException("Second argument must be one of " + Arrays.toString(ALLOWED_WND_SIZES));
    }
    return wndSize;
  }

  private static RasterDataNode getRaster(Term[] args) {
    Term rasterTerm = args[0];
    if (!(rasterTerm instanceof Ref)) {
      throw new EvalException("First argument must reference a raster");
    }
    Symbol rasterSymbol = ((Ref) rasterTerm).getSymbol();
    if (!(rasterSymbol instanceof RasterDataSymbol)) {
      throw new EvalException("First argument must reference a raster");
    }
    return ((RasterDataSymbol) rasterSymbol).getRaster();
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

  @Override
  public double evalD(EvalEnv env, Term[] args) throws EvalException {
    RasterDataNode raster = getRaster(args);
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
        throw new EvalException("Third argument must be one of +" + Arrays.toString(FUNCTION_NAMES));
    }
  }

  private double sum(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = getNanFilteredData(raster, wndSize, env);
    return Arrays.stream(data).sum();
  }

  private double mean(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = getNanFilteredData(raster, wndSize, env);
    double sum = Arrays.stream(data).sum();
    return sum / data.length;
  }

  private double median(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = getNanFilteredData(raster, wndSize, env);
    Arrays.sort(data);
    long count = data.length;
    if (count % 2 == 0) {
      return (data[(int) (count / 2)] + data[(int) (count / 2 - 1)]) / 2;
    }
    return data[(int) Math.ceil(count / 2.)];
  }

  private double min(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = getNanFilteredData(raster, wndSize, env);
    return Arrays.stream(data).min().orElse(Double.NaN);
  }

  private double max(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = getNanFilteredData(raster, wndSize, env);
    return Arrays.stream(data).max().orElse(Double.NaN);
  }

  private double[] getNanFilteredData(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = new double[wndSize * wndSize];
    Arrays.fill(data, Double.NaN);
    RasterDataEvalEnv dataEvalEnv = (RasterDataEvalEnv) env;
    int centerX = dataEvalEnv.getPixelX();
    int centerY = dataEvalEnv.getPixelY();
    try {
      Dimension rasterSize = raster.getRasterSize();
      for (int i = 0; i < wndSize; i++) {
        int offsetI = i * wndSize;
        for (int j = 0; j < wndSize; j++) {
          int x = centerX - wndSize / 2 + i;
          int y = centerY - wndSize / 2 + j;
          if (x < 0 || x >= rasterSize.width || y < 0 || y >= rasterSize.height || !raster.isPixelValid(x, y)) {
            data[offsetI + j] = Double.NaN;
          } else {
            double pixelValue = raster.readPixels(x, y, 1, 1, new double[1])[0];
            data[offsetI + j] = pixelValue;
          }
        }
      }
    } catch (IOException e) {
      throw new EvalException("Error reading raster data", e);
    }
    return Arrays.stream(data).filter(d -> !Double.isNaN(d)).toArray();
  }

}
