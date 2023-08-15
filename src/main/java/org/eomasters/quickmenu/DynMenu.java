package org.eomasters.quickmenu;

import org.esa.snap.rcp.util.Dialogs;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@ActionID(category = "Other", id = "EOM_DynMenu")
@ActionRegistration(
    displayName = "#CTL_DynMenuActionName",
    menuText = "#CTL_DynMenuActionMenuText",
    lazy = false)
// File is 100, Edit is 200, Tools is 600
@ActionReference(path = "Menu", position = 0)
@NbBundle.Messages({
  "CTL_DynMenuActionName=Dyn Menu",
  "CTL_DynMenuActionMenuText=Dyn Menu",
})
public class DynMenu extends AbstractAction
    implements Presenter.Toolbar, Presenter.Menu {
  private static int NUM_QUICK_ACTIONS = 1;
  private final ClickListener clickListener = new ClickListener();
  private static DynamicJMenu dynamicMenu;
  private final DefaultListModel<JMenuItem> menuModel;

  public DynMenu() {
    menuModel = new DefaultListModel<>();
    dynamicMenu = new DynamicJMenu(Bundle.CTL_DynMenuActionMenuText(), menuModel);
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

  private JMenu createMenu() {
    updateMenuItems();
    return dynamicMenu;
  }

  private void updateMenuItems() {
    menuModel.clear();
    for (int i = 0; i < NUM_QUICK_ACTIONS; i++) {
      JMenuItem menuItem = new JMenuItem("Dyn Item " + i);
      menuItem.addActionListener(clickListener);
      menuModel.add(i, menuItem);
    }
    NUM_QUICK_ACTIONS++;
  }

  private class ClickListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      Dialogs.showInformation("Clicked: " + e.getActionCommand());
      updateMenuItems();
    }
  }
}
