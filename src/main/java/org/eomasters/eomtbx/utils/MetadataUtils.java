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

package org.eomasters.eomtbx.utils;

import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;

public class MetadataUtils {
  
  /**
   * Retrieves a MetadataAttribute from a given MetadataElement using a path.
   * The path is a String of names separated with '/'.
   * 
   * @param element the root MetadataElement to start navigation from
   * @param path the path to the attribute (e.g., "level1/level2/attributeName")
   * @return the MetadataAttribute at the specified path, or null if not found
   * @throws IllegalArgumentException if element or path is null
   */
  public static MetadataAttribute getAttributeFromPath(MetadataElement element, String path) {
    if (element == null) {
      throw new IllegalArgumentException("MetadataElement cannot be null");
    }
    if (path == null) {
      throw new IllegalArgumentException("Path cannot be null");
    }
    
    String[] pathComponents = path.split("/");
    if (pathComponents.length == 0) {
      return null;
    }
    
    MetadataElement currentElement = element;
    
    // Navigate through all path components except the last one (which should be the attribute name)
    for (int i = 0; i < pathComponents.length - 1; i++) {
      String componentName = pathComponents[i].trim();
      if (componentName.isEmpty()) {
        continue; // Skip empty components (e.g., from leading/trailing slashes)
      }
      
      currentElement = currentElement.getElement(componentName);
      if (currentElement == null) {
        return null; // Path not found
      }
    }
    
    // The last component should be the attribute name
    String attributeName = pathComponents[pathComponents.length - 1].trim();
    if (attributeName.isEmpty()) {
      return null;
    }
    
    return currentElement.getAttribute(attributeName);
  }

}
