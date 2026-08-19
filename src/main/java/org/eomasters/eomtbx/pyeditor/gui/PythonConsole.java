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

package org.eomasters.eomtbx.pyeditor.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * A console component for displaying output from Python execution.
 */
public class PythonConsole extends JPanel {

  private static final int MIN_CONSOLE_HEIGHT = 100;

  private final JTextPane consoleTextPane;
  private final Style defaultStyle;
  private final Style errorStyle;

  /**
   * Creates a new PyConsole component.
   */
  public PythonConsole() {
    setLayout(new BorderLayout());

    // Create console text area
    consoleTextPane = new JTextPane();
    consoleTextPane.setEditable(false);
    consoleTextPane.setBackground(Color.BLACK.brighter());

    StyledDocument styledDocument = consoleTextPane.getStyledDocument();
    Style basicStyle = styledDocument.addStyle("basicStyle", null);
    basicStyle.addAttribute(StyleConstants.FontFamily, "Monospaced");
    basicStyle.addAttribute(StyleConstants.FontSize, 13);
    StyleConstants.setBold(basicStyle, true);

    defaultStyle = styledDocument.addStyle("defaultStyle", basicStyle);
    StyleConstants.setForeground(defaultStyle, Color.WHITE);

    errorStyle = styledDocument.addStyle("errorStyle", basicStyle);
    StyleConstants.setForeground(errorStyle, new Color(216, 73, 63));

    // Create scroll pane for console
    JScrollPane consoleScrollPane = new JScrollPane(consoleTextPane);
    consoleScrollPane.setMinimumSize(new Dimension(0, MIN_CONSOLE_HEIGHT));

    add(consoleScrollPane, BorderLayout.CENTER);
  }

  /**
   * Clears the console text area.
   */
  public void clear() {
    if (consoleTextPane != null) {
      try {
        consoleTextPane.getDocument().remove(0, consoleTextPane.getDocument().getLength());
      } catch (BadLocationException locationException) {
        JOptionPane.showMessageDialog(this, locationException.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public void setText(String text, boolean isError) {
    if (consoleTextPane != null) {
      clear();
      insertTextAt(0, text, isError);
    }
  }

  public void appendText(String text, boolean isError) {
    if (consoleTextPane != null) {
      insertTextAt(consoleTextPane.getDocument().getLength(), text, isError);
    }
  }

  private void insertTextAt(int position, String text, boolean isError) {
    if (consoleTextPane != null) {
      try {
        StyledDocument styledDocument = consoleTextPane.getStyledDocument();
        styledDocument.insertString(position, text, isError ? errorStyle : defaultStyle);
        consoleTextPane.setCaretPosition(styledDocument.getLength());
      } catch (BadLocationException locationException) {
        JOptionPane.showMessageDialog(this, locationException.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }

  }

}
