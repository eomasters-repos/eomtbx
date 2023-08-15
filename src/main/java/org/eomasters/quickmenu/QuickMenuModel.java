package org.eomasters.quickmenu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.eomasters.quickmenu.old.QuickMenuItem;
import org.esa.snap.rcp.SnapApp;

public class QuickMenuModel extends AbstractListModel<JMenuItem> {
  private static final int DEFAULT_NUM_QUICK_ACTIONS = 5;
  private static final String PREFERENCE_KEY_NUM_QUICK_ACTIONS = "org.eomasters.NumQuickActions";

  private final ArrayList<QuickMenuItem> menuItems;

  public QuickMenuModel() {
    this(new ArrayList<>());
  }

  public QuickMenuModel(ArrayList<QuickMenuItem> menuItems) {
    this.menuItems = menuItems;
    this.menuItems.sort(
        Comparator.comparingLong(QuickMenuItem::getClicks)); // TODO move to back ground task
    addAutoSortListenerTo(menuItems);
  }

  @Override
  public int getSize() {
    final Preferences preference = SnapApp.getDefault().getPreferences();
    return preference.getInt(PREFERENCE_KEY_NUM_QUICK_ACTIONS, DEFAULT_NUM_QUICK_ACTIONS);
    //    int numElements = 0;
    //    for (int i = 0; i < maxActions; i++) {
    //      QuickMenuItem quickMenuItem = menuItems.get(i);
    //      if(quickMenuItem.getClicks() > 0) {
    //        numElements++;
    //      }else {
    //        break;
    //      }
    //    }
    //    return numElements;
  }

  @Override
  public JMenuItem getElementAt(int index) {
    QuickMenuItem quickMenuItem = menuItems.get(index);
    return new JMenuItem(quickMenuItem.getDisplayName());
//    return findMenuItem(quickMenuItem, SnapMenuAccessor.getJMenuBar());
  }

  private void addAutoSortListenerTo(ArrayList<QuickMenuItem> quickMenuItems) {
    quickMenuItems.forEach(
        item ->
            item.addClicksChangeListener(
                evt -> {
                  //noinspection SuspiciousMethodCalls
                  int origIndex = quickMenuItems.indexOf(evt.getSource());
                  QuickMenuItem thisItem = quickMenuItems.get(origIndex);
                  long thisItemClicks = thisItem.getClicks();
                  for (int i = origIndex - 1; i >= 0; i--) {
                    QuickMenuItem beforeItem = quickMenuItems.get(i);
                    if (beforeItem.getClicks() > thisItemClicks || i == 0) {
                      quickMenuItems.add(i, quickMenuItems.remove(origIndex));
                      fireContentsChanged(this, i + 1, origIndex);
                    }
                  }
                }));
  }

  private JMenuItem findMenuItem(QuickMenuItem quickMenuItem, MenuElement parent) {
    if (parent.getSubElements().length == 0 && parent instanceof JMenuItem) {
      JMenuItem menuItem = (JMenuItem) parent;
      String text = menuItem.getText();
      if (quickMenuItem.getDisplayName().equals(text)) {
        String path = Util.getPath(menuItem);
        if (quickMenuItem.getPath().equals(path)) return menuItem;
      }
    }
    MenuElement[] subElements = parent.getSubElements();
    for (MenuElement subElement : subElements) {
      findMenuItem(quickMenuItem, subElement);
    }
    return null;
  }
}
