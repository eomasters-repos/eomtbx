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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;

/**
 * Representing an icon and provides access to the different sizes.
 */
public class Icon {

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

  private final String name;

  /**
   * Creates a new Icon with the given name.
   *
   * @param name the name of the icon
   */
  public Icon(String name) {
    this.name = name;
  }

  public ImageIcon getImage(SIZE size) {
    return createIcon(size);
  }

  public List<? extends Image> getImages() {
    ArrayList<Image> images = new ArrayList<>();
    SIZE[] sizes = SIZE.values();
    for (SIZE size : sizes) {
      images.add(getImage(size).getImage());
    }
    return images;
  }
  protected ImageIcon createIcon(SIZE size) {
    URL resource = Icons.class.getResource("/org/eomasters/eomtbx/icons/" + name + "_" + size.getSize() + ".png");
    if (resource == null) {
      throw new IllegalStateException("Icon " + name + " with size " + size.getSize() + " not found.");
    }
    return new ImageIcon(resource);
  }

  public Icon withAddition(Icon addition) {
    return new IconWithAddition(this, addition);
  }

  private static class IconWithAddition extends Icon {

    private final Icon source;
    private final Icon addition;

    public IconWithAddition(Icon source, Icon addition) {
      super(source.name + "_" + addition.name);
      this.source = source;
      this.addition = addition;
    }

    @Override
    protected ImageIcon createIcon(SIZE size) {
      ImageIcon srcImage = source.getImage(size);
      ImageIcon addImage = addition.getImage(size);

      int pixelSize = size.getSize();
      BufferedImage image = new BufferedImage(pixelSize, pixelSize, BufferedImage.TYPE_4BYTE_ABGR);
      Graphics2D canvas = image.createGraphics();
      int quarter = pixelSize / 4;
      canvas.drawImage(srcImage.getImage(), quarter, 0, pixelSize - quarter, pixelSize - quarter, null);
      canvas.drawImage(addImage.getImage(), 0, 0, null);
      return new ImageIcon(image);
    }
  }
}
