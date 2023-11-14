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

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;

public class PopupPanel {

  private int delay = 500;
  private final JComponent component;
  protected final JPopupMenu popup;
  private JComponent invoker;

  public PopupPanel(JComponent component) {
    popup = new JPopupMenu();
    popup.setBorder(new BevelBorder(BevelBorder.RAISED));
    this.component = component;
    if (this.component != null) {
      popup.add(component);
    }
    popup.pack();
  }

  public void setPopupDelay(int delay) {
    this.delay = delay;
  }

  public void showPopupAt(int x, int y) {
    popup.show(invoker, x, y);
  }

  public void hidePopup() {
    popup.setVisible(false);
  }

  public void installTo(JComponent component) {
    invoker = component;
    invoker.addMouseListener(new ShowPopupPanelListener());
  }

  // Makes the bounds independent of parent
  private Rectangle getScreenLocationBounds(Component component) {
    Rectangle bounds = component.getBounds();
    synchronized (component.getTreeLock()) {
      if (component.isShowing()) {
        bounds.setLocation(component.getLocationOnScreen());
      }
    }
    return bounds;
  }

  private class HidePopupPanelListener extends MouseAdapter {

    @Override
    public void mouseExited(MouseEvent e) {
      if (!component.isVisible()) {
        return;
      }
      Rectangle bounds = getScreenLocationBounds(component);
      if (!bounds.contains(e.getLocationOnScreen())) {
        hidePopup();
      }
    }
  }

  private class ShowPopupPanelListener extends MouseAdapter {

    @Override
    public void mouseEntered(MouseEvent e) {
      // popup should be made visible only if the mouse is still over the invoker after a short delay
      ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
      executor.schedule(() -> {
        Point mousePosition = invoker.getMousePosition(true);
        if (mousePosition != null) {
          showPopupAt(mousePosition.x - 10, mousePosition.y - 10);
          component.addMouseListener(new HidePopupPanelListener());
        }
      }, delay, TimeUnit.MILLISECONDS);
    }

  }

}
