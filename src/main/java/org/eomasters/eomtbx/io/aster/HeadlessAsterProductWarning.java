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

package org.eomasters.eomtbx.io.aster;

import org.eomasters.eomtbx.EomtbxRuntime;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductManager.Event;
import org.esa.snap.core.datamodel.ProductManager.Listener;

class HeadlessAsterProductWarning implements Listener {

  @Override
  public void productAdded(Event event) {
    Product product = event.getProduct();
    if (product.getProductReader() instanceof AsterProductReader) {
      EomtbxRuntime.LOGGER.warning(
          "Be careful when using ASTER data with BEAM-DIMAP and ZNAP format. Tie-point grids might not be correctly stored."
              + "Check if issue in SNAP is fixed: https://forum.step.esa.int/t/znap-dimap-format-does-not-preserve-tie-point-geo-dings-correctly/43047");
    }
  }

  @Override
  public void productRemoved(Event event) {
  }
}
