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

package org.eomasters.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A text field with a label and an optional icon and button. This is a replacement for the JIDE LabeledTextField.
 */
public class LabeledTextField extends JPanel {

  private final JTextField textField;
  private final AbstractButton button;

  /**
   * Creates a new LabeledTextField with the specified icon and label text.
   *
   * @param icon      the icon to display next to the label
   * @param labelText the text for the label
   */
  public LabeledTextField(Icon icon, String labelText) {
    super(new BorderLayout(5, 0));

    JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
    JLabel label = new JLabel(labelText);
    if (icon != null) {
      label.setIcon(icon);
    }
    labelPanel.add(label);

    textField = new JTextField();

    add(labelPanel, BorderLayout.WEST);
    add(textField, BorderLayout.CENTER);

    // Create button if needed
    button = createButton();
    if (button != null) {
      add(button, BorderLayout.EAST);
    }
  }

  /**
   * Returns the text field component.
   *
   * @return the text field
   */
  public JTextField getTextField() {
    return textField;
  }

  /**
   * Returns the button component.
   *
   * @return the button, or null if no button was created
   */
  public AbstractButton getButton() {
    return button;
  }

  /**
   * Returns the text in the text field.
   *
   * @return the text
   */
  public String getText() {
    return textField.getText();
  }

  /**
   * Sets the text in the text field.
   *
   * @param text the text to set
   */
  public void setText(String text) {
    textField.setText(text);
  }

  /**
   * Creates a button for this component. This method can be overridden by subclasses to create a custom button.
   *
   * @return the button, or null if no button should be created
   */
  protected AbstractButton createButton() {
    return null;
  }
}
