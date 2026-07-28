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

package org.eomasters.eomtbx.s2superres;

import static org.eomasters.eomtbx.s2superres.S2SuperResolveImage.SCALING;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D.Double;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.TiePointGrid;

public class TiePointHandler {

  private final ArrayList<TiePointGrid> tiePointGrids;
  private final Rectangle srcPixelRegion;

  public TiePointHandler(Product source, Product target, Rectangle tgtSceneRegion, Rectangle srcPixelRegion) {
    this.srcPixelRegion = srcPixelRegion;
    tiePointGrids = new ArrayList<>();
    TiePointGrid[] sourceTiePointGrids = source.getTiePointGrids();
    for (TiePointGrid srcTpg : sourceTiePointGrids) {
      if (!target.containsTiePointGrid(srcTpg.getName())) {
        TiePointGrid tgtTPG = createTiePointGrid(tgtSceneRegion, srcTpg);
        tgtTPG.setScalingFactor(srcTpg.getScalingFactor());
        tgtTPG.setScalingOffset(srcTpg.getScalingOffset());
        target.addTiePointGrid(tgtTPG);
        tiePointGrids.add(tgtTPG);
      }
    }
  }

  private static TiePointGrid createTiePointGrid(Rectangle tgtSceneRegion, TiePointGrid srcTpg) {
    int tgtTpgWidth = Math.max(2, (int) (tgtSceneRegion.getWidth() / (srcTpg.getSubSamplingX() * SCALING)));
    int tgtTpgHeight = Math.max(2, (int) (tgtSceneRegion.getHeight() / (srcTpg.getSubSamplingY() * SCALING)));
    int tgtSubsamplingX = tgtSceneRegion.width / tgtTpgWidth;
    int tgtSubsamplingY = tgtSceneRegion.height / tgtTpgHeight;

    return new TiePointGrid(srcTpg.getName(), tgtTpgWidth, tgtTpgHeight, 0, 0,
                            tgtSubsamplingX, tgtSubsamplingY);
  }

  public void fillTiePointGridsWithData(Product source) {
    AffineTransform t2sTransform = new AffineTransform();
    t2sTransform.scale(1.0 / SCALING, 1.0 / SCALING);
    Rectangle sourceImageRect = new Rectangle(source.getSceneRasterSize());

    tiePointGrids.stream().parallel().
          forEach(tiePointGrid -> {
                    int tgtTpgWidth = tiePointGrid.getGridWidth();
                    int tgtTpgHeight = tiePointGrid.getGridHeight();
                    int tgtSubsamplingX = (int) tiePointGrid.getSubSamplingX();
                    int tgtSubsamplingY = (int) tiePointGrid.getSubSamplingY();

                    Rectangle tpgRegion = new Rectangle(srcPixelRegion.x, srcPixelRegion.y,
                                                        tgtTpgWidth * tgtSubsamplingX,
                                                        tgtTpgHeight * tgtSubsamplingY);
                    Rectangle tpgRegionInSource = sourceImageRect.intersection(
                        t2sTransform.createTransformedShape(tpgRegion).getBounds());

                    RenderedImage sourceImage = source.getTiePointGrid(tiePointGrid.getName()).getSourceImage();
                    Raster data = sourceImage.getData(tpgRegionInSource);
                    Double srcPoint = new Double();
                    Double tgtPoint = new Double();
                    float[] tgtTpData = new float[tgtTpgWidth * tgtTpgHeight];
                    for (int y = 0; y < tgtTpgHeight; y++) {
                      double tpScenePointY = (y * tgtSubsamplingY) + srcPixelRegion.y;
                      for (int x = 0; x < tgtTpgWidth; x++) {
                        double tpScenePointX = (x * tgtSubsamplingX) + srcPixelRegion.x;
                        tgtPoint.setLocation(tpScenePointX, tpScenePointY);
                        t2sTransform.transform(tgtPoint, srcPoint);
                        double pointX = Math.min(srcPoint.x, tpgRegionInSource.getMaxX() - 1);
                        double pointY = Math.min(srcPoint.y, tpgRegionInSource.getMaxY() - 1);
                        tgtTpData[y * tgtTpgWidth + x] = data.getSampleFloat((int) pointX, (int) pointY, 0);
                      }
                    }

                    tiePointGrid.setData(ProductData.createInstance(tgtTpData));
                    if (tiePointGrid.getDiscontinuity() != TiePointGrid.DISCONT_NONE) {
                      int discontinuity = TiePointGrid.getDiscontinuity(tgtTpData);
                      tiePointGrid.setDiscontinuity(discontinuity);
                    }
                  }
          );
  }
}
