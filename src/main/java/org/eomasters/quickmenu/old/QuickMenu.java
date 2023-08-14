package org.eomasters.quickmenu.old;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.eomasters.quickmenu.DynamicJMenu;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.Presenter;

//@ActionID(category = "Other", id = "EOM_QuickMenu")
//@ActionRegistration(
//    displayName = "#CTL_QuickMenuActionName",
//    menuText = "#CTL_QuickMenuActionMenuText",
//    lazy = false)
//// File is 100, Edit is 200, Tools is 600
//@ActionReference(path = "Menu", position = 590)
//@NbBundle.Messages({
//  "CTL_QuickMenuActionName=Quick Menu",
//  "CTL_QuickMenuActionMenuText=Quick Menu",
//})
public class QuickMenu extends AbstractAction
    implements Presenter.Toolbar, Presenter.Menu, DynamicMenuContent {
  private static DynamicJMenu dynamicMenu;
  private final ListModel<JMenuItem> menuModel;

  public QuickMenu() {
    menuModel = new QuickMenuModel();
    menuModel.addListDataListener(new ListDataListener() {
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
    dynamicMenu = new DynamicJMenu("Bundle.CTL_QuickMenuActionMenuText()", menuModel);
  }

  private void updateMenu() {
    dynamicMenu.removeAll();
    for (int i = 0; i < menuModel.getSize(); i++) {
      dynamicMenu.add(menuModel.getElementAt(i));
    }
  }

  @Override
  public JMenuItem getMenuPresenter() {
    return createMenu();
  }

  @Override
  public Component getToolbarPresenter() {
    return createMenu();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // do nothing
  }

  @Override
  public JComponent[] getMenuPresenters() {
    return new JComponent[] {createMenu()};
  }

  @Override
  public JComponent[] synchMenuPresenters(JComponent[] items) {
    return new JComponent[] {createMenu()};
  }

  private JMenu createMenu() {
    return dynamicMenu;
  }
}
