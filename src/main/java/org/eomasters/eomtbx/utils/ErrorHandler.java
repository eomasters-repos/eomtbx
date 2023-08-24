/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * --> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.utils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.esa.snap.core.util.SystemUtils;

/**
 * Handles errors.
 */
public class ErrorHandler {

  /**
   * Handles the given throwable. In a headless environment the throwable is logged to the console. If GUI is available
   * a dialog is shown.
   *
   * @param message the message
   * @param t       the throwable
   */
  public static void handle(String message, Throwable t) {
    if (isHeadless()) {
      SystemUtils.LOG.log(Level.SEVERE, message, t);
      return;
    }

    JDialog dialog = new JDialog();
    dialog.setTitle("Error");
    dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    dialog.setSize(400, 300);
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);


    // todo - think about how this dialog should look like
    // create report file containing system properties and installed/active plugins and stacktrace
    // attach to mail
    // Dialogs.showError("Error", message);
    // JButton mailButton = new JButton("Report by Mail");
    // mailButton.addActionListener(e -> {
    //   try {
    //     Desktop.getDesktop().mail(new URI("mailto:issues@eomasters.org"));
    //   } catch (URISyntaxException | IOException e1) {
    //     SystemUtils.LOG.log(Level.SEVERE, message, e1);
    //   }
    // });
    // JButton forumButton = new JButton("Report in Forum");
    // forumButton.addActionListener(e -> {
    //   try {
    //     Desktop.getDesktop().mail(new URI("https://www.eomasters.org/forum"));
    //   } catch (URISyntaxException | IOException e1) {
    //     SystemUtils.LOG.log(Level.SEVERE, message, e1);
    //   }
    // });
    //
    // JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
    // panel.add(mailButton);
    // panel.add(forumButton);
    // ImageIcon icon = TangoIcons.status_dialog_error(TangoIcons.Res.R16);
    // JLabel balloonDetails = new JLabel(message);
    // NotificationDisplayer.getDefault().notify("Error",
    //     icon,
    //     balloonDetails,
    //     panel,
    //     NotificationDisplayer.Priority.HIGH,
    //     NotificationDisplayer.Category.ERROR);
  }

  private static boolean isHeadless() {
    return System.getProperty("java.awt.headless", "false").equals("true");
  }


  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      JFrame window = new JFrame("Test ErrorHandler");
      window.setSize(400, 300);
      Container container = window.getContentPane();
      container.setLayout(new BorderLayout());
      JButton openErrorHandler = new JButton("Open ErrorHandler");
      openErrorHandler.addActionListener(e -> {
        ErrorHandler.handle("Test", new Exception("Test"));
      });
      container.add(openErrorHandler);
      window.setVisible(true);
    });
  }
}
