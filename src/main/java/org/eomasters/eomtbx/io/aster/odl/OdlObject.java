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

import java.util.HashMap;
import java.util.Map;

public class OdlObject {

  private final String name;
  private final Map<String, String> attributes = new HashMap<>();

  public OdlObject(String name) {
    this.name = name;
  }

  public void addAttribute(String key, String value) {
    attributes.put(key, value);
  }

  public String getName() {
    return name;
  }

  public boolean hasValue() {
    return attributes.containsKey(OdlParser.VALUE_KEY);
  }

  public String getValue() {
    return attributes.get(OdlParser.VALUE_KEY);
  }

  public boolean hasAttributes() {
    return !attributes.isEmpty();
  }
  public Map<String, String> getAttributes() {
    return attributes;
  }

  @Override
  public String toString() {
    return "OdlObject{" +
        "name='" + name + '\'' +
        ", attributes=" + attributes +
        '}';
  }
}
