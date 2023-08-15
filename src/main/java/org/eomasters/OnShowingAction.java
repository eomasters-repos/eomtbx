package org.eomasters;

import org.eomasters.quickmenu.SnapMenuAccessor;
import org.openide.windows.OnShowing;

import javax.swing.*;

@OnShowing
public class OnShowingAction implements Runnable {
  @Override
  public void run() {
      JMenuBar jMenuBar = SnapMenuAccessor.getJMenuBar();
      jMenuBar.addContainerListener(
        new java.awt.event.ContainerAdapter() {
          public void componentAdded(java.awt.event.ContainerEvent evt) {
            System.out.println("evt = " + evt);
          }
        });

      jMenuBar.addMouseListener(
        new java.awt.event.MouseAdapter() {
          public void mouseClicked(java.awt.event.MouseEvent evt) {
            System.out.println("evt = " + evt);
          }
        }
    );
    System.out.println();
  }
}
