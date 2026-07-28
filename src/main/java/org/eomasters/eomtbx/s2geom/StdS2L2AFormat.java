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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;

public class StdS2L2AFormat extends StdS2Format {

  public static final Map<String, Integer> BAND_NAME_INDEX_MAP = new LinkedHashMap<>();
  public static final Map<Integer, String[]> RES_BAND_NAMES_MAP = new LinkedHashMap<>();

  static {
    RES_BAND_NAMES_MAP.put(10, new String[]{"B2", "B3", "B4", "B8"});
    RES_BAND_NAMES_MAP.put(20, new String[]{"B1", "B5", "B6", "B7", "B8A", "B11", "B12"});
    RES_BAND_NAMES_MAP.put(60, new String[]{"B9"});

    BAND_NAME_INDEX_MAP.put("B1", 0);
    BAND_NAME_INDEX_MAP.put("B2", 1);
    BAND_NAME_INDEX_MAP.put("B3", 2);
    BAND_NAME_INDEX_MAP.put("B4", 3);
    BAND_NAME_INDEX_MAP.put("B5", 4);
    BAND_NAME_INDEX_MAP.put("B6", 5);
    BAND_NAME_INDEX_MAP.put("B7", 6);
    BAND_NAME_INDEX_MAP.put("B8", 7);
    BAND_NAME_INDEX_MAP.put("B8A", 8);
    BAND_NAME_INDEX_MAP.put("B9", 9);
    BAND_NAME_INDEX_MAP.put("B11", 11);
    BAND_NAME_INDEX_MAP.put("B12", 12);
  }

  static final String S2_L2A_PRODUCT_TYPE = "S2_MSI_Level-2A";

  public StdS2L2AFormat(Product product) {
    super(product);
  }

  @Override
  public Map<String, String[]> getSpectralGroups() {
    return Map.of("surface_reflectance", BAND_NAME_INDEX_MAP.keySet().toArray(new String[0]));
  }

  @Override
  public Map<Integer, String[]> getBandNamesPerResolution() {
    return RES_BAND_NAMES_MAP;
  }

  @Override
  public String getName() {
    return "Standard_" + S2_L2A_PRODUCT_TYPE;
  }


  @Override
  public boolean isProductSupported() {
    if (!getProduct().isMultiSize()) {
      return false;
    }
    MetadataElement root = getProduct().getMetadataRoot();
    MetadataElement dataStrip = root.getElement("Level-2A_DataStrip_ID");
    if (dataStrip == null) {
      return false;
    }
    MetadataElement generalInfo = dataStrip.getElement("General_Info");
    if (generalInfo == null) {
      return false;
    }
    MetadataElement processingInfo = generalInfo.getElement("Processing_Info");
    if (processingInfo == null) {
      return false;
    }
    MetadataAttribute processingBaseline = processingInfo.getAttribute("PROCESSING_BASELINE");
    if (processingBaseline == null) {
      return false;
    }

    return true;
  }

  @Override
  public Map<String, Integer> getNameBandIndexMap() {
    return BAND_NAME_INDEX_MAP;
  }

  @Override
  public Map<Integer, Integer> getIndexBandResolutionMap() {
    Map<Integer, Integer> indexResMap = new HashMap<>();
    indexResMap.put(0, isAfterProcessingBaseline4() ? 20 : 60);
    indexResMap.put(1, 10);
    indexResMap.put(2, 10);
    indexResMap.put(3, 10);
    indexResMap.put(4, 20);
    indexResMap.put(5, 20);
    indexResMap.put(6, 20);
    indexResMap.put(7, 10);
    indexResMap.put(8, 20);
    indexResMap.put(9, 60);
    indexResMap.put(10, 60);
    indexResMap.put(11, 20);
    indexResMap.put(12, 20);
    return Collections.unmodifiableMap(indexResMap);
  }

  @Override
  protected boolean isAfterProcessingBaseline4() {
    MetadataElement root = getProduct().getMetadataRoot();
    MetadataElement dataStrip = root.getElement("Level-2A_DataStrip_ID");
    MetadataElement generalInfo = dataStrip.getElement("General_Info");
    MetadataElement processingInfo = generalInfo.getElement("Processing_Info");
    String processingBaseline = processingInfo.getAttributeString("PROCESSING_BASELINE");
    return Double.parseDouble(processingBaseline) >= 4.0;
  }

}
