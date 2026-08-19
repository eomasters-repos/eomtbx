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

package org.eomasters.eomtbx.utils;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.operator.CropDescriptor;
import javax.media.jai.operator.TranslateDescriptor;

public class JaiUtils {

  /**
   * Crops a specified region from a single-band image and translates it such that the cropped region
   * is positioned at the origin (0, 0) of the resulting image.
   *
   * @param image The single-band image to be cropped and translated.
   * @param region The rectangle defining the region to crop from the image.
   * @param hints Rendering hints to be used for the crop and translate operations.
   * @return A RenderedImage that represents the cropped and translated region.
   */
  public static RenderedImage cropAndTranslate(RenderedImage image, Rectangle region, RenderingHints hints) {
    var croppedImage = CropDescriptor.create(image, (float) region.x, (float) region.y,
                                             (float) region.width, (float) region.height, hints);
    hints = hints == null ? new RenderingHints(null) : hints;
    ImageLayout layout = getOrAddImageLayout(hints);
    layout.setTileGridXOffset(0);
    layout.setTileGridYOffset(0);

    return TranslateDescriptor.create(croppedImage, (float) -region.x, (float) -region.y,
                                      Interpolation.getInstance(Interpolation.INTERP_NEAREST), hints);
  }

  private static ImageLayout getOrAddImageLayout(RenderingHints hints) {
    ImageLayout layout;
    if(hints.containsKey(JAI.KEY_IMAGE_LAYOUT)) {
      layout = (ImageLayout) hints.get(JAI.KEY_IMAGE_LAYOUT);
    } else {
      layout = new ImageLayout();
      hints.put(JAI.KEY_IMAGE_LAYOUT, layout);
    }
    return layout;
  }
}
