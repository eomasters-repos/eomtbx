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

package org.eomasters.eomtbx.wvleditor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.ModalDialog;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

@ActionID(category = "View", id = "EOM_WvlEditorAction")
@ActionRegistration(
    displayName = "#CTL_WvlEditorAction_MenuText",
    popupText = "#CTL_WvlEditorAction_MenuText",
    iconBase = "org/eomasters/eomtbx/icons/WvlEditor_16.png"
)
@ActionReferences({
    @ActionReference(path = "Menu/Optical", position = 5),
    @ActionReference(path = "Context/Product/Product", position = 55),
})
@NbBundle.Messages({
    "CTL_WvlEditorAction_MenuText=Modify Wavelengths...",
    "CTL_WvlEditorAction_ShortDescription=Open editor to modify multiply wavelengths of one more or products"
})
public class WvlEditorAction extends AbstractAction implements HelpCtx.Provider {

  @Override
  public void actionPerformed(ActionEvent e) {
    ModalDialog modalDialog = new WvlEditorDialog(SnapApp.getDefault().getMainFrame());
    modalDialog.show();
  }

  @Override
  public HelpCtx getHelpCtx() {
    return new HelpCtx(WvlEditorDialog.HID_EOMTBX_WvlEditor);
  }

}
