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

package org.eomasters.eomtbx.assets.type.site;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.ValueSet;
import com.bc.ceres.binding.converters.ArrayConverter;
import com.bc.ceres.core.ProgressMonitor;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetException;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.PropertyHelper;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.eomasters.snap.utils.ProductUtilities;
import org.esa.snap.core.datamodel.GcpDescriptor;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PinDescriptor;
import org.esa.snap.core.datamodel.Placemark;
import org.esa.snap.core.datamodel.PlacemarkDescriptor;
import org.esa.snap.core.datamodel.PlacemarkGroup;
import org.esa.snap.core.datamodel.PointDescriptor;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductNode;
import org.esa.snap.core.datamodel.VectorDataNode;
import org.esa.snap.core.util.GeoUtils;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class SitesType extends AssetType {

  public static final String PROP_SITES = "sites";
  public static final String PROP_SOURCE_FILE = "sourceFile";
  static final String PROP_CROP_TO_BOUNDS = "crop";
  static final String PROP_ADD_TO_PINS = "toPins";
  static final String PROP_ADD_TO_GCP = "toGcps";
  static final String PROP_ADD_TO_VDN = "toVDN";
  static final String PROP_SELECTED_VECTOR_NODE = "vectorGroup";
  static final String PROP_NEW_VECTOR_NODE = "newVN";
  static final String PROP_NEW_NODE_NAME = "newNodeName";

  public SitesType() {
    super("Sites", "List of sites.");
  }

  @Override
  public Icon getIcon() {
    return Icons.PIN;
  }

  @Override
  public String getHelpId() {
    return super.getHelpId() + ".sites";
  }

  @Override
  protected void initAssetProperties(PropertySet properties) {
    Property sites = PropertyHelper.createProperty(PROP_SITES, Site[].class, "The array of sites.");
    sites.getDescriptor().setNotNull(true);
    sites.getDescriptor().setNotEmpty(true);
    sites.getDescriptor().setConverter(new ArrayConverter(Site[].class, new Site.Converter()));
    properties.addProperty(sites);

    Property sourceFile = PropertyHelper.createProperty(PROP_SOURCE_FILE, Path.class, "Path to the source file.");
    sourceFile.getDescriptor().setNotNull(true);
    properties.addProperty(sourceFile);
  }

  @Override
  protected void initAddConfiguration(PropertySet addConfig, Asset asset, Product product) {
    Property cropToBounds = Property.create(PROP_CROP_TO_BOUNDS, true);
    cropToBounds.getDescriptor().setDisplayName("Only include within bounds");
    cropToBounds.getDescriptor().setDescription("Only sites within the product bounds will be added to the product.");
    addConfig.addProperty(cropToBounds);

    Property addToPins = Property.create(PROP_ADD_TO_PINS, true);
    addToPins.getDescriptor().setDisplayName("Add to pins");
    addToPins.getDescriptor().setDescription("Add the sites to the existing pin group.");
    addConfig.addProperty(addToPins);

    Property addToGcp = Property.create(PROP_ADD_TO_GCP, false);
    addToGcp.getDescriptor().setDisplayName("Add to GCPs");
    addToGcp.getDescriptor().setDescription("Add the sites to the existing GCP group.");
    addConfig.addProperty(addToGcp);

    Property addToVg = Property.create(PROP_ADD_TO_VDN, false);
    addToVg.getDescriptor().setDisplayName("Add to a vector node");
    addToVg.getDescriptor().setDescription("Add the sites to an existing vector node.");
    addConfig.addProperty(addToVg);

    Property selectedVectorGroup = Property.create(PROP_SELECTED_VECTOR_NODE, String.class);
    selectedVectorGroup.getDescriptor().setDisplayName("Vector node");
    String[] vectorNames = Arrays.stream(product.getVectorDataGroup().toArray())
                                 .filter(node -> node != product.getPinGroup().getVectorDataNode())
                                 .filter(node -> node != product.getGcpGroup().getVectorDataNode())
                                 .map(ProductNode::getName)
                                 .toArray(String[]::new);
    selectedVectorGroup.getDescriptor().setValueSet(new ValueSet(vectorNames));
    addConfig.addProperty(selectedVectorGroup);

    Property createVg = Property.create(PROP_NEW_VECTOR_NODE, false);
    createVg.getDescriptor().setDisplayName("Create a new vector node");
    createVg.getDescriptor().setDescription("Create a new vector node and add the sites to it.");
    addConfig.addProperty(createVg);

    Property newVectorGroup = Property.create(PROP_NEW_NODE_NAME, String.class);
    try {
      newVectorGroup.setValue(ProductUtilities.createValidNodeName(asset.getName()));
    } catch (ValidationException e) {
      // ignore and leave the property empty
    }
    newVectorGroup.getDescriptor().setDisplayName("Name of node");
    newVectorGroup.getDescriptor().setDescription("Create a new vector node and add the sites to it.");
    newVectorGroup.getDescriptor().setNotEmpty(true);
    newVectorGroup.getDescriptor().setValidator((property, value) -> {
      if (product.containsRasterDataNode((String) value)) {
        throw new ValidationException(String.format("The product already contains a node with the name '%s'", value));
      }
    });
    addConfig.addProperty(newVectorGroup);

  }


  @Override
  public void addToProduct(Asset asset, Product product, PropertySet addConfig, ProgressMonitor pm)
      throws AssetException {
    PlacemarkGroup targetGroup;
    PlacemarkDescriptor placemarkDescriptor;
    if (addConfig.getValue(PROP_ADD_TO_PINS)) {
      targetGroup = product.getPinGroup();
      placemarkDescriptor = PinDescriptor.getInstance();
    } else if (addConfig.getValue(PROP_ADD_TO_GCP)) {
      targetGroup = product.getGcpGroup();
      placemarkDescriptor = GcpDescriptor.getInstance();
    } else if (addConfig.getValue(PROP_ADD_TO_VDN)) {
      String nodeName = addConfig.getValue(PROP_SELECTED_VECTOR_NODE);
      VectorDataNode vectorDataNode = product.getVectorDataGroup().get(nodeName);
      targetGroup = vectorDataNode.getPlacemarkGroup();
      placemarkDescriptor = PointDescriptor.getInstance();
    } else if (addConfig.getValue(PROP_NEW_VECTOR_NODE)) {
      String nodeName = addConfig.getValue(PROP_NEW_NODE_NAME);
      VectorDataNode vectorDataNode = new VectorDataNode(nodeName, Placemark.createGeometryFeatureType());
      product.getVectorDataGroup().add(vectorDataNode);
      targetGroup = vectorDataNode.getPlacemarkGroup();
      placemarkDescriptor = PointDescriptor.getInstance();
    } else {
      throw new AssetException("No target node specified to add the sites to");
    }

    try {
      Site[] sites = asset.getAssetProperties().getProperty(PROP_SITES).getValue();
      pm.beginTask("Adding Sites to Product", sites.length);
      ArrayList<Placemark> placemarks = convertSitesToPlacemarks(sites, placemarkDescriptor, product,
          addConfig.getValue(PROP_CROP_TO_BOUNDS));
      placemarks.forEach(targetGroup::add);
      pm.worked(1);
    } catch (Throwable e) {
      throw new AssetException("Could not add sites asset to product", e);
    }finally {
      pm.done();
    }

  }

  private ArrayList<Placemark> convertSitesToPlacemarks(Site[] sites, PlacemarkDescriptor descriptor,
      Product product, boolean doCrop)
      throws FactoryException, TransformException {
    var placemarkList = new ArrayList<Placemark>();
    CoordinateReferenceSystem targetCRS = DefaultGeographicCRS.WGS84;
    Area productArea = null;
    if (doCrop) {
      productArea = createProductArea(product);
    }
    for (Site site : sites) {
      DirectPosition destPos = getGeoPosInDestination(site, targetCRS);
      boolean addPlacemark = true;
      if (productArea != null) {
        addPlacemark = productArea.contains(destPos.getOrdinate(0), destPos.getOrdinate(1));
      }

      if (addPlacemark) {
        addPlacemark(descriptor, product, site, destPos, placemarkList);
      }
    }
    return placemarkList;
  }

  private void addPlacemark(PlacemarkDescriptor descriptor, Product product, Site site, DirectPosition destPos,
      ArrayList<Placemark> placemarkList) {
    Placemark placemark = Placemark.createPointPlacemark(descriptor,
        site.getName(), site.getName(), site.getDescription(), null,
        new GeoPos(destPos.getOrdinate(1), destPos.getOrdinate(0)),
        product.getSceneGeoCoding());
    SiteStyle style = site.getStyle();
    String cssString = style.toCssString();
    placemark.setStyleCss(cssString);
    placemarkList.add(placemark);
  }

  private DirectPosition getGeoPosInDestination(Site site, CoordinateReferenceSystem targetCRS)
      throws FactoryException, TransformException {
    CoordinateReferenceSystem siteCrs = CRS.decode(site.getCrs());
    MathTransform transform = CRS.findMathTransform(siteCrs, targetCRS, true);
    DirectPosition2D sitePos = new DirectPosition2D(siteCrs, site.getY(), site.getX());
    return transform.transform(sitePos, null);
  }

  private static Area createProductArea(Product product) {
    GeneralPath[] boundary = GeoUtils.createGeoBoundaryPaths(product);
    Area area = new Area();
    for (GeneralPath generalPath : boundary) {
      area.add(new Area(generalPath));
    }
    return area;
  }

}
