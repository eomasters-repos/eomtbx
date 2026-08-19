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

package org.eomasters.eomtbx.assets.type.geometry;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetCreationService;
import org.eomasters.eomtbx.assets.AssetException;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.PropertyHelper;
import org.eomasters.eomtbx.validator.EpsgCrsValidator;
import org.esa.snap.core.util.FeatureUtils;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class FileGeometryCreationService extends AssetCreationService {
  // Todo - later add geoJson and GML

  public static final String PROP_SOURCE_FILE = "sourceFile";
  public static final String PROP_CRS = "crs";
  private static final String DEFAULT_CRS_CODE = "EPSG:4326";

  @Override
  public String getName() {
    return "Geometry File";
  }

  @Override
  public String getDescription() {
    return "Reads a geometry from a file (Shapefile, WKT)";
  }

  @Override
  public Class<? extends AssetType> getAssetTypeClass() {
    return GeometryType.class;
  }

  @Override
  public String getHelpId() {
    return super.getHelpId() + ".type.geometry";
  }

  @Override
  public Asset createAsset(String name, String description, String[] tags, PropertySet properties,
      ProgressMonitor pm)
      throws AssetException {
    if (name == null || name.isBlank()) {
      throw new AssetException("A name must not be given for the asset.");
    }
    Asset geometryAsset = new Asset(name, new GeometryType(), description, tags);
    Path filePath = properties.getProperty(PROP_SOURCE_FILE).getValue();

    GeometryCollection geometryCollection;
    String crsString;

    String shapefileSuffix = ".shp";
    try {
      pm.beginTask("Reading Geometries ...", -1);
      if (filePath.getFileName().toString().toLowerCase().endsWith(shapefileSuffix)) {
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = readCollectionFromShapefile(filePath);
        geometryCollection = getGeometries(collection);
        crsString = CRS.toSRS(collection.getSchema().getCoordinateReferenceSystem());
        if (crsString == null) {
          crsString = properties.getProperty(PROP_CRS).getValue();
        }
      } else {
        geometryCollection = getGeometries(filePath);
        crsString = properties.getProperty(PROP_CRS).getValue();
      }

      WKTWriter wktWriter = new WKTWriter();
      String wktString = wktWriter.write(geometryCollection);
      geometryAsset.getAssetProperties().setValue(GeometryType.PROP_WKT, wktString);
      geometryAsset.getAssetProperties().setValue(GeometryType.PROP_CRS, crsString);
      geometryAsset.getAssetProperties().setValue(PROP_SOURCE_FILE, filePath);
      return geometryAsset;

    } catch (Exception e) {
      throw new AssetException("Cannot create geometry asset.", e);
    } finally {
      pm.done();
    }
  }

  private static FeatureCollection<SimpleFeatureType, SimpleFeature> readCollectionFromShapefile(
      Path filePath) throws IOException {
    // prevent confusing error logging, in case *.shx file is not found
    Logger readerLogger = Logging.getLogger(ShapefileReader.class);
    Level origLevel = readerLogger.getLevel();
    readerLogger.setLevel(Level.SEVERE);
    try {
      return FeatureUtils.loadFeatureCollectionFromShapefile(filePath.toFile());
    } finally {
      readerLogger.setLevel(origLevel);
    }
  }

  private static GeometryCollection getGeometries(Path filePath) throws ParseException, IOException {
    Geometry readGeometry = new WKTReader().read(Files.newBufferedReader(filePath));
    if (readGeometry instanceof GeometryCollection) {
      return (GeometryCollection) readGeometry;
    } else {
      GeometryFactory geoFac = new GeometryFactory();
      return geoFac.createGeometryCollection(new Geometry[]{readGeometry});
    }
  }

  private static GeometryCollection getGeometries(FeatureCollection<SimpleFeatureType, SimpleFeature> collection) {
    List<Geometry> geometryList = new ArrayList<>();
    try (FeatureIterator<SimpleFeature> features = collection.features()) {
      while (features.hasNext()) {
        SimpleFeature feature = features.next();
        geometryList.add((Geometry) feature.getDefaultGeometry());
      }
    }
    GeometryFactory geoFac = new GeometryFactory();
    return geoFac.createGeometryCollection(geometryList.toArray(Geometry[]::new));
  }

  @Override
  protected void initFactoryProperties(PropertySet properties) {
    Property file = PropertyHelper.createProperty(PROP_SOURCE_FILE, Path.class,
        "Path to file which defines a geometry.");
    file.getDescriptor().setNotNull(true);
    file.getDescriptor().setNotEmpty(true);
    properties.addProperty(file);

    Property crs = PropertyHelper.createProperty(PROP_CRS, String.class,
        "The CRS as EPSG code. The CRS defined by the input, if available, has priority.");
    crs.getDescriptor().setNotNull(true);
    crs.getDescriptor().setNotEmpty(true);
    crs.getDescriptor().setDisplayName("CRS Code");
    crs.getDescriptor().setDefaultValue(DEFAULT_CRS_CODE);
    crs.getDescriptor().setValidator(new EpsgCrsValidator());
    properties.addProperty(crs);
  }
}
