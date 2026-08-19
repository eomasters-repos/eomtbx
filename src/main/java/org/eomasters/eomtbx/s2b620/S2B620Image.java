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

package org.eomasters.eomtbx.s2b620;

import static org.eomasters.eomtbx.s2b620.S2B620.IMAGE_DATA_TYPE;
import static org.eomasters.eomtbx.s2b620.S2B620.NO_DATA_VALUE;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.PointOpImage;
import org.eomasters.eomtbx.utils.Range;
import org.esa.snap.core.util.jai.SingleBandedSampleModel;

/**
 * S2B620Image is an extension of the PointOpImage class, which performs
 * operations for estimating the reflectance at 620 nm from the 665 nm band data.
 * The reflectance computation is based on a polynomial function and optionally
 * utilizes a range for filtering input values. (<a href="https://doi.org/10.1016/j.isprsjprs.2023.09.020">Paper</a>)
 *
 * The computed values are stored in the destination image, with invalid inputs
 * assigned a no-data value.
 */
public class S2B620Image extends PointOpImage {

  private static final double a = 169.3846;
  private static final double b = -15.57556;
  private static final double c = 1.316727;
  private static final double d = 0.0001484814;

  private final Range inputRange;

  /**
   * Constructs an instance of the S2B620Image class to derive reflectance at 620 nm
   * from the reflectance at 665 nm using a polynomial transformation. Optionally,
   * a range can be specified to filter the input values.
   *
   * @param image665 The input RenderedImage representing the reflectance at 665 nm.
   * @param inputRange An optional Range object that specifies the valid range of input values.
   *                   If null, no range constraints are applied.
   */
  public S2B620Image(RenderedImage image665, Range inputRange) {
    super(image665, createImageLayout(image665), new RenderingHints(JAI.KEY_TILE_CACHE, JAI.getDefaultInstance().getTileCache()), false);
    this.inputRange = inputRange;
  }

  @Override
  protected void computeRect(PlanarImage[] sources, WritableRaster dest, Rectangle destRect) {
    Raster b665 = sources[0].getData(destRect);
    for (int y = destRect.y; y < destRect.y + destRect.height; y++) {
      for (int x = destRect.x; x < destRect.x + destRect.width; x++) {
        float value665 = b665.getSampleFloat(x, y, 0);
        if (inputRange == null || inputRange.isInRange(value665)) {
          dest.setSample(x, y, 0, estimateRrs620(value665));
        }else {
          dest.setSample(x, y, 0, NO_DATA_VALUE);
        }
      }
    }
  }

  public static float estimateRrs620(double b665) {
    return (float) (a * Math.pow(b665, 3) + b * Math.pow(b665, 2) + c * b665 + d);
  }

  private static ImageLayout createImageLayout(RenderedImage image665) {
    int smWidth = image665.getTileWidth();
    int smHeight = image665.getTileHeight();
    SingleBandedSampleModel sm = new SingleBandedSampleModel(IMAGE_DATA_TYPE, smWidth, smHeight);
    return new ImageLayout(image665).setSampleModel(sm);
  }

}
