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

package org.eomasters.eomtbx.s2geom;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Vector;
import javax.media.jai.PlanarImage;
import javax.media.jai.PointOpImage;

public final class CombineDetectorImages extends PointOpImage {


  public CombineDetectorImages(RenderedImage[] images) {
    super(new Vector<>(Arrays.asList(images)), null, null, false);
  }

  static double[] combineVertically(double[][] valueLines) {
    if(valueLines.length == 0) {
      return new double[0];
    }
    double[] sumLine = new double[valueLines[1].length];
    Arrays.fill(sumLine, Double.NaN);
    for (double[] valueLine : valueLines) {
      for (int x = 0; x < valueLine.length; x++) {
        double value = valueLine[x];
        // set if not NaN
        if (!Double.isNaN(value)) {
          sumLine[x] = value;
        }
      }
    }
    return sumLine;
  }

  @Override
  protected void computeRect(PlanarImage[] sources, WritableRaster destRaster, Rectangle destRect) {
    double[][] valueLines = new double[sources.length][destRect.width * destRect.height];
    for (int i = 0; i < sources.length; i++) {
      sources[i].getData(destRect)
                .getSamples(destRect.x, destRect.y, destRect.width, destRect.height, 0, valueLines[i]);
    }
    double[] averageLine = combineVertically(valueLines);
    destRaster.setSamples(destRect.x, destRect.y, destRect.width, destRect.height, 0, averageLine);
  }

}
