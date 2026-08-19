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

package org.eomasters.eomtbx.io.emit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import ucar.ma2.Array;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

public class EmitL1bReader extends AbstractEmitReader {

  private static final String OBS_TIME_BANDNAME = "obs_time";

  /**
   * Constructs a new abstract product reader.
   *
   * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader
   *                     implementations
   */
  protected EmitL1bReader(ProductReaderPlugIn readerPlugIn) {
    super(readerPlugIn);
  }

  protected void customizeProduct(NetcdfFile mainFile, Product product) throws IOException {
    Group sensorGroup = findGroup(mainFile, EmitL2aConstants.SENSOR_BAND_PARAMETERS_GROUP);
    Variable wavelengths = sensorGroup.findVariable("wavelengths");
    Array wvlArray = wavelengths.read();
    addSpectralBands(product, mainFile, EmitL1bConstants.RADIANCE_VARIABLE, wvlArray);
    StringBuilder autoGroupingPattern = new StringBuilder();
    autoGroupingPattern.append(EmitL1bConstants.RADIANCE_VARIABLE);
    Path radiancePath = Paths.get(mainFile.getLocation());
    Path observationPath = getSiblingFilePath(radiancePath, "RAD", "OBS");
    if (Files.exists(observationPath)) {
      NetcdfFile observationFile = NetcdfFiles.open(observationPath.toString());
      addObservationBands(product, observationFile);
      autoGroupingPattern.append(":obs/obs_*");
      product.setSceneTimeCoding(new EmitL1bTimeCoding(product.getBand(OBS_TIME_BANDNAME)));
      product.setPointingFactory(new EmitL1bPointingFactory(product));
    }

    product.setAutoGrouping(autoGroupingPattern.toString());
  }

  private void addObservationBands(Product product, NetcdfFile obsFile) throws IOException {
    Variable obsVariable = obsFile.findVariable(EmitL1bConstants.OBS_VARIABLE);
    Group bandParameters = findGroup(obsFile, EmitConstants.SENSOR_BAND_PARAMETERS_GROUP);
    Variable bandDescrVariable = bandParameters.findVariable(EmitL1bConstants.OBSERVATION_BANDS_VARIABLE);
    Array descriptions = bandDescrVariable.read();
    for (int i = 0; i < EmitL1bConstants.OBSERVATION_BAND_NAMES.length; i++) {
      Band band = addBand(EmitL1bConstants.OBSERVATION_BAND_NAMES[i], obsVariable, new int[]{0, 0, i},
                          product);
      band.setDescription((String) descriptions.getObject(i));
    }
  }

}
