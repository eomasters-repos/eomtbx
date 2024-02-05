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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.prefs.Preferences;
import org.eomasters.eomtbx.quickmenu.QuickMenu;
import org.eomasters.eomtbx.quickmenu.QuickMenuOptions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class EomToolboxTest {

  @Test
  void exportPreferences() throws Exception {
    Preferences qmPreferences = QuickMenu.getInstance().getPreferences();
    QuickMenuOptions quickMenuOptions = new QuickMenuOptions();
    quickMenuOptions.setNumActions(7);
    QuickMenuOptions.putToPreferences(quickMenuOptions, qmPreferences);
    qmPreferences.flush();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    EomToolbox.exportPreferences(out);
    String xml = out.toString(StandardCharsets.UTF_8);
    Document document = EomToolbox.loadPreferencesDocument(
        new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    NodeList nodeList = document.getElementsByTagName("node");
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      Node nameAttribute = node.getAttributes().getNamedItem("name");
      if (nameAttribute != null && nameAttribute.getNodeValue().equals("quickmenu")) {
        Node map = node.getFirstChild();
        NodeList entries = map.getChildNodes();
        for (int j = 0; j < entries.getLength(); j++) {
          Node emtry = entries.item(j);
          if (emtry.getAttributes().getNamedItem("key").getNodeValue().equals("numActions")) {
            assertEquals("7", emtry.getAttributes().getNamedItem("value").getNodeValue());
          }
        }
      }
    }
  }

  @Test
  void importPreferences() throws Exception {
    // The preferences path of SNAP is different depending on if the NetBeans platform runs or if the API is only invoked from the tests.
    // This is caused by NbPreferences.forModule(this.getClass()) in SnapApp.getPreferences().
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
        + "<!DOCTYPE preferences SYSTEM \"http://java.sun.com/dtd/preferences.dtd\">\n"
        + "<preferences EXTERNAL_XML_VERSION=\"1.0\">\n"
        + "  <root type=\"system\">\n"
        + "    <map/>\n"
        + "    <node name=\"org\">\n"
        + "      <map/>\n"
        + "      <node name=\"esa\">\n"
        + "        <map/>\n"
        // + "        <node name=\"snap\">\n"
        // + "          <map/>\n"
        + "          <node name=\"snap\">\n"
        + "            <map/>\n"
        + "            <node name=\"rcp\">\n"
        + "              <map/>\n"
        + "              <node name=\"eomtbx\">\n"
        + "                <map/>\n"
        + "                <node name=\"quickmenu\">\n"
        + "                  <map>\n"
        + "                    <entry key=\"numActions\" value=\"7\"/>\n"
        + "                  </map>\n"
        + "                  <node name=\"actions\">\n"
        + "                    <map/>\n"
        + "                  </node>\n"
        + "                </node>\n"
        + "              </node>\n"
        + "            </node>\n"
        + "          </node>\n"
        // + "        </node>\n"
        + "      </node>\n"
        + "    </node>\n"
        + "  </root>\n"
        + "</preferences>";
    InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

    Preferences eomtbxPreferences = EomToolbox.getPreferences();
    assertFalse(List.of(eomtbxPreferences.node("quickmenu").keys()).contains("numActions"));
    EomToolbox.importPreferences(is);
    assertTrue(List.of(eomtbxPreferences.node("quickmenu").keys()).contains("numActions"));
    assertEquals(7, eomtbxPreferences.node("quickmenu").getInt("numActions", -1));
  }
}
