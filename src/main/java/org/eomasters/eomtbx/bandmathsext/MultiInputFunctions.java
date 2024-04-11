package org.eomasters.eomtbx.bandmathsext;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.dataop.barithm.RasterDataEvalEnv;
import org.esa.snap.core.jexp.EvalEnv;
import org.esa.snap.core.jexp.Function;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.jexp.impl.AbstractFunction;

public class MultiInputFunctions {

  public static final Function MIN = new AbstractFunction.D("minOf", -1) {

    public double evalD(final EvalEnv env, final Term[] args) {
      return Arrays.stream(args)
                   .filter(new RemoveInvalidPixels(env))
                   .map(term -> term.evalD(env)).filter(Double::isFinite)
                   .min(Double::compare).orElse(Double.NaN);
    }
  };

  public static final Function MAX = new AbstractFunction.D("maxOf", -1) {

    public double evalD(final EvalEnv env, final Term[] args) {
      return Arrays.stream(args)
                   .filter(new RemoveInvalidPixels(env))
                   .map(term -> term.evalD(env)).filter(Double::isFinite)
                   .max(Double::compare).orElse(Double.NaN);
    }
  };

  public static final Function MEAN = new AbstractFunction.D("meanOf", -1) {

    public double evalD(final EvalEnv env, final Term[] args) {
      double[] values = Arrays.stream(args)
                             .filter(new RemoveInvalidPixels(env))
                             .map(term -> term.evalD(env))
                             .filter(Double::isFinite)
                             .mapToDouble(Double::doubleValue)
                             .toArray();
      if(values.length == 0) {
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
}
