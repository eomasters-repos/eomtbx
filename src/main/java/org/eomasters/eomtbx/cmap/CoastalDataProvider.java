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

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.media.jai.operator.ConstantDescriptor;
import org.eomasters.geo.GlobalGrid;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.CrsGeoCoding;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.Scene;
import org.esa.snap.core.util.GeoUtils;
import org.esa.snap.dataio.znap.ZnapProductReaderPlugIn;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

public class CoastalDataProvider {

  private static final String MAP_TILE_FILENAME_TEMPLATE = "EOM_CoastalMap_Flags_%s.znap.zip";
  private final ExecutorService downloads;
  private final CoastalMap map;
  @SuppressWarnings({"FieldCanBeLocal", "unused"}) // might be disposed if not keeping it as a field
  private final ScheduledExecutorService cacheCleaner;
  private final HashMap<Point, CachedProduct> cachedProducts = new HashMap<>();
  private final Set<Point> waterTileIds;
  private Hashtable<Point, Future<Boolean>> submittedDownloads;

  public CoastalDataProvider(CoastalMap map) throws IOException {
    downloads = Executors.newFixedThreadPool(2);
    cacheCleaner = startCacheCleaner();
    this.map = map;
    waterTileIds = CoastalMap.getInstance().getWaterTileIds();
  }

  public void init(Scene scene) {
    GeoCoding geoCoding = scene.getGeoCoding();
    Geometry geometry = GeoUtils.computeGeometryUsingPixelRegion(geoCoding,
        new Rectangle(0, 0, scene.getRasterWidth(), scene.getRasterHeight()));
    Set<Point> tileIDs = new HashSet<>(map.getMapTileIDs(geometry));
    submittedDownloads = new Hashtable<>();
    Path downloadDir = map.getCacheDir();
    if (!Files.exists(downloadDir)) {
      try {
        Files.createDirectories(downloadDir);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
    for (Point tileID : tileIDs) {
      if (!waterTileIds.contains(tileID)) {
        String tileFileName = getMapFileName(tileID);
        DownloadableTile downloadable = new DownloadableTile(downloadDir, tileFileName, map.getRemoteLocation());
        submittedDownloads.put(tileID, downloads.submit(downloadable));
      }
    }
    downloads.shutdown();
  }

  public int getMapValue(double lon, double lat) throws IOException {
    Point cellId = map.getMapGrid().getCellId(lon, lat);
    if (waterTileIds.contains(cellId)) {
      return 0;
    }
    Band band = retrieveBand(cellId);
    PixelPos pixelPos = band.getGeoCoding().getPixelPos(new GeoPos(lat, lon), null);
    try {
      return band.getSampleInt((int) pixelPos.x, (int) pixelPos.y);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int[] getMapValues(double lon0, double lat0, double lon1, double lat1) throws IOException {
    List<Integer> values = new ArrayList<>();
    List<Point> intersectedCells = map.getMapGrid().getIntersectedCells(lon0, lat1, lon1, lat0);
    for (Point intersectedCell : intersectedCells) {
      Band band;
      if (waterTileIds.contains(intersectedCell)) {
        band = retrieveDummyWaterBand(intersectedCell);
      } else {
        band = retrieveBand(intersectedCell);
      }
      Geometry geometry = GeoUtils.computeRasterGeometry(band.getGeoCoding(),
          band.getRasterWidth(), band.getRasterHeight());
      Geometry intersection = geometry.intersection(new GeometryBuilder().box(lon0, lat0, lon1, lat1));
      if (!intersection.isEmpty()) {
        Rectangle rectangle = GeoUtils.computePixelRegionUsingGeometry(band.getGeoCoding(), band.getRasterWidth(),
            band.getRasterHeight(), intersection, 0, false, false);
        int[] samples = band.readPixels(rectangle.x, rectangle.y, rectangle.width, rectangle.height, (int[]) null);
        Arrays.stream(samples).forEach(values::add);
      }
    }
    return values.stream().mapToInt(i -> i).toArray();
  }

  private static Band retrieveDummyWaterBand(Point intersectedCell) {
    Band band = new Band("dummyWater", ProductData.TYPE_INT8, 36000, 36000);
    band.setSourceImage(ConstantDescriptor.create(36000f, 36000f, new Byte[]{0}, null));
    try {
      band.setGeoCoding(
          new CrsGeoCoding(DefaultGeographicCRS.WGS84, 36000, 36000, intersectedCell.getX(), intersectedCell.getY(),
              3 / 36000.0, 3 / 36000.0, 0.5, 0.5));
    } catch (FactoryException | TransformException e) {
      throw new RuntimeException(e);
    }
    return band;
  }

  private Band retrieveBand(Point intersectedCell) throws IOException {
    waitForDownload(intersectedCell);
    Product product = getProduct(intersectedCell);
    return product.getBandAt(0);
  }

  private Product getProduct(Point tileId) throws IOException {
    synchronized (cachedProducts) {
      if (!cachedProducts.containsKey(tileId)) {
        String mapFileName = getMapFileName(tileId);
        Path cachedMapFile = map.getCacheDir().resolve(mapFileName);
        if (Files.exists(cachedMapFile)) {
          ZnapProductReaderPlugIn plugin = new ZnapProductReaderPlugIn();
          ProductReader reader = plugin.createReaderInstance();
          Product product = reader.readProductNodes(cachedMapFile, null);
          cachedProducts.put(tileId, new CachedProduct(product));
        } else {
          throw new IOException(String.format("Could not find coastal map file '%s'", cachedMapFile.toAbsolutePath()));
        }
      }

      CachedProduct cachedProduct = cachedProducts.get(tileId);
      if (cachedProduct != null) {
        return cachedProduct.getProduct();
      } else {
        throw new IOException(String.format("No coastal map file available for '%s'", tileId));
      }
    }
  }

  private void waitForDownload(Point tileId) throws IOException {
    if (!downloads.isTerminated()) {
      if (submittedDownloads.containsKey(tileId)) {
        try {
          if (!submittedDownloads.get(tileId).isDone()) {
            submittedDownloads.get(tileId).get(); // waits for completion of download
          }
        } catch (Exception e) {
          throw new IOException(String.format("Could not download coastal map tile for id [%d, %d]", tileId.x, tileId.y), e);
        }
      }
    }
  }

  private static String getMapFileName(Point tileID) {
    return String.format(MAP_TILE_FILENAME_TEMPLATE, GlobalGrid.formatCellId(tileID));
  }

  private ScheduledExecutorService startCacheCleaner() {
    final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1);
    cacheCleaner.scheduleAtFixedRate(() -> {
      Instant now = Instant.now();
      cachedProducts.entrySet().removeIf(entry -> {
        Instant lastAccess = entry.getValue().getLastAccess();
        Duration between = Duration.between(lastAccess, now);
        return between.compareTo(Duration.ofSeconds(10)) > 0;
      });
    }, 5, 5, TimeUnit.SECONDS);
    return cacheCleaner;
  }

  private static class CachedProduct {

    private final Product product;
    private Instant lastAccess;

    public CachedProduct(Product product) {
      this.product = product;
    }

    public Product getProduct() {
      updateLastAccess();
      return product;
    }

    private void updateLastAccess() {
      this.lastAccess = Instant.now();
    }

    public Instant getLastAccess() {
      return lastAccess;
    }

  }
}
