/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/eomtbx
 * ======================================================================
 * Copyright (C) 2023 - 2026 Marco Peters
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

package org.eomasters.eomtbx.s2superres;

import java.util.List;
import org.esa.snap.core.datamodel.Mask;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductNodeGroup;

public class MaskHandler {

  static void copyMasks(Product source, Product target, List<String> requestedSpectralBands) {
    final ProductNodeGroup<Mask> sourceMaskGroup = source.getMaskGroup();
    for (int i = 0; i < sourceMaskGroup.getNodeCount(); i++) {
      final Mask mask = sourceMaskGroup.get(i);
      String maskName = mask.getName();
      String refBandName = extractRefBandName(maskName);
      if (refBandName == null || requestedSpectralBands.contains(refBandName)) {
        if (!target.getMaskGroup().contains(maskName)
            && mask.getImageType().canTransferMask(mask, target)) {
          mask.getImageType().transferMask(mask, target);
        }
      }
    }
  }

  private static String extractRefBandName(String maskName) {
    int underscoreIndex = maskName.lastIndexOf("_");
    return underscoreIndex > -1 ? maskName.substring(underscoreIndex + 1) : null;
  }
}
