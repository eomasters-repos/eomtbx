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

package org.eomasters.eomtbx.editor;

import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.swing.binding.Binding;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.binding.ComponentAdapter;
import com.bc.ceres.swing.binding.PropertyEditor;
import com.bc.ceres.swing.binding.internal.TextComponentAdapter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.nio.file.Path;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * An editor for {@link Path}s using a file chooser dialog. It is registered as service in the file
 * <code>META-INF/services/com.bc.ceres.swing.binding.PropertyEditor</code>.
 */
public class PathEditor extends PropertyEditor {

  @Override
  public boolean isValidFor(PropertyDescriptor propertyDescriptor) {
    return Path.class.isAssignableFrom(propertyDescriptor.getType())
        && !Boolean.TRUE.equals(propertyDescriptor.getAttribute("directory"));
  }

  @Override
  public JComponent createEditorComponent(PropertyDescriptor propertyDescriptor, BindingContext bindingContext) {
    final JTextField textField = new JTextField();
    final ComponentAdapter adapter = new TextComponentAdapter(textField);
    final Binding binding = bindingContext.bind(propertyDescriptor.getName(), adapter);
    final JPanel editorPanel = new JPanel(new BorderLayout(2, 2));
    editorPanel.add(textField, BorderLayout.CENTER);
    final JButton etcButton = new JButton("...");
    final Dimension size = new Dimension(26, 16);
    etcButton.setPreferredSize(size);
    etcButton.setMinimumSize(size);
    etcButton.addActionListener(e -> {
      final JFileChooser fileChooser = new JFileChooser();
      Optional<Path> currentPath = getCurrentPath(propertyDescriptor, binding);
      currentPath.ifPresent(path -> fileChooser.setSelectedFile(path.toFile()));
      int i = fileChooser.showDialog(editorPanel, "Select");
      if (i == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
        binding.setPropertyValue(fileChooser.getSelectedFile().toPath());
      }
    });
    editorPanel.add(etcButton, BorderLayout.EAST);
    return editorPanel;
  }

  static Optional<Path> getCurrentPath(PropertyDescriptor propertyDescriptor, Binding binding) {
    Path currentFile = (Path) binding.getPropertyValue();
    if (currentFile == null) {
      currentFile = (Path) propertyDescriptor.getDefaultValue();
    }
    return Optional.ofNullable(currentFile);
  }
}
