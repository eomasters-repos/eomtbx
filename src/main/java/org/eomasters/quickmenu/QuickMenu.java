package org.eomasters.quickmenu;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.swing.*;
import org.eomasters.quickmenu.old.QuickMenuItem;
import org.esa.snap.rcp.util.Dialogs;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

@ActionID(category = "Other", id = "EOM_QuickMenu")
@ActionRegistration(
    displayName = "#CTL_QuickMenuActionName",
    menuText = "#CTL_QuickMenuActionMenuText",
    lazy = false)
// File is 100, Edit is 200, Tools is 600
@ActionReference(path = "Menu", position = 580)
@NbBundle.Messages({
  "CTL_QuickMenuActionName=Quick Menu",
  "CTL_QuickMenuActionMenuText=Quick Menu",
})
public class QuickMenu extends AbstractAction implements Presenter.Toolbar, Presenter.Menu {
  private static DynamicJMenu dynamicJMenu;
  private final QuickMenuModel menuModel;

  int numCalls = 0;

  public QuickMenu() {
    SnapMenuAccessor snapMenuAccessor = new SnapMenuAccessor();
    ArrayList<QuickMenuItem> menuItems = snapMenuAccessor.getMenuItems();
    menuModel = new QuickMenuModel(menuItems);
    dynamicJMenu = new DynamicJMenu(Bundle.CTL_QuickMenuActionMenuText(), menuModel);
    //    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    //    scheduledExecutorService.scheduleAtFixedRate(
    //        () -> {
    //          numCalls++;
    //          JMenuBar jMenuBar = SnapMenuAccessor.getJMenuBar();
    //          if (jMenuBar != null) {
    //            System.out.println("jMenuBar = " + jMenuBar);
    //            SnapMenuAccessor menuAccessor = new SnapMenuAccessor();
    //            menuAccessor.addListenersToMenu(menuItems,jMenuBar);
    //            scheduledExecutorService.shutdown();
    //          } else {
    //            System.out.println("numCalls = " + numCalls);
    //          }
    //        },
    //        500,
    //        1000,
    //        TimeUnit.MILLISECONDS);
    dynamicJMenu.addMouseListener(
        new java.awt.event.MouseAdapter() {
          public void mouseClicked(java.awt.event.MouseEvent evt) {
            JMenuBar jMenuBar = SnapMenuAccessor.getJMenuBar();
            if (jMenuBar != null) {
              System.out.println("jMenuBar = " + jMenuBar);
              SnapMenuAccessor menuAccessor = new SnapMenuAccessor();
              menuAccessor.addListenersToMenu(menuItems, jMenuBar);
              dynamicJMenu.removeMouseListener(this);
            }
          }
        });
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
    System.out.println("e = " + e);
    // do nothing
  }

  private JMenu createMenu() {
    updateMenuItems();
    return dynamicJMenu;
  }

  private void updateMenuItems() {
    dynamicJMenu.removeAll();
    for (int i = 0; i < menuModel.getSize(); i++) {
      JMenuItem elementAt = menuModel.getElementAt(i);
      elementAt.addActionListener(new ClickListener());
      dynamicJMenu.add(elementAt);
    }
  }

  private class ClickListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      //        Dialogs.showInformation("Clicked: " + e.getActionCommand());
      updateMenuItems();
    }
  }
}
