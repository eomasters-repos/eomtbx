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

import java.util.ArrayList;
import java.util.List;

public class OdlGroup {

  private static final String PATH_SEPARATOR = "/";
  private final String name;
  private final List<OdlGroup> subGroups = new ArrayList<>();
  private final List<OdlObject> objects = new ArrayList<>();

  public OdlGroup(String name) {
    this.name = name;
  }

  public void addSubGroup(OdlGroup group) {
    subGroups.add(group);
  }

  public void addObject(OdlObject object) {
    objects.add(object);
  }

  public String getName() {
    return name;
  }

  public List<OdlGroup> getSubGroups() {
    return subGroups;
  }

  public List<OdlObject> getObjects() {
    return objects;
  }

  @Override
  public String toString() {
    return "OdlGroup{" +
        "name='" + name + '\'' +
        ", subGroups=" + subGroups +
        ", objects=" + objects +
        '}';
  }

  public String getAttribute(String path) {
    String[] parts = path.split(PATH_SEPARATOR);
    OdlGroup currentGroup = this;

    // Traverse the path to find the group or object
    for (int i = 0; i < parts.length - 1; i++) {
      String part = parts[i];
      boolean found = false;

      // Check sub-groups
      for (OdlGroup subGroup : currentGroup.getSubGroups()) {
        if (subGroup.getName().equals(part)) {
          currentGroup = subGroup;
          found = true;
          break;
        }
      }

      if (!found) {
        // Check objects in the current group
        for (OdlObject object : currentGroup.getObjects()) {
          if (object.getName().equals(part)) {
            // We found the object, now return the attribute
            String attributeName = parts[parts.length - 1];
            return object.getAttributes().get(attributeName);
          }
        }
        // If nothing is found, return null
        return null;
      }
    }

    // If the final part of the path refers to an object, get the attribute from it
    String objectName = parts[parts.length - 2];
    String attributeName = parts[parts.length - 1];

    for (OdlObject object : currentGroup.getObjects()) {
      if (object.getName().equals(objectName)) {
        return object.getAttributes().get(attributeName);
      }
    }

    return null;
  }

  public OdlGroup getSubGroup(String path) {
    String[] parts = path.split(PATH_SEPARATOR);
    OdlGroup currentGroup = this;

    // Traverse the path to find the group
    for (String part : parts) {
      boolean found = false;

      // Check sub-groups
      for (OdlGroup subGroup : currentGroup.getSubGroups()) {
        if (subGroup.getName().equals(part)) {
          currentGroup = subGroup;
          found = true;
          break;
        }
      }

      if (!found) {
        // If any part of the path is not found, return null
        return null;
      }
    }

    return currentGroup;
  }
}
