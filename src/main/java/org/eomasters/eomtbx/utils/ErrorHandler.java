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

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.EomToolbox;
import org.eomasters.eomtbx.icons.Icons;
import org.eomasters.eomtbx.utils.FileSharingService.UploadResponse;
import org.esa.snap.core.util.SystemUtils;
import org.hsqldb.lib.StringInputStream;

/**
 * Handles errors.
 */
public class ErrorHandler {

  /**
   * Shows a simple error dialog.
   *
   * @param title   the title
   * @param message the error message
   */
  public static void handleError(String title, String message) {
    SystemUtils.LOG.log(Level.WARNING, String.format("%s: %s", title, message));
    if (EomToolbox.isHeadless()) {
      return;
    }
    Dialogs.error(title, message);
  }

  /**
   * Shows an error dialog extended by the cause exception.
   *
   * @param title     the title
   * @param message   the error message
   * @param exception the cause exception
   */
  public static void handleError(String title, String message, Exception exception) {
    SystemUtils.LOG.log(Level.WARNING, message, exception);
    if (EomToolbox.isHeadless()) {
      return;
    }
    Dialogs.error(title, message, exception);
  }

  /**
   * Handles the given throwable. In a headless environment the throwable is only logged to the console. If GUI is
   * available a dialog is shown in addition.
   *
   * @param message   the message
   * @param exception the throwable
   */
  public static void handleUnexpectedException(String message, Throwable exception) {
    SystemUtils.LOG.log(Level.SEVERE, message, exception);
    if (EomToolbox.isHeadless()) {
      return;
    }

    JPanel contentPane = new JPanel(new MigLayout("top, left, fillx, gap 5 5"));
    contentPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

    JLabel anErrorOccurred = new JLabel("A System Exception Occurred");
    anErrorOccurred.setFont(anErrorOccurred.getFont().deriveFont(Font.BOLD, 28f));
    contentPane.add(anErrorOccurred, "top, left, wrap");

    JTextArea headerText = new MultiLineText(
        "Sorry, this should not have happened. Please help to fix this problem and report the issue to EOMasters.\n");
    contentPane.add(headerText, "top, left, growx, wmin 10, wrap");

    SystemReport errorReport = new SystemReport().name("EOMTBX_Error_Report").message(message).throwable(exception).logTail(150);
    CollapsiblePanel reportArea = CollapsiblePanel.createLongTextPanel("Error Report Preview", errorReport.generate());
    contentPane.add(reportArea, "top, left, grow, wrap");

    showDialog(contentPane, errorReport);
  }

  private static void showDialog(JPanel contentPane, SystemReport errorReport) {
    JButton byMail = createMailButton(errorReport);
    byMail.requestFocusInWindow();
    contentPane.add(byMail, "right");
    JButton close = new JButton("Close");
    contentPane.add(close, "right, wrap");

    JDialog dialog = new JDialog();
    close.addActionListener(e -> {
      dialog.setVisible(false);
      dialog.dispose();
    });

    dialog.setIconImage(Icons.EOMTBX.s16.getImage());
    dialog.setTitle("Unexpected Error");
    dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    dialog.setLocationRelativeTo(null);
    dialog.setContentPane(contentPane);
    dialog.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
    dialog.pack();
    dialog.setVisible(true);
  }

  private static JButton createMailButton(SystemReport errorReport) {
    JButton byMail = new JButton("Report by Mail");
    byMail.addActionListener(e -> {
      try {
        JPanel panel = new JPanel(new MigLayout("top, left, fillx, gap 5 5"));
        FileSharingService sharingService = FileSharing.getService();
        String serviceName = sharingService.getName().replace(" ", "_");
        panel.add(new JLabel("<html>The error report will be uploaded to the <b>" + serviceName
            + "</b> file sharing service and linked in the e-mail.<br>" + "Please note the <b>"
            + "Terms of Service</b> and the <b>Privacy Policy</b>.<br>"), "top, left, wrap");
        JButton tosBtn = new JButton("Open Terms of Service");
        tosBtn.addActionListener(new OpenUriAdaptor(sharingService.getTosUrl()));
        panel.add(new JLabel(), "split 4, top, left, growx");
        panel.add(tosBtn, "top, left");
        JButton privacyBtn = new JButton("Open Privacy Policy");
        privacyBtn.addActionListener(new OpenUriAdaptor(sharingService.getPrivacyUrl()));
        panel.add(privacyBtn, "top, left");
        panel.add(new JLabel(), "top, left, growx, wrap");
        panel.add(new JLabel("Do you want to continue?"), "top, left, wrap");
        panel.add(new JLabel(
                "If not, please use the preview area to either copy the report to the clipboard or save it "
                    + "as a file and provide it with the mail."), "top, left, wrap");

        boolean uploadAllowed = Dialogs.confirmation("Report Error", panel, "errorReportUpload." + serviceName);

        String bodyText = createMailBody(errorReport, uploadAllowed, sharingService);
        MailTo mailTo = new MailTo("error@eomasters.com").subject("EOMTBX Error Report").body(bodyText);
        Desktop.getDesktop().mail(mailTo.toUri());
      } catch (Exception ex) {
        ErrorHandler.handleError("Error opening mail client", "Could not open mail client:\n" + ex.getMessage());
      }

    });
    return byMail;
  }

  private static String createMailBody(SystemReport errorReport, boolean uploadAllowed,
      FileSharingService sharingService) throws IOException {
    StringBuilder bodyText = new StringBuilder("<Please description briefly what you did before the error occurred. "
        + "Screenshots help to explain your work.>\n\n");
    if (uploadAllowed) {
      DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("uuuuMMdd_HHmmss")
          .withZone(ZoneId.from(ZoneOffset.UTC));
      String reportName = errorReport.getName();
      Instant created = errorReport.getCreatedAt();
      String fileName = String.format(reportName + "_%s.txt", timeFormatter.format(created));
      String report = errorReport.generate();
      UploadResponse uploadResponse = sharingService.uploadFile(fileName, new StringInputStream(report));
      if (uploadResponse.getStatus() != 200) {
        ErrorHandler.handleError("Error Report",
            "The error report could not be uploaded:\n" + uploadResponse.getStatus() + " "
                + uploadResponse.getStatusMessage());
        bodyText.append("<Add the error report manually by attaching it as file or pasting the text here.");
      } else {
        bodyText.append("The error report can be found at ").append(uploadResponse.getUrl());
      }
    } else {
      bodyText.append("<Add the error report manually by attaching it as file or pasting the text here.");
    }
    return bodyText.toString();
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
      container.setLayout(new MigLayout("top, left, fillx, gap 5 5"));
      JButton errorDialog = new JButton("Show Error Dialog");
      errorDialog.addActionListener(e -> ErrorHandler.handleError("Title", "An error occurred."));
      JButton extendedErrorDialog = new JButton("Show Extended Error Dialog");
      extendedErrorDialog.addActionListener(e -> ErrorHandler.handleError("Title", "An error occurred.",
          new Exception("Test", new Exception("theCause"))));
      JButton openErrorHandler = new JButton("Show Exception Handler");
      openErrorHandler.addActionListener(
          e -> ErrorHandler.handleUnexpectedException("Test", new Exception("Test", new Exception("theCause"))));
      container.add(errorDialog);
      container.add(extendedErrorDialog);
      container.add(openErrorHandler);
      window.pack();
      window.setVisible(true);
    });
  }

}
