/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
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
 * -> http://www.gnu.org/licenses/gpl-3.0.html
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.utils;

import java.awt.BorderLayout;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.EomToolbox;

/**
 * Utility class for showing dialogs.
 */
public final class Dialogs {

  private static final String CONFIRMATION_NODE = "confirmation";


  /**
   * Shows a message dialog.
   *
   * @param title   the title
   * @param message the message
   */
  public static void message(String title, String message) {
    message(title, new JLabel(message));
  }

  /**
   * Shows a message dialog.
   *
   * @param title   the title
   * @param message the message component
   */
  public static void message(String title, JComponent message) {
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
  }

  /**
   * Shows a message dialog.
   *
   * @param title   the title
   * @param icon    the dialog icon
   * @param message the message component
   */
  public static void message(String title, Icon icon, JComponent message) {
    JOptionPane.showMessageDialog(null, message, title, JOptionPane.PLAIN_MESSAGE, icon);
  }

  /**
   * Shows an error dialog.
   *
   * @param title        the title
   * @param errorMessage the error message
   */
  public static void error(String title, String errorMessage) {
    JOptionPane.showMessageDialog(null, errorMessage, title, JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Shows an error dialog with a collapsible area showing more details about the error. The details area is collapsed
   * by default. The details can be exported to a file or to the clipboard
   *
   * @param title     the title
   * @param message   the message
   * @param exception the exception
   */
  public static void error(String title, String message, Exception exception) {
    JPanel messagePanel = new JPanel(new MigLayout("top, left, fillx, gap 5 5"));
    messagePanel.add(new JLabel(message), "wrap");
    StringWriter stringWriter = new StringWriter();
    exception.printStackTrace(new PrintWriter(stringWriter));
    CollapsiblePanel detailsArea = CollapsiblePanel.createLongTextPanel("Details", stringWriter.toString());
    messagePanel.add(detailsArea, "top, left, grow, wrap");
    messagePanel.doLayout();
    JOptionPane.showMessageDialog(null, messagePanel, title, JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Shows a dialog asking the user for input.
   *
   * @param title  the title
   * @param prompt the prompt
   */
  public static String input(String title, String prompt) {
    return input(title, new JLabel(prompt));
  }

  /**
   * Shows a prompt dialog.
   *
   * @param title  the title
   * @param prompt the prompt component
   */
  public static String input(String title, JComponent prompt) {
    return JOptionPane.showInputDialog(null, prompt, title, JOptionPane.QUESTION_MESSAGE);
  }

  /**
   * Asks the user for confirmation.
   *
   * @param title    the title
   * @param question the question
   * @return {@code true} if the user confirmed, {@code false} otherwise
   */
  public static boolean confirmation(String title, String question) {
    return confirmation(title, new JLabel(question));
  }


  /**
   * Asks the user for confirmation.
   *
   * @param title             the title
   * @param questionComponent the question component
   * @return {@code true} if the user confirmed, {@code false} otherwise
   */
  public static boolean confirmation(String title, JComponent questionComponent) {
    return confirmation(title, questionComponent, null);
  }

  /**
   * Asks the user for confirmation. The user can choose to store the answer and not to be asked again.
   *
   * @param title             the title
   * @param questionComponent the question component
   * @param preferenceKey     the preference key to store the answer
   * @return {@code true} if the user confirmed, {@code false} otherwise
   */
  public static boolean confirmation(String title, JComponent questionComponent, String preferenceKey) {
    if (preferenceKey != null) {
      if (EomToolbox.getPreferences().node(CONFIRMATION_NODE).getBoolean(preferenceKey, false)) {
        return true;
      }
    }
    JPanel msgPanel = new JPanel(new BorderLayout());
    msgPanel.add(questionComponent, BorderLayout.NORTH);
    if (preferenceKey != null) {
      JCheckBox checkBox = new JCheckBox("Store my answer and don't ask again");
      checkBox.setHorizontalAlignment(JCheckBox.RIGHT);
      checkBox.setHorizontalTextPosition(JCheckBox.RIGHT);
      msgPanel.add(checkBox, BorderLayout.SOUTH);
    }
    boolean confirmed =
        JOptionPane.showConfirmDialog(null, msgPanel, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
            == JOptionPane.YES_OPTION;
    if (preferenceKey != null) {
      EomToolbox.getPreferences().node(CONFIRMATION_NODE).putBoolean(preferenceKey, confirmed);
    }
    return confirmed;
  }

}
