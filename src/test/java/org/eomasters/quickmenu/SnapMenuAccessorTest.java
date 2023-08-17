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

package org.eomasters.quickmenu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.junit.jupiter.api.Test;

class SnapMenuAccessorTest {

  @Test
  void getPath() {
    JMenuBar menuBar = new JMenuBar();
    JMenuItem raster = new JMenuItem("Raster");
    menuBar.add(raster);
    JPopupMenu rasterPopup = new JPopupMenu("Raster");
    rasterPopup.setInvoker(raster);
    JMenuItem subset = new JMenuItem("Subset");
    rasterPopup.add(subset);
    JMenuItem geometric = new JMenuItem("Geometric");
    rasterPopup.add(geometric);
    JMenuItem reproject = new JMenuItem("Reproject");
    geometric.add(reproject);
    JMenuItem resample = new JMenuItem("Resample");
    geometric.add(resample);

    assertEquals("Menu/Raster/", SnapMenuAccessor.getPath(subset));
    assertEquals("Menu/Raster/Geometric/", SnapMenuAccessor.getPath(reproject));
    assertEquals("Menu/Raster/Geometric/", SnapMenuAccessor.getPath(resample));
  }
}
