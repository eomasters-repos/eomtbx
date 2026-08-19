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

import com.bc.ceres.core.ProgressMonitor;
import java.awt.image.Raster;
import java.util.stream.IntStream;
import javax.media.jai.OpImage;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.Tile;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.util.ProductUtils;

@OperatorMetadata(label = "EOMasters Coastal Map", alias = "EOM_CoastalMap", version = "1.0", authors = "EOMasters",
    category = "Raster/Masks", description = "Generates a map which provides indicators for coastal areas.")
public class CoastalMapOp extends Operator {

  @SourceProduct(description = "A geo-coded source product.")
  private Product sourceProduct;

  @TargetProduct(description = "The target product with the coastal map.")
  private Product targetProduct;

  @Parameter(alias = "includeSource", defaultValue = "false", label = "Include Source Product Data",
      description = "If true, the operator will include the source data in the target product.")
  private boolean includeSourceData;
  @Parameter(alias = "aggregate", defaultValue = "false", label = "Use Aggregation",
      description = "If true, the operator will use aggregation to compute the coastal map, instead of "
          + "nearest neighbour interpolation. This is useful if the resolution of the source product is significantly "
          + "lower then the resolution of the coastal map.")
  private boolean useAggregation;

  @Parameter(defaultValue = "true", label = "Add Masks",
      description = "Beside the flag data, for easier usage, masks are added to the product too.")
  private boolean addMasks;
  private OpImage coastMapImage;


  @Override
  public void initialize() throws OperatorException {
    validate();
    targetProduct = createTargetProduct();
    Band cmapRaster;
    try {
      cmapRaster = CoastalMap.addCoastMapFlagBand(targetProduct);
      coastMapImage = CoastalMap.getCoastMapImage(cmapRaster, useAggregation);
    } catch (Exception e) {
      throw new OperatorException("Not able to initialize Coastal Map operation", e);
    }
    if (addMasks) {
      CoastalMap.addCoastalMapMasks(targetProduct);
    }
  }

  @Override
  public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {
    if(pm.isCanceled()) {
      return;
    }
    Raster data = coastMapImage.getData(targetTile.getRectangle());
    byte[] dataBufferByte = targetTile.getDataBufferByte();
    int[] dataBufferInt = IntStream.range(0, dataBufferByte.length).map(i -> dataBufferByte[i]).toArray();

    data.getSamples(targetTile.getMinX(), targetTile.getMinY(),
        targetTile.getWidth(), targetTile.getHeight(), 0,
        dataBufferInt);
    targetTile.setSamples(dataBufferInt);
  }

  private Product createTargetProduct() {
    String productType = "EOM_CM";
    final int sceneWidth = sourceProduct.getSceneRasterWidth();
    final int sceneHeight = sourceProduct.getSceneRasterHeight();
    if (includeSourceData) {
      Product targetProduct = new Product(sourceProduct.getName() + "_eom_cm",
          sourceProduct.getProductType() + "_" + productType, sceneWidth, sceneHeight);
      ProductUtils.copyProductNodes(sourceProduct, targetProduct);
      sourceProduct.getBandNames();
      for (String sourceBandName : sourceProduct.getBandNames()) {
        if (!targetProduct.containsBand(sourceBandName)) {
          ProductUtils.copyBand(sourceBandName, sourceProduct, targetProduct, true);
        }
      }
      return targetProduct;
    } else {
      targetProduct = new Product(sourceProduct.getName() + "_eom_cm", productType,
          sceneWidth, sceneHeight);
      ProductUtils.copyGeoCoding(sourceProduct, targetProduct);
    }
    return targetProduct;
  }

  private void validate() {
    GeoCoding sceneGeoCoding = sourceProduct.getSceneGeoCoding();
    if (sceneGeoCoding == null) {
      throw new OperatorException("The product must be geocoded");
    }
  }

  public static class Spi extends OperatorSpi {

    public Spi() {
      super(CoastalMapOp.class);
    }
  }

}
