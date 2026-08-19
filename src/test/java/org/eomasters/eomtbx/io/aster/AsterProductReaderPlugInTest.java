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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.esa.snap.core.dataio.DecodeQualification;
import org.junit.jupiter.api.Test;

class AsterProductReaderPlugInTest {

  @Test
  void getDecodeQualification() throws URISyntaxException {
    AsterProductReaderPlugIn readerPlugIn = new AsterProductReaderPlugIn();
    Path L1T003 = Paths.get(getClass().getResource("dummy/AST_L1T_00312032022003709_20231028073404_22717.hdf").toURI());
    Path L1T031 = Paths.get(getClass().getResource("dummy/AST_L1T_03111032022131947_20240813072649_3221810.hdf").toURI());
    assertEquals(DecodeQualification.INTENDED, readerPlugIn.getDecodeQualification(L1T003));
    assertEquals(DecodeQualification.INTENDED, readerPlugIn.getDecodeQualification(L1T031));

    Path auxNotWanted1 = Paths.get(getClass().getResource("dummy/AST_L1T_03112172023002443_20240813072942_2290706.hdf.met").toURI());
    assertEquals(DecodeQualification.UNABLE, readerPlugIn.getDecodeQualification(auxNotWanted1));
    Path auxNotWanted2 = Paths.get(getClass().getResource("dummy/AST_L1T_03112172023002443_20240813072942_2290706_BR.hdf").toURI());
    assertEquals(DecodeQualification.UNABLE, readerPlugIn.getDecodeQualification(auxNotWanted2));

    Path wrong = Paths.get(getClass().getResource("dummy/AST_L1B_03111032022131947_20240813072649_32.hdf").toURI());
    assertEquals(DecodeQualification.UNABLE, readerPlugIn.getDecodeQualification(wrong));

  }
}
