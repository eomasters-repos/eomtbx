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
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import javax.swing.AbstractAction;
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
import net.miginfocom.swing.MigLayout;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.rcp.util.Dialogs;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Handles errors.
 */
public class ErrorHandler {

  /**
   * Handles the given throwable. In a headless environment the throwable is only logged to the console. If GUI is
   * available a dialog is shown in addition.
   *
   * @param message the message
   * @param t       the throwable
   */
  public static void handle(String message, Throwable t) {
    SystemUtils.LOG.log(Level.SEVERE, message, t);
    if (isHeadless()) {
      return;
    }

    JPanel contentPane = new JPanel(new MigLayout("top, left, fillx, gap 5 5"));
    contentPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

    JLabel anErrorOccurred = new JLabel("An Error Occurred");
    anErrorOccurred.setFont(anErrorOccurred.getFont().deriveFont(Font.BOLD, 28f));
    contentPane.add(anErrorOccurred, "top, left, wrap");

    JTextArea headerText = new MultiLineText(
        "Sorry, this should not have happened. Please help to fix this problem and report the issue to EOMasters.\n");
    contentPane.add(headerText, "top, left, growx, wmin 10, wrap");

    ErrorReport errorReport = new ErrorReport(message, t);
    addReportArea(contentPane, errorReport.generate());

    JDialog dialog = new JDialog();

    JButton byMail = createMailButton();

    contentPane.add(byMail, "right");
    JButton close = new JButton("Close");
    close.addActionListener(e -> dialog.dispose());
    contentPane.add(close, "right, wrap");

    dialog.setTitle("Error");
    dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
    dialog.setContentPane(contentPane);
    dialog.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
    dialog.pack();
  }

  private static JButton createMailButton() {
    JButton byMail = new JButton("Report by Mail");
    byMail.addActionListener(e -> {
      try {
        String bodyText = "<PLEASE ADD A BRIEF DESCRIPTION WHAT YOU DID BEFORE THE ERROR OCCURRED>\n\n"
            + "<PLEASE PASTE IN THE ERROR REPORT HERE OR ATTACH IT AS A FILE>";
        MailTo mailTo = new MailTo("error@eomasters.com")
            .subject("EOMTBX Error Report")
            .body(bodyText);
        Desktop.getDesktop().mail(mailTo.toUri());
      } catch (Exception ex) {
        Dialogs.showError("Error opening mail client", "Could not open mail client:\n" + ex.getMessage());
      }

    });
    return byMail;
  }

  private static void addReportArea(JPanel contentPane, String reportText) {

    JTextArea textArea = new JTextArea(reportText);
    textArea.setColumns(70);
    textArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    textArea.setTabSize(4);
    textArea.setEditable(false);
    textArea.setRows(20);
    JScrollPane scrollPane = new JScrollPane(textArea,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JPanel reportPreview = new JPanel(new MigLayout("fill"));
    reportPreview.add(scrollPane, "top, left, grow, wrap");
    JButton exportBtn = createExportButton(contentPane, textArea);
    boolean headless = isHeadless();
    reportPreview.add(exportBtn, "top, left" + (!headless ? ", split 2" : ""));
    if (!headless) {
      JButton clipboardBtn = new JButton("Copy to Clipboard");
      clipboardBtn.addActionListener(e -> {
        StringSelection contents = new StringSelection(textArea.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, contents);
      });
      reportPreview.add(clipboardBtn, "top, left");
    }

    CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Error Report Preview");
    collapsiblePanel.setContent(reportPreview);

    contentPane.add(collapsiblePanel, "top, left, grow, wrap");
  }

  private static JButton createExportButton(JPanel contentPane, JTextArea textArea) {
    JButton exportBtn = new JButton("Export to File");
    exportBtn.addActionListener(e -> {
      FileIo exporter = new FileIo("Export Error Report");
      exporter.setParent(contentPane);
      Clock utcClock = Clock.systemUTC();
      Instant now = Instant.now(utcClock);
      DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("uuuuMMdd_HHmmss").withZone(utcClock.getZone());
      String fileName = String.format("EOMTBX_Error_Report_%s.txt", timeFormatter.format(now));
      exporter.setFileName(fileName);
      exporter.setFileFilters(FileIo.createFileFilter("Report file", "txt"));
      exporter.save(outputStream -> outputStream.write(textArea.getText().getBytes(StandardCharsets.UTF_8)));
    });
    return exportBtn;
  }

  private static boolean isHeadless() {
    return System.getProperty("java.awt.headless", "false").equals("true");
  }


  /**
   * Main method for testing.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      JFrame window = new JFrame("Test ErrorHandler");
      window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      window.setSize(400, 300);
      Container container = window.getContentPane();
      container.setLayout(new BorderLayout());
      JButton openErrorHandler = new JButton("Open ErrorHandler");
      openErrorHandler.addActionListener(e -> {
        Exception test = new Exception("Test", new Exception("theCause"));
        ErrorHandler.handle("Test", test);
      });
      container.add(openErrorHandler);
      window.pack();
      window.setVisible(true);
    });
  }

  /**
   * Action that opens the error handler.
   */
  @ActionID(category = "Help", id = "EOM_ErrorHandler")
  @ActionRegistration(displayName = "#CTL_ErrorHandlerActionName", lazy = false)
  @ActionReference(path = "Menu/Help", position = 10)
  @NbBundle.Messages({"CTL_ErrorHandlerActionName=Error Handler"})
  public static class Action extends AbstractAction {

    /**
     * Creates a new Action.
     */
    public Action() {
      super(Bundle.CTL_ErrorHandlerActionName());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Exception test = new Exception("Test", new Exception("theCause"));
      ErrorHandler.handle("Test", test);
    }
  }

}
