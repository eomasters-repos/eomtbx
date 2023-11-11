/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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

package org.eomasters.eomtbx.utils;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelImage;
import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.media.jai.JAI;
import org.esa.snap.core.datamodel.Mask;
import org.esa.snap.core.datamodel.PlainFeatureFactory;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.VectorDataNode;
import org.esa.snap.core.image.VirtualBandOpImage;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.util.FeatureUtils;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
  If not otherwise defined, masks are combined by an OR operation.
 **/
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ValidMaskImageBuilder {

  private static final int VALID = 255;
  private final Product sourceProduct;
  private ArrayList<MaskImage> maskImages = new ArrayList<>();
  private MaskOperation operation;


  public ValidMaskImageBuilder(Product sourceProduct) {
    this.sourceProduct = sourceProduct;
    operation = MaskOperation.OR;
  }

  public RenderedImage create() throws ValidMaskBuilderException {
    if (maskImages.isEmpty()) {
      return createConstantMask(VALID);
    }
    RenderedImage mask = maskImages.get(0).create(sourceProduct);
    for (int i = 1; i < maskImages.size(); i++) {
      MaskImage element = maskImages.get(i);
      RenderedImage maskImage = element.create(sourceProduct);
      mask = JAI.create(element.getOperationName(), mask, maskImage);
    }

    return mask;
  }

  /**
   * Switches the combination operation to <code>OR</code>
   * @return the current builder instance
   */
  public ValidMaskImageBuilder or() {
    operation = MaskOperation.OR;
    return this;
  }

  /**
   * Switches the combination operation to <code>AND</code>
   * @return the current builder instance
   */
  public ValidMaskImageBuilder and() {
    operation = MaskOperation.AND;
    return this;
  }


  public ValidMaskImageBuilder withMaskImage(RenderedImage maskImage) {
    if (maskImage != null) {
      maskImages.add(new WrappedImage(operation, maskImage));
    }
    return this;
  }

  public ValidMaskImageBuilder withExpression(String validExpression) {
    if (validExpression != null) {
      maskImages.add(new ValidExprImage(operation, validExpression));
    }
    return this;
  }

  public ValidMaskImageBuilder withWkt(Geometry roi) {
    if (roi != null) {
      maskImages.add(new WktRoiImage(operation, roi));
    }
    return this;
  }

  public ValidMaskImageBuilder withWkt(String wktString) throws ParseException {
    if (wktString != null) {
      maskImages.add(new WktRoiImage(operation, new WKTReader().read(wktString)));
    }
    return this;
  }

  public ValidMaskImageBuilder withShapeFile(File shapeFile) {
    if (shapeFile != null) {
      maskImages.add(new ShapefileImage(operation, shapeFile));
    }
    return this;
  }

  public ValidMaskImageBuilder withShapeFile(Path shapeFile) {
    if (shapeFile != null) {
      maskImages.add(new ShapefileImage(operation, shapeFile));
    }
    return this;
  }

  public ValidMaskImageBuilder withShapeFile(URL shapeUrl) {
    if (shapeUrl != null) {
      maskImages.add(new ShapefileImage(operation, shapeUrl));
    }
    return this;
  }

  @SuppressWarnings("SameParameterValue")
  private RenderedImage createConstantMask(int value) {
    ParameterBlock pb = new ParameterBlock();
    Dimension dimension = sourceProduct.getSceneRasterSize();
    pb.add(Float.valueOf(dimension.width));
    pb.add(Float.valueOf(dimension.height));
    pb.add(new Byte[]{(byte) value});
    return JAI.create("Constant", pb, null);
  }


  private enum MaskOperation {
    OR, AND
  }

  private abstract static class MaskImage {

    private final MaskOperation operation;

    public MaskImage(MaskOperation operation) {
      this.operation = operation;
    }

    public abstract RenderedImage create(Product product) throws ValidMaskBuilderException;

    String getOperationName() {
      return operation.name();
    }
  }

  private static class WrappedImage extends MaskImage {

    private final RenderedImage image;

    public WrappedImage(MaskOperation operation, RenderedImage image) {
      super(operation);
      this.image = image;
    }

    @Override
    public RenderedImage create(Product product) {
      return image;
    }
  }

  private static class ValidExprImage extends MaskImage {

    private final String validExpression;

    public ValidExprImage(MaskOperation operation, String validExpression) {
      super(operation);
      this.validExpression = validExpression;
    }

    @Override
    public RenderedImage create(Product product) {
      Term term = VirtualBandOpImage.parseExpression(validExpression, product);
      VirtualBandOpImage.Builder builder = VirtualBandOpImage.builder(term).sourceSize(product.getSceneRasterSize());
      builder.mask(true);
      return builder.create();
    }
  }

  private class WktRoiImage extends MaskImage {

    private final Geometry geometry;

    public WktRoiImage(MaskOperation operation, Geometry geometry) {
      super(operation);
      this.geometry = geometry;
    }

    @Override
    public RenderedImage create(Product product) {
      SimpleFeatureType wktFeatureType = PlainFeatureFactory.createDefaultFeatureType(DefaultGeographicCRS.WGS84);
      ListFeatureCollection newCollection = new ListFeatureCollection(wktFeatureType);
      SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(wktFeatureType);
      SimpleFeature wktFeature = featureBuilder.buildFeature("ID" + Long.toHexString(System.currentTimeMillis()));
      wktFeature.setDefaultGeometry(geometry);
      newCollection.add(wktFeature);
      FeatureCollection<SimpleFeatureType, SimpleFeature> wktFeatures = FeatureUtils.clipFeatureCollectionToProductBounds(
          newCollection, sourceProduct, null, ProgressMonitor.NULL);

      VectorDataNode roiNode = new VectorDataNode("WktRoiImage", wktFeatures);
      roiNode.setOwner(sourceProduct);

      Dimension dimension = product.getSceneRasterSize();
      Mask wktRoiMask = new Mask("m", dimension.width, dimension.height, Mask.VectorDataType.INSTANCE);
      Mask.VectorDataType.setVectorData(wktRoiMask, roiNode);
      wktRoiMask.setOwner(sourceProduct);
      MultiLevelImage sourceImage = wktRoiMask.getSourceImage();

      return sourceImage.getImage(0);
    }
  }

  private class ShapefileImage extends MaskImage {

    private final File shapeFile;

    public ShapefileImage(MaskOperation operation, Path shapePath) {
      this(operation, shapePath.toFile());

    }

    public ShapefileImage(MaskOperation operation, URL shapeUrl) {
      this(operation, new File(shapeUrl.getFile()));
    }

    public ShapefileImage(MaskOperation operation, File shapeFile) {
      super(operation);
      this.shapeFile = shapeFile;
    }


    @Override
    public RenderedImage create(Product product) throws ValidMaskBuilderException {
      Dimension dimension = product.getSceneRasterSize();
      final FeatureUtils.FeatureCrsProvider crsProvider = new Wgs84CrsProvider();
      DefaultFeatureCollection simpleFeatures;
      try {
        simpleFeatures = FeatureUtils.loadShapefileForProduct(shapeFile, sourceProduct,
            crsProvider, ProgressMonitor.NULL);
      } catch (IOException e) {
        throw new ValidMaskBuilderException("Cannot load shapefile.", e);
      }
      VectorDataNode shapefileRoiNode = new VectorDataNode("shapefileRoiImage", simpleFeatures);
      shapefileRoiNode.setOwner(sourceProduct);

      Mask shapefileMaks = new Mask("m", dimension.width, dimension.height, Mask.VectorDataType.INSTANCE);
      shapefileMaks.setOwner(sourceProduct);
      Mask.VectorDataType.setVectorData(shapefileMaks, shapefileRoiNode);
      return shapefileMaks.getSourceImage().getImage(0);

    }

    private class Wgs84CrsProvider implements FeatureUtils.FeatureCrsProvider {

      @Override
      public CoordinateReferenceSystem getFeatureCrs(Product product) {
        return DefaultGeographicCRS.WGS84;
      }

      @Override
      public boolean clipToProductBounds() {
        return true;
      }
    }

  }

}
