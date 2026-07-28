/*-
 * ========================LICENSE_START=================================
 * EOM Commons SNAP - Library of common utilities for ESA SNAP
 * -> https://www.eomasters.org/
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

package org.eomasters.eomtbx.ciop;

import com.bc.ceres.multilevel.MultiLevelImage;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.PointOpImage;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.image.ImageManager;

/**
 * An image which computes the Cyanobacteria Index (CI) based in the spectral shape defined by the three bands.
 */
public class CyanoIndexOpImage extends PointOpImage {

  private final double wvlFactor;

  /**
   * Creates a new masked image.
   *
   * @param low            the low source image
   * @param center         the center source image
   * @param high           the high source image
   * @param targetDataType the target data type
   */
  public CyanoIndexOpImage(Band low, Band center, Band high, int targetDataType) {
    super(low.getGeophysicalImage(), center.getGeophysicalImage(), high.getGeophysicalImage(),
          createImageLayout(center, targetDataType), new RenderingHints(JAI.KEY_TILE_CACHE, JAI.getDefaultInstance().getTileCache()), false);
    wvlFactor = calcWvlFactor(low, center, high);
  }

  @Override
  protected void computeRect(PlanarImage[] sources, WritableRaster dest, Rectangle destRect) {
    Raster lowRaster = sources[0].getData(destRect);
    Raster centerRaster = sources[1].getData(destRect);
    Raster highRaster = sources[2].getData(destRect);
    for (int y = destRect.y; y < destRect.y + destRect.height; y++) {
      for (int x = destRect.x; x < destRect.x + destRect.width; x++) {
        double lowValue = lowRaster.getSampleDouble(x, y, 0);
        double centerValue = centerRaster.getSampleDouble(x, y, 0);
        double highValue = highRaster.getSampleDouble(x, y, 0);
        dest.setSample(x, y, 0, calcIndex(lowValue, centerValue, highValue, wvlFactor));
      }
    }
  }

  static float calcWvlFactor(Band low, Band center, Band high) {
    return (center.getSpectralWavelength() - low.getSpectralWavelength())
        / (high.getSpectralWavelength() - low.getSpectralWavelength());
  }

  static double calcIndex(double lowValue, double centerValue, double highValue, double wvlFactor) {
    return -1 * (centerValue - lowValue - (highValue - lowValue) * wvlFactor);
  }

  private static ImageLayout createImageLayout(Band center, int targetDataType) {
    MultiLevelImage image = center.getGeophysicalImage();
    return ImageManager.createSingleBandedImageLayout(targetDataType, image.getWidth(),
        image.getHeight(), image.getTileWidth(), center.getGeophysicalImage().getTileHeight());
  }
}
