package org.eomasters.eomtbx.batchgpt;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BatchGptOptionProcessorTest {

  @Test
  void parseCommandline() {
    String[] params1 = BatchGptOptionProcessor.parseCommandline(
        "C:\\IdeaProjects\\eomtbx\\bgpt\\band_math_s2.xml -t D:\\EOData\\temp\\S3B_OL_1_EFR____20230717T104859.dim -q 6 D:\\EOData\\S2\\S2A_MSIL1C_20230627T105621_N0509_R094_T30SVG_20230627T162159.SAFE.zip");
    assertEquals(6, params1.length);
    assertEquals("C:\\IdeaProjects\\eomtbx\\bgpt\\band_math_s2.xml", params1[0]);
    assertEquals("-t", params1[1]);
    assertEquals("D:\\EOData\\temp\\S3B_OL_1_EFR____20230717T104859.dim", params1[2]);
    assertEquals("-q", params1[3]);
    assertEquals("6", params1[4]);
    assertEquals("D:\\EOData\\S2\\S2A_MSIL1C_20230627T105621_N0509_R094_T30SVG_20230627T162159.SAFE.zip", params1[5]);

    String[] params2 = BatchGptOptionProcessor.parseCommandline(
        "\"C:\\Idea Projects\\eomtbx\\localRes\\bgpt\\band_math_s2.xml\" D:\\EOData\\S2\\S2A_MSIL1C_20230627T105621_N0509_R094_T30SVG_20230627T162159.SAFE.zip");
    assertEquals(2, params2.length);
    assertEquals("C:\\Idea Projects\\eomtbx\\localRes\\bgpt\\band_math_s2.xml", params2[0]);
    assertEquals("D:\\EOData\\S2\\S2A_MSIL1C_20230627T105621_N0509_R094_T30SVG_20230627T162159.SAFE.zip", params2[1]);



  }
}