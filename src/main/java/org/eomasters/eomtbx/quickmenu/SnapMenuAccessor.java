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

package org.eomasters.eomtbx.quickmenu;

import java.awt.Component;
import javax.swing.JFrame;
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

  private static JMenuItem doFindMenuItem(ActionRef actionRef, MenuElement parent) {
    if (parent.getSubElements().length == 0) {
      if (parent instanceof JMenuItem) {
        JMenuItem menuItem = (JMenuItem) parent;
        String text = menuItem.getText();
        String path = getPath(menuItem);
        if (actionRef.getMenuRefs().contains(new MenuRef(path, text))) {
          return menuItem;
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

}
