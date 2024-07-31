/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Basic for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2024 Marco Peters
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

package org.eomasters.eomtbx.loopgpt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LoopGptOptionProcessorTest {

  @Test
  void parseCommandline() {
    String[] params1 = LoopGptOptionProcessor.parseCommandline(
        "C:\\IdeaProjects\\eomtbx\\bgpt\\band_math_s2.xml -t D:\\EOData\\temp\\S3B_OL_1_EFR____20230717T104859.dim -q 6 D:\\EOData\\S2\\S2A_MSIL1C_20230627T105621_N0509_R094_T30SVG_20230627T162159.SAFE.zip");
    assertEquals(6, params1.length);
    assertEquals("C:\\IdeaProjects\\eomtbx\\bgpt\\band_math_s2.xml", params1[0]);
    assertEquals("-t", params1[1]);
    assertEquals("D:\\EOData\\temp\\S3B_OL_1_EFR____20230717T104859.dim", params1[2]);
    assertEquals("-q", params1[3]);
    assertEquals("6", params1[4]);
    assertEquals("D:\\EOData\\S2\\S2A_MSIL1C_20230627T105621_N0509_R094_T30SVG_20230627T162159.SAFE.zip", params1[5]);

    String[] params2 = LoopGptOptionProcessor.parseCommandline(
        "\"C:\\Idea Projects\\eomtbx\\localRes\\bgpt\\band_math_s2.xml\" D:\\EOData\\S2\\S2A_MSIL1C_20230627T105621_N0509_R094_T30SVG_20230627T162159.SAFE.zip");
    assertEquals(2, params2.length);
    assertEquals("C:\\Idea Projects\\eomtbx\\localRes\\bgpt\\band_math_s2.xml", params2[0]);
    assertEquals("D:\\EOData\\S2\\S2A_MSIL1C_20230627T105621_N0509_R094_T30SVG_20230627T162159.SAFE.zip", params2[1]);


  }
}
