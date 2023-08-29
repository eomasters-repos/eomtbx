/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * --> https://www.eomasters.org/sw/EOMTBX
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
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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

class WindowFunction extends D {

  private static final int[] ALLOWED_WND_SIZES = new int[]{3, 5, 7};

  public WindowFunction() {
    super("wnd", 3, new int[]{Term.TYPE_D, Term.TYPE_I, Term.TYPE_S});
  }

  @Override
  public double evalD(EvalEnv env, Term[] args) throws EvalException {
    RasterDataNode raster = getRaster(args);
    int wndSize = getWndSize(env, args);
    String wndFunction = getWndFunction(env, args);

    switch (wndFunction) {
      case "sum":
        return sum(raster, wndSize, env);
      case "mean":
        return mean(raster, wndSize, env);
      case "median":
        return median(raster, wndSize, env);
      case "min":
        return min(raster, wndSize, env);
      case "max":
        return max(raster, wndSize, env);
      default:
        if (wndFunction.startsWith("p")) {
          return percentile(raster, wndSize, Integer.parseInt(wndFunction.substring(1)), env);
        } else {
          throw new EvalException("Third argument must be 'mean', 'median', 'min' or 'max'");
        }
    }
  }

  private double sum(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = getDataArray(raster, wndSize, env);
    return Arrays.stream(data).sum();
  }

  private double mean(RasterDataNode raster, int wndSize, EvalEnv env) {
    // not correct; needs to consider NaNs
    double sum = sum(raster, wndSize, env);
    return sum / (wndSize * wndSize);
  }

  private double median(RasterDataNode raster, int wndSize, EvalEnv env) {
    // not correct; needs to consider NaNs
    double[] dataArray = getDataArray(raster, wndSize, env);
    Arrays.sort(dataArray);
    return dataArray[wndSize * wndSize / 2];
  }

  private double min(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = getDataArray(raster, wndSize, env);
    return Arrays.stream(data).min().orElse(Double.NaN);
  }

  private double max(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = getDataArray(raster, wndSize, env);
    return Arrays.stream(data).max().orElse(Double.NaN);
  }

  private double percentile(RasterDataNode raster, int wndSize, int percentile, EvalEnv env) {
    return 0;
  }

  private double[] getDataArray(RasterDataNode raster, int wndSize, EvalEnv env) {
    double[] data = new double[wndSize * wndSize];
    Arrays.fill(data, Double.NaN);
    RasterDataEvalEnv dataEvalEnv = (RasterDataEvalEnv) env;
    int centerX = dataEvalEnv.getPixelX();
    int centerY = dataEvalEnv.getPixelY();
    try {
      Dimension rasterSize = raster.getRasterSize();
      for (int i = 0; i < wndSize; i++) {
        int iOffset = i * wndSize;
        for (int j = 0; j < wndSize; j++) {
          int x = centerX - wndSize / 2 + i;
          int y = centerY - wndSize / 2 + j;
          if (x < 0 || x >= rasterSize.width || y < 0 || y >= rasterSize.height || raster.isPixelValid(x, y)) {
            data[iOffset + j] = Double.NaN;
          } else {
            double pixelValue = raster.readPixels(x, y, 1, 1, new double[1])[0];
            data[iOffset + j] = pixelValue;
          }
        }
      }
    } catch (IOException e) {
      throw new EvalException("Error reading raster data", e);
    }
    return data;
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

}
