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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.Test;

class EomToolboxTest {

  @Test
  void importPreferences() throws Exception {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
        + "<!DOCTYPE preferences SYSTEM \"http://java.sun.com/dtd/preferences.dtd\">\n"
        + "<preferences EXTERNAL_XML_VERSION=\"1.0\">\n"
        + "  <root type=\"system\">\n"
        + "    <map/>\n"
        + "    <node name=\"snap\">\n"
        + "      <map/>\n"
        + "      <node name=\"eomtbx\">\n"
        + "        <map>\n"
        + "          <entry key=\"quickMenu.numActions\" value=\"7\"/>\n"
        + "        </map>\n"
        + "      </node>\n"
        + "    </node>\n"
        + "  </root>\n"
        + "</preferences>";
    InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

    Preferences eomtbxPreferences = EomToolbox.getPreferences();
    assertFalse(List.of(eomtbxPreferences.keys()).contains("quickMenu.numActions"));
    EomToolbox.importPreferences(is);
    assertTrue(List.of(eomtbxPreferences.keys()).contains("quickMenu.numActions"));
  }
}
