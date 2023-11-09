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

import java.net.URL;
import javax.swing.ImageIcon;

/**
 * Representing an icon and provides access to the different sizes.
 */
class RasterIcon extends Icon {

  /**
   * Creates a new Icon with the given name.
   *
   * @param name the name of the icon
   */
  public RasterIcon(String name) {
    super(name);
  }

  @Override
  protected ImageIcon createIcon(SIZE size) {
    URL resource = Icons.class.getResource("/org/eomasters/eomtbx/icons/" + getName() + "_" + size.getSize() + ".png");
    if (resource == null) {
      throw new IllegalStateException("Icon " + getName() + " with size " + size.getSize() + " not found.");
    }
    return new ImageIcon(resource);
  }

}
