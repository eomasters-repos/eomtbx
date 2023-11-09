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

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

public abstract class Icon {

  protected final String name;

  public Icon(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public ImageIcon getImageIcon(SIZE size) {
    return createIcon(size);
  }

  public List<? extends Image> getImages() {
    ArrayList<Image> images = new ArrayList<>();
    SIZE[] sizes = SIZE.values();
    for (SIZE size : sizes) {
      images.add(getImageIcon(size).getImage());
    }
    return images;
  }

  protected abstract ImageIcon createIcon(SIZE size);

  public Icon withAddition(Icon addition) {
    return new IconWithAddition(this, addition);
  }

  public enum SIZE {
    S16(16), S24(24), S32(32), S48(48);

    private final int size;

    SIZE(int size) {
      this.size = size;
    }

    public int getSize() {
      return size;
    }
  }

}
