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

package org.eomasters.eomtbx.assets.type.geometry;

import static org.eomasters.eomtbx.assets.type.geometry.WktGeometryCreationService.PROP_CRS;
import static org.eomasters.eomtbx.assets.type.geometry.WktGeometryCreationService.PROP_WKT;

import com.bc.ceres.swing.binding.BindingContext;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.assets.AssetCreationService;
import org.eomasters.eomtbx.assets.gui.AssetCreationGuiService;

/**
 * This class represents the GUI component for creating WKT Geometry assets. It is necessary to have a multi-line text
 * area.
 */
public class WktGeometryCreationGuiService extends AssetCreationGuiService {

  @Override
  public Class<? extends AssetCreationService> getFactoryType() {
    return WktGeometryCreationService.class;
  }

  @Override
  public JPanel createPanel(BindingContext context) {
    JPanel panel = new JPanel(new MigLayout("", "[][grow, fill]"));
    JTextArea wktArea = new JTextArea(3, 45);
    wktArea.setLineWrap(true);
    JTextField crsField = new JTextField();

    panel.add(new JLabel("WKT:"));
    JScrollPane wktScrollPane = new JScrollPane(wktArea);
    wktScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    wktScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    panel.add(wktScrollPane, "growy, pushy, wrap");
    panel.add(new JLabel("CRS:"));
    panel.add(crsField);

    context.bind(PROP_WKT, wktArea);
    context.bind(PROP_CRS, crsField);

    return panel;
  }

  @Override
  public String getHelpId() {
    return super.getHelpId() + ".wktGeometry";
  }
}
