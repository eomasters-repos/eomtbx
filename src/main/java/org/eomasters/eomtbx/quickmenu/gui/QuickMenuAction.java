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

package org.eomasters.eomtbx.quickmenu.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MouseInputAdapter;
import org.eomasters.eomtbx.quickmenu.ActionRef;
import org.eomasters.eomtbx.quickmenu.MenuRef;
import org.eomasters.eomtbx.quickmenu.QuickMenu;
import org.eomasters.eomtbx.quickmenu.SnapMenuAccessor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 * The class inserts the QuickMenu into the menu bar.
 */
@ActionID(category = "EOM", id = "EOM_QuickMenu")
@ActionRegistration(displayName = "#CTL_QuickMenuActionName", lazy = false)
// File is 100, Edit is 200, Tools is 600
@ActionReference(path = "Menu", position = 580)
@NbBundle.Messages({"CTL_QuickMenuActionName=" + QuickMenuAction.QUICK_MENU_NAME})
public class QuickMenuAction extends AbstractAction implements Presenter.Toolbar, Presenter.Menu {

  public static final String QUICK_MENU_NAME = "Quick Menu";
  public static final String[] SPECIAL_GROUPS = {"Export", "Import"};

  private final JMenu menu;
  private int numActions;

  /**
   * Creates a new QuickMenuAction.
   */
  public QuickMenuAction() {
    menu = new JMenu(Bundle.CTL_QuickMenuActionName());
    menu.addMouseListener(new MenuUpdater());
    Preferences preferences = QuickMenu.getInstance().getPreferences();
    preferences.addPreferenceChangeListener(evt -> {
      if (evt.getKey().equals(QuickMenuOptions.PREFERENCE_KEY_NUM_QUICK_ACTIONS)) {
        setNumActions(Integer.parseInt(evt.getNewValue()));
      }
    });
    setNumActions(preferences.getInt(QuickMenuOptions.PREFERENCE_KEY_NUM_QUICK_ACTIONS,
        QuickMenuOptions.DEFAULT_NUM_QUICK_ACTIONS));
  }

  private static JMenuItem createMenuItem(ActionRef actionRef, JMenuItem origMenuItem) {
    JMenuItem menuItem = new JMenuItem(getEnhancedText(actionRef));
    menuItem.setToolTipText(origMenuItem.getToolTipText());
    menuItem.setIcon(origMenuItem.getIcon());
    menuItem.addActionListener(e -> origMenuItem.doClick());
    return menuItem;
  }

  private static String getEnhancedText(ActionRef actionRef) {
    List<MenuRef> menuRefs = actionRef.getMenuRefs();
    String text = actionRef.getMenuRef().getText();
    for (MenuRef menuRef : menuRefs) {
      String path = menuRef.getPath();
      for (String specialGroup : SPECIAL_GROUPS) {
        if (path.contains(specialGroup)) {
          return String.format("%s (%s)", text, specialGroup);
        }
      }
    }
    return text;
  }

  private void setNumActions(int num) {
    numActions = num;
  }

  @Override
  public JMenuItem getMenuPresenter() {
    return updateMenu();
  }

  private JMenu updateMenu() {
    List<ActionRef> refs = QuickMenu.getInstance().getActionReferences();
    Stream<ActionRef> limit =
        refs.stream()
            .filter(ref -> ref.getClicks() > 0)
            .limit(numActions);
    List<ActionRef> actions = limit.collect(Collectors.toList());

    menu.removeAll();
    if (actions.isEmpty()) {
      JMenuItem menuItem = new JMenuItem("<No Quick Actions yet>");
      menuItem.setToolTipText("Please use the menu to fill Quick Menu");
      menuItem.setEnabled(false);
      menu.add(menuItem);
    } else {
      actions.forEach(actionRef -> addMenuItemToMenu(actionRef, menu));
    }
    return menu;
  }

  @Override
  public Component getToolbarPresenter() {
    return updateMenu();
  }

  @Override
  public void actionPerformed(ActionEvent ignored) {
  }

  private void addMenuItemToMenu(ActionRef actionRef, JMenu menu) {
    // using the original menu item will trigger the action to increase the click counter
    // this is intended, otherwise menu items within the quick menu would not be counted
    JMenuItem origMenuItem = SnapMenuAccessor.findMenuItem(actionRef);
    if (origMenuItem != null) {
      menu.add(createMenuItem(actionRef, origMenuItem));
    }
  }

  private class MenuUpdater extends MouseInputAdapter {

    @Override
    public void mouseEntered(MouseEvent e) {
      updateMenu();
    }
  }
}
