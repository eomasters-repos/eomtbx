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

package org.eomasters.eomtbx.s2superres;

import static org.esa.snap.core.util.FeatureUtils.createGeoBoundaryPolygon;

import com.bc.ceres.core.ProgressMonitor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import org.eomasters.eomtbx.EomOperator;
import org.eomasters.eomtbx.EomOperatorSpi;
import org.eomasters.eomtbx.EomtbxRuntime;
import org.eomasters.eomtbx.s2geom.S2DataFormat;
import org.eomasters.eomtbx.s2geom.StdS2L1CFormat;
import org.eomasters.eomtbx.s2geom.StdS2L2AFormat;
import org.eomasters.eomtbx.s2geom.TheiaS2L2AFormat;
import org.eomasters.eomtbx.s2superres.onnx.Onnx;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.CrsGeoCoding;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.util.FeatureUtils;
import org.esa.snap.core.util.GeoUtils;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.core.util.converters.JtsGeometryConverter;
import org.geotools.feature.FeatureCollection;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

@OperatorMetadata(alias = "S2SuperRes",
    category = "Optical/Geometric",
    version = "1.0.1",
    authors = "Marco Peters",
    copyright = "(c) 2024 Marco Peters",
    description = "Super-resolution of a Sentinel-2 product to 5m ground resolution.")
public class S2SuperResOp extends EomOperator {

  public static final int TARGET_PIXEL_SIZE = 5; // meter
  public static final int MIN_BORDER_MARGIN = 32;
  private static final int DST_TILE_SIZE = 250;
  private static final int SQUARE_PIXEL_LIMIT = 16 * 1000000; // 16 million pixels
  private static final int ORIGINAL_SCENE_SIZE = 10980;

  @SourceProduct(description = "The Sentinel-2 source product")
  private Product sourceProduct;

  @Parameter(description = "Pixel region at 10m to process. Use the following format: {x},{y},{width},{height}.",
      converter = RectangleWithNullConverter.class)
  private Rectangle rasterRegion;
  @Parameter(description = "An ESRI shapefile, providing the considered geographical region(s).")
  private Path shapefile;
  @Parameter(converter = JtsGeometryConverter.class,
      description = "The considered geographical region as a geometry in well-known text format (WKT).")
  private Geometry wktRegion;

  @Parameter(valueSet = {"B2", "B3", "B4", "B5", "B6", "B7", "B8", "B8A", "B11", "B12"},
      defaultValue = "B2,B3,B4,B5,B6,B7,B8,B8A,B11,B12",
      description = "Bands that will be super-resolved to 5m.", notNull = true, notEmpty = true)
  private String[] superResolveBands;
  @Parameter(description = "Whether to include 60m bands and resample them to 5m", defaultValue = "false",
      label = "Resample 60m bands")
  private boolean resample60mBands;
  @Parameter(description = "Whether sun and view geometry bands shall be resampled to 5m. "
      + "This option is not working when using Theia L2A products and it needs original sized Sentinel-2 products.",
      defaultValue = "false")
  private boolean resampleGeometryBands;
  @Parameter(description = "Whether to include other remaining bands and resample them to 5m, "
      + "like B_opaque_clouds, B_cirrus_clouds, B_snow_and_ice_areas.", defaultValue = "false")
  private boolean resampleOtherBands;

  private TiePointHandler tiePointHandler;

  @Override
  public void initialize() throws OperatorException {
    if (isDebugMode()) {
      String[] availableExecutionProviderNames = Onnx.getAvailableExecutionProviderNames();
      System.out.println("Available ONNX execution providers:");
      for (String providerName : availableExecutionProviderNames) {
        System.out.println(providerName);
      }
    }

    Product source = getSourceProduct();
    S2DataFormat sourceFormat = validate(source);
    validateParameters(sourceFormat);

    Rectangle srcPixelRegion = getSrcPixelRegion(source);

    TileCache tileCache = JAI.getDefaultInstance().getTileCache();
    long memCapacity = tileCache.getMemoryCapacity();
    long GB = 1024L * 1024L * 1024L;
    long wantedMem = (long) (Runtime.getRuntime().maxMemory() * 0.25);
    if (memCapacity < wantedMem) {
      tileCache.setMemoryCapacity(wantedMem);
      EomtbxRuntime.LOGGER.info("Changed JAI cache size to " + wantedMem / GB + " GB");
    }

    Rectangle bufferedTargetSceneRegion = getBufferedTargetSceneRegion(source, srcPixelRegion);

    Dimension tileSize = new Dimension(DST_TILE_SIZE, DST_TILE_SIZE);
    Product target = createTargetProduct(source, bufferedTargetSceneRegion, tileSize);

    tiePointHandler = new TiePointHandler(source, target, bufferedTargetSceneRegion, srcPixelRegion);

    List<String> requestedSpectralBands = getRequestedSpectralBands();
    if (resampleGeometryBands) {
      GeometryBands geometryBands = new GeometryBands(tileSize);
      geometryBands.createBandsAndImages(source, sourceFormat, target, bufferedTargetSceneRegion,
                                         requestedSpectralBands);
    }
    if (resample60mBands || resampleOtherBands) {
      OtherBands otherBands = new OtherBands(tileSize);
      if (resample60mBands) {
        otherBands.create60mBandsAndImages(sourceFormat, source, target, bufferedTargetSceneRegion);
      }
      if (resampleOtherBands) {
        otherBands.createOtherBandsAndImages(source, requestedSpectralBands, target, bufferedTargetSceneRegion);
      }
    }

    S2SuperResolvedBands.addBandsAndImagesToTarget(source, sourceFormat, superResolveBands, target,
                                                   bufferedTargetSceneRegion, tileSize, isDebugMode());

    MaskHandler.copyMasks(source, target, requestedSpectralBands);

    sortSpectralTargetBands(target, sourceFormat);
    setTargetProduct(target);
  }

  private static Rectangle getBufferedTargetSceneRegion(Product source, Rectangle srcPixelRegion) {
    AffineTransform ten2FiveScale = AffineTransform.getScaleInstance(S2SuperResolveImage.SCALING,
                                                                     S2SuperResolveImage.SCALING);
    Dimension sourceSceneDimension = source.getSceneRasterSize();
    Rectangle tgtFullScene = ten2FiveScale.createTransformedShape(new Rectangle(sourceSceneDimension)).getBounds();
    Rectangle targetSceneRegion = ten2FiveScale.createTransformedShape(srcPixelRegion).getBounds();
    return addMarginBuffer(tgtFullScene, targetSceneRegion);
  }

  private static Rectangle addMarginBuffer(Rectangle tgtFullScene, Rectangle tgtRegion) {
    Margin targetMargin = Margin.create(tgtFullScene, tgtRegion, MIN_BORDER_MARGIN);
    return new Rectangle((tgtRegion.x) + targetMargin.getLeft(), (tgtRegion.y) + targetMargin.getTop(),
                         tgtRegion.width - (targetMargin.getLeft() + targetMargin.getRight()),
                         tgtRegion.height - (targetMargin.getTop() + targetMargin.getBottom())
    );
  }


  private static void checkPixelRegionLimit(Rectangle srcPixelRegion) {
    if (srcPixelRegion.isEmpty()) {
      throw new OperatorException("Region for computation is empty.");
    }
    int squarePixels = srcPixelRegion.width * srcPixelRegion.height;
    if (squarePixels > SQUARE_PIXEL_LIMIT) {
      String message = String.format(
          "Unfortunately, computation is currently limited to a region of 16 million pixels. "
              + "Provided region is greater %d x %d pixels.",
          srcPixelRegion.width, srcPixelRegion.height);
      throw new OperatorException(message);
    }
  }

  private void validateParameters(S2DataFormat sourceFormat) {
    if (resampleGeometryBands) {
      if (TheiaS2L2AFormat.NAME.equals(sourceFormat.getName())) {
        throw new OperatorException("Geometry bands cannot be resampled for Theia Sentinel-2 products.");
      }
      if (sourceProduct.getSceneRasterWidth() != ORIGINAL_SCENE_SIZE) {
        throw new OperatorException(
            "Geometry bands can only be resampled for Sentinel-2 products in their original size.");
      }
    }

    if (shapefile != null) {
      if (!Files.exists(shapefile)) {
        throw new OperatorException("Specified file does not exist: " + shapefile);
      } else if (!shapefile.getFileName().toString().endsWith(".shp")) {
        throw new OperatorException("Shapefile must have '.shp' extension: " + shapefile);
      }
    }
  }

  private Rectangle getSrcPixelRegion(Product source) {
    Rectangle srcPixelRegion;

    Dimension dimension = source.getSceneRasterSize();
    if (rasterRegion != null) {
      srcPixelRegion = rasterRegion;
    } else if (wktRegion != null) {
      srcPixelRegion = GeoUtils.computePixelRegionUsingGeometry(source.getSceneGeoCoding(), dimension.width,
                                                                dimension.height,
                                                                wktRegion, 0, false, false);
    } else if (shapefile != null) {
      var clipGeometry = createGeoBoundaryPolygon(sourceProduct);
      var shapeFileGeometry = loadShapeFileGeometryWGS84(shapefile, clipGeometry);
      srcPixelRegion = GeoUtils.computePixelRegionUsingGeometry(source.getSceneGeoCoding(), dimension.width,
                                                                dimension.height, shapeFileGeometry,
                                                                0, false, false);
    } else {
      // no region specified
      srcPixelRegion = new Rectangle(source.getSceneRasterSize());
    }
    checkPixelRegionLimit(srcPixelRegion);
    return srcPixelRegion;
  }

  static Geometry loadShapeFileGeometryWGS84(Path file, Geometry clipGeometry) {
    try {
      var simpleFeatures = FeatureUtils.loadFeatureCollectionFromShapefile(file.toFile());
      var shapeFileGeometryAsWGS84 = getShapeFileGeometryAsWGS84(simpleFeatures);
      return shapeFileGeometryAsWGS84.intersection(clipGeometry);
    } catch (IOException e) {
      throw new OperatorException("Not able to load shapefile", e);
    }
  }

  private static Geometry getShapeFileGeometryAsWGS84(
      FeatureCollection<SimpleFeatureType, SimpleFeature> simpleFeatures) {
    Geometry shapeFileGeometry;
    try {
      BoundingBox bounds = simpleFeatures.getBounds().toBounds(DefaultGeographicCRS.WGS84);
      var jtsEnvelope = new Envelope(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
      shapeFileGeometry = new GeometryFactory().toGeometry(jtsEnvelope);
      shapeFileGeometry.normalize();
    } catch (TransformException e) {
      throw new RuntimeException(e);
    }
    return shapeFileGeometry;
  }

  @Override
  public void doExecute(ProgressMonitor pm) throws OperatorException {
    try {
      pm.beginTask("Preparing data...", 1);
      tiePointHandler.fillTiePointGridsWithData(getSourceProduct());
      pm.worked(1);
    } finally {
      pm.done();
    }
  }

  private static Product createTargetProduct(Product source, Rectangle tgtSceneRegion, Dimension tileSize) {
    Product target = new Product(source.getName() + "_superres", source.getProductType(),
                                 tgtSceneRegion.width, tgtSceneRegion.height);
    target.setDescription("Super-resolved Sentinel-2 product");
    initTargetGeoCoding(tgtSceneRegion, source.getSceneGeoCoding(), target);
    target.setStartTime(source.getStartTime());
    target.setEndTime(source.getEndTime());
    target.setDescription(source.getDescription());
    target.setAutoGrouping(source.getAutoGrouping());
    target.setPreferredTileSize(tileSize);
    ProductUtils.copyMetadata(source, target);
    ProductUtils.copyFlagCodings(source, target);
    ProductUtils.copyIndexCodings(source, target);
    // unfortunately, copyVectorData() triggers the log
    // INFO: org.esa.snap.core.datamodel.Product: raster width XXXX not equal to 10980
    ProductUtils.copyVectorData(source, target);
    ProductUtils.copyQuicklookBandName(source, target);

    return target;
  }

  private void sortSpectralTargetBands(Product target, S2DataFormat sourceFormat) {
    Map<String, String[]> spectralGroups = sourceFormat.getSpectralGroups();
    ArrayList<Band> bands = new ArrayList<>();
    for (Entry<String, String[]> group : spectralGroups.entrySet()) {
      for (String bandName : group.getValue()) {
        if (target.containsBand(bandName)) {
          Band band = target.getBand(bandName);
          target.removeBand(band);
          bands.add(band);
        }
      }
    }
    for (int i = 0; i < bands.size(); i++) {
      Band band = bands.get(i);
      target.getBandGroup().add(i, band);
    }
  }

  private List<String> getRequestedSpectralBands() {
    if (superResolveBands == null || superResolveBands.length == 0) {
      throw new OperatorException("No super-resolved bands specified. At least one must be given.");
    }
    List<String> requestedList = new ArrayList<>(List.of(superResolveBands));
    if (resample60mBands) {
      requestedList.addAll(List.of(OtherBands.SPECTRAL_60M_BANDS));
    }
    return requestedList;
  }

  private static void initTargetGeoCoding(Rectangle targetSceneRegion, GeoCoding sceneGeoCoding,
                                          Product target) {
    if (sceneGeoCoding instanceof CrsGeoCoding crsGeoCoding) {
      MathTransform i2mMathTransform = crsGeoCoding.getImageToMapTransform();
      if (i2mMathTransform instanceof AffineTransform i2mAffineTransform) {
        double easting = i2mAffineTransform.getTranslateX();
        double northing = i2mAffineTransform.getTranslateY();
        try {
          CrsGeoCoding geoCoding = new CrsGeoCoding(sceneGeoCoding.getMapCRS(), targetSceneRegion.width,
                                                    targetSceneRegion.height,
                                                    easting + targetSceneRegion.x * TARGET_PIXEL_SIZE,
                                                    northing - targetSceneRegion.y * TARGET_PIXEL_SIZE,
                                                    TARGET_PIXEL_SIZE, TARGET_PIXEL_SIZE, 0, 0);
          target.setSceneGeoCoding(geoCoding);
        } catch (Exception e) {
          throw new OperatorException(e);
        }
      }
    }
    if (target.getSceneGeoCoding() == null) {
      throw new OperatorException("Could not create GeoCoding for target product");
    }
  }

  private S2DataFormat validate(Product sourceProduct) {
    S2DataFormat format = isSupported(sourceProduct);
    if (format == null) {
      throw new OperatorException(
          String.format("Not supported source product %s Path: '%s'", sourceProduct.getName(),
                        sourceProduct.getFileLocation()));
    }
    return format;
  }

  private S2DataFormat isSupported(Product product) {
    List<S2DataFormat> knownFormats = List.of(new StdS2L2AFormat(product), new StdS2L1CFormat(product),
                                              new TheiaS2L2AFormat(product)
    );
    for (S2DataFormat format : knownFormats) {
      if (format.isProductSupported()) {
        return format;
      }
    }
    return null;
  }

  public static class Spi extends EomOperatorSpi {

    public Spi() {
      super(S2SuperResOp.class);
    }

  }
}
