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

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.dataop.barithm.RasterDataEvalEnv;
import org.esa.snap.core.jexp.EvalEnv;
import org.esa.snap.core.jexp.Function;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.jexp.impl.AbstractFunction;

/**
 * Implements various functions for which can take a variable length of arguments. Currently, these are MIN, MAX and
 * MEAN. All implementations exclude invalid pixels from the calculation.
 *
 * @author Marco Peters
 */
class MultiInputFunctions {

  /**
   * Function that returns the minimum value of the arguments.
   */
  static final Function MIN = new AbstractFunction.D("minOf", -1) {

    public double evalD(final EvalEnv env, final Term[] args) {
      return Arrays.stream(args)
                   .filter(new RemoveInvalidPixels(env))
                   .map(term -> term.evalD(env)).filter(Double::isFinite)
                   .min(Double::compare).orElse(Double.NaN);
    }
  };

  /**
   * Function that returns the maximum value of the arguments.
   */
  static final Function MAX = new AbstractFunction.D("maxOf", -1) {

    public double evalD(final EvalEnv env, final Term[] args) {
      return Arrays.stream(args)
                   .filter(new RemoveInvalidPixels(env))
                   .map(term -> term.evalD(env)).filter(Double::isFinite)
                   .max(Double::compare).orElse(Double.NaN);
    }
  };

  /**
   * Function that returns the index of the minimum value of the arguments.
   */
  static final Function INDEX_OF_MIN = new AbstractFunction.I("indexOfMin", -1) {

    public int evalI(final EvalEnv env, final Term[] args) {
      List<Double> valueList = Arrays.stream(args).map(new ReplaceInvalidTermsByNaN(env)).
                                     map(term -> term.evalD(env)).collect(Collectors.toList());

      int minIndex = -1;
      for (int i = 0; i < valueList.size(); i++) {
        if (!Double.isNaN(valueList.get(i)) && (minIndex == -1 || valueList.get(i) < valueList.get(minIndex))) {
          minIndex = i;
        }
      }
      return minIndex;
    }
  };

  /**
   * Function that returns the index of the maximum value of the arguments.
   */
  static final Function INDEX_OF_MAX = new AbstractFunction.I("indexOfMax", -1) {
    public int evalI(final EvalEnv env, final Term[] args) {
      List<Double> valueList = Arrays.stream(args).map(new ReplaceInvalidTermsByNaN(env)).
                                     map(term -> term.evalD(env)).collect(Collectors.toList());
      int maxIndex = -1;
      for (int i = 0; i < valueList.size(); i++) {
        if (!Double.isNaN(valueList.get(i)) && (maxIndex == -1 || valueList.get(i) > valueList.get(maxIndex))) {
          maxIndex = i;
        }
      }
      return maxIndex;
    }
  };

  /**
   * Function that returns the mean value of the arguments.
   */
  static final Function MEAN = new AbstractFunction.D("meanOf", -1) {

    public double evalD(final EvalEnv env, final Term[] args) {
      double[] values = Arrays.stream(args)
                              .filter(new RemoveInvalidPixels(env))
                              .map(term -> term.evalD(env))
                              .filter(Double::isFinite)
                              .mapToDouble(Double::doubleValue)
                              .toArray();
      if (values.length == 0) {
        return Double.NaN;
      }
      return DoubleStream.of(values).sum() / values.length;
    }
  };

  private static class RemoveInvalidPixels implements Predicate<Term> {

    private final EvalEnv env;

    public RemoveInvalidPixels(EvalEnv env) {
      this.env = env;
    }

    @Override
    public boolean test(Term term) {
      RasterDataNode raster = TermUtils.getRaster(term);
      if (raster != null) {
        RasterDataEvalEnv dataEvalEnv = (RasterDataEvalEnv) env;
        return raster.isPixelValid(dataEvalEnv.getPixelX(), dataEvalEnv.getPixelY());
      }
      return true; // okay, not a raster
    }
  }

  private static class ReplaceInvalidTermsByNaN implements java.util.function.Function<Term, Term> {

    private final EvalEnv env;

    public ReplaceInvalidTermsByNaN(EvalEnv env) {
      this.env = env;
    }

    @Override
    public Term apply(Term term) {
      RasterDataNode raster = TermUtils.getRaster(term);
      if (raster != null) {
        RasterDataEvalEnv dataEvalEnv = (RasterDataEvalEnv) env;
        boolean pixelValid = raster.isPixelValid(dataEvalEnv.getPixelX(), dataEvalEnv.getPixelY());
        return pixelValid ? term : Term.ConstD.NAN;
      }
      return term; // okay, not a raster
    }
  }
}
