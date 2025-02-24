/*-
 * ========================LICENSE_START=================================
 * EOMTBX Basic - EOMasters Toolbox Basic for SNAP
 * -> https://www.eomasters.org/eomtbx
 * ======================================================================
 * Copyright (C) 2023 - 2025 Marco Peters
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

package org.eomasters.eomtbx.actions;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.eomasters.gui.Dialogs;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.esa.snap.rcp.SnapApp;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;

public class FeedbackDialog {

  private static final String BASE_URL = "https://github.com/eomasters-repos/eomtbx-issues/issues/new?template=";
  private static final String BUG_REPORT_URL = BASE_URL + "01_bug_report.yml&used_toolbox=";
  private static final String FEATURE_REQUEST_URL = BASE_URL + "02_feature_request.yml&used_toolbox=";

  // Create and show the feedback dialog
  public void showDialog() {
    JDialog dialog = new JDialog();
    dialog.setTitle("Feedback");
    dialog.setIconImage(Icons.EOMASTERS.getImageIcon(Icon.SIZE_16).getImage());
    dialog.setModal(true);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setSize(new Dimension(400, 200));
    dialog.setLocationRelativeTo(SnapApp.getDefault().getMainFrame());

    // Construct the message
    JLabel messageLabel = new JLabel("<html><b>Thank you for providing feedback!</b><br><br>" +
                                         "You must have a GitHub account. Your are asked to login or to create one.<br>"
                                         + "You can either report a bug or request a feature:<br></html>");

    // predefining combo boxes is not working at the moment, but this might be fixed. Let's keep it.
    String toolboxType = "Basic%20Toolbox";
    if (isProToolboxInstalled()) {
      toolboxType = "Pro%20Toolbox";
    }

    // Buttons for bug report and feature request
    JButton bugButton = new JButton("Report a Bug");
    bugButton.addActionListener(new OpenUrlAction(BUG_REPORT_URL + toolboxType, dialog));

    JButton featureButton = new JButton("Request a Feature");
    featureButton.addActionListener(new OpenUrlAction(FEATURE_REQUEST_URL + toolboxType, dialog));

    // Layout
    JPanel buttonPanel = new JPanel(new FlowLayout());
    buttonPanel.add(bugButton);
    buttonPanel.add(featureButton);

    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    dialog.setContentPane(contentPane);
    contentPane.add(messageLabel, BorderLayout.CENTER);
    contentPane.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setVisible(true);
  }

  private static boolean isProToolboxInstalled() {
    ModuleInfo codeNameBase = Modules.getDefault().findCodeNameBase("org.eomasters.eomtbxp.eomtbxp-kit");
    return codeNameBase != null && codeNameBase.isEnabled();
  }

  // Helper class to open URLs in the default browser
  private static class OpenUrlAction implements ActionListener {

    private final String url;
    private final JDialog dialog;

    public OpenUrlAction(String url, JDialog dialog) {
      this.url = url;
      this.dialog = dialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        Desktop.getDesktop().browse(new URI(url));
        dialog.dispose();
      } catch (Exception ex) {
        Dialogs.error(null, "Error", "Failed to open URL: " + url);
      }
    }
  }
}
