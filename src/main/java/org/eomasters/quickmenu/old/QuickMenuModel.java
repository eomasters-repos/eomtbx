package org.eomasters.quickmenu.old;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import javax.swing.*;

import org.eomasters.quickmenu.Util;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.UIUtils;

public class QuickMenuModel extends javax.swing.AbstractListModel<javax.swing.JMenuItem> {
  private static final int DEFAULT_NUM_QUICK_ACTIONS = 5;
  private static final String PREFERENCE_KEY_NUM_QUICK_ACTIONS = "org.eomasters.NumQuickActions";

  private static final ArrayList<QuickMenuItem> QUICK_MENU_ITEMS = new ArrayList<>();
  private static final String[] EXCLUDE_PATH_ELEMENTS = {"Quick Menu", "Reopen Product"};

  public QuickMenuModel() {
    Executors.newSingleThreadScheduledExecutor()
        .schedule(
            () -> {
              JMenuBar jMenuBar = getJMenuBar();
              collectMenuActions(QUICK_MENU_ITEMS, jMenuBar);
              QUICK_MENU_ITEMS.sort(
                  Comparator.comparingLong(
                      QuickMenuItem::getClicks)); // TODO move to back ground task
              addAutoSortListener();
                fireContentsChanged(this, 0, getSize());
            },
            5000,
            TimeUnit.MILLISECONDS);
  }

  private static JMenuBar getJMenuBar() {
    SnapApp snapApp = SnapApp.getDefault();
    JFrame rootJFrame = UIUtils.getRootJFrame(snapApp.getMainFrame());
    return rootJFrame.getJMenuBar();
  }

  @Override
  public int getSize() {
    final Preferences preference = SnapApp.getDefault().getPreferences();
    int maxActions = preference.getInt(PREFERENCE_KEY_NUM_QUICK_ACTIONS, DEFAULT_NUM_QUICK_ACTIONS);
    return Math.min(maxActions, QUICK_MENU_ITEMS.size());
  }

  @Override
  public JMenuItem getElementAt(int index) {
    QuickMenuItem quickMenuItem = QUICK_MENU_ITEMS.get(index);
    return findMenuItem(quickMenuItem, getJMenuBar());
  }

  private void addAutoSortListener() {
    QUICK_MENU_ITEMS.forEach(
        item ->
            item.addClicksChangeListener(
                evt -> {
                  //noinspection SuspiciousMethodCalls
                  int origIndex = QUICK_MENU_ITEMS.indexOf(evt.getSource());
                  QuickMenuItem thisItem = QUICK_MENU_ITEMS.get(origIndex);
                  long thisItemClicks = thisItem.getClicks();
                  for (int i = origIndex - 1; i >= 0; i--) {
                    QuickMenuItem beforeItem = QUICK_MENU_ITEMS.get(i);
                    if (beforeItem.getClicks() > thisItemClicks || i == 0) {
                      QUICK_MENU_ITEMS.add(i, QUICK_MENU_ITEMS.remove(origIndex));
                      fireContentsChanged(this, i + 1, origIndex);
                    }
                  }
                }));
  }

  private JMenuItem findMenuItem(QuickMenuItem quickMenuItem, MenuElement parent) {
    if (parent.getSubElements().length == 0 && parent instanceof JMenuItem) {
      JMenuItem menuItem = (JMenuItem) parent;
      String text = menuItem.getText();
      if (quickMenuItem.getText().equals(text)) {
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

  private static void collectMenuActions(
      ArrayList<QuickMenuItem> quickMenuItems, MenuElement parent) {
    if (parent.getSubElements().length == 0 && parent instanceof JMenuItem) {
      JMenuItem menuItem = (JMenuItem) parent;
      String text = menuItem.getText();
      if (!text.isBlank()) {
        String path = Util.getPath(menuItem);
        if (!shallExclude(menuItem.getText(), path)) {
          quickMenuItems.add(new QuickMenuItem(path, menuItem.getText()));
          // add listener to increment clicks
          menuItem.addActionListener(
              e -> {
                quickMenuItems.stream()
                    .filter(
                        quickMenuItem ->
                            quickMenuItem.getText().equals(text)
                                && quickMenuItem.getPath().equals(path))
                    .findFirst()
                    .ifPresent(QuickMenuItem::incrementClicks);
              });
        }
      }
    }
    MenuElement[] subElements = parent.getSubElements();
    for (MenuElement subElement : subElements) {
      collectMenuActions(quickMenuItems, subElement);
    }
  }

  private static boolean shallExclude(String text, String path) {
    for (String excludePathElement : EXCLUDE_PATH_ELEMENTS) {
      if ((path + text).contains(excludePathElement)) {
        return true;
      }
    }
    return false;
  }
}
