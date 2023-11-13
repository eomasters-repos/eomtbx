/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
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
 * -> http://www.gnu.org/licenses/gpl-3.0.html
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.utils.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

public class DropdownButton extends JButton {

  private final JPopupMenu popupMenu;

  public DropdownButton(ImageIcon icon) {
    this(new JLabel(icon));
  }

  public DropdownButton(String label) {
    this(new JLabel(label));
  }

  private DropdownButton(JLabel westLabel) {
    setLayout(new BorderLayout(5, 0));
    westLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
    add(westLabel, BorderLayout.WEST);
    JLabel arrowLabel = new JLabel("▼");
    add(arrowLabel, BorderLayout.EAST);

    popupMenu = new JPopupMenu();
    popupMenu.setVisible(false);

    addActionListener(this::actionPerformed);
  }

  public void setPopupComponent(JComponent component) {
    popupMenu.removeAll();
    popupMenu.add(component);
  }

  public void showPopup(boolean show) {
    if(show) {
      popupMenu.show(DropdownButton.this, 0, DropdownButton.this.getHeight());
    } else {
      popupMenu.setVisible(false);
    }
  }

  private void actionPerformed(ActionEvent e) {
    showPopup(!popupMenu.isVisible());
  }

}
