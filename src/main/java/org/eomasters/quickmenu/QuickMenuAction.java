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
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MouseInputAdapter;
import org.esa.snap.rcp.SnapApp;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

@ActionID(category = "Other", id = "EOM_QuickMenu")
@ActionRegistration(displayName = "#CTL_QuickMenuActionName", lazy = false)
// File is 100, Edit is 200, Tools is 600
@ActionReference(path = "Menu", position = 580)
@NbBundle.Messages({
    "CTL_QuickMenuActionName=Quick Menu",
})
public class QuickMenuAction extends AbstractAction implements Presenter.Toolbar, Presenter.Menu {

  private static final int DEFAULT_NUM_QUICK_ACTIONS = 5;
  private static final String PREFERENCE_KEY_NUM_QUICK_ACTIONS = "org.eomasters.quickmenu.NumActions";

  private final JMenu menu;

  public QuickMenuAction() {
    menu = new JMenu(Bundle.CTL_QuickMenuActionName());
    menu.addMouseListener(new MenuUpdater());
  }

  @Override
  public JMenuItem getMenuPresenter() {
    return updateMenu();
  }

  private JMenu updateMenu() {
    List<ActionRef> refs = QuickMenu.getInstance().getActionReferences();
    final Preferences preference = SnapApp.getDefault().getPreferences();
    int maxActions = preference.getInt(PREFERENCE_KEY_NUM_QUICK_ACTIONS, DEFAULT_NUM_QUICK_ACTIONS);
    Stream<ActionRef> limit =
        refs.stream()
            .filter(ref -> ref.getClicks() > 0)
            .sorted(Comparator.comparingLong(ActionRef::getClicks).reversed())
            .limit(maxActions);
    menu.removeAll();
    List<ActionRef> actions = limit.collect(Collectors.toList());
    if (actions.isEmpty()) {
      JMenuItem menuItem = new JMenuItem("<No Quick Actions yet");
      menuItem.setEnabled(false);
      menu.add(menuItem);
    } else {
      actions.forEach(
          actionRef -> {
            // using the original menu item will trigger the action to increase the click counter
            // this is intended, otherwise menu items within the quick menu would not be counted
            // TODO: move to SnapMenuAccessor
            JMenuItem origMenuItem = SnapMenuAccessor.findMenuItem(actionRef);
            if (origMenuItem != null) {
              JMenuItem menuItem = new JMenuItem(actionRef.getDisplayName());
              menuItem.setToolTipText(origMenuItem.getToolTipText());
              menuItem.setIcon(origMenuItem.getIcon());
              menuItem.addActionListener(e -> origMenuItem.doClick());
              menu.add(menuItem);
            }
          });
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

  private class MenuUpdater extends MouseInputAdapter {

    @Override
    public void mouseEntered(MouseEvent e) {
      updateMenu();
    }
  }
}
