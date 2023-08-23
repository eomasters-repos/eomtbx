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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.event.MouseInputAdapter;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.UIUtils;

public class SnapMenuAccessor {

  public static void initClickCounter() {
    JMenuBar menuBar = getMenuBar();
    MenuElement[] subElements = menuBar.getSubElements();
    for (MenuElement subElement : subElements) {
      if (subElement instanceof JMenuItem) {
        JMenuItem menuItem = (JMenuItem) subElement;
        menuItem.addMouseListener(new TemporaryClickCounterAdder());
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

  private static void addClickCounter(List<ActionRef> actionRefs, MenuElement parent) {
    if (parent.getSubElements().length == 0) {
      if (parent instanceof JMenuItem) {
        JMenuItem menuItem = (JMenuItem) parent;
        String text = menuItem.getText();
        String path = getPath(menuItem);
        MenuRef menuRef = new MenuRef(path, text);
        actionRefs.stream()
            .filter(actionRef -> actionRef.getMenuRefs().contains(menuRef))
            .findFirst().
            ifPresent(actionRef -> menuItem.addActionListener(new ClickCounter(actionRef)));
      }
    }
    MenuElement[] subElements = parent.getSubElements();
    for (MenuElement subElement : subElements) {
      addClickCounter(actionRefs, subElement);
    }
  }


  private static class TemporaryClickCounterAdder extends MouseInputAdapter {

    @Override
    public void mouseEntered(MouseEvent e) {
      JMenuItem menuItem = (JMenuItem) e.getSource();
      addClickCounter(QuickMenu.getInstance().getActionReferences(), menuItem);
      menuItem.removeMouseListener(this);
    }

  }

  private static class ClickCounter implements ActionListener {

    private final ActionRef actionRef;

    public ClickCounter(ActionRef actionRef) {
      this.actionRef = actionRef;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      actionRef.incrementClicks();
      QuickMenu.getInstance().sort();
    }
  }
}
