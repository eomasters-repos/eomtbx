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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import org.eomasters.eomtbx.converter.ConverterRegistrar;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.Product;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssetTypeTest {

  @BeforeAll
  public static void beforeClass() {
    ConverterRegistrar.registerConverter();
  }

  private AssetType testType;

  @BeforeEach
  public void setUp() {
    testType = new TestAssetType();
  }

  @Test
  void creation() {
    assertEquals("Test", testType.getName());
    assertEquals("descr", testType.getDescription());
    assertEquals(3, testType.createAssetProperties().getProperties().length);

    assertNotNull(testType.createAssetProperties().getProperty("testProp"));
    assertNotNull(testType.createAssetProperties().getProperty("testProp2"));
    assertNotNull(testType.createAssetProperties().getProperty("testProp3"));
    assertEquals("default", testType.createAssetProperties().getProperty("testProp3").getValueAsText());
  }

  @Test
  void getName() {
    assertEquals("Test", testType.getName());
  }

  @Test
  void getDescription() {
    assertEquals("descr", testType.getDescription());
  }

  @Test
  public void createPropertyContainer() {
    PropertyContainer propertyContainer = testType.createAssetProperties();
    assertNotNull(propertyContainer);
    assertEquals(3, propertyContainer.getProperties().length);
  }

  private static class TestAssetType extends AssetType {

    public TestAssetType() {
      super("Test", "descr");
    }

    @Override
    protected void initAssetProperties(PropertySet attributes) {
      final Property testProp = Property.create("testProp", "testValue");
      final Property testProp2 = Property.create("testProp2", new GeoPos(10, 11));
      testProp2.getDescriptor().setDefaultConverter();
      final Property testProp3 = Property.create("testProp3", String.class, "default", true);

      attributes.addProperties(testProp, testProp2, testProp3);
    }

    @Override
    protected void initAddConfiguration(PropertySet addConfig, Asset asset, Product product) {
      // nothing
    }

    @Override
    public void addToProduct(Asset asset, Product product, PropertySet addConfig, ProgressMonitor pm) {
      // nothing
    }
  }

}
