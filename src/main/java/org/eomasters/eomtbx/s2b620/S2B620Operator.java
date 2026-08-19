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

import static org.eomasters.utils.Exceptions.throwIf;

import java.nio.file.Path;
import org.eomasters.eomtbx.EomOperator;
import org.eomasters.eomtbx.utils.BandUtils;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.core.util.converters.JtsGeometryConverter;
import org.locationtech.jts.geom.Geometry;

// Deriving the Band 620 from band 665 is defined in the paper:
// Estimating the concentration of total suspended solids in inland and coastal waters from Sentinel-2 MSI: A semi-analytical approach
// https://doi.org/10.1016/j.isprsjprs.2023.09.020

@OperatorMetadata(alias = "S2B620",
    category = "Optical/Preprocessing",
    version = "1.0",
    authors = "Marco Peters",
    copyright = "(c) 2025 Marco Peters",
    description = "Derive band at 620 using the band at 665.")
public class S2B620Operator extends EomOperator {

  @SourceProduct(description = "An atmospherically corrected Sentinel-2 product")
  private Product sourceProduct;

  @TargetProduct
  private Product targetProduct;

  @Parameter(description = "Limit input range [0, 0.065]", defaultValue = "True")
  private boolean limitInputRange = true;

  @Parameter(description = "The valid expression defining the considered geographical region.")
  private String validExpression;
  @Parameter(description = "An ESRI shapefile, providing the considered geographical region(s).")
  private Path shapefile;
  @Parameter(converter = JtsGeometryConverter.class, label = "WKT region",
      description = "The considered geographical region as a geometry in well-known text format (WKT).")
  private Geometry wktRegion;

  @Parameter(defaultValue = "True",
      description = "Whether to include source data or only generate band at 620nm")
  private boolean includeSourceBands = true;


  @Override
  public void initialize() throws OperatorException {
    Band band665 = BandUtils.findClosestToCenterBand(sourceProduct.getBands(), 664, 666);
    throwIf(band665 == null, new OperatorException("Band at wavelength 665 not found"));

    targetProduct = new Product(sourceProduct.getName() + "_B620", "S2B620",
                                band665.getRasterWidth(), band665.getRasterHeight());
    S2B620 s2B620 = new S2B620(sourceProduct);
    s2B620.setLimitInputRange(limitInputRange);
    s2B620.setValidExpression(validExpression);
    s2B620.setShapefile(shapefile);
    s2B620.setWktRegion(wktRegion);
    final Band b620 = s2B620.deriveBand620From(band665);

    if (includeSourceBands) {
      ProductUtils.copyProductNodes(sourceProduct, targetProduct);
      BandUtils.copyBands(sourceProduct, targetProduct, true);
      // copy explicitly after bands, because bands are needed by masks
      ProductUtils.copyMasks(sourceProduct, targetProduct);
      // use the name and not the object, it is not the same object
      int indexB665 = targetProduct.getBandGroup().indexOf(band665.getName());
      targetProduct.getBandGroup().add(indexB665, b620);
    } else {
      targetProduct.addBand(b620);
      ProductUtils.copyGeoCoding(band665, b620);
      ProductUtils.copyTimeInformation(sourceProduct, targetProduct);
      ProductUtils.copyPreferredTileSize(sourceProduct, targetProduct);
    }

  }

  public static class Spi extends OperatorSpi {

    public Spi() {
      super(S2B620Operator.class);
    }
  }

}
