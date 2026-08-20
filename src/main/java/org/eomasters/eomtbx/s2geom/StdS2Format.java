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

import static org.eomasters.eomtbx.s2geom.S2FormatHelper.S2_INDEX_NAME_MAP;
import static org.eomasters.utils.Exceptions.throwIf;

import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.jai.operator.AddConstDescriptor;
import javax.media.jai.operator.SubtractConstDescriptor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;

public abstract class StdS2Format extends S2DataFormat {

  public StdS2Format(Product product) {
    super(product);
  }

  private static double getRawOffset(Band band) {
    return band.getScalingOffset() / band.getScalingFactor();
  }

  private static MetadataElement getTileAngles(Product product) {
    MetadataElement metadataRoot = product.getMetadataRoot();
    MetadataElement granules = metadataRoot.getElement("Granules");
    if (granules == null) {
      return null;
    }
    MetadataElement tileElement = granules.getElementAt(
        0); // element name is not constant - contains the level and TileId
    if (tileElement == null) {
      return null;
    }
    MetadataElement geometricInfo = tileElement.getElement("Geometric_Info");
    if (geometricInfo == null) {
      return null;
    }
    return geometricInfo.getElement("Tile_Angles");
  }

  @Override
  public abstract String getName();

  @Override
  public abstract Map<String, Integer> getNameBandIndexMap();

  protected abstract boolean isAfterProcessingBaseline4();

  @Override
  public Map<Integer, RenderedImage> getDetectorImages() {
    HashMap<Integer, RenderedImage> detectorImages = new HashMap<>();
    for (Entry<Integer, String> entry : S2_INDEX_NAME_MAP.entrySet()) {
      String bandName = String.format("B_detector_footprint_%s", entry.getValue());
      Band band = getProduct().getBand(bandName);
      if (band != null) {
        detectorImages.put(entry.getKey(), band.getSourceImage());
      }
    }
    return detectorImages;
  }

  @Override
  public final SunAngleData getSunAngleData() {
    MetadataElement tileAngles = StdS2Format.getTileAngles(getProduct());
    throwIf(tileAngles == null, new IllegalStateException("Expected that product has 'Tile_Angles' metadata element."));
    MetadataElement sunAnglesGrid = tileAngles.getElement("Sun_Angles_Grid");
    double[][] zenithsArrays = S2FormatHelper.getAngleData(sunAnglesGrid.getElement("Zenith"));
    double[][] azimuthsArrays = S2FormatHelper.getAngleData(sunAnglesGrid.getElement("Azimuth"));
    return new SunAngleData(zenithsArrays, azimuthsArrays);
  }

  @Override
  public final ViewAngleData getViewAngles() {
    MetadataElement tileAngles = StdS2Format.getTileAngles(getProduct());
    throwIf(tileAngles == null, new IllegalStateException("Expected that product has 'Tile_Angles' metadata element."));
    MetadataElement[] subElements = tileAngles.getElements();
    List<MetadataElement> viewingAnglesGrids = new ArrayList<>();
    for (MetadataElement subElement : subElements) {
      if (subElement.getName().equals("Viewing_Incidence_Angles_Grids")) {
        viewingAnglesGrids.add(subElement);
      }
    }
    Map<Integer, Map<Integer, double[][]>> bandDetectorViewZenithGrids = new HashMap<>();
    Map<Integer, Map<Integer, double[][]>> bandDetectorViewAzimuthGrids = new HashMap<>();
    for (MetadataElement viewingAnglesGrid : viewingAnglesGrids) {
      int bandId = viewingAnglesGrid.getAttributeInt("bandId");
      int detectorId = viewingAnglesGrid.getAttributeInt("detectorId");
      MetadataElement zenith = viewingAnglesGrid.getElement("Zenith");
      MetadataElement azimuth = viewingAnglesGrid.getElement("Azimuth");

      S2FormatHelper.initViewGridsMap(bandId, detectorId, zenith, bandDetectorViewZenithGrids);
      S2FormatHelper.initViewGridsMap(bandId, detectorId, azimuth, bandDetectorViewAzimuthGrids);
    }
    return new ViewAngleData(bandDetectorViewZenithGrids, bandDetectorViewAzimuthGrids);
  }

  /**
   * Harmonizes the source image of a given band.
   * <p>
   * If the processing baseline of the data is >=4.0 then the data needs to be harmonized with data from previous PBs:
   * harm_DN = DN - 1000
   *
   * @param band The band object containing the source image to be harmonized.
   * @return The harmonized source image.
   */
  @Override
  public final RenderedImage harmonizeSourceImage(Band band) {
    if (isAfterProcessingBaseline4()) {
      double rawOffset = StdS2Format.getRawOffset(band);
      return AddConstDescriptor.create(band.getSourceImage(), new double[]{rawOffset}, null);
    }
    return band.getSourceImage();
  }

  /**
   * Harmonizes the target image of a given band.
   * <p>
   * If the processing baseline of the data is >= 4.0, the target image needs to be harmonized with data from previous
   * processing baselines.
   *
   * @param band      The band object to access the necessary metadata.
   * @param nbarImage The input image to be processed.
   * @return The harmonized target image.
   */
  @Override
  public final RenderedImage harmonizeTargetImage(Band band, RenderedImage nbarImage) {
    if (isAfterProcessingBaseline4()) {
      double rawOffset = StdS2Format.getRawOffset(band);
      return SubtractConstDescriptor.create(nbarImage, new double[]{rawOffset}, null);
    }
    return nbarImage;
  }

  @Override
  public String getCommonSpectralBandName(String formatBandName) {
    if (!S2FormatHelper.S2_NAME_INDEX_MAP.containsKey(formatBandName)) {
      throw new IllegalArgumentException("Unknown band name: " + formatBandName);
    }
    return formatBandName;
  }

  @Override
  public boolean isCommonSpectralBandName(String bandName) {
      return S2FormatHelper.S2_NAME_INDEX_MAP.containsKey(bandName);
  }

}
