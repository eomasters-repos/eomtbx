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

package org.eomasters.eomtbx.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.eomasters.gui.CollapsiblePanel;
import org.eomasters.gui.Dialogs;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.eomasters.snap.utils.SnapSystemReport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Action that opens a dialog showing a system report.
 */
@ActionID(category = "Help", id = "EOMTBX_SystemReport")
@ActionRegistration(displayName = "#SystemReportActionName", lazy = false)
//#Action 'Report an Issue' is at 305, and 'Tutorials' is 310, so we use 306
@ActionReference(path = "Menu/Help", position = 306)
@NbBundle.Messages({"SystemReportActionName=Create System Report"})
public class SystemReportAction extends AbstractAction {

  /**
   * Creates a new Action.
   */
  public SystemReportAction() {
    super(Bundle.SystemReportActionName(), Icons.REPORT.getImageIcon(Icon.SIZE_16));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    SnapSystemReport systemReport = new SnapSystemReport().name("SNAP_System_Report").logTail(100);
    // JPanel reportPanel = new JPanel(new BorderLayout());
    CollapsiblePanel reportArea = CollapsiblePanel.createLongTextPanel("System Report", systemReport.generate());
    reportArea.setCollapsed(false);

    Dialogs.message("System Report", Icons.REPORT.getImageIcon(Icon.SIZE_48), reportArea);
  }

}
