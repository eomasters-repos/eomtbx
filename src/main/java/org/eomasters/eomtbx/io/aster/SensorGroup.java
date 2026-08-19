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

package org.eomasters.eomtbx.io.aster;

import static org.eomasters.utils.Exceptions.throwIf;

import java.awt.Dimension;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.dataio.netcdf.util.DataTypeUtils;
import ucar.nc2.Group;
import ucar.nc2.Variable;

class SensorGroup {

  private static final Map<String, String> BAND_NAME_MAP = new HashMap<>();
  private static final int NO_DATA_VALUE = 0;

  static {
    BAND_NAME_MAP.put("ImageData1", "VNIR_BAND_1");
    BAND_NAME_MAP.put("ImageData2", "VNIR_BAND_2");
    BAND_NAME_MAP.put("ImageData3N", "VNIR_BAND_3N");
    BAND_NAME_MAP.put("ImageData4", "SWIR_BAND_4");
    BAND_NAME_MAP.put("ImageData5", "SWIR_BAND_5");
    BAND_NAME_MAP.put("ImageData6", "SWIR_BAND_6");
    BAND_NAME_MAP.put("ImageData7", "SWIR_BAND_7");
    BAND_NAME_MAP.put("ImageData8", "SWIR_BAND_8");
    BAND_NAME_MAP.put("ImageData9", "SWIR_BAND_9");
    BAND_NAME_MAP.put("ImageData10", "TIR_BAND_10");
    BAND_NAME_MAP.put("ImageData11", "TIR_BAND_11");
    BAND_NAME_MAP.put("ImageData12", "TIR_BAND_12");
    BAND_NAME_MAP.put("ImageData13", "TIR_BAND_13");
    BAND_NAME_MAP.put("ImageData14", "TIR_BAND_14");
  }

  private final Group ncGroup;
  private final SENSOR sensor;

  public SensorGroup(SENSOR sensor, Group ncGroup) {
    throwIf(ncGroup == null, new IllegalStateException("ncGroup must not be null"));
    this.sensor = sensor;
    this.ncGroup = ncGroup;
  }

  public String getSensorName() {
    return sensor.name();
  }

  int getResolution() {
    return sensor.resolution;
  }

  public Dimension getRasterDimension() {
    List<ucar.nc2.Dimension> dataDimensions = ncGroup.findGroup(getSensorName() + "_Swath")
                                                     .findGroup("Data_Fields")
                                                     .getDimensions();
    throwIf(dataDimensions.size() != 2,
        new IllegalStateException(String.format("Dimension of %s should be 2D", getSensorName())));
    int width = getDimensionLength(dataDimensions, "ImagePixel");
    int height = getDimensionLength(dataDimensions, "ImageLine");
    return new Dimension(width, height);
  }

  private int getDimensionLength(List<ucar.nc2.Dimension> dimensions, String dimensionName) {
    return dimensions.stream()
                     .filter(dimension -> dimension.getShortName().equals(dimensionName))
                     .findFirst()
                     .map(ucar.nc2.Dimension::getLength)
                     .orElseThrow(() -> new IllegalArgumentException(dimensionName + " dimension not found"));
  }

  public List<TiePointGrid> getTiePointGrids() throws IOException {
    Dimension dimension = getRasterDimension();
    List<Variable> variables = ncGroup.findGroup(getSensorName() + "_Swath").findGroup("Geolocation_Fields").getVariables();
    List<TiePointGrid> tiePointGrids = new ArrayList<>();
    for (Variable variable : variables) {
      String shortName = variable.getShortName();
      int width = variable.getShape(NO_DATA_VALUE);
      int height = variable.getShape(1);
      double subSamplingX = (double) dimension.width / (width - 1);
      double subSamplingY = (double) dimension.height / (height - 1);
      TiePointGrid tpg = new TiePointGrid(shortName, width, height, 0.5, 0.5,
          subSamplingX, subSamplingY, getAsFloatArray(variable));
      tiePointGrids.add(tpg);
    }
    return tiePointGrids;
  }

  private static float[] getAsFloatArray(Variable variable) throws IOException {
    double[] doubles = (double[]) variable.read().copyTo1DJavaArray();
    float[] data = new float[doubles.length];
    for (int j = NO_DATA_VALUE; j < doubles.length; j++) {
      data[j] = (float) doubles[j];
    }
    return data;
  }

  public List<Map.Entry<Band, Variable>> addBands(AsterMetadata metadata, Product product) throws IOException {
    List<Variable> variables = ncGroup.findGroup(getSensorName() + "_Swath").findGroup("Data_Fields").getVariables();

    List<Entry<Band, Variable>> bands = createBands(variables, metadata);
    bands.sort(Comparator.comparingInt(o -> o.getKey().getSpectralBandIndex()));
    bands.stream().map(Entry::getKey).forEach(product::addBand);
    return bands;
  }

  @Nonnull
  private List<Entry<Band, Variable>> createBands(List<Variable> variables, AsterMetadata metadata) throws IOException {
    List<Entry<Band, Variable>> bands = new ArrayList<>();
    for (Variable variable : variables) {
      Dimension rasterDimension = getRasterDimension();
      Band band = new Band(BAND_NAME_MAP.get(variable.getShortName()), DataTypeUtils.getRasterDataType(variable),
          rasterDimension.width, rasterDimension.height);
      band.setDescription(String.format("%s band image at ground resolution of %dm", getSensorName(), getResolution()));
      band.setNoDataValue(NO_DATA_VALUE);
      band.setNoDataValueUsed(true);

      AsterMetadata.ConversionInfo convInfo = metadata.getConversionInfo(getSensorName(), band.getName());
      band.setScalingFactor(convInfo.scaling);
      band.setScalingOffset(convInfo.offset);
      band.setUnit(convInfo.unit);

      AsterMetadata.SpectralInfo spectralInfo = metadata.getSpectralInfo(band.getName());
      band.setSpectralBandIndex(spectralInfo.spectralIndex);
      band.setSpectralWavelength(spectralInfo.centralWvl);
      band.setSpectralBandwidth(spectralInfo.bandWidth);

      bands.add(new AbstractMap.SimpleEntry<>(band, variable));
    }
    return bands;
  }

  enum SENSOR {
   VNIR(15),  SWIR(30), TIR(90);

    public final int resolution;

    SENSOR(int resolution) {
      this.resolution = resolution;
    }
  }

}
