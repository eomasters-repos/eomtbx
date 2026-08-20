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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;

public class S2FormatHelper {

  public static final Map<String, Integer> S2_NAME_INDEX_MAP = new LinkedHashMap<>();
  public static final Map<Integer, String> S2_INDEX_NAME_MAP = new LinkedHashMap<>();

  static {
    S2_NAME_INDEX_MAP.put("B1", 0);
    S2_NAME_INDEX_MAP.put("B2", 1);
    S2_NAME_INDEX_MAP.put("B3", 2);
    S2_NAME_INDEX_MAP.put("B4", 3);
    S2_NAME_INDEX_MAP.put("B5", 4);
    S2_NAME_INDEX_MAP.put("B6", 5);
    S2_NAME_INDEX_MAP.put("B7", 6);
    S2_NAME_INDEX_MAP.put("B8", 7);
    S2_NAME_INDEX_MAP.put("B8A", 8);
    S2_NAME_INDEX_MAP.put("B9", 9);
    S2_NAME_INDEX_MAP.put("B10", 10);
    S2_NAME_INDEX_MAP.put("B11", 11);
    S2_NAME_INDEX_MAP.put("B12", 12);

    S2_INDEX_NAME_MAP.put(0, "B1");
    S2_INDEX_NAME_MAP.put(1, "B2");
    S2_INDEX_NAME_MAP.put(2, "B3");
    S2_INDEX_NAME_MAP.put(3, "B4");
    S2_INDEX_NAME_MAP.put(4, "B5");
    S2_INDEX_NAME_MAP.put(5, "B6");
    S2_INDEX_NAME_MAP.put(6, "B7");
    S2_INDEX_NAME_MAP.put(7, "B8");
    S2_INDEX_NAME_MAP.put(8, "B8A");
    S2_INDEX_NAME_MAP.put(9, "B9");
    S2_INDEX_NAME_MAP.put(10, "B10");
    S2_INDEX_NAME_MAP.put(11, "B11");
    S2_INDEX_NAME_MAP.put(12, "B12");
  }

  static SunAngleData createSunAngleData(MetadataElement sunAnglesGridElement) {
    double[][] zenithsArrays = getAngleData(sunAnglesGridElement.getElement("Zenith"));
    double[][] azimuthsArrays = getAngleData(sunAnglesGridElement.getElement("Azimuth"));
    return new SunAngleData(zenithsArrays, azimuthsArrays);
  }

  static void initViewGridsMap(int bandId, int detectorId,
      MetadataElement angleElement, Map<Integer, Map<Integer, double[][]>> map) {
    double[][] zenithData = getAngleData(angleElement);
    Map<Integer, double[][]> bandDetectorAngleMap = map.getOrDefault(bandId, new HashMap<>());
    bandDetectorAngleMap.put(detectorId, zenithData);
    map.put(bandId, bandDetectorAngleMap);
  }

  static double[][] getAngleData(MetadataElement angleElement) {
    MetadataElement valuesList = angleElement.getElement("Values_List");
    int numAttributes = valuesList.getNumAttributes();
    double[][] angleData = new double[numAttributes][];
    for (int i = 0; i < numAttributes; i++) {
      MetadataAttribute values = valuesList.getAttributeAt(i);
      String valuesAsString = values.getData().getElemString();
      String[] stringValues = valuesAsString.split(" ");
      angleData[i] = new double[stringValues.length];
      for (int j = 0; j < stringValues.length; j++) {
        angleData[i][j] = Double.parseDouble(stringValues[j]);
      }
    }
    return angleData;
  }
}
