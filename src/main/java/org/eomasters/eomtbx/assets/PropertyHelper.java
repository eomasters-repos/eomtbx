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

package org.eomasters.eomtbx.assets;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.ValidationException;
import java.util.Arrays;

public final class PropertyHelper {

  private PropertyHelper() {

  }

  public static void initDefaults(PropertyContainer propertyContainer) {
    Arrays.stream(propertyContainer.getProperties())
          .forEach(property -> {
            if (property.getDescriptor().getConverter() == null) {
              property.getDescriptor().setDefaultConverter();
            }
          });
    propertyContainer.setDefaultValues();
  }

  public static Property createProperty(String name, Class<?> type, String description) {
    return PropertyHelper.createProperty(name, type, description, null);
  }

  public static Property createProperty(String name, Class<?> type, String description, Object defaultValue) {
    Property property = com.bc.ceres.binding.Property.create(name, type);
    property.getDescriptor().setDescription(description);
    if (defaultValue != null) {
      property.getDescriptor().setDefaultValue(defaultValue);
    }
    return property;
  }

  public static boolean isEnabled(Property property) {
    final PropertyDescriptor propertyDescriptor = property.getDescriptor();
    final Object enabled = propertyDescriptor.getAttribute("enabled");
    return Boolean.TRUE.equals(enabled);
  }

  /**
   * Creates a selection group for the given properties. The properties must be of type Boolean. The boolean represents
   * the selected state, as for CheckBoxes or RadioButtons. Only one of the properties in this group can be selected.
   *
   * @param properties the properties to be included in the selection group
   * @throws IllegalArgumentException if any of the properties is not of type Boolean
   */
  public static void createSelectionGroup(Property... properties) {
    for (Property property : properties) {
      if (!Boolean.class.isAssignableFrom(property.getType())) {
        throw new IllegalArgumentException(String.format("Property '%s' must be of type Boolean", property.getName()));
      }
      property.addPropertyChangeListener(evt -> {
        if (evt.getNewValue().equals(true)) {
          // if this property is selected then deselect all other
          for (Property prop : properties) {
            if (!prop.getName().equals(evt.getPropertyName())) {
              try {
                prop.setValue(false);
              } catch (ValidationException e) {
                throw new RuntimeException(e);
              }
            }
          }
        }
        if (evt.getNewValue().equals(false)) {
          // if this property is deselected, check if some other is selected. If not, set back to true
          boolean anySelected = false;
          for (Property prop : properties) {
            if (prop.getValue().equals(true)) {
              anySelected = true;
              break;
            }
          }
          if (!anySelected) {
            try {
              property.setValue(true);
            } catch (ValidationException e) {
              throw new RuntimeException(e);
            }
          }
        }

      });
    }
  }
}
