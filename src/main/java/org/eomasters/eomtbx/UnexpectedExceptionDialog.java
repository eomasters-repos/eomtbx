/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2024 Marco Peters
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

package org.eomasters.eomtbx;

import com.jidesoft.swing.MultilineLabel;
import java.awt.Desktop;
import java.awt.Font;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;
import org.eomasters.gui.CollapsiblePanel;
import org.eomasters.gui.Dialogs;
import org.eomasters.gui.UriField;
import org.eomasters.icons.Icon;
import org.eomasters.snap.utils.SnapSystemReport;
import org.eomasters.utils.ErrorHandler;
import org.eomasters.utils.FileSharing;
import org.eomasters.utils.FileSharingService;
import org.eomasters.utils.FileSharingService.UploadResponse;
import org.eomasters.utils.MailTo;
import org.hsqldb.lib.StringInputStream;

public class UnexpectedExceptionDialog {

  private UnexpectedExceptionDialog() {
  }

  static void showDialog(String message, Throwable exception) {
    JPanel contentPane = new JPanel(new MigLayout("top, left, fillx, gap 5 5"));
    contentPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

    JLabel anErrorOccurred = new JLabel("A System Exception Occurred");
    anErrorOccurred.setFont(anErrorOccurred.getFont().deriveFont(Font.BOLD, 28f));
    contentPane.add(anErrorOccurred, "top, left, wrap");

    JTextArea headerText = new MultilineLabel(
        "Sorry, this should not have happened. Please help to fix this problem and report the issue to EOMasters.");
    contentPane.add(headerText, "top, left, growx, wmin 10, wrap");

    SnapSystemReport errorReport = new SnapSystemReport().name("EOMTBX_Error_Report")
                                                         .message(message)
                                                         .throwable(exception)
                                                         .logTail(150);
    CollapsiblePanel reportArea = CollapsiblePanel.createLongTextPanel("Error Report Preview", errorReport.generate());
    contentPane.add(reportArea, "top, left, grow, wrap");

    showDialog(contentPane, errorReport);
  }

  private static void showDialog(JPanel contentPane, SnapSystemReport errorReport) {
    JDialog dialog = new JDialog();
    JPanel buttonPanel = createButtonPanel(errorReport, dialog);
    contentPane.add(buttonPanel, "top, left, grow, wrap");

    dialog.setIconImage(EomtbxIcons.EOMTBX.getImageIcon(Icon.SIZE_16).getImage());
    dialog.setTitle("Unexpected Error");
    dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    dialog.setLocationRelativeTo(null);
    dialog.setContentPane(contentPane);
    dialog.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
    dialog.pack();
    dialog.setVisible(true);
  }

  private static JPanel createButtonPanel(SnapSystemReport errorReport, JDialog dialog) {
    JPanel btnPanel = new JPanel(new MigLayout("flowx, top, right"));
    JButton byMail = createMailButton(errorReport);
    byMail.requestFocusInWindow();
    btnPanel.add(byMail, "right");
    JButton reportInForum = new JButton("Report in Forum");
    reportInForum.addActionListener(e -> {
      try {
        Desktop.getDesktop().browse(EomToolbox.FORUM_URL);
      } catch (IOException ex) {
        Dialogs.error("Error opening browser", "Could not open browser:\n" + ex.getMessage());
      }
    });
    btnPanel.add(reportInForum, "right");

    JButton close = new JButton("Close");
    btnPanel.add(close, "right, wrap");
    close.addActionListener(e -> {
      dialog.setVisible(false);
      dialog.dispose();
    });
    return btnPanel;
  }

  private static JButton createMailButton(SnapSystemReport errorReport) {
    JButton byMail = new JButton("Report by Mail");
    byMail.addActionListener(e -> {
      try {
        JPanel panel = new JPanel(new MigLayout("top, left, fillx, gap 5 5"));
        FileSharingService sharingService = FileSharing.getService();
        String serviceName = sharingService.getName().replace(" ", "_");
        panel.add(new JLabel("<html>The error report will be uploaded to the <b>" + serviceName
            + "</b> file sharing service and linked in the e-mail.<br>" + "Please note the<br>"), "top, left, wrap");

        JPanel linkPanel = new JPanel(new MigLayout("top, left, gap 5 5"));
        UriField openTermsOfService = new UriField(sharingService.getTosUrl(), "Terms of Service");
        UriField openPrivacyPolicy = new UriField(sharingService.getPrivacyUrl(), "Privacy Policy");
        linkPanel.add(openTermsOfService, "top");
        linkPanel.add(new JLabel(" & "), "top");
        linkPanel.add(openPrivacyPolicy, "top");

        panel.add(linkPanel, "top, left, wrap");
        panel.add(new JLabel("Do you want to continue?"), "top, left, wrap");
        panel.add(new JLabel(
            "If not, please use the preview area to either copy the report to the clipboard or save it "
                + "as a file and provide it with the mail."), "top, left, wrap");

        boolean uploadAllowed = Dialogs.confirmation("Report Error", panel, null,
            "errorReportUpload." + serviceName, EomToolbox.getPreferences());

        String bodyText = createMailBody(errorReport, uploadAllowed, sharingService);
        MailTo mailTo = new MailTo("error@eomasters.com").subject("EOMTBX Error Report").body(bodyText);
        Desktop.getDesktop().mail(mailTo.toUri());
      } catch (Exception ex) {
        ErrorHandler.handleError("Error opening mail client", "Could not open mail client:\n" + ex.getMessage());
      }

    });
    return byMail;
  }

  private static String createMailBody(SnapSystemReport errorReport, boolean uploadAllowed,
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
}
