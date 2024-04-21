/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
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

import java.util.List;
import org.eomasters.eomtbx.bandmathsext.MapPosSymbol.SymbolMapX;
import org.eomasters.eomtbx.bandmathsext.MapPosSymbol.SymbolMapY;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.dataop.barithm.ProductNamespaceExtender;
import org.esa.snap.core.jexp.WritableNamespace;

/**
 * Extends the namespace of a product.
 */
public class BandMathsExtender implements ProductNamespaceExtender {

  @Override
  public void extendNamespace(Product product, String namePrefix, WritableNamespace namespace) {
    List<RasterDataNode> rasterDataNodes = product.getRasterDataNodes();
    for (RasterDataNode rasterDataNode : rasterDataNodes) {
      namespace.registerSymbol(new ValidSymbol(namePrefix, rasterDataNode));
    }
    namespace.registerSymbol(new SymbolMapX(product.getSceneGeoCoding(), product.getSceneRasterSize()));
    namespace.registerSymbol(new SymbolMapY(product.getSceneGeoCoding(), product.getSceneRasterSize()));

    namespace.registerFunction(new WindowFunctions());

    namespace.registerFunction(new AreValidFunction());

    namespace.registerFunction(MultiInputFunctions.MIN);
    namespace.registerFunction(MultiInputFunctions.MAX);
    namespace.registerFunction(MultiInputFunctions.MEAN);
  }

}
