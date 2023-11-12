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
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class PopupPanel extends JPanel {

  protected final JDialog dialog;

  public PopupPanel() {
    this(null);
  }

  public PopupPanel(JComponent component) {
    setLayout(new BorderLayout());
    setBorder(new BevelBorder(BevelBorder.RAISED));
    if (component != null) {
      add(component, BorderLayout.CENTER);
    }

    dialog = new JDialog();
    dialog.setUndecorated(true);
    dialog.setContentPane(this);
    dialog.setAlwaysOnTop(true);
  }

  public void showAt(int x, int y) {
    dialog.setLocation(x, y);
    dialog.pack();
    dialog.setVisible(true);
  }

  @Override
  public void setVisible(boolean aFlag) {
    super.setVisible(aFlag);
    dialog.setVisible(aFlag);
  }

  public void installTo(JComponent component) {
    component.addMouseListener(new ShowPopupPanelListener());
  }

  private class HidePopupPanelListener extends MouseAdapter {

    @Override
    public void mouseExited(MouseEvent e) {
      Rectangle bounds = getScreenLocationBounds(e.getComponent());
      if (!bounds.contains(e.getLocationOnScreen())) {
        dialog.setVisible(false);
        e.getComponent().removeMouseListener(this);
      }
    }

    // Makes the bounds independent of parent
    private Rectangle getScreenLocationBounds(Component component) {
      Rectangle bounds = component.getBounds();
      bounds.setLocation(getLocationOnScreen());
      return bounds;
    }
  }

  private class ShowPopupPanelListener extends MouseAdapter {

    @Override
    public void mouseEntered(MouseEvent e) {
      showAt(e.getLocationOnScreen().x - 10, e.getLocationOnScreen().y - 10);
      e.getComponent().addMouseListener(new HidePopupPanelListener());
    }
  }

}
