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
import com.bc.ceres.core.ProgressMonitor;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetCreationService;
import org.eomasters.eomtbx.assets.AssetException;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.PropertyHelper;
import org.esa.snap.core.dataio.placemark.PlacemarkIO;
import org.esa.snap.core.datamodel.CrsGeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PinDescriptor;
import org.esa.snap.core.datamodel.Placemark;
import org.geotools.referencing.CRS;

public class PlacemarkFileCreationService extends AssetCreationService {

  public static final String PROP_SOURCE_FILE = "sourceFile";
  private static final String DEFAULT_CRS_CODE = "EPSG:4326";

  /**
   * Implementations of this abstract class must call this constructor and provide the name and the description of the
   * factory.
   */
  public PlacemarkFileCreationService() {
    super();
  }

  @Override
  public String getName() {
    return "Placemark File";
  }

  @Override
  public String getDescription() {
    return "Creates a list of sites from a SNAP placemark file.";
  }

  @Override
  public Class<? extends AssetType> getAssetTypeClass() {
    return SitesType.class;
  }

  @Override
  public String getHelpId() {
    return super.getHelpId() + ".type.sites";
  }

  @Override
  public Asset createAsset(String name, String description, String[] tags, PropertySet properties,
      ProgressMonitor pm)
      throws AssetException {
    if (name == null || name.isBlank()) {
      throw new AssetException("A name must not be given for the asset.");
    }

    Asset sitesAsset = new Asset(name, new SitesType(), description, tags);
    Path filePath = properties.getProperty(PROP_SOURCE_FILE).getValue();

    try {
      pm.beginTask("Reading placemarks ...", -1);
      List<Placemark> placemarks = readPlacemarks(filePath);
      Site[] sites = placemarks.stream().map(PlacemarkFileCreationService::createSite).toArray(Site[]::new);
      sitesAsset.getAssetProperties().setValue(SitesType.PROP_SITES, sites);
      sitesAsset.getAssetProperties().setValue(SitesType.PROP_SOURCE_FILE, filePath);
    } finally {
      pm.done();
    }
    return sitesAsset;
  }

  private static List<Placemark> readPlacemarks(Path filePath) throws AssetException {
    List<Placemark> placemarks;
    try (BufferedReader reader = Files.newBufferedReader(filePath)) {
      placemarks = PlacemarkIO.readPlacemarks(reader, getCrsGeoCoding(), new PinDescriptor());
    } catch (IOException e) {
      throw new AssetException("Not able to create sites from placemark file", e);
    }
    return placemarks;
  }

  private static CrsGeoCoding getCrsGeoCoding() throws AssetException {
    try {
      Rectangle imageBounds = new Rectangle(0, 0, 36000, 18000);
      return new CrsGeoCoding(CRS.decode(DEFAULT_CRS_CODE), imageBounds, new AffineTransform());
    } catch (Exception e) {
      throw new AssetException("Not able to create temporary WGS84 CRS geo-coding", e);
    }
  }

  private static Site createSite(Placemark placemark) {
    GeoPos geoPos = placemark.getGeoPos();
    Site site = new Site(placemark.getLabel(), geoPos.getLon(), geoPos.getLat(),
        placemark.getText(), DEFAULT_CRS_CODE);
    site.setStyle(SiteStyle.create(placemark.getStyleCss()));
    return site;
  }

  @Override
  protected void initFactoryProperties(PropertySet properties) {
    Property file = PropertyHelper.createProperty(PROP_SOURCE_FILE, Path.class, "Path to the placemark file.");
    file.getDescriptor().setNotNull(true);
    file.getDescriptor().setNotEmpty(true);
    properties.addProperty(file);
  }
}
