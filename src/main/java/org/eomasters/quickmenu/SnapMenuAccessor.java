package org.eomasters.quickmenu;

import java.util.ArrayList;
import javax.swing.*;
import org.eomasters.quickmenu.old.QuickMenuItem;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.UIUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class SnapMenuAccessor {

  private final String[] excludePathElements;

  public SnapMenuAccessor() {
    excludePathElements = new String[] {"Quick Menu", "Dyn Menu", "Reopen Product"};
  }

  ArrayList<QuickMenuItem> getMenuItems() {
    ArrayList<QuickMenuItem> quickMenuItems = new ArrayList<>();
    collectMenuActions(quickMenuItems, FileUtil.getConfigFile("Menu"));
    return quickMenuItems;
  }

  private void collectMenuActions(ArrayList<QuickMenuItem> quickMenuItems, FileObject menuFolder) {
    for (FileObject fileObject : menuFolder.getChildren()) {
      try {
        if (fileObject.isFolder()) {
          collectMenuActions(quickMenuItems, fileObject);
        } else {
          String lcName = fileObject.getName().toLowerCase();
          if (containsOneOf(lcName, "separator", "spacer", "master-help")) {
            continue;
          }
          String path = fileObject.getPath();
          Object originalFileObject = fileObject.getAttribute("originalFile");
            if (originalFileObject == null) {
                continue;
            }
          String originalFile = originalFileObject.toString();
          FileObject delegatesTo = FileUtil.getConfigFile(originalFile);
          if (delegatesTo == null) {
            continue;
          }
          String displayName = (String) delegatesTo.getAttribute("displayName");
          if(displayName == null || displayName.isBlank()) {
              continue;
          }
          if (!shallExclude(displayName, path)) {
            quickMenuItems.add(new QuickMenuItem(path, displayName));
          }
        }
      } catch (Exception e) {
        // it is important that now exception is thrown here
        // we need to catch and handle
        throw new RuntimeException(e);
      }
    }
  }

  private static boolean containsOneOf(String text, String ... contains) {
    for (String testString : contains) {
        if (text.contains(testString)) {
            return true;
        }
    }
    return false;
  }

  public void addListenersToMenu(ArrayList<QuickMenuItem> quickMenuItems, MenuElement parent) {
    for (int i = 0; i < quickMenuItems.size(); i++) {
      QuickMenuItem quickMenuItem =  quickMenuItems.get(i);
//      FileObject configFile = FileUtil.getConfigFile("Menu/Window/eu-esa-snap-netbeans-tile-TileSingleAction.shadow");
//      DataObject dob = null;
//      try {
//        dob = DataObject.find(configFile);
//        Node nodeDelegate = dob.getNodeDelegate();
//      } catch (DataObjectNotFoundException e) {
//        throw new RuntimeException(e);
//      }

      JMenuItem menuItem = findMenuItem(quickMenuItem, parent);
      if (menuItem != null) {
        menuItem.addActionListener(e -> quickMenuItem.incrementClicks());
      }
    }
  }

  private JMenuItem findMenuItem(QuickMenuItem quickMenuItem, MenuElement parent) {
      if (parent.getSubElements().length == 0)
          if (parent instanceof JMenuItem) {
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

  // Get Action for a FileObject
//  FileObject configFile = FileUtil.getConfigFile("Menu/Window/eu-esa-snap-netbeans-tile-TileSingleAction.shadow");
//  DataObject dob = DataObject.find(configFile);
//  InstanceCookie ic = dob.getLookup().lookup(InstanceCookie.class);
//if (ic != null) {
//    Object instance = ic.instanceCreate();
//    if (instance instanceof Action) {
//      return ((Action) instance);
//    }
//  }
//return null;

  private boolean shallExclude(String text, String path) {
    for (String excludePathElement : excludePathElements) {
      if ((path + text).contains(excludePathElement)) {
        return true;
      }
    }
    return false;
  }

  public static JMenuBar getJMenuBar() {
    SnapApp snapApp = SnapApp.getDefault();
    JFrame rootJFrame = UIUtils.getRootJFrame(snapApp.getMainFrame());
    return rootJFrame.getJMenuBar();
  }
}
