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

package org.eomasters.eomtbx.assets.type.site;

import com.bc.ceres.swing.binding.BindingContext;
import java.awt.EventQueue;
import javax.swing.JFrame;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.converter.ConverterRegistrar;
import org.esa.snap.core.datamodel.Product;

public class SitesAddingGuiMain {

  public static void main(String[] args) {
    ConverterRegistrar.registerConverter();
    EventQueue.invokeLater(() -> {
      try {
        final var frame = new JFrame("SiteType GUI Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SitesType sitesType = new SitesType();
        var sitesTypeUi = new AddSitesGuiService();
        Asset asset = new Asset("name", sitesType);
        var configuration = sitesType.createAddConfiguration(asset, new Product("asd", "dasf", 10, 10));
        var context = new BindingContext(configuration);
        frame.setContentPane(sitesTypeUi.createPanel(context));
        frame.setLocationRelativeTo(null);
        frame.setLocation(100, 650);
        frame.setVisible(true);
        frame.pack();
      } catch (Exception e) {
        // noinspection CallToPrintStackTrace
        e.printStackTrace();
      }
    });
  }
}
