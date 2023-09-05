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

package org.eomasters.eomtbx.utils;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class NumberCellEditor extends DefaultCellEditor {

  private final Class<? extends Number> numberClass;

  public NumberCellEditor(Class<? extends Number> numberClass) {
    super(new JTextField());
    this.numberClass = numberClass;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    final JTextField textField = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
    textField.selectAll();
    textField.setHorizontalAlignment(JTextField.RIGHT);
    return textField;
  }

  @Override
  public boolean stopCellEditing() {
    JTextField textField = (JTextField) getComponent();
    try {
      switch (numberClass.getSimpleName()) {
        case "Integer":
          Integer.parseInt(textField.getText());
          break;
        case "Long":
          Long.parseLong(textField.getText());
          break;
        case "Float":
          Float.parseFloat(textField.getText());
          break;
        case "Double":
          Double.parseDouble(textField.getText());
          break;
        default:
          throw new IllegalArgumentException("Unsupported number class: " + numberClass);
      }
    } catch (NumberFormatException ignored) {
      ((JComponent) getComponent()).setBorder(new LineBorder(Color.red));
      return false;
    }

    if (!super.stopCellEditing()) {
      ((JComponent) getComponent()).setBorder(new LineBorder(Color.red));
      return false;
    }

    return true;
  }
}
