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

package org.eomasters.eomtbx.s2geom;

import java.awt.image.RenderedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Mask;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductNodeGroup;

public abstract class S2DataFormat {

  private final Product product;

  public S2DataFormat(Product product) {
    this.product = product;
  }

  public Product getProduct() {
    return product;
  }

  // TODO - Landsat from Col2 could be corrected too but there are no easy to obtain geometric information
// These can be taken from a L1C (but only for band 4) or computed from the ANG.txt see
// -> https://qiuhongyuan.medium.com/how-to-get-view-azimuth-angle-vaa-for-landsat-8-level-2-b20b213001d3
  // implementation in C2RCC is not applicable
  // impl: https://www.usgs.gov/landsat-missions/solar-illumination-and-sensor-viewing-angle-coefficient-files

  public static S2DataFormat getSupportedFormat(Product product) throws Exception {
    List<S2DataFormat> knownFormats = List.of(new StdS2L1CFormat(product),
        new StdS2L2AFormat(product)
        // currently not supported due to issue https://gitlab.orfeo-toolbox.org/maja/maja/-/issues/370
        // new TheiaS2L2AFormat(product)
    );
    for (S2DataFormat format : knownFormats) {
      if (format.isProductSupported()) {
        return format;
      }
    }
    String formatNames = getSupportedFormatNames(knownFormats);
    throw new Exception(
        java.lang.String.format("Product '%s' not supported. Must be one of: %n%s", product.getName(), formatNames));
  }


  public final S2GeometryAngles getGeometryAngles() {
    return new S2GeometryAngles(getSunAngleData(), getViewAngles());
  }

  public final S2GeometryImages getS2GeometryImages() {
    return new S2GeometryImages(getGeometryAngles(), getDetectorMaskImages(),
        getDetectorImages(), getIndexBandResolutionMap());
  }

  public final Map<Integer, Map<Integer, RenderedImage>> getDetectorMaskImages() {
    HashMap<Integer, Map<Integer, RenderedImage>> map = new HashMap<>();
    ProductNodeGroup<Mask> masks = getProduct().getMaskGroup();
    for (Entry<String, Integer> entry : getNameBandIndexMap().entrySet()) {
      Integer bandIndex = entry.getValue();
      String paddedName = S2FormatHelper.S2_INDEX_NAME_MAP.get(bandIndex);
      if (paddedName.length() == 2) {
        paddedName = new String(ArrayUtils.insert(1, paddedName.toCharArray(), '0'));
      }
      for (int detectorIndex = 1; detectorIndex <= 12; detectorIndex++) {
        String maskName = String.format("detector_footprint-%s-%02d", paddedName, detectorIndex);
        Mask mask = masks.get(maskName);
        if (mask != null) {
          Map<Integer, RenderedImage> detectorImages = map.getOrDefault(bandIndex, new HashMap<>());
          detectorImages.put(detectorIndex, mask.getSourceImage());
          map.put(bandIndex, detectorImages);
        }
      }
    }
    return map;
  }

  public final int getResolutionOfBand(String bandName) {
    return getIndexBandResolutionMap().get(getNameBandIndexMap().get(bandName));
  }

  public abstract Map<String, String[]> getSpectralGroups();

  public abstract Map<Integer, String[]> getBandNamesPerResolution();

  public abstract Map<Integer, RenderedImage> getDetectorImages();

  public abstract String getName();

  public abstract boolean isProductSupported();

  public abstract Map<String, Integer> getNameBandIndexMap();

  public abstract Map<Integer, Integer> getIndexBandResolutionMap();

  public abstract SunAngleData getSunAngleData();

  public abstract ViewAngleData getViewAngles();

  public abstract RenderedImage harmonizeSourceImage(Band band);

  public abstract RenderedImage harmonizeTargetImage(Band band, RenderedImage nbarImage);

  public abstract String getCommonSpectralBandName(String formatBandName);

  public abstract boolean isCommonSpectralBandName(String formatBandName);

  private static String getSupportedFormatNames(List<S2DataFormat> knownFormats) {
    List<String> names = knownFormats.stream().map(S2DataFormat::getName).collect(Collectors.toList());
    return java.lang.String.join(", ", names);
  }

}
