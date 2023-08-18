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

package org.eomasters.quickmenu;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.event.MouseInputAdapter;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.UIUtils;

public class SnapMenuAccessor {

  public static void addListenersToMenuItems(List<ActionRef> actionRefs, MenuElement parent) {
    if (parent.getSubElements().length == 0) {
      if (parent instanceof JMenuItem) {
        JMenuItem menuItem = (JMenuItem) parent;
        String text = menuItem.getText();
        String path = getPath(menuItem);
        Optional<ActionRef> optional = actionRefs.stream().filter(actionRef -> actionRefMatches(actionRef, text, path))
            .findFirst();
        optional.ifPresent(actionRef -> menuItem.addActionListener(e -> actionRef.incrementClicks()));
      }
    }
    MenuElement[] subElements = parent.getSubElements();
    for (MenuElement subElement : subElements) {
      addListenersToMenuItems(actionRefs, subElement);
    }
  }

  public static void initClickCounter() {
    JMenuBar menuBar = getMenuBar();
    MenuElement[] subElements = menuBar.getSubElements();
    for (MenuElement subElement : subElements) {
      if (subElement instanceof JMenuItem) {
        JMenuItem menuItem = (JMenuItem) subElement;
        menuItem.addMouseListener(new ClickCounterAdderListener());
      }
    }
  }

  public static JMenuItem findMenuItem(ActionRef quickMenuItem) {
    return doFindMenuItem(quickMenuItem, getMenuBar());
  }

  private static JMenuItem doFindMenuItem(ActionRef actionRef, MenuElement parent) {
    if (parent.getSubElements().length == 0) {
      if (parent instanceof JMenuItem) {
        JMenuItem menuItem = (JMenuItem) parent;
        String text = menuItem.getText();
        if (actionRef.getDisplayName().equals(text)) {
          String path = getPath(menuItem);
          if (actionRef.getPath().equals(path)) {
            return menuItem;
          }
        }
      }
    }
    MenuElement[] subElements = parent.getSubElements();
    for (MenuElement subElement : subElements) {
      JMenuItem menuItem = doFindMenuItem(actionRef, subElement);
      if (menuItem != null) {
        return menuItem;
      }
    }
    return null;
  }

  public static JMenuBar getMenuBar() {
    SnapApp snapApp = SnapApp.getDefault();
    JFrame rootFrame = UIUtils.getRootJFrame(snapApp.getMainFrame());
    return rootFrame.getJMenuBar();
  }

  static String getPath(JMenuItem menuItem) {
    Component element = menuItem.getParent();
    StringBuilder path = new StringBuilder();
    while (element != null) {
      if (element instanceof JMenuItem) {
        JMenuItem item = (JMenuItem) element;
        String text = item.getText();
        if (!text.isBlank()) {
          path.insert(0, text + "/");
        }
      }
      if (element instanceof JPopupMenu) {
        JPopupMenu popupMenu = (JPopupMenu) element;
        element = popupMenu.getInvoker();
      } else {
        element = element.getParent();
      }
      if (element instanceof JMenuBar) {
        path.insert(0, "Menu/");
        break;
      }
    }
    return path.toString();
  }

  private static boolean actionRefMatches(ActionRef actionRef, String text, String path) {
    return actionRef.getDisplayName().equals(text) && actionRef.getPath().equals(path);
  }

  private static class ClickCounterAdderListener extends MouseInputAdapter {

    @Override
    public void mouseClicked(MouseEvent e) {
      JMenuItem menuItem = (JMenuItem) e.getSource();
      addListenersToMenuItems(QuickMenu.getInstance().getActionReferences(), menuItem);
      menuItem.removeMouseListener(this);
    }
  }
}
