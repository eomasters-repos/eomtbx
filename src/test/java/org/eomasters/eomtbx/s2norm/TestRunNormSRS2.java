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

package org.eomasters.eomtbx.s2norm;

import com.bc.ceres.core.PrintWriterProgressMonitor;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;

class TestRunNormSRS2 {

  public static void main(String[] args) {
    try {
      String directory = "D:\\EOData\\_projects\\cfactor4s2_test";

      runStdProductWithNoDataArea(directory);
      // runStdProduct(directory);
      // runTheiaProduct(directory);
      // runStripedProduct(directory);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void runStdProductWithNoDataArea(String directory) throws IOException {
    String filePath = directory + "\\S2B_MSIL2A_20240515T101559_N0510_R065_T32UPE_20240515T144033.SAFE.zip";
    Instant start = Instant.now();
    Product l2aProduct = ProductIO.readProduct(filePath);
    Product resultProduct = GPF.createProduct("NormSRS2", GPF.NO_PARAMS, l2aProduct);
    ProductIO.writeProduct(resultProduct, directory + "\\S2B_MSIL2A_20240515T101559_normsrs2.znap.zip", "ZNAP",
        new PrintWriterProgressMonitor(System.out));
    Instant end = Instant.now();
    Duration duration = Duration.between(start, end);

    System.out.printf("Written: %s%n", resultProduct.getFileLocation());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss:SS");
    System.out.println("Duration: " + formatter.format(LocalTime.ofNanoOfDay(duration.toNanos())));
  }

  private static void runStdProduct(String directory) throws IOException {
    String filePath = directory + "\\S2B_MSIL2A_20240508T184919_N0510_R113_T10SEG_20240508T220318.SAFE\\MTD_MSIL2A.xml";
    Product l2aProduct = ProductIO.readProduct(filePath);
    Product resultProduct = GPF.createProduct("NormSRS2", GPF.NO_PARAMS, l2aProduct);
    Instant start = Instant.now();
    ProductIO.writeProduct(resultProduct, directory + "\\S2B_MSIL2A_20240508T184919_normsrs2.znap.zip", "ZNAP",
        new PrintWriterProgressMonitor(System.out));
    Instant end = Instant.now();
    Duration duration = Duration.between(start, end);

    System.out.printf("Written: %s%n", resultProduct.getFileLocation());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss:SS");
    System.out.println("Duration: " + formatter.format(LocalTime.ofNanoOfDay(duration.toNanos())));
  }

  private static void runStripedProduct(String directory) throws IOException {
    Product l2aProduct = ProductIO.readProduct(directory + "\\S2A_MSIL2A_20240830T011721_N0511_R088_T52JGP_20240830T040100.SAFE.zip");
    Product resultProduct = GPF.createProduct("NormSRS2", GPF.NO_PARAMS, l2aProduct);
    Instant start = Instant.now();
    ProductIO.writeProduct(resultProduct, directory + "\\S2A_MSIL2A_20240830_striped_normsrs2.znap.zip", "ZNAP",
        new PrintWriterProgressMonitor(System.out));
    Instant end = Instant.now();
    Duration duration = Duration.between(start, end);

    System.out.printf("Written: %s%n", resultProduct.getFileLocation());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss:SS");
    System.out.println("Duration: " + formatter.format(LocalTime.ofNanoOfDay(duration.toNanos())));
  }

  private static void runTheiaProduct(String directory) throws IOException {
    String filePath = directory + "\\SENTINEL2B_20240828-112706-986_L2A_T28PGC_C_V3-1.zip";
    Product l2aProduct = ProductIO.readProduct(filePath);
    Product resultProduct = GPF.createProduct("NormSRS2", Map.of("debugMode", true), l2aProduct);
    Instant start = Instant.now();
    ProductIO.writeProduct(resultProduct, directory + "\\SENTINEL2B_20240828_L2A_T28PGC_normsrs2.znap.zip", "ZNAP",
        new PrintWriterProgressMonitor(System.out));
    Instant end = Instant.now();
    Duration duration = Duration.between(start, end);

    System.out.printf("Written: %s%n", resultProduct.getFileLocation());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss:SS");
    System.out.println("Duration: " + formatter.format(LocalTime.ofNanoOfDay(duration.toNanos())));
  }


}
