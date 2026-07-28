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

import com.bc.ceres.core.ProgressMonitor;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Iterator;
import javax.media.jai.OpImage;
import org.eomasters.eomtbx.io.AbstractReaderPlugin;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.dataio.geocoding.ComponentFactory;
import org.esa.snap.core.dataio.geocoding.ComponentGeoCoding;
import org.esa.snap.core.dataio.geocoding.ForwardCoding;
import org.esa.snap.core.dataio.geocoding.GeoChecks;
import org.esa.snap.core.dataio.geocoding.GeoRaster;
import org.esa.snap.core.dataio.geocoding.InverseCoding;
import org.esa.snap.core.dataio.geocoding.forward.PixelForward;
import org.esa.snap.core.dataio.geocoding.inverse.PixelQuadTreeInverse;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.ProductData.UTC;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.core.image.ImageManager;
import org.esa.snap.core.image.ResolutionLevel;
import org.esa.snap.core.util.io.FileUtils;
import org.esa.snap.dataio.netcdf.util.Constants;
import org.esa.snap.dataio.netcdf.util.DataTypeUtils;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.AttributeContainer;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

public abstract class AbstractEmitReader extends AbstractProductReader {

  public AbstractEmitReader(ProductReaderPlugIn readerPlugIn) {super(readerPlugIn);}

  @Override
  protected final Product readProductNodesImpl() throws IOException {
    Path path = AbstractReaderPlugin.getAsPath(getInput()).toAbsolutePath();
    NetcdfFile ncFile = NetcdfFiles.open(path.toString());
    return createProduct(path.getFileName().toString(), ncFile);
  }

  private Product createProduct(String fileName, NetcdfFile mainFile) throws IOException {
    Product product = createBasicEmitProduct(fileName, mainFile);
    customizeProduct(mainFile, product);
    return product;
  }

  protected abstract void customizeProduct(NetcdfFile mainFile, Product product) throws IOException;

  protected static Dimension getDimension(NetcdfFile ncFile) {
    ucar.nc2.Dimension crosstrack = ncFile.findDimension(EmitConstants.CROSSTRACK_DIMENSION);
    ucar.nc2.Dimension downtrack = ncFile.findDimension(EmitConstants.DOWNTRACK_DIMENSION);
    if (crosstrack == null || downtrack == null) {
      throw new IllegalStateException(
          String.format("Dimensions '%s' and '%s' not found.", EmitConstants.CROSSTRACK_DIMENSION,
                        EmitConstants.DOWNTRACK_DIMENSION));
    }
    return new Dimension(crosstrack.getLength(), downtrack.getLength());
  }

  protected void addMetadata(Product product, NetcdfFile mainFile) {
    AttributeContainer attributes = mainFile.getRootGroup().attributes();
    Iterator<Attribute> iterator = attributes.iterator();
    MetadataElement gAttributes = new MetadataElement("Global_Attributes");
    MetadataElement root = product.getMetadataRoot();
    root.addElement(gAttributes);
    while (iterator.hasNext()) {
      Attribute next = iterator.next();
      String stringValue = AbstractEmitReader.getStringValue(next);
      if (stringValue != null) {
        String attribName = next.getShortName();
        ProductData attribValue = ProductData.createInstance(stringValue);
        MetadataAttribute attribute = new MetadataAttribute(attribName, attribValue, true);
        gAttributes.addAttribute(attribute);
      }
    }
  }

  protected void addGeoCoding(Product product, NetcdfFile ncFile) throws IOException {
    Group locationGroup = AbstractEmitReader.findGroup(ncFile, EmitConstants.LOCATION_GROUP);
    Variable lonVar = locationGroup.findVariable(EmitConstants.LON_VARIABLE);
    Variable latVar = locationGroup.findVariable(EmitConstants.LAT_VARIABLE);

    final double[] longitudes = (double[]) lonVar.read().copyTo1DJavaArray();
    final double[] latitudes = (double[]) latVar.read().copyTo1DJavaArray();
    final GeoRaster geoRaster = new GeoRaster(longitudes, latitudes, lonVar.getShortName(), latVar.getShortName(),
                                              product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                                              product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                                              EmitConstants.RASTER_RESOLUTION_IN_KM, 0.5, 0.5, 1.0, 1.0);

    final ForwardCoding forward = ComponentFactory.getForward(PixelForward.KEY);
    final InverseCoding inverse = ComponentFactory.getInverse(PixelQuadTreeInverse.KEY);

    final ComponentGeoCoding sceneGeoCoding = new ComponentGeoCoding(geoRaster, forward, inverse,
                                                                     GeoChecks.ANTIMERIDIAN);
    sceneGeoCoding.initialize();
    product.setSceneGeoCoding(sceneGeoCoding);
  }

  private static String getStringValue(Attribute attribute) {
    String stringValue = null;
    if(attribute.isString()) {
      stringValue = attribute.getStringValue();
    }else {
      Number numericValue = attribute.getNumericValue();
      if(numericValue != null) {
        stringValue = numericValue.toString();
      }
    }
    return stringValue;
  }

  protected static Group findGroup(NetcdfFile ncFile, String fullName) {
    Group locationGroup = ncFile.findGroup(fullName);
    if (locationGroup == null) {
      throw new IllegalStateException(String.format("Group '%s' not found.", fullName));
    }
    return locationGroup;
  }

  @Override
  protected final void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight,
                                              int sourceStepX, int sourceStepY, Band destBand, int destOffsetX,
                                              int destOffsetY, int destWidth, int destHeight, ProductData destBuffer,
                                              ProgressMonitor pm) {
    throw new IllegalStateException("Method should not be called");
  }

  @Override
  public final void readTiePointGridRasterData(TiePointGrid tpg, int destOffsetX, int destOffsetY, int destWidth,
                                               int destHeight, ProductData destBuffer, ProgressMonitor pm) {
    throw new IllegalStateException("Method should not be called");
  }

  protected Product createBasicEmitProduct(String fileName, NetcdfFile mainFile) throws IOException {
    String productName = FileUtils.getFilenameWithoutExtension(fileName);
    Dimension dimension = getDimension(mainFile);
    Product product = new Product(productName, getReaderPlugIn().getFormatNames()[0], dimension.width, dimension.height,
                                  this);
    Attribute titleAttribute = mainFile.findGlobalAttribute(EmitConstants.TITLE_ATTRIBUTE);
    if (titleAttribute != null && titleAttribute.isString()) {
      product.setDescription(titleAttribute.getStringValue());
    }
    Attribute coverageStartAttribute = mainFile.findGlobalAttribute(EmitConstants.TIME_COVERAGE_START_ATTRIBUTE);
    Attribute coverageEndAttribute = mainFile.findGlobalAttribute(EmitConstants.TIME_COVERAGE_END_ATTRIBUTE);
    if (coverageStartAttribute != null && coverageEndAttribute != null) {
      product.setStartTime(parse(coverageStartAttribute.getStringValue()));
      product.setEndTime(parse(coverageEndAttribute.getStringValue()));
    }
    product.setPreferredTileSize(EmitConstants.PREFERRED_TILE_SIZE);
    addMetadata(product, mainFile);
    addGeoCoding(product, mainFile);
    addLocationBands(product, mainFile);
    return product;
  }

  protected void addSpectralBands(Product product, NetcdfFile ncFile, String variableName, Array wvlArray) throws IOException {
    Variable variable = ncFile.findVariable(variableName);
    if (variable == null) {
      throw new IllegalStateException(String.format("Variable '%s' not found.", variableName));
    }
    int numChannels = variable.getDimension(2).getLength();
    Group bandParameters = findGroup(ncFile, EmitConstants.SENSOR_BAND_PARAMETERS_GROUP);
    Variable wvlVariable = bandParameters.findVariable(EmitConstants.WAVELENGTHS_VARIABLE);
    Variable fwhmVariable = bandParameters.findVariable(EmitConstants.FWHM_VARIABLE);
    Array wvl = wvlVariable.read();
    Array fwhm = fwhmVariable.read();
    for (int i = 0; i < numChannels; i++) {
      if(wvlArray.getByte(i) != 0) {
        int spectralIndex = i + 1;
        String bandName = String.format("%s_%d", variable.getShortName(), spectralIndex);
        Band band = AbstractEmitReader.addBand(bandName, variable, new int[]{0, 0, i}, product);
        band.setSpectralBandIndex(spectralIndex);
        band.setUnit(variable.findAttribute(EmitConstants.UNITS_ATTRIBUTE).getStringValue());
        band.setDescription(variable.getDescription());
        band.setSpectralWavelength(wvl.getFloat(i));
        band.setSpectralBandwidth(fwhm.getFloat(i));
      }
    }
  }

  private void addLocationBands(Product product, NetcdfFile ncFile) {
    Group locationGroup = findGroup(ncFile, EmitConstants.LOCATION_GROUP);
    Variable lonVar = locationGroup.findVariable(EmitConstants.LON_VARIABLE);
    addBand(lonVar.getShortName(), lonVar, new int[0], product);
    Variable latVar = locationGroup.findVariable(EmitConstants.LAT_VARIABLE);
    addBand(latVar.getShortName(), latVar, new int[0], product);
    Variable elevVar = locationGroup.findVariable(EmitConstants.ELEV_VARIABLE);
    addBand(elevVar.getShortName(), elevVar, new int[0], product);
  }

  public static UTC parse(String timeString) throws DateTimeParseException {
    ZonedDateTime time = ZonedDateTime.parse(timeString, EmitConstants.TIME_FORMATTER);
    Date date = Date.from(time.toInstant());
    return UTC.create(date, time.get(ChronoField.MICRO_OF_SECOND));
  }

  protected static Band addBand(String bandName, Variable variable, int[] imageOrigin,
                                Product product) {
    int rasterDataType = DataTypeUtils.getRasterDataType(variable);
    OpImage image = AbstractEmitReader.createImage(product, variable, imageOrigin, rasterDataType);
    Band band = product.addBand(bandName, rasterDataType);
    band.setSourceImage(image);
    band.setDescription(variable.getDescription());
    Attribute fillValueAttribute = variable.findAttribute(Constants.FILL_VALUE_ATT_NAME);
    if (fillValueAttribute != null) {
      Number numericValue = fillValueAttribute.getNumericValue();
      if (numericValue != null) {
        band.setNoDataValue(numericValue.doubleValue());
        band.setNoDataValueUsed(true);
      }
    }
    return band;
  }

  private static OpImage createImage(Product product, Variable variable, int[] imageOrigin,
                                     int rasterDataType) {
    int imageDataType = ImageManager.getDataBufferType(rasterDataType);
    return new EmitOpImage(variable, imageOrigin, false, variable, imageDataType,
                           product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                           product.getPreferredTileSize(),
                           ResolutionLevel.MAXRES);
  }

  protected static Path getSiblingFilePath(Path radiancePath, String replace, String replacement) {
    String fileName = radiancePath.getFileName().toString();
    if (fileName.contains(replace.toUpperCase())) {
      fileName = fileName.replace(replace.toUpperCase(), replacement.toUpperCase());
    } else if (fileName.contains(replace.toLowerCase())) {
      fileName = fileName.replace(replace.toLowerCase(), replacement.toLowerCase());
    }
    return radiancePath.resolveSibling(fileName);
  }

}
