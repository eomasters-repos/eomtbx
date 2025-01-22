/*-
 * ========================LICENSE_START=================================
 * EOMTBXP Toolbox Module - Toolbox Module for the EOMasters Pro Toolbox for SNAP
 * -> https://www.eomasters.org/eomtbx/modules/eomtbxp-toolbox
 * ======================================================================
 * Copyright (C) 2023 - 2025 Marco Peters
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

/*
 * Copyright (C) 2014-2015 CS-SI (foss-contact@thor.si.c-s.fr)
 * Copyright (C) 2013-2015 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.eomasters.eomtbx.io.s2l2a;

import eu.esa.opt.dataio.s2.S2Config.Sentinel2ProductLevel;
import eu.esa.opt.dataio.s2.S2ProductReaderPlugIn;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.metadata.MetadataInspector;
import org.esa.snap.core.util.io.SnapFileFilter;

public class S2L2AScaledB1FootprintProductReaderPlugIn extends S2ProductReaderPlugIn {

  public static final String PRODUCT_NAME_REGEX = "S2([AB])_MSIL2A_[0-9]{8}T[0-9]{6}_N[0-9]{4}_R[0-9]{3}_T[0-9]{2}[A-Z]{3}_[0-9]{8}T[0-9]{6}.*";
  private static final Pattern PRODUCT_NAME_PATTERN = Pattern.compile(PRODUCT_NAME_REGEX);
  private static final String FORMAT_NAME = "S2L2A_SCALED_B1_FOOTPRINT";

  @Override
  public MetadataInspector getMetadataInspector() {
    return new EomS2L2aMetadataInspector(Sentinel2ProductLevel.L2A);
  }


  @Override
  public DecodeQualification getDecodeQualification(Object input) {
    if (!(input instanceof File)) {
      return DecodeQualification.UNABLE;
    }
    File file = (File) input;
    Path inputPath = file.toPath();
    boolean isLocalFileSystem = inputPath.getFileSystem().provider() == FileSystems.getDefault().provider();
    if (!isLocalFileSystem && !inputPath.isAbsolute()) {
      return DecodeQualification.UNABLE;
    }
    if (!isValidExtension(file)) {
      return DecodeQualification.UNABLE;
    }

    String productName = EomS2L2AUtils.getProductName(inputPath);
    if (PRODUCT_NAME_PATTERN.matcher(productName).matches()) {
      return DecodeQualification.SUITABLE;
    }
    return DecodeQualification.UNABLE;
  }

  @Override
  public ProductReader createReaderInstance() {
    return new S2L2AScaledB1FootprintProductReader(this);
  }

  @Override
  public SnapFileFilter getProductFileFilter() {
    return new SnapFileFilter(FORMAT_NAME, getDefaultFileExtensions(), getDescription(null));
  }

  @Override
  public String[] getDefaultFileExtensions() {
    return new String[]{".xml", ".zip"};
  }

  @Override
  public String[] getFormatNames() {
    return new String[]{FORMAT_NAME};
  }

  @Override
  public String getDescription(Locale locale) {
    return "Sentinel-2 L2A - scaled B1 det_footprint";
  }
}
