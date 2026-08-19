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

package org.eomasters.eomtbx.spex.gui;

import javax.swing.JPanel;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.snap.gui.gpf.AbstractOpAction;
import org.eomasters.snap.gui.gpf.OperatorDialog;
import org.eomasters.snap.gui.gpf.ParametersPanel;
import org.esa.snap.core.gpf.descriptor.OperatorDescriptor;
import org.esa.snap.ui.DefaultAppContext;

public class SpexOpAction extends AbstractOpAction {

  private static final String HELP_ID = "eomtbx.spex.processor.gui";
  private static final String SPEX_OP_NAME = "Spex";
  private static final String DIALOG_TITLE = "SpeX - Spectral Indices";
  private static final String ACTION_NAME = "SpeX Processor";

  public SpexOpAction() {
    super(ACTION_NAME, EomtbxIcons.SPEX_OP, SPEX_OP_NAME, DIALOG_TITLE, HELP_ID);
  }

  protected ParametersPanel<JPanel> getParametersPanel(OperatorDescriptor operatorDescriptor) {
    return new SpexOpParametersPanel(operatorDescriptor);
  }

  public static void main(String[] args) {
    final DefaultAppContext context = new DefaultAppContext("spexOp");
    SpexOpAction opAction = new SpexOpAction();
    final OperatorDialog dialog = opAction.createDialog(context);
    dialog.show();
  }
}
