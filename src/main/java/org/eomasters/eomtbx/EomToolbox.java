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

import com.bc.ceres.binding.ConverterRegistry;
import com.jidesoft.swing.MultilineLabel;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.Cleaner;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.quickmenu.QuickMenu;
import org.eomasters.gui.CollapsiblePanel;
import org.eomasters.gui.Dialogs;
import org.eomasters.gui.UriField;
import org.eomasters.icons.Icon.SIZE;
import org.eomasters.snap.utils.PathConverter;
import org.eomasters.snap.utils.SnapSystemReport;
import org.eomasters.utils.ErrorHandler;
import org.eomasters.utils.FileSharing;
import org.eomasters.utils.FileSharingService;
import org.eomasters.utils.FileSharingService.UploadResponse;
import org.eomasters.utils.MailTo;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.runtime.EngineConfig;
import org.hsqldb.lib.StringInputStream;
import org.openide.modules.OnStart;
import org.openide.modules.OnStop;
import org.openide.windows.OnShowing;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The EOM-Toolbox. Provides general static methods for the EOMTBX.
 */
public class EomToolbox {

  public static final Logger LOG;
  static {
    LOG = Logger.getLogger(EomToolbox.class.getSimpleName());
    LOG.setLevel(EngineConfig.instance().logLevel()); // inherit log level from SNAP
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
      window.setLocationRelativeTo(null);

      Container container = window.getContentPane();
      container.setLayout(new MigLayout("top, left, fillx, gap 5 5"));
      JButton errorDialog = new JButton("Show Error Dialog");
      errorDialog.addActionListener(e -> ErrorHandler.handleError("Title", "An error occurred."));
      JButton extendedErrorDialog = new JButton("Show Extended Error Dialog");
      extendedErrorDialog.addActionListener(e -> ErrorHandler.handleError("Title", "An error occurred.",
          new Exception("Test", new Exception("theCause"))));
      JButton openErrorHandler = new JButton("Show Exception Handler");
      openErrorHandler.addActionListener(
          e -> EomToolbox.handleUnexpectedException("Test", new Exception("Test", new Exception("theCause"))));
      container.add(errorDialog);
      container.add(extendedErrorDialog);
      container.add(openErrorHandler);
      window.pack();
      window.setVisible(true);
    });
  }

  /**
   * The ID of the EOM-Toolbox.
   */
  public static final String TOOLBOX_ID = "eomtbx";
  public static final URI EOMASTERS_URL = URI.create("https://www.eomasters.org");
  public static final URI FORUM_URL = URI.create("https://www.eomasters.org/forum");
  private static final Preferences preferences = SnapApp.getDefault().getPreferences().node(TOOLBOX_ID);
  private static final Cleaner cleaner = Cleaner.create();

  /**
   * Returns the preferences of the EOM-Toolbox.
   *
   * @return the preferences
   */
  public static Preferences getPreferences() {
    return preferences;
  }

  /**
   * Exports the preferences of the EOM-Toolbox.
   *
   * @param out the output stream
   * @throws IOException if an I/O error occurs
   */
  public static void exportPreferences(OutputStream out) throws IOException {
    try {
      getPreferences().exportSubtree(new PrintStream(out));
    } catch (BackingStoreException e) {
      throw new IOException(e);
    }
  }

  /**
   * Imports the preferences of the EOM-Toolbox.
   *
   * @param in the input stream
   * @throws IOException if an I/O error occurs
   */
  public static void importPreferences(InputStream in) throws IOException {
    // Preferences.importPreferences(in); is not working for some reason
    try {
      Document document = loadPreferencesDocument(in);
      XPath xpath = XPathFactory.newInstance().newXPath();
      Preferences preferences = getPreferences();
      String expression = createSubNodesExpression(preferences);
      NodeList prefNodes = (NodeList) xpath.compile(expression).evaluate(document, XPathConstants.NODESET);
      addNodes(preferences, prefNodes);
      preferences.flush();
    } catch (XPathExpressionException | BackingStoreException e) {
      throw new IOException(e);
    }

  }

  private static void addNodes(Preferences parentNode, NodeList prefNodes) {
    for (int i = 0; i < prefNodes.getLength(); i++) {
      Node item = prefNodes.item(i);
      if (item.getNodeType() == Node.ELEMENT_NODE) {
        processNode(parentNode, item);
      }
    }
  }

  private static void processNode(Preferences parentNode, Node item) {
    if (item.getNodeName().equals("node")) {
      String name = item.getAttributes().getNamedItem("name").getNodeValue();
      Preferences node = parentNode.node(name);
      addNodes(node, item.getChildNodes());
    } else if (item.getNodeName().equals("map")) {
      processMap(parentNode, item.getChildNodes());
    }
  }

  private static void processMap(Preferences parentNode, NodeList entries) {
    for (int j = 0; j < entries.getLength(); j++) {
      Node entry = entries.item(j);
      if (entry.getNodeType() == Node.ELEMENT_NODE && entry.getNodeName().equals("entry")) {
        String key = entry.getAttributes().getNamedItem("key").getNodeValue();
        String value = entry.getAttributes().getNamedItem("value").getNodeValue();
        parentNode.put(key, value);
      }
    }
  }

  private static String createSubNodesExpression(Preferences preferences) {
    String[] splits = preferences.absolutePath().replaceFirst("/", "").split("/");
    StringBuilder sb = new StringBuilder();
    sb.append("/preferences/root[@type='system']");
    for (String split : splits) {
      sb.append("/node[@name='").append(split).append("']");
    }
    sb.append("/*");
    return sb.toString();
  }

  static Document loadPreferencesDocument(InputStream in) throws IOException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setIgnoringElementContentWhitespace(true);
    dbf.setIgnoringComments(true);
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      return db.parse(new InputSource(in));
    } catch (ParserConfigurationException | SAXException e) {
      throw new IOException(e);
    }
  }

  /**
   * Handles the given throwable. In a headless environment the throwable is only logged to the console. If GUI is
   * available a dialog is shown in addition.
   *
   * @param message   the message
   * @param exception the throwable
   */
  public static void handleUnexpectedException(String message, Throwable exception) {
    LOG.log(Level.SEVERE, message, exception);
    if (org.eomasters.utils.SystemHelper.isHeadless()) {
      return;
    }

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

    dialog.setIconImage(EomtbxIcons.EOMTBX.getImageIcon(SIZE.S16).getImage());
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
        Desktop.getDesktop().browse(FORUM_URL);
      } catch (IOException ex) {
        Dialogs.error("Error opening browser", "Could not open browser:\n" + ex.getMessage(), null);
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
            "errorReportUpload." + serviceName, getPreferences());

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


  /**
   * Invoked when the application is started. Not intended to be called by clients.
   */
  @OnStart
  public static class OnStartOperation implements Runnable {

    @Override
    public void run() {
      new Thread(
          () -> {
            ConverterRegistry.getInstance().setConverter(Path.class, new PathConverter());
            QuickMenu.getInstance().init();
          }).start();
    }
  }

  /**
   * Invoked when the GUI is shown. Not intended to be called by clients.
   */
  @OnShowing
  public static class OnShowingOperation implements Runnable {

    @Override
    public void run() {
      new Thread(() -> QuickMenu.getInstance().start()).start();
    }

  }

  /**
   * Invoked when the application is stopped. Not intended to be called by clients.
   */
  @OnStop
  public static class OnStopOperation implements Runnable {

    @Override
    public void run() {
      QuickMenu.getInstance().stop();
    }
  }
}
