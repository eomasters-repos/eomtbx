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

package org.eomasters.eomtbx.cmap;

import com.bc.ceres.core.ProgressMonitor;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import javax.media.jai.PlanarImage;

public class ImageUtils {

  public static BufferedImage getAsBufferedImage(RenderedImage mapImage, ProgressMonitor pm) {
    PlanarImage image = PlanarImage.wrapRenderedImage(mapImage);
    BufferedImage bufferedImage = createCompatibleEmptyBufferedImage(image);
    loadIntoBufferedImage(image, bufferedImage, pm);
    return bufferedImage;
  }

  private static BufferedImage createCompatibleEmptyBufferedImage(PlanarImage image) {
    SampleModel sourceSm = image.getSampleModel();
    SampleModel sampleModel = sourceSm.createCompatibleSampleModel(image.getWidth(), image.getHeight());
    Point location = image.getBounds().getLocation();
    return new BufferedImage(
        image.getColorModel(),
        WritableRaster.createWritableRaster(sampleModel, location),
        image.getColorModel().isAlphaPremultiplied(),
        null);
  }

  private static void loadIntoBufferedImage(PlanarImage image, BufferedImage bufferedImage, ProgressMonitor pm) {

    try {
      Point[] allTiles = image.getTileIndices(image.getBounds());
      pm.beginTask("Loading data ...", allTiles.length);
      image.queueTiles(allTiles);
      Arrays.stream(allTiles).forEach(point -> {
        if (!pm.isCanceled()) {
          Raster tile = image.getTile(point.x, point.y);
          bufferedImage.setData(tile);
          pm.worked(1);
        }
      });
    } finally {
      pm.done();
    }
  }
}
