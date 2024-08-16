/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Basic for SNAP
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

package org.eomasters.eomtbx.actions;

import java.awt.event.ActionEvent;
import java.net.URI;
import javax.swing.AbstractAction;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.icons.Icon;
import org.esa.snap.rcp.util.BrowserUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Action that opens a dialog showing a system report.
 */
@ActionID(category = "Tools", id = "EOMTBX_CoastalMap")
@ActionRegistration(displayName = "#CoastalMapName", lazy = false)
//#Action 'Report an Issue' is at 305, and 'Tutorials' is 310, so we use 306
@ActionReference(path = "Menu/Help", position = 308)
@NbBundle.Messages({"CoastalMapName=Coastal Map Data"})
public class CoastalMapAction extends AbstractAction {

  /**
   * Creates a new Action.
   */
  public CoastalMapAction() {
    super(Bundle.CoastalMapName(), EomtbxIcons.COASTAL_MAP.getImageIcon(Icon.SIZE_16));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    BrowserUtils.openInBrowser(URI.create("https://www.eomasters.org/knowledge/coastalmap"));
  }

}
