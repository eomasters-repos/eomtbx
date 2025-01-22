/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Basic for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2025 Marco Peters
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

package org.eomasters.eomtbx.quickmenu;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.UIUtils;

/**
 * Provides access to the SNAP menu.
 */
public class SnapMenuAccessor {

  /**
   * Returns the menu bar of SNAP.
   *
   * @return the menu bar
   */
  public static JMenuBar getMenuBar() {
    SnapApp snapApp = SnapApp.getDefault();
    JFrame rootFrame = UIUtils.getRootJFrame(snapApp.getMainFrame());
    return rootFrame.getJMenuBar();
  }

  /**
   * Finds the menu item for the given action reference.
   *
   * @param actionRef the action reference
   * @return the menu item
   */
  public static JMenuItem findMenuItem(ActionRef actionRef) {
    return doFindMenuItem(actionRef, getMenuBar());
  }

  /**
   * Returns the path of the menu item.
   *
   * @param menuItem the menu item
   * @return the path
   */
  public static String getPath(JMenuItem menuItem) {
    Component element = menuItem.getParent();
    StringBuilder path = new StringBuilder();
    while (element != null) {
      if (element instanceof JMenuItem) {
        JMenuItem item = (JMenuItem) element;
        String text = item.getText();
        if (!text.isBlank()) {
          path.insert(0, text + "/");
        }
        element = element.getParent();
      } else if (element instanceof JPopupMenu) {
        JPopupMenu popupMenu = (JPopupMenu) element;
        element = popupMenu.getInvoker();
      } else if (element instanceof JMenuBar) {
        path.insert(0, "Menu/");
        break;
      }

    }
    return path.toString();
  }

  private static JMenuItem doFindMenuItem(ActionRef actionRef, MenuElement parent) {
    if (parent instanceof Container) {
      Component[] subComponents;
      if (parent instanceof JMenu) {
        JMenu subMenu = (JMenu) parent;
        subComponents = subMenu.getMenuComponents();
      } else {
        subComponents = ((Container) parent).getComponents();
      }
      for (Component subComponent : subComponents) {
        if (subComponent instanceof MenuElement) {
          JMenuItem found = doFindMenuItem(actionRef, (MenuElement) subComponent);
          if (found != null) {
            return found;
          }
        }
      }
      if (parent instanceof JMenuItem) {
        JMenuItem menuItem = (JMenuItem) parent;
        String text = menuItem.getText();
        String path = getPath(menuItem);
        if (actionRef.getMenuRefs().contains(new MenuRef(path, text))) {
          return menuItem;
        }
      }
    }
    return null;
  }

}
