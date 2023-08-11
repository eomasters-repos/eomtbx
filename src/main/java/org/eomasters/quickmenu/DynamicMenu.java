package org.eomasters.quickmenu;

import org.openide.awt.DynamicMenuContent;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class DynamicMenu extends JMenu implements DynamicMenuContent {

  private final ListModel<JMenuItem> menuItems;

  public DynamicMenu(String dynamicMenu, ListModel<JMenuItem> menuItems) {
    super(dynamicMenu);
    this.menuItems = menuItems;
    updateMenu();
    menuItems.addListDataListener(
        new ListDataListener() {
          @Override
          public void intervalAdded(ListDataEvent e) {
            updateMenu();
          }

          @Override
          public void intervalRemoved(ListDataEvent e) {
            updateMenu();
          }

          @Override
          public void contentsChanged(ListDataEvent e) {
            updateMenu();
          }
        });
  }

  private void updateMenu() {
    removeAll();
    for (int i = 0; i < menuItems.getSize(); i++) {
      add(menuItems.getElementAt(i));
    }
  }

  @Override
  public JComponent[] getMenuPresenters() {
    updateMenu();
    return new JComponent[] {this};
  }

  @Override
  public JComponent[] synchMenuPresenters(JComponent[] items) {
    return getMenuPresenters();
  }
}
