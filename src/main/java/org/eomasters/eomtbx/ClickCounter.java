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

package org.eomasters.eomtbx;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.event.MouseInputAdapter;
import org.eomasters.eomtbx.quickmenu.ActionRef;
import org.eomasters.eomtbx.quickmenu.MenuRef;
import org.eomasters.eomtbx.quickmenu.QuickMenu;
import org.eomasters.eomtbx.quickmenu.SnapMenuAccessor;

class ClickCounter {

  /**
   * Initializes the click counter for all menu items. Adds only temporary mouse listeners to the menu items which is
   * removed if once clicked and listeners have been added to the sub menu items
   */
  public static void initMenuItemClickCounter() {
    JMenuBar menuBar = SnapMenuAccessor.getMenuBar();
    if (menuBar != null) {
      MenuElement[] subElements = menuBar.getSubElements();
      for (MenuElement subElement : subElements) {
        if (subElement instanceof JMenuItem) {
          JMenuItem menuItem = (JMenuItem) subElement;
          menuItem.addMouseListener(new TemporaryClickCounterAdder());
        }
      }
    }
  }

  private static void addClickListener(List<ActionRef> actionRefs, MenuElement parent) {
    if (parent.getSubElements().length == 0) {
      if (parent instanceof JMenuItem) {
        JMenuItem menuItem = (JMenuItem) parent;
        String text = menuItem.getText();
        String path = SnapMenuAccessor.getPath(menuItem);
        MenuRef menuRef = new MenuRef(path, text);
        actionRefs.stream()
            .filter(actionRef -> actionRef.getMenuRefs().contains(menuRef))
            .findFirst()
            .ifPresent(actionRef -> menuItem.addActionListener(new ClickListener(actionRef)));
      }
    }
    MenuElement[] subElements = parent.getSubElements();
    for (MenuElement subElement : subElements) {
      addClickListener(actionRefs, subElement);
    }
  }

  private static class TemporaryClickCounterAdder extends MouseInputAdapter {

    @Override
    public void mouseEntered(MouseEvent e) {
      JMenuItem menuItem = (JMenuItem) e.getSource();
      addClickListener(QuickMenu.getInstance().getActionReferences(), menuItem);
      menuItem.removeMouseListener(this);
    }

  }

  private static class ClickListener implements ActionListener {

    private final ActionRef actionRef;

    public ClickListener(ActionRef actionRef) {
      this.actionRef = actionRef;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      actionRef.incrementClicks();
      QuickMenu.getInstance().sort();
    }
  }
}
