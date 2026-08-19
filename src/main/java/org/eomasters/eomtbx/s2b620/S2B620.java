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

package org.eomasters.eomtbx.s2b620;

import java.awt.image.RenderedImage;
import java.nio.file.Path;
import org.eomasters.eomtbx.utils.Range;
import org.eomasters.snap.utils.MaskedOpImage;
import org.eomasters.snap.utils.ValidMaskBuilderException;
import org.eomasters.snap.utils.ValidMaskImageBuilder;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.image.ImageManager;
import org.locationtech.jts.geom.Geometry;

/**
 * The S2B620 class is designed to create a derived spectral band at 620 nm reflectance from an existing band at 665 nm
 * reflectance. The transformation is based on a mathematical operation implemented in the S2B620Image class, and the
 * derived band can include optional constraints to limit its validity. <br> See <a
 * href="https://doi.org/10.1016/j.isprsjprs.2023.09.020">Paper</a>
 * <p>
 * This class provides functionality to configure additional parameters such as: - input range limits, - valid pixel
 * expressions, - input region constraints using WKT geometry or shapefiles.
 * <p>
 * The derived band is primarily intended for use in remote sensing applications involving Sentinel-2 satellite data.
 */
public class S2B620 {

  public static final String B620_NAME = "B620";
  public static final int PRODUCT_DATA_TYPE = ProductData.TYPE_FLOAT32;
  public static final int IMAGE_DATA_TYPE = ImageManager.getDataBufferType(PRODUCT_DATA_TYPE);
  public static final float NO_DATA_VALUE = Float.NaN;

  static final Range INPUT_RANGE = new Range(0, 0.065);
  private final Product sourceProduct;
  private boolean limitInputRange;
  private String validExpression;
  private Path shapefile;
  private Geometry wktRegion;

  /**
   * Constructs an instance of the S2B620 class to facilitate the generation of a derived spectral band at 620 nm
   * reflectance based on an input product.
   *
   * @param sourceProduct The source Sentinel-2 product which contains the band data to be used for deriving the 620 nm
   *                      band.
   */
  public S2B620(Product sourceProduct) {
    this.sourceProduct = sourceProduct;
  }

  /**
   * Sets whether the input range should be limited during operations involving the source product. The range is defined
   * as [0, 0.065]
   *
   * @param limitInputRange A boolean value indicating whether to restrict the input range. If true, the input range is
   *                        constrained; otherwise, it is unrestricted.
   */
  public void setLimitInputRange(boolean limitInputRange) {
    this.limitInputRange = limitInputRange;
  }

  /**
   * Sets the valid expression used to filter input data. Pixels considered invalid be set to no-data.
   *
   * @param validExpression A string representation of the valid expression.
   */
  public void setValidExpression(String validExpression) {
    this.validExpression = validExpression;
  }

  /**
   * Sets the shapefile to be define the region of interest. Pixels outside of region will be set to no-data.
   *
   * @param shapefile The path to the shapefile that defines the region of interest or other spatial parameters.
   */
  public void setShapefile(Path shapefile) {
    this.shapefile = shapefile;
  }

  /**
   * Sets the region of interest using a Well-Known Text (WKT) geometry. Pixels outside of region will be set to
   * no-data.
   *
   * @param wktRegion A Geometry object representing the region of interest in WKT format.
   */
  public void setWktRegion(Geometry wktRegion) {
    this.wktRegion = wktRegion;
  }

  /**
   * Derives a new Band object representing the reflectance at 620 nm from the input Band of reflectance at 665 nm.
   * The method utilizes a polynomial transformation to compute the 620 nm reflectance and applies masking
   * operations based on a valid expression, shapefile, and region of interest if specified.
   *
   * @param band665 The input Band object representing the reflectance at 665 nm.
   * @return A new Band object representing the reflectance at 620 nm.
   * @throws OperatorException If a valid mask cannot be created during the process.
   */
  public Band deriveBand620From(Band band665) {
    final Band b620 = new Band(B620_NAME, PRODUCT_DATA_TYPE, band665.getRasterWidth(),
                               band665.getRasterHeight());
    b620.setImageToModelTransform(band665.getImageToModelTransform());
    b620.setDescription("Reflectance at 620 derived from band at 665");
    b620.setNoDataValueUsed(true);
    b620.setNoDataValue(NO_DATA_VALUE);
    b620.setSpectralWavelength(620);
    b620.setUnit(band665.getUnit());
    RenderedImage s2B620Image = new S2B620Image(band665.getGeophysicalImage(), limitInputRange ? INPUT_RANGE : null);

    try {
      RenderedImage maskImage = new ValidMaskImageBuilder(sourceProduct)
          .withExpression(validExpression)
          .withGeometryArea(wktRegion)
          .withShapeFile(shapefile)
          .withMaskImage(band665.getValidMaskImage())
          .withTileSize(sourceProduct.getPreferredTileSize())
          .create();
      s2B620Image = new MaskedOpImage(s2B620Image, maskImage, NO_DATA_VALUE);
      b620.setSourceImage(s2B620Image);
      return b620;
    } catch (ValidMaskBuilderException e) {
      throw new OperatorException("Not able to create mask.", e);
    }
  }


}
