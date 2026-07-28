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

package org.eomasters.eomtbx.io.aster.odl;

import java.util.Stack;

public class OdlParser {

  static final String VALUE_KEY = "VALUE";

  public OdlGroup parse(String odlString) {
    String[] lines = odlString.replaceAll(" ", "").split("\\n");
    OdlGroup rootGroup = new OdlGroup("Root"); // Root group
    Stack<OdlGroup> groupStack = new Stack<>();
    groupStack.push(rootGroup);

    for (String line : lines) {
      line = line.trim();
      if (line.startsWith("GROUP=")) {
        String groupName = line.substring(6).trim();
        OdlGroup group = new OdlGroup(groupName);
        groupStack.peek().addSubGroup(group);
        groupStack.push(group);
      } else if (line.startsWith("END_GROUP=")) {
        groupStack.pop();
      } else if (line.startsWith("OBJECT=")) {
        String objectName = line.substring(7).trim();
        OdlObject object = new OdlObject(objectName);
        groupStack.peek().addObject(object);
      } else if (line.startsWith("END_OBJECT=")) {
        // Do nothing since we're adding objects directly to groups
      } else {
        if (!groupStack.isEmpty() && !groupStack.peek().getObjects().isEmpty()) {
          String[] parts = line.split("=", 2);
          if (parts.length == 2) {
            String key = parts[0].trim();
            String value = parts[1].trim().replaceAll("^\"|\"$", ""); // Strip any surrounding quotes
            OdlObject obj = groupStack.peek().getObjects().get(groupStack.peek().getObjects().size() - 1);
            obj.addAttribute(key, value);
          }
        }
      }
    }
    return rootGroup;
  }

}
