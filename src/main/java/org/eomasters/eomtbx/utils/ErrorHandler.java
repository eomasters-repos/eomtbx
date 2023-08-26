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
import java.awt.Dimension;
import java.awt.Font;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.miginfocom.swing.MigLayout;
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


    JPanel contentPane = new JPanel();
    contentPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    contentPane.setLayout(new MigLayout("top, left, fillx, gap 5 5"));

    JLabel anErrorOccurred = new JLabel("An Error Occurred");
    anErrorOccurred.setFont(anErrorOccurred.getFont().deriveFont(Font.BOLD, 28f));
    contentPane.add(anErrorOccurred, "top, left, wrap");

    JTextArea headerText = new JTextArea(
        "Sorry, this should not have happened. Please help to fix this problem and report the issue to EOMasters.\n");
    headerText.setEditable(false);
    headerText.setLineWrap(true);
    headerText.setWrapStyleWord(true);
    headerText.setFont(new JLabel().getFont());
    headerText.setBackground(anErrorOccurred.getBackground());
    headerText.setPreferredSize(new Dimension(350, headerText.getPreferredSize().height));
    contentPane.add(headerText, "top, left, growx, wmin 10, wrap");

    addReportArea(contentPane);

    contentPane.add(new JButton("By Mail"), "right, split");
    contentPane.add(new JButton("In Forum"), "right, wrap");

    JDialog dialog = new JDialog();
    dialog.setTitle("Error");
    dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
    dialog.setContentPane(contentPane);
    dialog.pack();
  }

  private static void addReportArea(JPanel contentPane) {
    JPanel reportPreview = new JPanel(new MigLayout("fill, flowy"));

    JTextArea textArea = new JTextArea("Report Preview");
    textArea.setColumns(20);
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setRows(20);
    JScrollPane scrollPane = new JScrollPane(textArea,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    reportPreview.add(scrollPane, "top, left, grow");
    JButton export = new JButton("Export");
    export.addActionListener(e -> {
      FileIO exporter = new FileIO("Export Error Report");
      exporter.setParent(contentPane);
      exporter.setFileFilters(new FileNameExtensionFilter("Report files", "txt"));
      exporter.save(outputStream -> outputStream.write(textArea.getText().getBytes(StandardCharsets.UTF_8)));

    });
    reportPreview.add(export, "top, left");

    CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Report Preview");
    collapsiblePanel.showSeparator(false);
    collapsiblePanel.setContent(reportPreview);

    contentPane.add(collapsiblePanel, "top, left, grow, wrap");
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
      openErrorHandler.addActionListener(e -> ErrorHandler.handle("Test", new Exception("Test")));
      container.add(openErrorHandler);
      window.pack();
      window.setVisible(true);
    });
  }
}
