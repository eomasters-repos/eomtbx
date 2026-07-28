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

package org.eomasters.eomtbx.assets;

import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Nonnull;
import org.eomasters.eomtbx.assets.type.geometry.GeometryType;
import org.eomasters.eomtbx.assets.type.geometry.WktGeometryCreationService;
import org.eomasters.eomtbx.assets.type.site.CsvFileCreationService;
import org.eomasters.eomtbx.assets.type.site.CsvFileCreationService.SEPARATOR;
import org.eomasters.eomtbx.assets.type.site.PlacemarkFileCreationService;
import org.eomasters.eomtbx.spex.TestPreferences;

public class TestLibrary {

  public static AssetLibrary createTestLibrary() throws AssetException, IOException {
    AssetLibrary library = AssetLibrary.create(new TestPreferences());
    addPlacemarkSite(library);
    addCsvSites(library);
    addGeometries(library);
    return library;
  }

  private static void addCsvSites(AssetLibrary library) throws AssetException {
    AssetCreationService factory = new CsvFileCreationService();
    PropertySet properties = factory.createFactoryProperties();
    Path resolve = getCurrentWorkingPath().resolve(
        "modules/toolbox/src/test/resources/org/eomasters/eomtbx/assets/type/site/test_csv_TechSites.txt");
    properties.setValue(CsvFileCreationService.PROP_SOURCE_FILE, resolve);
    properties.setValue(CsvFileCreationService.PROP_SEPARATOR, SEPARATOR.COMMA);
    Asset asset = factory.createAsset("Water Samples", "Taken at Bodensee", new String[]{"Water-Project"},
        properties, ProgressMonitor.NULL);
    asset.setUserNotes("Date: 2023-06-27");
    library.add(asset);
  }

  private static void addPlacemarkSite(AssetLibrary library) throws AssetException {
    PlacemarkFileCreationService factory = new PlacemarkFileCreationService();
    PropertySet properties = factory.createFactoryProperties();
    Path resolve =getCurrentWorkingPath().resolve(
        "modules/toolbox/src/test/resources/org/eomasters/eomtbx/assets/type/site/test_Shanghai.placemark");
    properties.setValue(PlacemarkFileCreationService.PROP_SOURCE_FILE,
        resolve);
    library.add(factory.createAsset("Shanghai", "test_descr", new String[]{"ABC-Project"},
        properties, ProgressMonitor.NULL));
  }

  private static void addGeometries(AssetLibrary library) throws AssetException {
    WktGeometryCreationService factory = new WktGeometryCreationService();
    PropertySet props = factory.createFactoryProperties();
    props.setValue(GeometryType.PROP_WKT, "POINT(1 2)");
    props.setValue(GeometryType.PROP_CRS, "EPSG:32632");
    library.add(factory.createAsset("test", "test_descr", new String[0], props, ProgressMonitor.NULL));
  }

  private static @Nonnull Path getCurrentWorkingPath() {
    return Paths.get("").toAbsolutePath();
  }
}
