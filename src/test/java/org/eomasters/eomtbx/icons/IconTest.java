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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.swing.ImageIcon;
import org.eomasters.eomtbx.icons.Icon.SIZE;
import org.junit.jupiter.api.Test;

class IconTest {

  @Test
  public void createNotExistingIcon() {
    assertThrows(IllegalStateException.class, () -> {
      RasterIcon notExisting = new RasterIcon("NotExisting");
      notExisting.getImageIcon(SIZE.S32);
    });

  }

  @Test
  public void createIcon() {
    Icon eomtbx = new SvgIcon("EomToolbox");
    assertInstanceOf(ImageIcon.class, eomtbx.getImageIcon(SIZE.S48));
    assertInstanceOf(ImageIcon.class, eomtbx.getImageIcon(SIZE.S32));
    assertInstanceOf(ImageIcon.class, eomtbx.getImageIcon(SIZE.S24));
    assertInstanceOf(ImageIcon.class, eomtbx.getImageIcon(SIZE.S16));
  }

}
