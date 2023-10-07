package org.eomasters.eomtbx.bandmathsext;

import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.dataop.barithm.RasterDataSymbol;
import org.esa.snap.core.jexp.Symbol;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.jexp.Term.Ref;

class TermUtils {

  static RasterDataNode getRaster(Term arg) {
    if (arg instanceof Ref) {
      Symbol rasterSymbol = ((Ref) arg).getSymbol();
      if (rasterSymbol instanceof RasterDataSymbol) {
        return ((RasterDataSymbol) rasterSymbol).getRaster();
      }
    }
    return null;
  }
}
