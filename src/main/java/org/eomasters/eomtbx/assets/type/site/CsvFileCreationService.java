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
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetException;
import org.eomasters.eomtbx.assets.AssetCreationService;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.PropertyHelper;

public class CsvFileCreationService extends AssetCreationService {

  public enum SEPARATOR {
    COMMA(','),
    TAB('\t'),
    SPACE(' '),
    SEMI_COLON(';');

    private final char separatorChar;

    SEPARATOR(char c) {

      this.separatorChar = c;
    }

    public char getSeparatorChar() {
      return separatorChar;
    }
  }

  public static final String PROP_SOURCE_FILE = "sourceFile";
  public static final String PROP_SEPARATOR = "separator";

  @Override
  public String getName() {
    return "CSV File";
  }

  @Override
  public String getDescription() {
    return "Creates a list of sites from a CSV file.";
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
      ProgressMonitor pm) throws AssetException {
    if (name == null || name.isBlank()) {
      throw new AssetException("A name must not be given for the asset.");
    }

    Asset sitesAsset = new Asset(name, new SitesType(), description, tags);
    // add a copy of the path, can be deleted by the user
    Path filePath = properties.getProperty(PROP_SOURCE_FILE).getValue();
    CsvFileCreationService.SEPARATOR separator = properties.getProperty(PROP_SEPARATOR).getValue();

    try {
      pm.beginTask("Reading sites ...", -1);
      List<Site> sites = CsvSiteReader.readSites(filePath, separator.getSeparatorChar());
      sitesAsset.getAssetProperties().setValue(SitesType.PROP_SITES, sites.toArray(new Site[0]));
    } catch (IOException e) {
      String msg = String.format("Failed to read sites from CSV file: %s", e.getMessage());
      throw new AssetException(msg, e);
    } finally {
      pm.done();
    }

    sitesAsset.getAssetProperties().setValue(SitesType.PROP_SOURCE_FILE, filePath);

    return sitesAsset;
  }

  @Override
  protected void initFactoryProperties(PropertySet properties) {
    Property file = PropertyHelper.createProperty(PROP_SOURCE_FILE, Path.class, "The CSV file to read");
    file.getDescriptor().setNotNull(true);
    file.getDescriptor().setNotEmpty(true);
    properties.addProperty(file);

    Property separator = PropertyHelper.createProperty(PROP_SEPARATOR, SEPARATOR.class,
        "The separator used in the CSV file", SEPARATOR.COMMA);
    separator.getDescriptor().setNotNull(true);
    properties.addProperty(separator);
  }

}
