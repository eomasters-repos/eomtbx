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

package org.eomasters.eomtbx.ciop.gui;

import javax.swing.JPanel;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.snap.gui.gpf.AbstractOpAction;
import org.eomasters.snap.gui.gpf.OperatorDialog;
import org.eomasters.snap.gui.gpf.ParametersPanel;
import org.esa.snap.core.gpf.descriptor.OperatorDescriptor;
import org.esa.snap.ui.DefaultAppContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Processors", id = "org.esa.eomtbx.ciop.gui.CyanoIndexOpAction")
@ActionRegistration(displayName = "#TXT_MenuTitle", lazy = false)
@ActionReference(path = "Menu/Optical/Thematic Water Processing", position = 230)
@NbBundle.Messages({"TXT_MenuTitle=SpeX"})
public class CyanoIndexOpAction extends AbstractOpAction {

  private static final String HELP_ID = "eomtbx.ciop.processor.gui";
  protected static final String OP_NAME = "CyanoIndex";
  protected static final String DIALOG_TITLE = "Cyanobacteria Index (CI)";
  protected static final String ACTION_NAME = "Cyanobacteria Index (CI)";

  public CyanoIndexOpAction() {
    super(ACTION_NAME, EomtbxIcons.EOMTBX, OP_NAME, DIALOG_TITLE, HELP_ID);
  }

  protected ParametersPanel<JPanel> getParametersPanel(OperatorDescriptor operatorDescriptor) {
    return new CyanoIndexParametersPanel(operatorDescriptor);
  }

  public static void main(String[] args) {
    final DefaultAppContext context = new DefaultAppContext(OP_NAME);
    CyanoIndexOpAction opAction = new CyanoIndexOpAction();
    final OperatorDialog dialog = opAction.createDialog(context);
    dialog.show();
  }
}
