/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Free for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2024 Marco Peters
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

package org.eomasters.eomtbx;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.lang.reflect.Field;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.junit.jupiter.api.Test;

class EomtbxIconsTest {

  /**
   * Shows the icons in a new window.
   *
   * @param args the command line arguments
   * @throws IllegalAccessException if the icons are not accessible
   */
  public static void main(String[] args) throws IllegalAccessException {
    final JFrame frame = new JFrame("Icons");
    frame.setPreferredSize(new Dimension(400, 400));
    Container contentPane = frame.getContentPane();
    Field[] fields = EomtbxIcons.class.getFields();
    for (Field field : fields) {
      if (Icon.class.isAssignableFrom(field.getType())) {
        contentPane.add(new JLabel(((Icon) field.get(null)).getImageIcon(Icon.SIZE_48)));
      }
    }
    int gridsize = (int) Math.ceil(Math.sqrt(contentPane.getComponents().length));
    contentPane.setLayout(new GridLayout(gridsize, gridsize));
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    SwingUtilities.invokeLater(() -> frame.setVisible(true));
  }

  @Test
  void getIcons() {
    assertNotNull(EomtbxIcons.EOMTBX);
    assertNotNull(Icons.IMPORT);
    assertNotNull(Icons.EXPORT);
    assertNotNull(EomtbxIcons.WVL_EDITOR);
  }

}
