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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetException;
import org.eomasters.eomtbx.assets.type.site.CsvFileCreationService.SEPARATOR;
import org.eomasters.eomtbx.converter.ConverterRegistrar;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CsvSitesFactoryTest {

  private static CsvFileCreationService service;

  @BeforeAll
  static void beforeAll() {
    ConverterRegistrar.registerConverter();
    service = new CsvFileCreationService();
  }

  @AfterAll
  static void afterAll() {
    ConverterRegistrar.deregisterConverter();
  }

  @Test
  void testBasicProperties() {
    assertEquals("CSV File", service.getName());
    assertEquals("Creates a list of sites from a CSV file.", service.getDescription());
    assertEquals("eomtbx.assetLibrary.type.sites", service.getHelpId());
    assertEquals(SitesType.class, service.getAssetTypeClass());
  }

  @Test
  void createAsset() throws AssetException, URISyntaxException {
    PropertyContainer props = new PropertyContainer();
    service.initFactoryProperties(props);
    // noinspection DataFlowIssue
    props.setValue(
        CsvFileCreationService.PROP_SOURCE_FILE, Paths.get(SitesType.class.getResource("test_csv_TechSites.txt").toURI()));
    props.setValue(CsvFileCreationService.PROP_SEPARATOR, SEPARATOR.COMMA);

    Asset asset = service.createAsset("test", "test_descr", new String[]{"XYZ-Project"}, props, ProgressMonitor.NULL);
    assertEquals(SitesType.class, asset.getType().getClass());
    assertEquals("test", asset.getName());
    assertEquals("test_descr", asset.getDescription());
    assertArrayEquals(new String[]{"XYZ-Project"}, asset.getTags());
    assertEquals(2, asset.getAssetProperties().getProperties().length);
    assertEquals(6, asset.getAssetProperties().getProperty(SitesType.PROP_SITES).<Site[]>getValue().length);
    String csvFilePath = String.join(File.separator, "org", "eomasters", "eomtbx", "assets",
        "type", "site", "test_csv_TechSites.txt");

    assertTrue(asset.getAssetProperties()
                    .getProperty(PlacemarkFileCreationService.PROP_SOURCE_FILE).getValueAsText()
                    .endsWith(csvFilePath));
  }

  @Test
  void getFactoryProperties() {
    PropertySet props = service.createFactoryProperties();

    PropertyDescriptor sourceFileDescriptor = props.getProperty(CsvFileCreationService.PROP_SOURCE_FILE).getDescriptor();
    assertEquals("sourceFile", sourceFileDescriptor.getName());
    assertEquals("The CSV file to read", sourceFileDescriptor.getDescription());
    assertTrue(sourceFileDescriptor.isNotNull());
    assertTrue(sourceFileDescriptor.isNotEmpty());

    PropertyDescriptor delimiterDescriptor = props.getProperty(CsvFileCreationService.PROP_SEPARATOR).getDescriptor();
    assertEquals("separator", delimiterDescriptor.getName());
    assertEquals("The separator used in the CSV file", delimiterDescriptor.getDescription());
    assertEquals(SEPARATOR.COMMA, props.getValue(CsvFileCreationService.PROP_SEPARATOR));
    assertTrue(delimiterDescriptor.isNotNull());
  }
}
