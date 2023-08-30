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

import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.dataop.barithm.RasterDataEvalEnv;
import org.esa.snap.core.jexp.EvalEnv;
import org.esa.snap.core.jexp.EvalException;
import org.esa.snap.core.jexp.Symbol;
import org.esa.snap.core.jexp.Term;

/**
 * Allows to check iF a pixel is valid or not. The implementation uses
 * {@link RasterDataNode#isPixelValid(int, int) isPixelValid(x, y)} to check if a pixel is valid or not. In a band math
 * expression the symbol is used as follows:
 * <pre>
 *   &lt;band_name&gt;.invalid
 * </pre>
 */
public class InvalidSymbol implements Symbol {

  private final String symbolName;
  private final int symbolType;
  private final RasterDataNode raster;

  /**
   * Creates a new instance.
   *
   * @param symbolName the name of the symbol
   * @param raster     the raster data node
   */
  public InvalidSymbol(final String symbolName, final RasterDataNode raster) {
    this.symbolName = symbolName;
    this.symbolType = Term.TYPE_B;
    this.raster = raster;
  }

  @Override
  public String getName() {
    return symbolName;
  }

  @Override
  public int getRetType() {
    return symbolType;
  }

  @Override
  public boolean isConst() {
    return false;
  }

  @Override
  public boolean evalB(final EvalEnv env) throws EvalException {
    RasterDataEvalEnv rasterEnv = (RasterDataEvalEnv) env;
    return !raster.isPixelValid(rasterEnv.getPixelX(), rasterEnv.getPixelY());
  }

  @Override
  public int evalI(final EvalEnv env) throws EvalException {
    return Term.toI(evalB(env));
  }

  @Override
  public double evalD(final EvalEnv env) throws EvalException {
    return Term.toD(evalB(env));
  }

  @Override
  public String evalS(EvalEnv env) throws EvalException {
    return Boolean.toString(evalB(env));
  }

}
