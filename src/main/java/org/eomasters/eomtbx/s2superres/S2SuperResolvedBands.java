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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.BandSelectDescriptor;
import javax.media.jai.operator.FormatDescriptor;
import org.apache.commons.lang3.ArrayUtils;
import org.eomasters.eomtbx.s2geom.S2DataFormat;
import org.eomasters.eomtbx.utils.JaiUtils;
import org.eomasters.snap.utils.ReplaceNaNOpImage;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.image.ImageManager;
import org.esa.snap.core.util.ProductUtils;

public class S2SuperResolvedBands {

  public static final String[] SUPER_RESOLVED_BANDS = {"B2", "B3", "B4", "B5", "B6", "B7", "B8", "B8A", "B11", "B12"};

  private S2SuperResolvedBands() {}

  public static void addBandsAndImagesToTarget(Product source, S2DataFormat sourceFormat, String[] requestedBands,
                                               Product target,
                                               Rectangle targetSceneRegion, Dimension tileSize, boolean debugMode) {
    Map<String, List<String>> superResolveBandsList = new HashMap<>();
    Map<String, String[]> spectralGroups = sourceFormat.getSpectralGroups();
    // it is only necessary to iterate over the known groups and check if the bands are requested
    for (Entry<String, String[]> spectralGroup : spectralGroups.entrySet()) {
      List<String> bandList = superResolveBandsList.computeIfAbsent(spectralGroup.getKey(), k -> new ArrayList<>());
      String[] spectralBands = spectralGroup.getValue();
      for (String spectralBandName : spectralBands) {
        String commonBandName = sourceFormat.getCommonSpectralBandName(spectralBandName);
        boolean neededSpectralBand = ArrayUtils.contains(SUPER_RESOLVED_BANDS, commonBandName);
        if (neededSpectralBand) {
          // band is needed as one of the ten super-resolved bands
          bandList.add(spectralBandName);
        }
      }
    }

    // they have the following bands: B2, B3, B4, B5, B6, B7, B8, B8A, B11, B12
    Map<String, RenderedImage> superResolveImages = createSuperResolveGroupImages(source, sourceFormat,
                                                                                  superResolveBandsList, tileSize,
                                                                                  debugMode);
    for (Entry<String, String[]> spectralGroup : spectralGroups.entrySet()) {
      String[] spectralBands = spectralGroup.getValue();
      RenderedImage multiBandResolveImage = superResolveImages.get(spectralGroup.getKey());

      for (String spectralBandName : spectralBands) {
        String commonBandName = sourceFormat.getCommonSpectralBandName(spectralBandName);
        int index = ArrayUtils.indexOf(requestedBands, commonBandName);
        if (index > -1) {
          // if requested as output also add it to the target product
          Band sourceBand = source.getBand(spectralBandName);
          Band targetBand = addTargetBandToTargetProduct(sourceBand, target);
          RenderingHints hints = new RenderingHints(JAI.KEY_TILE_CACHE, null);
          // mapping is necessary for the case if not all bands are requested
          var indexInImage = ArrayUtils.indexOf(SUPER_RESOLVED_BANDS, commonBandName);
          var singleBandImage = BandSelectDescriptor.create(multiBandResolveImage, new int[]{indexInImage}, hints);
          RenderedImage dataImage = JaiUtils.cropAndTranslate(singleBandImage, targetSceneRegion, hints);
          ReplaceNaNOpImage replaceNaNOpImage = new ReplaceNaNOpImage(dataImage, sourceBand.getNoDataValue());
          RenderedOp finalDataImage = FormatDescriptor.create(replaceNaNOpImage,
                                                              ImageManager.getDataBufferType(sourceBand.getDataType()),
                                                              null);
          targetBand.setSourceImage(finalDataImage);
        }
      }
    }
  }

  private static Band addTargetBandToTargetProduct(Band srcBand, Product target) {
    Band targetBand = new Band(srcBand.getName(), srcBand.getDataType(), target.getSceneRasterWidth(),
                               target.getSceneRasterHeight());
    ProductUtils.copyRasterDataNodeProperties(srcBand, targetBand);
    target.addBand(targetBand);
    return targetBand;
  }

  private static Map<String, RenderedImage> createSuperResolveGroupImages(Product source, S2DataFormat sourceFormat,
                                                                          Map<String, List<String>> bandGroups,
                                                                          Dimension tileSize, boolean debugMode) {
    Map<String, RenderedImage> images = new HashMap<>();
    for (Entry<String, List<String>> targetBandGroups : bandGroups.entrySet()) {
      images.put(targetBandGroups.getKey(),
                 S2SuperResolveImage.create(sourceFormat, source, targetBandGroups.getValue(),
                                            tileSize, debugMode));
    }
    return images;
  }


}
