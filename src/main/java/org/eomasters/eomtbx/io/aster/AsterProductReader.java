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

import com.bc.ceres.core.ProgressMonitor;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.ImageInputStream;
import org.eomasters.eomtbx.EomtbxRuntime;
import org.eomasters.eomtbx.io.aster.SensorGroup.SENSOR;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.geocoding.ComponentFactory;
import org.esa.snap.core.dataio.geocoding.ComponentGeoCoding;
import org.esa.snap.core.dataio.geocoding.GeoRaster;
import org.esa.snap.core.dataio.geocoding.forward.TiePointBilinearForward;
import org.esa.snap.core.dataio.geocoding.inverse.TiePointInverse;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.ProductData.UTC;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class AsterProductReader extends AbstractProductReader {

  private static final String START_TIME_PATTERN = "MMddyyyyHHmmss";

  private static final String LATITUDE_GRID_NAME = "Latitude";
  private static final String LONGITUDE_GRID_NAME = "Longitude";
  private final AsterProductReaderPlugIn asterPlugIn;


  /**
   * Constructs a new product reader for ASTER data.
   *
   * <p>
   * Following the <a href="https://lpdaac.usgs.gov/documents/300/ASTER_L1T_Product_Specification.pdf">ASTER L1T Product
   * Specification</a>
   *
   * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader
   *                     implementations
   */
  protected AsterProductReader(AsterProductReaderPlugIn readerPlugIn) {
    super(readerPlugIn);
    this.asterPlugIn = readerPlugIn;
  }

  @Override
  protected Product readProductNodesImpl() throws IOException {
    throwIf(asterPlugIn.getDecodeQualification(getInput()) != DecodeQualification.INTENDED,
        new IOException(String.format("Unsupported input: %s", getInput())));

    Path path = AsterProductReaderPlugIn.getAsPath(getInput());
    ImageInputStream imageInputStream = new FileCacheImageInputStream(Files.newInputStream(path), null);
    NetcdfFile ncFile = NetcdfFileOpener.open(imageInputStream);

    List<SensorGroup> sensorGroups = getSensorGroups(ncFile);
    Dimension sceneDimension = getSceneDimension(sensorGroups);

    String filename = path.getFileName().toString();
    String productName = filename.substring(0, filename.indexOf('.'));
    Product product = new Product(productName, AsterProductReaderPlugIn.FORMAT_NAME,
        sceneDimension.width, sceneDimension.height);
    product.setDescription("ASTER Level 1 Precision Terrain Corrected Registered At-Sensor Radiance");
    AsterMetadata metadata = new AsterMetadata(ncFile);
    setObservationTime(product, getObservationTime(productName, metadata));
    addSensorDataToProduct(sensorGroups, metadata, product);

    metadata.addToProduct(product);
    return product;
  }

  private static void setObservationTime(Product product, UTC observationTime) {
    product.setStartTime(observationTime);
    product.setEndTime(observationTime);
  }

  private static UTC getObservationTime(String productName, AsterMetadata metadata) {
    // The date-time starts after 11 chars, "AST_L1T_" and the version "003|031", which is 14 characters
    int beginIndex = "AST_L1T_003".length();
    String dateTimeStr = productName.substring(beginIndex, beginIndex + START_TIME_PATTERN.length());
    try {
      return UTC.parse(dateTimeStr, START_TIME_PATTERN);
    } catch (ParseException e) {
      EomtbxRuntime.LOGGER.warning("Could not extract scene observation time from product name: " + productName + ".");
      UTC observationTime = null;
      try {
        observationTime = metadata.getObservationTime();
      } catch (Exception ex) {
        EomtbxRuntime.LOGGER.warning(
            "Error occurred when trying to extract scene observation time from product metadata. " + e.getMessage());
      }
      if (observationTime == null) {
        EomtbxRuntime.LOGGER.warning("Could not extract scene observation time from product metadata.");
        EomtbxRuntime.LOGGER.warning("Product will have no scene stat and end time.");
      }
      return observationTime;
    }
  }

  private void addSensorDataToProduct(List<SensorGroup> sensorGroups, AsterMetadata metadata, Product product)
      throws IOException {
    throwIf(sensorGroups.isEmpty(), new IllegalStateException("No sensor data available."));
    addGeolocationGrids(sensorGroups.get(0), product);
    TiePointGrid latGrid = product.getTiePointGrid(LATITUDE_GRID_NAME);
    TiePointGrid lonGrid = product.getTiePointGrid(LONGITUDE_GRID_NAME);
    SensorGroup highestResGroup = sensorGroups.stream()
                                              .min(Comparator.comparingInt(SensorGroup::getResolution))
                                              .orElseThrow(() -> new IllegalStateException(
                                                  "Not able to create GEoCoding for product"));
    GeoCoding productGc = createGeoCoding(highestResGroup, lonGrid, latGrid, highestResGroup.getResolution());
    product.setSceneGeoCoding(productGc);

    for (SensorGroup sensorGroup : sensorGroups) {

      List<Map.Entry<Band, Variable>> bands = sensorGroup.addBands(metadata, product);
      throwIf(bands.isEmpty(),
          new IllegalStateException(String.format("No bands available for sensor %s.", sensorGroup.getSensorName())));
      GeoCoding sensorGc = createGeoCoding(sensorGroup, lonGrid, latGrid, highestResGroup.getResolution());

      int sceneHeight = product.getSceneRasterHeight();
      int sceneWidth = product.getSceneRasterWidth();
      for (Map.Entry<Band, Variable> bandVariableEntry : bands) {
        Band band = bandVariableEntry.getKey();
        band.setGeoCoding(sensorGc);
        band.setImageToModelTransform(AffineTransform.getScaleInstance((double) sceneWidth / band.getRasterWidth(),
            (double) sceneHeight / band.getRasterHeight()));
        RenderedImage sourceImage = new AsterImage(band, bandVariableEntry.getValue(), product.getPreferredTileSize());
        band.setSourceImage(sourceImage);
      }
    }

  }

  @Nonnull
  private static GeoCoding createGeoCoding(SensorGroup sensorGroup, TiePointGrid lonGrid, TiePointGrid latGrid,
      int sceneResolution) {
    final double[] longitudes = toDoubleArray(lonGrid.getGridData());
    final double[] latitudes = toDoubleArray(latGrid.getGridData());
    Dimension sensorDimension = sensorGroup.getRasterDimension();
    double subsamplingX = (double) sensorDimension.width / (lonGrid.getGridWidth() - 1);
    double subsamplingY = (double) sensorDimension.height / (lonGrid.getGridHeight() - 1);
    double rasterResolutionInKm = sensorGroup.getResolution() / 1000.0;
    // it is not clear where the offset is, especially for the different resolution and if there is e.g. only the TIR sensor
    // but the current implementation works quite well
    // -> using as offset 0.5 for the highest resolution and scale it for the lower resolution
    double offsetX = lonGrid.getOffsetX() * ((double) sceneResolution / sensorGroup.getResolution());
    double offsetY = lonGrid.getOffsetY() * ((double) sceneResolution / sensorGroup.getResolution());
    final GeoRaster geoRaster = new GeoRaster(longitudes, latitudes,
        lonGrid.getName(), latGrid.getName(),
        lonGrid.getGridWidth(), lonGrid.getGridHeight(),
        sensorDimension.width, sensorDimension.height,
        rasterResolutionInKm,
        offsetX, offsetY,
        subsamplingX, subsamplingY);

    ComponentGeoCoding sensorGc = new ComponentGeoCoding(geoRaster,
        ComponentFactory.getForward(TiePointBilinearForward.KEY),
        ComponentFactory.getInverse(TiePointInverse.KEY));
    sensorGc.initialize();
    return sensorGc;
  }

  // CRS currently not supported
  // private static GeoCoding createCrsGeoCoding(SensorGroup sensorGroup, String bandName, AsterMetadata metadata) {
  //   try {
  //     OdlGroup sensorMetadata = metadata.getSensorMetadata(sensorGroup);
  //     String bandId = bandName.substring(bandName.lastIndexOf('_') + 1, bandName.length() - 1);
  //     String processingParametersPath = "PRODUCTSPECIFICMETADATA" + sensorGroup.getSensorName() + "/"
  //         + sensorGroup.getSensorName() + "BAND" + bandId + "DATA" + "/"
  //         + "PROCESSINGPARAMETERS" + bandId + "/";
  //     int utmZone = Integer.parseInt(sensorMetadata.getAttribute(processingParametersPath
  //             + "UTMZONECODE" + bandId + "/"
  //             + "VALUE"));
  //     String projParamsString = sensorMetadata.getAttribute(processingParametersPath
  //             + "PROJECTIONPARAMETERS" + bandId + "/"
  //             + "VALUE");
  //     double[] projectionParams = convertStringToDoubleArray(projParamsString);
  //     int utmZoneID = Math.abs(utmZone);
  //     int utmWgs84Code = utmZone > 0 ? 32600 + utmZoneID : 32700 + utmZoneID;
  //
  //     CoordinateReferenceSystem decode = null;
  //     try {
  //       decode = CRS.decode("EPSG:" + utmWgs84Code, true);
  //       Dimension dimension = sensorGroup.getRasterDimension();
  //       return new CrsGeoCoding(decode, dimension.width, dimension.height,
  //           );
  //     } catch (FactoryException e) {
  //       throw new RuntimeException(e);
  //     }
  //
  //   } catch (IOException e) {
  //     throw new RuntimeException(e);
  //   }
  //
  // }
  //
  // private static double[] convertStringToDoubleArray(String input) {
  //   // Remove the parentheses
  //   String trimmedInput = input.substring(1, input.length() - 1);
  //   // Split the string by comma
  //   String[] stringValues = trimmedInput.split(",");
  //   // Convert string values to double values
  //   double[] doubleValues = new double[stringValues.length];
  //   for (int i = 0; i < stringValues.length; i++) {
  //     doubleValues[i] = Double.parseDouble(stringValues[i].trim());
  //   }
  //   return doubleValues;
  // }

  private static void addGeolocationGrids(SensorGroup sensorGroup, Product product) throws IOException {
    List<TiePointGrid> grids = sensorGroup.getTiePointGrids();
    final TiePointGrid lonGrid = grids.stream()
                                      .filter(tpg -> tpg.getName().toLowerCase().contains("longitude"))
                                      .findFirst().orElse(null);
    final TiePointGrid latGrid = grids.stream()
                                      .filter(tpg -> tpg.getName().toLowerCase().contains("latitude"))
                                      .findAny().orElse(null);
    if (lonGrid == null || latGrid == null) {
      EomtbxRuntime.LOGGER.warning(String.format("Geolocation grid(s) not found for sensor %s.", sensorGroup.getSensorName()));
      return;
    }
    for (TiePointGrid grid : grids) {
      product.addTiePointGrid(grid);
    }
  }

  private static double[] toDoubleArray(ProductData data) {
    if (data == null || data.getElemSize() == 0) {
      return new double[0];
    }

    double[] result = new double[data.getNumElems()];

    for (int i = 0; i < result.length; i++) {
      result[i] = data.getElemDoubleAt(i);
    }
    return result;
  }

  private Dimension getSceneDimension(List<SensorGroup> sensorGroups) {
    Optional<SensorGroup> hasVnir = sensorGroups.stream().filter(
                                                    sensorGroup -> sensorGroup.getSensorName().equals(SENSOR.VNIR.name()))
                                                .findFirst();
    if (hasVnir.isPresent()) {
      SensorGroup sensorGroup = hasVnir.get();
      return sensorGroup.getRasterDimension();
    }
    Optional<SensorGroup> hasSwnir = sensorGroups.stream().filter(
                                                     sensorGroup -> sensorGroup.getSensorName().equals(SENSOR.SWIR.name()))
                                                 .findFirst();
    if (hasSwnir.isPresent()) {
      SensorGroup sensorGroup = hasSwnir.get();
      return sensorGroup.getRasterDimension();
    }
    Optional<SensorGroup> hasTir = sensorGroups.stream().filter(
                                                   sensorGroup -> sensorGroup.getSensorName().equals(SENSOR.TIR.name()))
                                               .findFirst();
    if (hasTir.isPresent()) {
      SensorGroup sensorGroup = hasTir.get();
      return sensorGroup.getRasterDimension();
    }
    throw new IllegalStateException("Product contains neither SWIR, VNIR nor TIR data.");
  }

  private List<SensorGroup> getSensorGroups(NetcdfFile ncFile) {
    List<SensorGroup> sensorGroupList = new ArrayList<>();
    for (SENSOR sensor : SensorGroup.SENSOR.values()) {
      Group group = ncFile.getRootGroup().findGroup(sensor.name());
      if (group != null) {
        sensorGroupList.add(new SensorGroup(sensor, group));
      }
    }
    return sensorGroupList;

  }

  @Override
  protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight,
      int sourceStepX, int sourceStepY, Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight,
      ProductData destBuffer, ProgressMonitor pm) {
    throw new IllegalStateException("readBandRasterData() called for " + destBand.getName() + ".");
  }


}
