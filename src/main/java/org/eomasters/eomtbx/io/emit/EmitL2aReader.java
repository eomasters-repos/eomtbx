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

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;
import org.eomasters.eomtbx.io.emit.EmitL2aConstants.FlagInfo;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

public class EmitL2aReader extends AbstractEmitReader {

  private static final String FLAGS_BAND_NAME = "flags";
  private static final String AOD550_BAND_NAME = "aod550";
  private static final String H20_BAND_NAME = "h20";
  private static final String H2O_UNIT = "g/cm^2";
  private static final String AOD550_Description = "Total aerosol optical depth at 550 nm";
  private static final String FLAG_NOT_SPACECRAFT_EXPR = "!flags.Spacecraft";
  private static final String UNCERTAINTY_RELATION = "uncertainty";

  /**
   * Constructs a new abstract product reader.
   *
   * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader
   *                     implementations
   */
  protected EmitL2aReader(ProductReaderPlugIn readerPlugIn) {
    super(readerPlugIn);
  }

  @Override
  protected void customizeProduct(NetcdfFile reflFile, Product product) throws IOException {
    StringBuilder autoGroupPattern = new StringBuilder();
    Group sensorGroup = findGroup(reflFile, EmitL2aConstants.SENSOR_BAND_PARAMETERS_GROUP);
    Variable goodWavelengths = sensorGroup.findVariable("good_wavelengths");
    Array goodWvl = goodWavelengths.read();
    addSpectralBands(product, reflFile, EmitL2aConstants.REFLECTANCE_VARIABLE, goodWvl);
    autoGroupPattern.append(EmitL2aConstants.REFLECTANCE_VARIABLE);
    Path reflectancePath = Paths.get(reflFile.getLocation());
    Path maskPath = getSiblingFilePath(reflectancePath, "RFL", "MASK");
    if (Files.exists(maskPath)) {
      NetcdfFile maskFile = NetcdfFiles.open(maskPath.toString());
      Variable maskVariable = maskFile.findVariable(EmitL2aConstants.MASK_VARIABLE);
      if (maskVariable != null) {
        addFlagsAndMasks(product, maskVariable);
        Band aod550Band = addBand(AOD550_BAND_NAME, maskVariable, new int[]{0, 0, 5}, product);
        aod550Band.setDescription(AOD550_Description);
        aod550Band.setValidPixelExpression(FLAG_NOT_SPACECRAFT_EXPR);
        Band h2oBand = addBand(H20_BAND_NAME, maskVariable, new int[]{0, 0, 6}, product);
        h2oBand.setUnit(H2O_UNIT);
        h2oBand.setValidPixelExpression(FLAG_NOT_SPACECRAFT_EXPR);
      }
    }
    Path uncPath = getSiblingFilePath(reflectancePath, "RFL", "RFLUNCERT");
    if (Files.exists(maskPath)) {
      NetcdfFile uncFile = NetcdfFiles.open(uncPath.toString());
      addSpectralBands(product, uncFile, EmitL2aConstants.REFL_UNC_VARIABLE, goodWvl);
      Stream<String> reflBandNames = Arrays.stream(product.getBandNames())
                                           .filter(name -> name.matches(
                                               EmitL2aConstants.REFLECTANCE_VARIABLE + "_\\d{1,3}"));
      reflBandNames.map(product::getBand)
                   .forEach(band -> {
                     band.setAncillaryRelations(UNCERTAINTY_RELATION);
                     band.addAncillaryVariable(
                         product.getBand(EmitL2aConstants.REFL_UNC_VARIABLE + "_" + band.getSpectralBandIndex()),
                         UNCERTAINTY_RELATION);
                   });

      autoGroupPattern.append(String.format(":" + UNCERTAINTY_RELATION + "/%s*", EmitL2aConstants.REFL_UNC_VARIABLE));
    }
    product.setAutoGrouping(autoGroupPattern.toString());
  }

  private static void addFlagsAndMasks(Product product, Variable variable) {
    FlagCoding flags = new FlagCoding(FLAGS_BAND_NAME);
    for (int i = 0; i < EmitL2aConstants.MASK_FLAGS.length; i++) {
      FlagInfo maskFlag = EmitL2aConstants.MASK_FLAGS[i];
      if (maskFlag != null) {
        flags.addFlag(maskFlag.name, maskFlag.bitMask, maskFlag.description);
      }
    }
    product.getFlagCodingGroup().add(flags);

    Band flagBand = product.addBand(FLAGS_BAND_NAME, ProductData.TYPE_UINT8);
    flagBand.setSampleCoding(flags);
    EmitL2aFlagsOpImage flagsOpImage = new EmitL2aFlagsOpImage(variable, product.getSceneRasterSize(),
                                                               product.getPreferredTileSize());
    flagBand.setSourceImage(flagsOpImage);
    product.addMask("cloud_mask", "flags.Cloud",
                    "Probability this pixel is cloud", Color.ORANGE, 0.5);
    product.addMask("cirrus_mask", "flags.Cirrus",
                    "Probability this pixel is cirrus", Color.YELLOW, 0.5);
    product.addMask("water_mask", "flags.Water",
                    "Probability this pixel is standing water", Color.BLUE, 0.5);
    product.addMask("spacecraft_mask", "flags.Spacecraft",
                    "Indicating spacecraft issue", Color.MAGENTA, 0.5);
    product.addMask("dilated_cloud_mask", "flags.Dilated_cloud",
                    "Dilated cloud mask", Color.WHITE, 0.5);
    product.addMask("aggregated_mask", "flags.Aggregated",
                    "Aggregate bad data mask", Color.RED, 0.5);
  }

}
