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

package org.eomasters.eomtbx.icons;

import java.util.Objects;
import javax.swing.ImageIcon;

/**
 * Representing an icon and provides access to the different sizes.
 */
public class Icon {

  public final ImageIcon s16;
  public final ImageIcon s24;
  public final ImageIcon s32;
  public final ImageIcon s48;

  private final String name;

  /**
   * Creates a new Icon with the given name.
   *
   * @param name the name of the icon
   */
  public Icon(String name) {
    this.name = name;
    s16 = createIcon(16);
    s24 = createIcon(24);
    s32 = createIcon(32);
    s48 = createIcon(48);
  }

  private ImageIcon createIcon(int size) {
    return new ImageIcon(
        Objects.requireNonNull(Icons.class.getResource("/org/eomasters/eomtbx/icons/" + name + "_" + size + ".png")));
  }

}
