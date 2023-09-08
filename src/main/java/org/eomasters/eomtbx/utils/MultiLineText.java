/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
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
 * -> http://www.gnu.org/licenses/gpl-3.0.html
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.utils;

import java.awt.Dimension;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import javax.swing.JLabel;
import javax.swing.JTextArea;

/**
 * A multi-line text component based on a JTextArea.
 *
 * <p>If used in a MigLayout the "wmin" property should be set.
 */
public class MultiLineText extends JTextArea {

  /**
   * Creates a new MultiLineText with the given text.
   *
   * @param text the text
   */
  public MultiLineText(String text) {
    super(text);
    setEditable(false);
    setLineWrap(true);
    setWrapStyleWord(true);
    JLabel label = new JLabel();
    setFont(label.getFont());
    setBackground(label.getBackground());
    int textWidth = getTextWidth(text);
    int preferredWidth = (int) (Math.ceil(textWidth / 50.) * 50);
    setPreferredWidth(preferredWidth);
  }

  /**
   * Sets the preferred width of the component while keeping the preferred height.
   *
   * @param preferredWidth the preferred width
   */
  public void setPreferredWidth(int preferredWidth) {
    setPreferredSize(new Dimension(preferredWidth, getPreferredSize().height));
  }

  private int getTextWidth(String text) {
    AffineTransform identity = new AffineTransform();
    FontRenderContext frc = new FontRenderContext(identity, true, true);
    return (int) (getFont().getStringBounds(text, frc).getWidth());
  }
}
