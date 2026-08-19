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

import static org.eomasters.eomtbx.s2geom.S2FormatHelper.S2_NAME_INDEX_MAP;
import static org.eomasters.utils.Exceptions.throwIf;

import java.awt.image.RenderedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;

public class TheiaS2L2AFormat extends S2DataFormat {

  public static final String NAME = "Theia_S2_MSI_Level-2A";
  public static final Map<String, Integer> BAND_NAME_INDEX_MAP;
  public static final Map<Integer, String[]> RES_BAND_NAMES_MAP;
  private static final Map<Integer, Integer> BAND_INDEX_RESOLUTION_MAP;

  static {
    HashMap<String, Integer> nameMap = new LinkedHashMap<>();
    nameMap.put("Surface_Reflectance_B2", 1);
    nameMap.put("Surface_Reflectance_B3", 2);
    nameMap.put("Surface_Reflectance_B4", 3);
    nameMap.put("Surface_Reflectance_B5", 4);
    nameMap.put("Surface_Reflectance_B6", 5);
    nameMap.put("Surface_Reflectance_B7", 6);
    nameMap.put("Surface_Reflectance_B8", 7);
    nameMap.put("Surface_Reflectance_B8A", 8);
    nameMap.put("Surface_Reflectance_B11", 11);
    nameMap.put("Surface_Reflectance_B12", 12);
    nameMap.put("Flat_Reflectance_B2", 1);
    nameMap.put("Flat_Reflectance_B3", 2);
    nameMap.put("Flat_Reflectance_B4", 3);
    nameMap.put("Flat_Reflectance_B5", 4);
    nameMap.put("Flat_Reflectance_B6", 5);
    nameMap.put("Flat_Reflectance_B7", 6);
    nameMap.put("Flat_Reflectance_B8", 7);
    nameMap.put("Flat_Reflectance_B8A", 8);
    nameMap.put("Flat_Reflectance_B11", 11);
    nameMap.put("Flat_Reflectance_B12", 12);
    BAND_NAME_INDEX_MAP = Collections.unmodifiableMap(nameMap);

    HashMap<Integer, String[]> resMap = new LinkedHashMap<>();
    resMap.put(10,
               new String[]{"Surface_Reflectance_B2", "Surface_Reflectance_B3", "Surface_Reflectance_B4",
                   "Surface_Reflectance_B8", "Flat_Reflectance_B2", "Flat_Reflectance_B3", "Flat_Reflectance_B4",
                   "Flat_Reflectance_B8",});
    resMap.put(20,
               new String[]{"Surface_Reflectance_B5", "Surface_Reflectance_B6", "Surface_Reflectance_B7",
                   "Surface_Reflectance_B8A", "Surface_Reflectance_B11", "Surface_Reflectance_B12",
                   "Flat_Reflectance_B5", "Flat_Reflectance_B6", "Flat_Reflectance_B7", "Flat_Reflectance_B8A",
                   "Flat_Reflectance_B11", "Flat_Reflectance_B12"});
    RES_BAND_NAMES_MAP = Collections.unmodifiableMap(resMap);

    Map<Integer, Integer> indexResMap = new HashMap<>();
    indexResMap.put(1, 10);
    indexResMap.put(2, 10);
    indexResMap.put(3, 10);
    indexResMap.put(4, 20);
    indexResMap.put(5, 20);
    indexResMap.put(6, 20);
    indexResMap.put(7, 10);
    indexResMap.put(8, 20);
    indexResMap.put(11, 20);
    indexResMap.put(12, 20);
    BAND_INDEX_RESOLUTION_MAP = Collections.unmodifiableMap(indexResMap);
  }

  public TheiaS2L2AFormat(Product product) {
    super(product);
  }

  @Override
  public Map<String, String[]> getSpectralGroups() {
    return Map.of(
        "Surface_Reflectance",
        new String[]{"Surface_Reflectance_B2", "Surface_Reflectance_B3", "Surface_Reflectance_B4",
            "Surface_Reflectance_B5", "Surface_Reflectance_B6", "Surface_Reflectance_B7", "Surface_Reflectance_B8",
            "Surface_Reflectance_B8A", "Surface_Reflectance_B11", "Surface_Reflectance_B12"},
        "Flat_Reflectance",
        new String[]{"Flat_Reflectance_B2", "Flat_Reflectance_B3", "Flat_Reflectance_B4",
            "Flat_Reflectance_B5", "Flat_Reflectance_B6", "Flat_Reflectance_B7", "Flat_Reflectance_B8",
            "Flat_Reflectance_B8A", "Flat_Reflectance_B11", "Flat_Reflectance_B12"});
  }

  @Override
  public Map<Integer, RenderedImage> getDetectorImages() {
    return Map.of();
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getCommonSpectralBandName(String formatSpecificName) {
    if (!isCommonSpectralBandName(formatSpecificName)) {
      throw new IllegalArgumentException("Unknown band name: " + formatSpecificName);
    }
    return getCommonName(formatSpecificName);
  }

  @Override
  public boolean isCommonSpectralBandName(String formatSpecificName) {
    return S2FormatHelper.S2_NAME_INDEX_MAP.containsKey(getCommonName(formatSpecificName));
  }

  private static String getCommonName(String formatSpecificName) {
    if (BAND_NAME_INDEX_MAP.containsKey(formatSpecificName)) {
      return formatSpecificName.substring(formatSpecificName.lastIndexOf("_") + 1);
    } else {
      return formatSpecificName;
    }
  }

  @Override
  public boolean isProductSupported() {
    if (!getProduct().isMultiSize()) {
      return false;
    }
    // cannot check type, need to check metadata
    MetadataElement root = getProduct().getMetadataRoot();
    MetadataElement metadata = root.getElement("Metadata");
    if (metadata == null) {
      return false;
    }
    MetadataElement datasetIdentification = metadata.getElement("Dataset_Identification");
    if (datasetIdentification == null) {
      return false;
    }
    MetadataAttribute identifierAttrib = datasetIdentification.getAttribute("IDENTIFIER");
    if (identifierAttrib == null) {
      return false;
    }
    String identifier = identifierAttrib.getData().getElemString();

    return identifier.contains("SENTINEL2") || identifier.contains("L2A");
  }

  @Override
  public Map<String, Integer> getNameBandIndexMap() {
    return BAND_NAME_INDEX_MAP;
  }

  @Override
  public Map<Integer, String[]> getBandNamesPerResolution() {
    return RES_BAND_NAMES_MAP;
  }

  @Override
  public Map<Integer, Integer> getIndexBandResolutionMap() {
    return BAND_INDEX_RESOLUTION_MAP;
  }

  @Override
  public SunAngleData getSunAngleData() {
    MetadataElement anglesGridList = getAnglesGridList(getProduct());
    throwIf(anglesGridList == null,
            new IllegalStateException("Expected that product has 'Angles_Grids_List' metadata element"));
    MetadataElement sunAnglesGrids = anglesGridList.getElement("Sun_Angles_Grids");
    return S2FormatHelper.createSunAngleData(sunAnglesGrids);
  }

  @Override
  public ViewAngleData getViewAngles() {
    MetadataElement anglesGridList = getAnglesGridList(getProduct());
    throwIf(anglesGridList == null,
            new IllegalStateException("Expected that product has 'Angles_Grids_List' metadata element"));
    MetadataElement anglesGridsList = anglesGridList.getElement("Viewing_Incidence_Angles_Grids_List");
    Map<Integer, Map<Integer, double[][]>> bandDetectorViewZenithGrids = new HashMap<>();
    Map<Integer, Map<Integer, double[][]>> bandDetectorViewAzimuthGrids = new HashMap<>();
    for (MetadataElement bandDetectorAngleGrids : anglesGridsList.getElements()) {
      String bandName = bandDetectorAngleGrids.getAttributeString("band_id");
      int bandId = S2_NAME_INDEX_MAP.get(bandName);
      MetadataElement[] detectorAngleGrids = bandDetectorAngleGrids.getElements();
      for (MetadataElement detectorAngleGrid : detectorAngleGrids) {
        int detectorId = detectorAngleGrid.getAttributeInt("detector_id");
        MetadataElement zenith = detectorAngleGrid.getElement("Zenith");
        MetadataElement azimuth = detectorAngleGrid.getElement("Azimuth");

        S2FormatHelper.initViewGridsMap(bandId, detectorId, zenith, bandDetectorViewZenithGrids);
        S2FormatHelper.initViewGridsMap(bandId, detectorId, azimuth, bandDetectorViewAzimuthGrids);
      }
    }
    return new ViewAngleData(bandDetectorViewZenithGrids, bandDetectorViewAzimuthGrids);
  }

  private static MetadataElement getAnglesGridList(Product product) {
    MetadataElement metadataRoot = product.getMetadataRoot();
    MetadataElement metadataElement = metadataRoot.getElement("Metadata");
    if (metadataElement != null) {
      MetadataElement geometricInformations = metadataElement.getElement("Geometric_Informations");
      if (geometricInformations != null) {
        return geometricInformations.getElement("Angles_Grids_List");
      }
    }
    return null;
  }

  @Override
  public RenderedImage harmonizeSourceImage(Band band) {
    return band.getSourceImage();
  }

  @Override
  public RenderedImage harmonizeTargetImage(Band band, RenderedImage nbarImage) {
    return nbarImage;
  }

}
