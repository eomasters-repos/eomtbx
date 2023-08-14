package org.eomasters.quickmenu;

import javax.swing.*;
import java.awt.*;

public class Util {
  public static String getPath(JMenuItem menuItem) {
    Component element = menuItem.getParent();
    StringBuilder path = new StringBuilder();
    while (element != null && !(element instanceof JMenuBar)) {
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
    }
    return path.toString();
  }
}
