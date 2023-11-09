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

package org.eomasters.eomtbx.icons;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.lang.reflect.Field;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.eomasters.eomtbx.icons.Icon.SIZE;

/**
 * Eases access to the icons used within the EOMTBX.
 */
public final class Icons {


  public static void main(String[] args) throws IllegalAccessException {
    final JFrame frame = new JFrame("SpeX Parameters Panel");
    frame.setPreferredSize(new Dimension(400, 400));
    Container contentPane = frame.getContentPane();
    SIZE size = SIZE.S16;
    Field[] fields = Icons.class.getFields();
    for (Field field : fields) {
      if(Icon.class.isAssignableFrom(field.getType())) {
        contentPane.add(new JLabel(((Icon)field.get(null)).getImageIcon(size)));
      }
    }
    int gridsize = (int) Math.ceil(Math.sqrt(contentPane.getComponents().length));
    contentPane.setLayout(new GridLayout(gridsize, gridsize));
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    SwingUtilities.invokeLater(() -> frame.setVisible(true));
  }

  /**
   * An icon with ABCDEF letters.
   */
  public static final Icon ABCDEF = new SvgIcon("Abcdef");
  /**
   * An icon representing a checkmark.
   */
  public static final Icon CHECKMARK = new SvgIcon("Checkmark");
  /**
   * An icon showing document with an arrow pointing upwards.
   */
  public static final Icon DOC_ARROW_UP = new SvgIcon("DocArrowUp");
  /**
   * The EOMTBX icon.
   */
  public static final Icon EOMTBX = new SvgIcon("EomToolbox");
  /**
   * An icon for the export action.
   */
  public static final Icon EXPORT = new SvgIcon("Export");
  /**
   * A filter icon.
   */
  public static final Icon FILTER = new SvgIcon("Filter");
  /**
   * An icon representing groups or categories.
   */
  public static final Icon GROUPS = new SvgIcon("Groups");
  /**
   * An icon for the import action.
   */
  public static final Icon IMPORT = new SvgIcon("Import");
  /**
   * An icon with an 'i'.
   */
  public static final Icon INFO = new SvgIcon("Info");
  /**
   * An icon showing document with an arrow pointing upwards.
   */
  public static final Icon MINUS = new SvgIcon("Minus");
  // public static final Icon MINUS = new RasterIcon("Minus");
  /**
   * An icon showing document with an arrow pointing upwards.
   */
  public static final Icon PLUS = new SvgIcon("Plus");
  // public static final Icon PLUS = new RasterIcon("Plus");
  /**
   * An icon representing the SpeX Database.
   */
  public static final Icon SPEX_DB = new RasterIcon("SPEX_DB");
  /**
   * An icon for the System Report.
   */
  public static final Icon SYS_REPORT = new RasterIcon("SysReport");
  /**
   * An icon for the Wavelength Editor.
   */
  public static final Icon WVL_EDITOR = new RasterIcon("WvlEditor");

  private Icons() {
  }

}
