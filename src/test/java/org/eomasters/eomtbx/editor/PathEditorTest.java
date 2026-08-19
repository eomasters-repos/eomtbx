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

/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.eomasters.eomtbx.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.swing.binding.Binding;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.binding.ComponentAdapter;
import com.bc.ceres.swing.binding.internal.TextComponentAdapter;
import java.nio.file.Path;
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.eomasters.eomtbx.converter.ConverterRegistrar;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class PathEditorTest {

  @BeforeAll
  public static void setUp() {
    ConverterRegistrar.registerConverter();
  }

  @AfterAll
  public static void tearDown() {
    ConverterRegistrar.deregisterConverter();
  }

  @Test
  public void testIsApplicable() {
    PathEditor pathEditor = new PathEditor();

    PropertyDescriptor fileDescriptor = new PropertyDescriptor("test", Path.class);
    assertTrue(pathEditor.isValidFor(fileDescriptor));

    PropertyDescriptor doubleDescriptor = new PropertyDescriptor("test", Double.TYPE);
    assertFalse(pathEditor.isValidFor(doubleDescriptor));
  }

  @Test
  public void testCreateEditorComponent() {
    PathEditor pathEditor = new PathEditor();

    PropertyContainer propertyContainer = PropertyContainer.createValueBacked(V.class);
    BindingContext bindingContext = new BindingContext(propertyContainer);
    PropertyDescriptor propertyDescriptor = propertyContainer.getDescriptor("filePath");
    assertSame(Path.class, propertyDescriptor.getType());

    assertTrue(pathEditor.isValidFor(propertyDescriptor));
    JComponent editorComponent = pathEditor.createEditorComponent(propertyDescriptor, bindingContext);
    assertNotNull(editorComponent);
    assertSame(JPanel.class, editorComponent.getClass());
    assertEquals(2, editorComponent.getComponentCount());

    JComponent[] components = bindingContext.getBinding("filePath").getComponents();
    assertEquals(1, components.length);
    assertSame(JTextField.class, components[0].getClass());
  }

  @Test
  public void testGetCurrentPath() {
    PropertyContainer propertyContainer = PropertyContainer.createValueBacked(V.class);
    BindingContext bindingContext = new BindingContext(propertyContainer);
    PropertyDescriptor propertyDescriptor = propertyContainer.getDescriptor("filePath");

    final JTextField textField = new JTextField();
    final ComponentAdapter adapter = new TextComponentAdapter(textField);
    final Binding binding = bindingContext.bind(propertyDescriptor.getName(), adapter);
    Optional<Path> currentPath = PathEditor.getCurrentPath(propertyDescriptor, binding);
    assertNotNull(currentPath);
    assertFalse(currentPath.isPresent());
  }

  private static class V {

    Path filePath;
  }
}
