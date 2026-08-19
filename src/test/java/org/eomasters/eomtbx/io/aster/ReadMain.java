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

package org.eomasters.eomtbx.io.aster;

import java.io.IOException;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Product;

public class ReadMain {

  public static void main(String[] args) throws IOException {
    Product aster = ProductIO.readProduct(
        "D:\\EOData\\ASTER\\L1T_031\\DAY\\030800110181669\\030800110181669\\AST_L1T_03112172023002443_20240813072942_2290706.hdf");

    MetadataElement root = aster.getMetadataRoot();
    MetadataElement[] elements = root.getElements();
    MetadataElement element = elements[0];

    GeoCoding sceneGeoCoding = aster.getSceneGeoCoding();
    GeoPos geoPos = sceneGeoCoding.getGeoPos(new PixelPos(254, 3566), null);
    System.out.println(geoPos);

  }

}
