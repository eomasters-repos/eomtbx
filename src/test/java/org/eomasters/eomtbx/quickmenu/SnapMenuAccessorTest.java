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

package org.eomasters.eomtbx.quickmenu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.UIUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SnapMenuAccessorTest {

  private static JMenuBar menuBar;
  private static JMenuItem subset;
  private static JMenuItem reproject;
  private static JMenuItem resample;
  private static JMenuItem histogram;

  @BeforeAll
  static void beforeAll() {
    menuBar = new JMenuBar();
    JMenu subMenu = new JMenu("View");
    menuBar.add(subMenu);
    subMenu.add(new JMenuItem("Information"));
    histogram = new JMenuItem("Histogram");
    subMenu.add(histogram);
    JMenuItem raster = new JMenuItem("Raster");
    menuBar.add(raster);
    JPopupMenu rasterPopup = new JPopupMenu("Raster");
    raster.add(rasterPopup);
    rasterPopup.setInvoker(raster);
    subset = new JMenuItem("Subset");
    rasterPopup.add(subset);
    JMenuItem geometric = new JMenuItem("Geometric");
    rasterPopup.add(geometric);
    reproject = new JMenuItem("Reproject");
    geometric.add(reproject);
    resample = new JMenuItem("Resample");
    geometric.add(resample);
  }

  @Test
  void getMenuBar() {
    SnapApp snapApp = SnapApp.getDefault();
    JFrame rootFrame = UIUtils.getRootJFrame(snapApp.getMainFrame());
    rootFrame.setJMenuBar(menuBar);

    assertSame(menuBar, SnapMenuAccessor.getMenuBar());
  }

  @Test
  void findItem() {
    SnapApp snapApp = SnapApp.getDefault();
    JFrame rootFrame = UIUtils.getRootJFrame(snapApp.getMainFrame());
    rootFrame.setJMenuBar(menuBar);

    assertSame(subset, SnapMenuAccessor.findMenuItem(new ActionRef("subset", new MenuRef("Menu/Raster/", "Subset"))));
    assertSame(resample,
        SnapMenuAccessor.findMenuItem(new ActionRef("resample", new MenuRef("Menu/Raster/Geometric/", "Resample"))));
    assertSame(histogram,
        SnapMenuAccessor.findMenuItem(new ActionRef("histogram", new MenuRef("Menu/View/", "Histogram"))));
  }

  @Test
  void getPath() {
    assertEquals("Menu/Raster/", SnapMenuAccessor.getPath(subset));
    assertEquals("Menu/Raster/Geometric/", SnapMenuAccessor.getPath(reproject));
    assertEquals("Menu/Raster/Geometric/", SnapMenuAccessor.getPath(resample));
    assertEquals("Menu/View/", SnapMenuAccessor.getPath(histogram));
  }
}
