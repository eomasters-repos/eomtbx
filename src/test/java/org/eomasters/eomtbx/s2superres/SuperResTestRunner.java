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

package org.eomasters.eomtbx.s2superres;

import static java.time.Instant.now;

import com.bc.ceres.core.PrintWriterProgressMonitor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.main.GPT;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.dataio.netcdf.NetCdfActivator;

class SuperResTestRunner {

  public static void main(String[] args) throws Exception {
    initSnapAndGpf();
    String directory = "D:\\EOData\\_projects\\superresolve\\";
    // String inputPath = directory + S2A_MSIL1C_20230627T105621_N0509_R094_T30SVG_20230627T162159.znap.zip";
    String inputPath = directory + "S2B_MSIL1C_20230709T104629_N0509_R051_T30SWG_20230709T125343.SAFE.zip"; // no data
    IOPath ioPath = new IOPath(inputPath);
    // run(inputPath,
    //     "D:\\EOData\\_projects\\superresolve" + "\\S2A_MSIL1C_20230627T105621_superresolve_region1.znap.zip",
    //     (input, output) -> superRes(input, output,
    //                                 Map.of("pixelRegion", "0,0,1000,1000")));
    // run(inputPath,
    //     "D:\\EOData\\_projects\\superresolve" + "\\S2A_MSIL1C_20230627T105621_superresolve_region2.znap.zip",
    //     (input, output) -> superRes(input, output,
    //                                 Map.of("pixelRegion", "500,500,1000,1000",
    //                                        "superResolveBands", "B2,B3,B4",
    //                                        "resample60mBands", "true",
    //                                        "resampleGeometryBands", "true",
    //                                        "resampleOtherBands", "false",
    //                                        "debug", false)));
    // run(inputPath,
    //     "D:\\EOData\\_projects\\superresolve" + "\\S2A_MSIL1C_20230627T105621_superresolve_region3.znap.zip",
    //     (input, output) -> superRes(input, output,
    //                                 Map.of("pixelRegion", "5,5,500,500",
    //                                        "resample60mBands", "true",
    //                                        "resampleGeometryBands", "false",
    //                                        "resampleOtherBands", "true"
    //                                 )));
    // run(inputPath,
    //     "D:\\EOData\\_projects\\superresolve" + "\\S2A_MSIL1C_20230627T105621_superresolve_region4.znap.zip",
    //     (input, output) -> superRes(input, output,
    //                                 Map.of("pixelRegion", "10500,10500,480,480",
    //                                        "superResolveBands", "B2,B3,B4"
    //                                 )));
    // Preferences config = Config.instance("snap").load().preferences();
    // config.put("znap.use.zip.archive", "false");
    run(ioPath, (path) -> superRes(ioPath,
                                   Map.of("pixelRegion", "2000,4000,1000,1000",
                                          "resample60mBands", "true",
                                          "resampleGeometryBands", "true",
                                          "resampleOtherBands", "true"
                                   )));
  }


  private static void superRes(IOPath ioPath, Map<String, Object> parameters) throws IOException {
    Product inputProduct = ProductIO.readProduct(ioPath.getInputPath());
    Product resultProduct = GPF.createProduct("S2SuperRes", parameters, inputProduct);
    File outFile = new File(ioPath.getOutputPath());
    GPF.writeProduct(resultProduct, outFile, ioPath.getFormatName(), true, false,
                     new PrintWriterProgressMonitor(System.out));
    // GPF.writeProduct(resultProduct, new File(outPath), "GeoTIFF-BigTIFF", true, false, new PrintWriterProgressMonitor(System.out));
    // GPF.writeProduct(resultProduct, new File(outPath), "NetCDF4-CF", true, false, new PrintWriterProgressMonitor(System.out));
    // ProductIO.writeProduct(resultProduct, outFile, "ZNAP", false, new PrintWriterProgressMonitor(System.out));
  }

  private static void run(IOPath ioPath, Process process) throws Exception {
    Instant start = now();
    System.out.println("Start: " + start.atZone(java.time.ZoneId.systemDefault()));

    process.process(ioPath);

    Instant end = now();
    Duration duration = Duration.between(start, end);
    System.out.println("Duration: " + format(duration));
    System.out.printf("Written: %s%n", ioPath.getOutputPath());
  }

  /**
   * Formats a given {@link Duration} object into a string representation in the format of "H:MM:SS".
   *
   * @param duration The {@link Duration} object to be formatted.
   * @return A string representing the formatted duration in the "H:MM:SS" format.
   */
  private static String format(Duration duration) {
    return String.format("%d:%02d:%02d",
                         duration.toHours(),
                         duration.toMinutesPart(),
                         duration.toSecondsPart());
  }

  private interface Process {

    void process(IOPath ioPath) throws Exception;
  }

  private static void initSnapAndGpf() {
    if (System.getProperty("snap.context") == null) {
      System.setProperty("snap.context", "snap");
    }
    Locale.setDefault(Locale.ENGLISH); // Force usage of english locale
    SystemUtils.init3rdPartyLibs(GPT.class);
    // HdfActivator.activate();
    NetCdfActivator.activate();
  }

  private static class IOPath {

    private final String inputPath;

    public IOPath(String inputPath) {
      this.inputPath = inputPath;
    }

    public String getInputPath() {
      return inputPath;
    }

    public String getOutputPath() {
      Path path = Paths.get(inputPath);
      String fileName = path.getFileName().toString();
      path = path.getParent();
      Path outputFilePath = path.resolve(fileName.substring(0, 27) + "superresolve" + ".znap.zip");
      return outputFilePath.toAbsolutePath().toString();
    }

    public String getFormatName() {
      return "ZNAP";
    }
  }
}
