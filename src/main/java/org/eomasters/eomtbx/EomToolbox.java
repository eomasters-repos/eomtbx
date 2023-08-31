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

package org.eomasters.eomtbx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.eomasters.eomtbx.quickmenu.QuickMenu;
import org.esa.snap.rcp.SnapApp;
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

  /**
   * The ID of the EOM-Toolbox.
   */
  public static final String TOOLBOX_ID = "eomtbx";
  private static final Preferences preferences = SnapApp.getDefault().getPreferences().node(TOOLBOX_ID);

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
    // Preferences.importPreferences(in); is not working
    Document document = loadPreferencesDocument(in);
    XPath xpath = XPathFactory.newInstance().newXPath();
    String snapName = SnapApp.getDefault().getInstanceName().toLowerCase();
    String expression = String.format("/preferences/root[@type='system']/node[@name='%s']/node[@name='%s']/map/entry",
        snapName, TOOLBOX_ID);
    try {
      NodeList prefNodes = (NodeList) xpath.compile(expression).evaluate(document, XPathConstants.NODESET);
      Preferences preferences = getPreferences();
      for (int i = 0; i < prefNodes.getLength(); i++) {
        Node item = prefNodes.item(i);
        if (item.getNodeType() == Node.ELEMENT_NODE) {
          String key = item.getAttributes().getNamedItem("key").getNodeValue();
          String value = item.getAttributes().getNamedItem("value").getNodeValue();
          preferences.put(key, value);
        }
      }
      preferences.flush();
    } catch (XPathExpressionException | BackingStoreException e) {
      throw new RuntimeException(e);
    }

  }

  private static Document loadPreferencesDocument(InputStream in) throws IOException {
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
   * Invoked when the application is started. Not intended to be called by clients.
   */
  @OnStart
  public static class OnStartOperation implements Runnable {

    @Override
    public void run() {
      new Thread(() -> QuickMenu.getInstance().init()).start();
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
