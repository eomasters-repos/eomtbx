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

package org.eomasters.eomtbx.s2geom;

import static java.time.Instant.now;

import com.bc.ceres.core.PrintWriterProgressMonitor;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;

class S2GeomTestRunner {

  public static void main(String[] args) {
    try {
      String directory = "D:\\EOData\\_projects\\s2geom_test";

      runStdL2AProduct(directory); // OKAY
      // runL1CProduct(directory);
      // runTheiaProduct(directory);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void runTheiaProduct(String directory) throws IOException {
    String filePath = directory + "\\SENTINEL2B_20240828-112706-986_L2A_T28PGC_C_V3-1.zip";
    Product l2aProduct = ProductIO.readProduct(filePath);
    Instant start = now();
    System.out.println("Start: " + start.atZone(java.time.ZoneId.systemDefault()));
    Product resultProduct = GPF.createProduct("S2GeometryUpscaler", Map.of("includeSourceBands", false), l2aProduct);
    ProductIO.writeProduct(resultProduct,
        directory + "\\SENTINEL2B_20240828_L2A_T28PGC_packed_s2geom_priowritten.znap.zip",
        "ZNAP", new PrintWriterProgressMonitor(System.out));

    Instant end = now();
    Duration duration = Duration.between(start, end);
    System.out.println("Duration: " + format(duration));
    System.out.printf("Written: %s%n", resultProduct.getFileLocation());
  }


  private static void runStdL2AProduct(String directory) throws IOException {
    String filePath = directory + "\\S2B_MSIL2A_20240515T101559_N0510_R065_T32UPE_20240515T144033.SAFE.zip";
    Instant start = now();
    System.out.println("Start: " + start.atZone(java.time.ZoneId.systemDefault()));
    Product l2aProduct = ProductIO.readProduct(filePath);
    Product resultProduct = GPF.createProduct("S2GeometryUpscaler", Map.of("includeSourceBands", false), l2aProduct);
    ProductIO.writeProduct(resultProduct, directory + "\\S2B_MSIL2A_20240515T101559_s2geom_priowrittem.znap.zip",
                           "ZNAP", new PrintWriterProgressMonitor(System.out));
    // GPF.writeProduct(resultProduct, new File(directory + "\\S2B_MSIL2A_20240515T101559_s2geom_gpfwritten.znap.zip"), "ZNAP",
    //     false, new PrintWriterProgressMonitor(System.out));

    Instant end = now();
    Duration duration = Duration.between(start, end);
    System.out.println("Duration: " + format(duration));
    System.out.printf("Written: %s%n", resultProduct.getFileLocation());
  }

  private static void runL1CProduct(String directory) throws IOException {
    String filePath = directory + "\\S2A_MSIL1C_20230627T105621_N0509_R094_T30SVG_20230627T162159.SAFE.zip";
    Product l2aProduct = ProductIO.readProduct(filePath);
    Instant start = now();
    Product resultProduct = GPF.createProduct("S2GeometryUpscaler", Map.of("includeSourceBands", false), l2aProduct);
    ProductIO.writeProduct(resultProduct, directory + "\\S2A_MSIL1C_20230627T105621_s2geom.znap.zip", "ZNAP");
    Instant end = now();
    Duration duration = Duration.between(start, end);
    System.out.println("Duration: " + format(duration));
    System.out.printf("Written: %s%n", resultProduct.getFileLocation());
  }

  private static String format(Duration duration) {
    return String.format("%d:%02d:%02d",
                         duration.toHours(),
                         duration.toMinutesPart(),
                         duration.toSecondsPart());
  }

}
