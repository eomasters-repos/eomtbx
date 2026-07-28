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

import static org.eomasters.eomtbx.s2superres.S2SuperResolveImage.EXPECTED_SRC_RESOLUTION;
import static org.eomasters.eomtbx.utils.JaiUtils.cropAndTranslate;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.util.List;
import java.util.Map;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.FormatDescriptor;
import javax.media.jai.operator.ScaleDescriptor;
import org.eomasters.eomtbx.s2geom.S2DataFormat;
import org.eomasters.eomtbx.s2geom.S2GeometryImages;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;

public class GeometryBands {

  private final Dimension tileDimension;

  public GeometryBands(Dimension tileDimension) {
    this.tileDimension = tileDimension;
  }

  public void createBandsAndImages(Product source, S2DataFormat format, Product target,
                                   Rectangle targetSceneRegion,
                                   List<String> spectralBandNames) {
    S2GeometryImages s2GeometryImages = format.getS2GeometryImages();
    addSolarGeometryBands(target, targetSceneRegion, s2GeometryImages);
    addSatelliteGeometryBands(format, source, target, targetSceneRegion, s2GeometryImages, spectralBandNames);
  }

  private void addSatelliteGeometryBands(S2DataFormat format, Product source, Product target,
                                         Rectangle targetSceneRegion,
                                         S2GeometryImages s2GeometryImages, List<String> spectralBandNames) {
    Map<String, Integer> nameBandIndexMap = format.getNameBandIndexMap();
    for (String name : spectralBandNames) {
      int index = nameBandIndexMap.get(name);
      Band refBand = source.getBand(name);
      if (refBand != null) {
        String generalBandName = format.getCommonSpectralBandName(name);
        String satZenBandName = String.format("%s_%s", S2GeometryImages.VIEW_ZENITH_NAME, generalBandName);
        if (!target.containsBand(satZenBandName)) {
          Band viewZen = new Band(satZenBandName, ProductData.TYPE_FLOAT32, target.getSceneRasterWidth(),
                                  target.getSceneRasterHeight());
          target.addBand(viewZen);
          viewZen.setDescription(String.format("View zenith angle for %s at %dm resolution",
                                               name, S2SuperResOp.TARGET_PIXEL_SIZE));
          viewZen.setNoDataValueUsed(true);
          viewZen.setNoDataValue(Float.NaN);
          RenderedImage satZenAngleImage = s2GeometryImages.getViewZenithAngleImage(index,
                                                                                    S2SuperResOp.TARGET_PIXEL_SIZE,
                                                                                    tileDimension);
          RenderedImage formattedImage = FormatDescriptor.create(satZenAngleImage, DataBuffer.TYPE_FLOAT, null);
          RenderedImage cropAndTranslate = cropAndTranslate(formattedImage, targetSceneRegion, null);
          viewZen.setSourceImage(cropAndTranslate);
        }

        String satAziBandName = String.format("%s_%s", S2GeometryImages.VIEW_AZIMUTH_NAME, generalBandName);
        if (!target.containsBand(satAziBandName)) {
          Band viewAzi = new Band(satAziBandName, ProductData.TYPE_FLOAT32, target.getSceneRasterHeight(),
                                  target.getSceneRasterHeight());
          target.addBand(viewAzi);
          viewAzi.setDescription(String.format("View azimuth angle for %s at %dm resolution",
                                               name, S2SuperResOp.TARGET_PIXEL_SIZE));
          viewAzi.setNoDataValueUsed(true);
          viewAzi.setNoDataValue(Float.NaN);
          RenderedImage satAziAngleImage = s2GeometryImages.getViewAzimuthAngleImage(index,
                                                                                     S2SuperResOp.TARGET_PIXEL_SIZE,
                                                                                     tileDimension);
          RenderedImage formattedImage = FormatDescriptor.create(satAziAngleImage, DataBuffer.TYPE_FLOAT, null);
          RenderedImage cropAndTranslate = cropAndTranslate(formattedImage, targetSceneRegion, null);
          viewAzi.setSourceImage(cropAndTranslate);
        }
      }
    }
  }

  private void addSolarGeometryBands(Product target, Rectangle targetSceneRegion, S2GeometryImages s2GeometryImages) {
    float scaleFactor = (float) EXPECTED_SRC_RESOLUTION / S2SuperResOp.TARGET_PIXEL_SIZE;
    Interpolation interpolation = Interpolation.getInstance(Interpolation.INTERP_BILINEAR);
    RenderingHints hints = new RenderingHints(JAI.KEY_TILE_CACHE, null);
    ImageLayout imageLayout = new ImageLayout();
    imageLayout.setTileWidth(tileDimension.width);
    imageLayout.setTileHeight(tileDimension.height);
    hints.put(JAI.KEY_IMAGE_LAYOUT, imageLayout);

    Band solZen = new Band(S2GeometryImages.SUN_ZENITH_NAME, ProductData.TYPE_FLOAT32,
                           target.getSceneRasterWidth(), target.getSceneRasterHeight());
    target.addBand(solZen);
    solZen.setDescription("Sun zenith angle at " + S2SuperResOp.TARGET_PIXEL_SIZE + "m resolution");
    RenderedImage sunZenithImage = s2GeometryImages.getSunZenithImageForResolution(EXPECTED_SRC_RESOLUTION);
    RenderedOp sunZenithImageScaled = ScaleDescriptor.create(sunZenithImage, scaleFactor, scaleFactor, 0f, 0f,
                                                             interpolation, hints);
    RenderedOp formattedSunZenithImage = FormatDescriptor.create(sunZenithImageScaled, DataBuffer.TYPE_FLOAT, null);
    solZen.setSourceImage(cropAndTranslate(formattedSunZenithImage, targetSceneRegion, null));

    Band solAzi = new Band(S2GeometryImages.SUN_AZIMUTH_NAME, ProductData.TYPE_FLOAT32,
                           target.getSceneRasterWidth(), target.getSceneRasterHeight());
    target.addBand(solAzi);
    solAzi.setDescription("Sun azimuth angle at " + S2SuperResOp.TARGET_PIXEL_SIZE + "m resolution");
    RenderedImage sunAzimuthImage = s2GeometryImages.getSunAzimuthImageForResolution(EXPECTED_SRC_RESOLUTION);
    RenderedOp sunAzimuthImageScaled = ScaleDescriptor.create(sunAzimuthImage, scaleFactor, scaleFactor, 0f, 0f,
                                                              interpolation, hints);
    RenderedOp formattedSunAzimuthImage = FormatDescriptor.create(sunAzimuthImageScaled, DataBuffer.TYPE_FLOAT, null);
    solAzi.setSourceImage(cropAndTranslate(formattedSunAzimuthImage, targetSceneRegion, null));
  }

}
