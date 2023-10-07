package org.eomasters.eomtbx.bandmathsext;

import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.dataop.barithm.RasterDataEvalEnv;
import org.esa.snap.core.jexp.EvalEnv;
import org.esa.snap.core.jexp.EvalException;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.jexp.impl.AbstractFunction.B;

public class AreValidFunction extends B {

  public AreValidFunction() {
    super("areValid", -1);
  }

  @Override
  public boolean evalB(EvalEnv env, Term[] args) throws EvalException {
    RasterDataEvalEnv dataEvalEnv = (RasterDataEvalEnv) env;
    for (Term arg : args) {
      RasterDataNode raster = TermUtils.getRaster(arg);
      if (raster == null) {
        throw new EvalException("Arguments of areValid() must reference a raster");
      }
      boolean pixelValid = raster.isPixelValid(dataEvalEnv.getPixelX(), dataEvalEnv.getPixelY());
      if (!pixelValid) {
        return false;
      }
    }
    return true;
  }
}
