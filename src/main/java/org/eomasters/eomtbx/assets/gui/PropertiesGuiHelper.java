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

package org.eomasters.eomtbx.assets.gui;

import com.bc.ceres.binding.BindingException;
import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.swing.binding.Binding;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.binding.BindingProblemListener;
import com.bc.ceres.swing.binding.PropertyPane;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.eomasters.eomtbx.assets.HighlightingProblemListener;

public final class PropertiesGuiHelper {

  private PropertiesGuiHelper() {
  }

  public static JPanel createPropertiesPanel(BindingContext context) {
    PropertyPane propertyPane = new PropertyPane(context);
    changeProblemListener(context, new HighlightingProblemListener());
    return propertyPane.createPanel();
  }

  public static void changeProblemListener(BindingContext context, HighlightingProblemListener listener) {
    removeProblemListeners(context);
    context.addProblemListener(listener);
  }

  private static void removeProblemListeners(BindingContext bc) {
    BindingProblemListener[] problemListeners = bc.getProblemListeners();
    for (BindingProblemListener listener : problemListeners) {
      bc.removeProblemListener(listener);
    }
  }

  public static void changePreferredWidth(JComponent[] components, Class<? extends JComponent> type, int width) {
    for (JComponent component : components) {
      if (type.isInstance(component)) {
        Dimension preferredSize = component.getPreferredSize();
        preferredSize.width = width;
        component.setPreferredSize(preferredSize);
      }
    }
  }

  public static boolean validateValues(BindingContext bindings) {
    Property[] properties = bindings.getPropertySet().getProperties();
    for (Property property : properties) {
      try {
        property.validate(property.getValue());
      } catch (ValidationException e) {
        Binding binding = bindings.getBinding(property.getName());
        binding.reportProblem(new BindingException(e.getMessage()));
        JComponent focusComponent = binding.getComponentAdapter().getComponents()[0];
        focusComponent.requestFocus();
        return false;
      }
    }

    return true;
  }
}

