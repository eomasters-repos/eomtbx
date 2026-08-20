/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/eomtbx
 * ======================================================================
 * Copyright (C) 2023 - 2026 Marco Peters
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
 * -> http://www.gnu.org/licenses/gpl-3.0.html
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.pyeditor.gui.tabs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * A custom component for tabs in a JTabbedPane that includes a title and a close button.
 */
public class TabComponent extends JPanel {

  private static final Color EDITED_COLOR = Color.GREEN.darker();
  private static final Color DEFAULT_COLOR = Color.BLACK;
  private final JTabbedPane tabbedPane;
  private final Path filePath;
  private final TabCloseListener closeListener;
  private final JLabel label;

  /**
   * Creates a new TabComponent with the specified title, tabbed pane, file path, and close listener.
   *
   * @param title         the title to display in the tab
   * @param tabbedPane    the JTabbedPane that contains this tab
   * @param filePath      the path of the file in this tab
   * @param closeListener the listener to notify when the tab is closed
   */
  public TabComponent(String title, JTabbedPane tabbedPane, Path filePath, TabCloseListener closeListener) {
    super(new FlowLayout(FlowLayout.LEFT, 0, 0));
    this.tabbedPane = tabbedPane;
    this.filePath = filePath;
    this.closeListener = closeListener;

    setOpaque(false);

    // Create a label for the title
    label = new JLabel(title);
    Font defaultFont = label.getFont().deriveFont(Font.BOLD);
    label.setFont(defaultFont);
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
    add(label);

    // Create a close button
    JButton closeButton = new JButton("×");
    closeButton.setPreferredSize(new Dimension(17, 17));
    closeButton.setToolTipText("Close this tab");
    closeButton.setContentAreaFilled(false);
    closeButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    closeButton.setFocusable(false);

    // Add action listener to the close button
    closeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        closeTab();
      }
    });

    add(closeButton);
  }

  /**
   * Closes the tab if the close listener allows it.
   */
  private void closeTab() {
    // Find the index of this tab
    int index = findTabIndex();
    if (index != -1) {
      // Notify the close listener and close the tab if allowed
      if (closeListener == null || closeListener.onTabClose(filePath)) {
        tabbedPane.remove(index);
      }
    }
  }

  /**
   * Finds the index of this tab in the tabbed pane.
   *
   * @return the index of this tab, or -1 if not found
   */
  private int findTabIndex() {
    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
      if (tabbedPane.getTabComponentAt(i) == this) {
        return i;
      }
    }
    return -1;
  }

  public void setModified(boolean b) {
    if(b) {
      label.setForeground(EDITED_COLOR);
    } else {
      label.setForeground(DEFAULT_COLOR);
    }
    repaint();
  }

  /**
   * Interface for handling tab close events.
   */
  public interface TabCloseListener {

    /**
     * Called when a tab is about to be closed.
     *
     * @param filePath the path of the file in the tab being closed
     * @return true if the tab should be closed, false otherwise
     */
    boolean onTabClose(Path filePath);
  }
}
