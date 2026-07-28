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
import java.util.Map;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;

public class StdS2L1CFormat extends StdS2Format {

  public static final Map<String, Integer> BAND_NAME_INDEX_MAP = S2FormatHelper.S2_NAME_INDEX_MAP;
  public static final Map<Integer, String[]> RES_BAND_NAMES_MAP;
  private static final Map<Integer, Integer> BAND_INDEX_RESOLUTION_MAP;


  static {
    Map<Integer, String[]> resMap = new HashMap<>();
    resMap.put(10, new String[]{"B2", "B3", "B4", "B8"});
    resMap.put(20, new String[]{"B5", "B6", "B7", "B8A", "B11", "B12"});
    resMap.put(60, new String[]{"B1", "B9", "B10"});
    RES_BAND_NAMES_MAP = Collections.unmodifiableMap(resMap);

    Map<Integer, Integer> indexResMap = new HashMap<>();
    indexResMap.put(0, 60);
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
    BAND_INDEX_RESOLUTION_MAP = Collections.unmodifiableMap(indexResMap);

  }


  static final String S2_L1C_PRODUCT_TYPE = "S2_MSI_Level-1C";

  public StdS2L1CFormat(Product product) {
    super(product);
  }

  @Override
  public Map<String, String[]> getSpectralGroups() {
    return Map.of("reflectance", S2FormatHelper.S2_NAME_INDEX_MAP.keySet().toArray(new String[0]));
  }

  @Override
  public String getName() {
    return "Standard_" + S2_L1C_PRODUCT_TYPE;
  }


  @Override
  public boolean isProductSupported() {
    if (!getProduct().isMultiSize()) {
      return false;
    }
    MetadataElement root = getProduct().getMetadataRoot();
    MetadataElement dataStrip = root.getElement("Level-1C_DataStrip_ID");
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
    return BAND_INDEX_RESOLUTION_MAP;
  }

  @Override
  public Map<Integer, String[]> getBandNamesPerResolution() {
    return RES_BAND_NAMES_MAP;
  }


  protected boolean isAfterProcessingBaseline4() {
    MetadataElement root = getProduct().getMetadataRoot();
    MetadataElement dataStrip = root.getElement("Level-1C_DataStrip_ID");
    MetadataElement generalInfo = dataStrip.getElement("General_Info");
    MetadataElement processingInfo = generalInfo.getElement("Processing_Info");
    String processingBaseline = processingInfo.getAttributeString("PROCESSING_BASELINE");
    return Double.parseDouble(processingBaseline) >= 4.0;
  }

}
