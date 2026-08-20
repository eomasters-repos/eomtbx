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

import static org.esa.snap.core.datamodel.PlainFeatureFactory.DEFAULT_TYPE_NAME;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.ValueSet;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.SubProgressMonitor;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetException;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.PropertyHelper;
import org.eomasters.icons.Icon;
import org.esa.snap.core.datamodel.Placemark;
import org.esa.snap.core.datamodel.PlainFeatureFactory;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductNode;
import org.esa.snap.core.datamodel.VectorDataNode;
import org.esa.snap.core.util.FeatureUtils;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.JTS;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class GeometryType extends AssetType {

  public static final String PROP_WKT = "wkt";
  public static final String PROP_CRS = "crs";
  public static final String PROP_SOURCE_FILE = "sourceFile";

  static final String PROP_CROP_TO_BOUNDS = "crop";
  static final String PROP_ADD_TO_VDN = "toVDN";
  static final String PROP_SELECTED_VECTOR_NODE = "vectorGroup";
  static final String PROP_CREATE_VECTOR_NODE = "newVN";
  static final String PROP_NEW_NODE_NAME = "newNodeName";

  static final String DEFAULT_CRS = "EPSG:4326";


  public GeometryType() {
    super("Geometry", "A geometry");
  }

  @Override
  protected void initAssetProperties(PropertySet properties) {
    Property wkt = PropertyHelper.createProperty(PROP_WKT, String.class, "The Wkt of the geometry");
    wkt.getDescriptor().setNotNull(true);
    wkt.getDescriptor().setNotEmpty(true);
    properties.addProperty(wkt);

    Property crs = PropertyHelper.createProperty(PROP_CRS, String.class,
        "The CRS in which the coordinates are given (Default: EPSG:4326)");
    crs.getDescriptor().setNotNull(true);
    crs.getDescriptor().setNotEmpty(true);
    crs.getDescriptor().setDefaultValue(DEFAULT_CRS);
    properties.addProperty(crs);

    Property sourceFile = PropertyHelper.createProperty(PROP_SOURCE_FILE, Path.class, "Path to the source file.");
    properties.addProperty(sourceFile);
  }

  @Override
  protected void initAddConfiguration(PropertySet addConfig, Asset asset, Product product) {
    Property cropToBounds = Property.create(PROP_CROP_TO_BOUNDS, true);
    cropToBounds.getDescriptor().setDisplayName("Crop to product bounds");
    cropToBounds.getDescriptor().setDescription("Crop the geometry to the bounds of the the product.");
    addConfig.addProperty(cropToBounds);

    // only enabled if vdns area available. Also for sites
    String[] vectorNames = Arrays.stream(product.getVectorDataGroup().toArray())
                                 .filter(node -> node != product.getPinGroup().getVectorDataNode())
                                 .filter(node -> node != product.getGcpGroup().getVectorDataNode())
                                 .map(ProductNode::getName)
                                 .toArray(String[]::new);

    Property addToVg = Property.create(PROP_ADD_TO_VDN, vectorNames.length != 0);
    addToVg.getDescriptor().setDisplayName("Add to a vector node");
    addToVg.getDescriptor().setDescription("Add the sites to an existing vector node.");
    addConfig.addProperty(addToVg);

    Property selectedVectorGroup = Property.create(PROP_SELECTED_VECTOR_NODE, String.class);
    selectedVectorGroup.getDescriptor().setDisplayName("Vector node");
    selectedVectorGroup.getDescriptor().setValueSet(new ValueSet(vectorNames));
    addConfig.addProperty(selectedVectorGroup);

    Property createVg = Property.create(PROP_CREATE_VECTOR_NODE, vectorNames.length == 0);
    createVg.getDescriptor().setDisplayName("Create a new vector node");
    createVg.getDescriptor().setDescription("Create a new vector node and add the sites to it.");
    addConfig.addProperty(createVg);

    Property newVectorNodeName = Property.create(PROP_NEW_NODE_NAME, String.class);
    newVectorNodeName.getDescriptor().setDisplayName("Name of node");
    newVectorNodeName.getDescriptor().setDescription("Create a new vector node and add the sites to it.");
    newVectorNodeName.getDescriptor().setNotEmpty(true);
    newVectorNodeName.getDescriptor().setNotNull(true);
    newVectorNodeName.getDescriptor().setValidator((property, value) -> {
      String name = (String) value;
      if (product.containsRasterDataNode(name)) {
        throw new ValidationException(String.format("The product already contains a node with the name '%s'", value));
      }
    });

    addConfig.addProperty(newVectorNodeName);
  }

  @Override
  public void addToProduct(Asset asset, Product product, PropertySet addConfig, ProgressMonitor pm)
      throws AssetException {
    if (product.getSceneGeoCoding() == null) {
      throw new AssetException("The product must be geo-referenced in order to be able to add a geometry.");
    }

    VectorDataNode vectorDataNode;
    if (addConfig.getValue(PROP_ADD_TO_VDN)) {
      String nodeName = addConfig.getValue(PROP_SELECTED_VECTOR_NODE);
      vectorDataNode = product.getVectorDataGroup().get(nodeName);
    } else if (addConfig.getValue(PROP_CREATE_VECTOR_NODE)) {
      String nodeName = addConfig.getValue(PROP_NEW_NODE_NAME);
      if(product.containsRasterDataNode(nodeName)) {
        throw new AssetException(String.format("The product contains already node with the name '%s'", nodeName));
      }
      vectorDataNode = new VectorDataNode(nodeName, Placemark.createGeometryFeatureType());
    } else {
      throw new AssetException("No target node specified to add the sites to");
    }

    pm.beginTask(String.format("Adding asset '%s' to product.", asset.getName()), 10);
    try {
      DefaultFeatureCollection targetFeatures;
      try {
        var sourceFeatures = createSourceFeatures(asset);
        CoordinateReferenceSystem sourceCrs = sourceFeatures.getSchema().getCoordinateReferenceSystem();
        pm.worked(2);

        Geometry geoClipBounds = getGeoClipBounds(product, addConfig);
        CoordinateReferenceSystem targetCrs = product.getSceneCRS();
        pm.worked(2);

        targetFeatures = FeatureUtils.clipCollection(
            sourceFeatures, sourceCrs, geoClipBounds, DefaultGeographicCRS.WGS84,
            null, targetCrs, SubProgressMonitor.create(pm, 5));

      } catch (Exception e) {
        throw new AssetException("Not able to add geometry asset to product", e);
      }
      if (targetFeatures.isEmpty()) {
        throw new AssetException("No target features found after clipping.");
      }
      vectorDataNode.getFeatureCollection().addAll((Collection<? extends SimpleFeature>) targetFeatures);
      product.getVectorDataGroup().add(vectorDataNode);
      pm.worked(1);
    } finally {
      pm.done();
    }
  }

  private static Geometry getGeoClipBounds(Product product, PropertySet addConfig) {
    CoordinateReferenceSystem targetCrs = product.getSceneCRS();
    if (addConfig.getProperty(PROP_CROP_TO_BOUNDS).getValue()) {
      return FeatureUtils.createGeoBoundaryPolygon(product);
    } else {
      return getClipPolyongFromCrs(targetCrs);
    }
  }

  private static Geometry getClipPolyongFromCrs(CoordinateReferenceSystem targetCrs) {
    GeographicBoundingBox targetGeoBounds = CRS.getGeographicBoundingBox(targetCrs);
    if (targetGeoBounds == null) {
      String srs = CRS.toSRS(targetCrs);
      try {
        targetGeoBounds = CRS.getGeographicBoundingBox(CRS.decode(srs));
      } catch (FactoryException e) {
        // ignore and go on
      }
    }
    if (targetGeoBounds == null) {
      targetGeoBounds = GeographicBoundingBoxImpl.WORLD;
    }
    GeneralEnvelope targetExtent = new GeneralEnvelope(targetGeoBounds);
    return JTS.toPolygon(targetExtent.toRectangle2D());
  }

  private static ListFeatureCollection createSourceFeatures(Asset asset)
      throws FactoryException, ParseException {
    PropertyContainer assetProperties = asset.getAssetProperties();
    CoordinateReferenceSystem geometryCrs = CRS.decode(assetProperties.getProperty(PROP_CRS).getValueAsText());
    SimpleFeatureType featureType = PlainFeatureFactory.createPlainFeatureType(DEFAULT_TYPE_NAME, Geometry.class,
        geometryCrs);

    ListFeatureCollection sourceFeatures = new ListFeatureCollection(featureType);
    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
    Geometry geometryCollection = getGeometryCollection(asset);
    long id = System.nanoTime();
    for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
      Geometry subGeometry = geometryCollection.getGeometryN(i);
      SimpleFeature feature = featureBuilder.buildFeature("ID" + Long.toHexString(id++));
      feature.setDefaultGeometry(subGeometry);
      sourceFeatures.add(feature);
    }
    return sourceFeatures;
  }

  private static GeometryCollection getGeometryCollection(Asset asset) throws ParseException {
    String wktGeometry = asset.getAssetProperties().getProperty(PROP_WKT).getValueAsText();
    wktGeometry = wktGeometry.replace("\r", "");
    wktGeometry = wktGeometry.replace("\n", "");

    Geometry geometry = new WKTReader().read(wktGeometry);
    if (geometry instanceof GeometryCollection) {
      return (GeometryCollection) geometry;
    } else {
      return new GeometryFactory().createGeometryCollection(new Geometry[]{geometry});
    }
  }

  @Override
  public Icon getIcon() {
    return EomtbxIcons.GEOMETRIES;
  }

  @Override
  public String getHelpId() {
    return super.getHelpId() + ".geometry";
  }
}
