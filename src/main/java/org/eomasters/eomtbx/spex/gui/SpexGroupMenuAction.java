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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.icons.Icon;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

@ActionID(category = "Processors", id = "org.esa.eomtbx.spex.gui.SpexGroupMenuAction")
@ActionRegistration(displayName = "#TXT_SpexGroupMenuTitle", lazy = false)
@ActionReference(path = "Menu/Optical", position = 6)
@NbBundle.Messages({"TXT_SpexGroupMenuTitle=SpeX"})
public class SpexGroupMenuAction extends AbstractAction implements Presenter.Menu {

  public SpexGroupMenuAction() {
    putValue(NAME, Bundle.TXT_SpexGroupMenuTitle());
    putValue(SMALL_ICON, EomtbxIcons.SPEX_OP.getImageIcon(Icon.SIZE_16));
    putValue(LARGE_ICON_KEY, EomtbxIcons.SPEX_OP.getImageIcon(Icon.SIZE_32));
  }

  public void actionPerformed(ActionEvent e) {
    // nothing to do
  }

  @Override
  public JMenuItem getMenuPresenter() {
    JMenu groupItem = new JMenu(Bundle.TXT_SpexGroupMenuTitle());
    groupItem.setIcon(EomtbxIcons.SPEX_OP.getImageIcon(Icon.SIZE_16));
    groupItem.add(new JMenuItem(new SpexOpAction()));
    groupItem.add(new JMenuItem(new SpexDbOptionsAction()));
    return groupItem;
  }

}
