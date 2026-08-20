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

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import java.awt.Color;
import org.esa.snap.core.datamodel.Product;

public class TestDummies {

  static class AssetCreationRegistry extends org.eomasters.eomtbx.assets.AssetCreationRegistry {

    @Override
    protected void loadServices() {
      AssetCreationService one = new DummyAssetCreationService();
      services.put(one.getId(), one);
      AssetCreationService another = new AnotherAssetCreationService();
      services.put(another.getId(), another);
    }
  }

  static class AssetTypeRegistry extends org.eomasters.eomtbx.assets.AssetTypeRegistry {

    @Override
    protected void loadServices() {
      AssetType dummyAssetType = new DummyType();
      services.put(dummyAssetType.getId(), dummyAssetType);
      AssetType anotherAssetType = new AnotherDummyType();
      services.put(anotherAssetType.getId(), anotherAssetType);
    }
  }


  public static class DummyAssetCreationService extends AssetCreationService {

    @Override
    public String getName() {
      return "DUMMY Factory";
    }

    @Override
    public String getDescription() {
      return "no description";
    }

    @Override
    public Class<? extends AssetType> getAssetTypeClass() {
      return DummyType.class;
    }

    @Override
    public Asset createAsset(String name, String description, String[] tags, PropertySet properties,
        ProgressMonitor pm) {
      Asset asset = new Asset(name, new DummyType());
      asset.setDescription(description);
      asset.setTags(tags);
      return asset;
    }

    @Override
    protected void initFactoryProperties(PropertySet properties) {

    }

  }

  static class AnotherAssetCreationService extends AssetCreationService {

    @Override
    public String getName() {
      return "ANOTHER Factory";
    }

    @Override
    public String getDescription() {
      return "another dummy description";
    }

    @Override
    public Class<? extends AssetType> getAssetTypeClass() {
      return AnotherDummyType.class;
    }

    @Override
    public Asset createAsset(String name, String description, String[] tags, PropertySet properties,
        ProgressMonitor pm) {
      return new Asset("dummyName", new AnotherDummyType());
    }

    @Override
    protected void initFactoryProperties(PropertySet properties) {

    }

  }

  static class DummyType extends AssetType {

    public DummyType() {
      super("DUMMY Type", "no description");
    }

    @Override
    protected void initAssetProperties(PropertySet attributes) {
      attributes.addProperty(Property.create("Abc", 123));
      attributes.addProperty(Property.create("Second", "notLast"));
      attributes.addProperty(Property.create("3", true));
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

  static class AnotherDummyType extends AssetType {

    public AnotherDummyType() {
      super("ANOTHER Type", "another description");
    }

    @Override
    protected void initAssetProperties(PropertySet attributes) {
      attributes.addProperty(PropertyHelper.createProperty("Color", Color.class, "Beautiful color", Color.BLUE));
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
