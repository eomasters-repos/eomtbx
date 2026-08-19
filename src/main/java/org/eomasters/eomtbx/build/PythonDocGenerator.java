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

package org.eomasters.eomtbx.build;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.file.PathUtils;
import org.eomasters.eomtbx.utils.FileUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

/**
 * Java wrapper for the Python documentation generator. Uses GraalVM Python to execute the documentation generation
 * script.
 */
public class PythonDocGenerator {

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("Usage: PythonDocGenerator <source_dir> <output_dir>");
      System.exit(1);
    }

    String sourceDir = args[0];
    String outputDir = args[1];

    // Verify source directory exists
    Path sourcePath = Paths.get(sourceDir);
    if (!Files.exists(sourcePath)) {
      System.err.println("Error: Source directory '" + sourceDir + "' does not exist");
      System.exit(1);
    }

    var outputPath = Path.of(outputDir);
    if (Files.isDirectory(outputPath)) {
      PathUtils.deleteDirectory(outputPath);
    }
    try {
      Files.createDirectories(outputPath);
    } catch (IOException e) {
      System.err.println("Error creating output directory: " + e.getMessage());
      e.printStackTrace();
    }

    try {
      generateDocs(sourceDir, outputDir);
      System.out.println("Python documentation generated successfully!");
    } catch (Exception e) {
      System.err.println("Error generating documentation: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void generateDocs(String sourceDir, String outputDir) throws IOException, URISyntaxException {
    // Read the Python script
    var scriptResource = PythonDocGenerator.class.getResource("/scripts/generate_python_docs.py");
    Path scriptPath = FileUtils.getPath(scriptResource.toURI());
    if (!Files.exists(scriptPath)) {
      throw new IOException("Python script not found: " + scriptPath);
    }

    String pythonScript = Files.readString(scriptPath);

    // Create GraalVM Python context
    try (Context context = Context.newBuilder("python")
                                  .allowAllAccess(true)
                                  .allowIO(true)
                                  .build()) {

      // Set up sys.argv for the Python script safely via bindings
      context.getBindings("python").putMember("argv", new String[]{"generate_python_docs.py", sourceDir, outputDir});
      context.eval("python", "import sys\nsys.argv = argv");

      // Execute the Python script
      Source source = Source.newBuilder("python", pythonScript, "generate_python_docs.py").build();
      context.eval(source);
    }
  }
}
