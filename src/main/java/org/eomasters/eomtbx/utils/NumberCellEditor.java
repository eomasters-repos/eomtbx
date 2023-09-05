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
