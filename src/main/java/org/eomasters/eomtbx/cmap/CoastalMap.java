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

package org.eomasters.eomtbx.cmap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.media.jai.OpImage;
import org.eomasters.eomtbx.EomToolbox;
import org.eomasters.eomtbx.EomtbxRuntime;
import org.eomasters.eomtbx.cmap.gui.MaskModel;
import org.eomasters.eomtbx.utils.FileUtils;
import org.eomasters.geo.GlobalGrid;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.Mask;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductNodeGroup;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.util.SystemUtils;
import org.locationtech.jts.geom.Geometry;

public class CoastalMap {

  private static final String VERSION = "v1.0";

  private static final String DEFAULT_CACHE_DIR = EomtbxRuntime.getModuleAuxdataDir("coastalmap", VERSION).toString();
  private static final String DEFAULT_REMOTE_LOCATION = "https://s3-eu-central-1.ionoscloud.com/coastalmap";
  private static final int DEFAULT_PARALLEL_DOWNLOADS = 2;
  protected static final String COLOR_PREFERENCE = "color";
  protected static final String TRANSPARENCY_PREFERENCE = "transparency";
  protected static final String CACHE_DIR_PREFERENCE = "cacheDir";
  protected static final String REMOTE_LOCATION_PREFERENCE = "remoteLocation";
  protected static final String PARALLEL_DOWNLOADS_PREFERENCE = "parallelDownloads";

  private final Preferences preferences;
  private final Preferences maskPreferences;
  private final GlobalGrid mapGrid;

  public static CoastalMap getInstance() {
    return Holder.INSTANCE;
  }

  private CoastalMap(Preferences preferences) {
    this.preferences = preferences;
    mapGrid = new GlobalGrid(3, 3, 3.0 / 36000);
    maskPreferences = preferences.node("masks");
  }

  public static Band addCoastMapFlagBand(Product product) throws Exception {
    Band cmapRaster;
    if (!product.containsBand(FlagsAndMasks.getFlagBandName())) {
      cmapRaster = product.addBand(FlagsAndMasks.getFlagBandName(), FlagsAndMasks.getFlagsDataType());
    } else {
      throw new Exception(String.format("Product contains already a band named '%s'", FlagsAndMasks.getFlagBandName()));
    }
    cmapRaster.setDescription(FlagsAndMasks.getFlagBandDescription());
    FlagCoding flagCoding = FlagsAndMasks.getFlagCoding();
    if (!product.getFlagCodingGroup().contains(flagCoding.getName())) {
      product.getFlagCodingGroup().add(flagCoding);
    }
    cmapRaster.setSampleCoding(flagCoding);
    return cmapRaster;
  }

  public static void addCoastalMapMasks(Product product) {
    ProductNodeGroup<Mask> maskGroup = product.getMaskGroup();
    Dimension sceneDimension = product.getSceneRasterSize();
    for (Mask mask : FlagsAndMasks.getMasks(sceneDimension)) {
      if (!maskGroup.contains(mask.getName())) {
        maskGroup.add(mask);
      }
    }
  }

  public static OpImage getCoastMapImage(RasterDataNode cmapRaster, boolean aggregation) throws IOException {
    if (aggregation) {
      return new AggregatingCoastalMapOpImage(cmapRaster);
    } else {
      return new NearestNeighbourCoastalMapOpImage(cmapRaster);
    }
  }

  public Color getMaskColor(String maskName, Color color) {
    return new Color(maskPreferences.node(maskName).getInt(COLOR_PREFERENCE, color.getRGB()));
  }

  public double getMaskTransparency(String maskName, double defaultTransparency) {
    return maskPreferences.node(maskName).getDouble(TRANSPARENCY_PREFERENCE, defaultTransparency);
  }

  public void updatePreferences(MaskModel maskModel) {
    maskPreferences.node(maskModel.getName()).putInt(COLOR_PREFERENCE, maskModel.getColor().getRGB());
    maskPreferences.node(maskModel.getName()).putDouble(TRANSPARENCY_PREFERENCE, maskModel.getTransparency());
  }

  public Path getCacheDir() {
    String cachedDir = preferences.get(CACHE_DIR_PREFERENCE, DEFAULT_CACHE_DIR);
    Path cachePath = Path.of(cachedDir);
    copyOldCacheIfNecessary(cachePath);
    return cachePath;
  }

  private static void copyOldCacheIfNecessary(Path cachePath) {
    // todo changed in 1.5.0; remove in future version
    Path oldCacheDir = SystemUtils.getAuxDataPath().resolve(EomtbxRuntime.TOOLBOX_ID)
                                  .resolve("eom_cmap")
                                  .resolve(VERSION);
    if (Files.exists(oldCacheDir)) {
      FileUtils.moveDir(oldCacheDir, cachePath);
    }
  }

  public void setCacheDir(Path cacheDir) {
    preferences.put(CACHE_DIR_PREFERENCE, cacheDir.toAbsolutePath().toString());
  }

  public String getRemoteLocation() {
    return preferences.get(REMOTE_LOCATION_PREFERENCE, DEFAULT_REMOTE_LOCATION);
  }

  public void setRemoteLocation(String remoteLocation) {
    preferences.put(REMOTE_LOCATION_PREFERENCE, remoteLocation);
  }

  public int getParallelDownloads() {
    return preferences.getInt(PARALLEL_DOWNLOADS_PREFERENCE, DEFAULT_PARALLEL_DOWNLOADS);
  }

  public List<Point> getMapTileIDs(Geometry geometry) {
    return getIntersectedCells(geometry, mapGrid);
  }

  public GlobalGrid getMapGrid() {
    return mapGrid;
  }

  public Set<Point> getWaterTileIds() throws IOException {
    Set<Point> waterTiles = new HashSet<>();

    List<String> lines;
    try (InputStream resource = getClass().getResourceAsStream("waterTileIds.csv")) {
      if (resource == null) {
        throw new IOException("Not able to retrieve water tiles. Resource not found");
      }
      InputStreamReader inputStreamReader = new InputStreamReader(resource, StandardCharsets.UTF_8);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      lines = bufferedReader.lines().collect(Collectors.toList());
    }
    lines.remove(0); // ignore first line
    for (String line : lines) {
      String[] split = line.split(",");
      int x = Integer.parseInt(split[2]);
      int y = Integer.parseInt(split[1]);
      waterTiles.add(new Point(x, y));
    }
    return waterTiles;
  }

  private List<Point> getIntersectedCells(Geometry geometry, GlobalGrid grid) {
    Geometry boundary = geometry.getBoundary();
    double minX = boundary.getEnvelopeInternal().getMinX();
    double minY = boundary.getEnvelopeInternal().getMinY();
    double maxX = boundary.getEnvelopeInternal().getMaxX();
    double maxY = boundary.getEnvelopeInternal().getMaxY();

    return grid.getIntersectedCells(minX, minY, maxX, maxY);
  }

  private static class Holder {

    private static final CoastalMap INSTANCE = new CoastalMap(EomToolbox.getPreferences().node("cmap"));
  }

}
