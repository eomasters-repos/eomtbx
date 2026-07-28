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

package org.eomasters.eomtbx.assets.type.site;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class for reading site data from a CSV file.
 * <p>
 * The CSV file must have the following format:
 * <p>
 * Each line of the file represents a site. Each site record consists of the following columns:
 * <ul>
 *   <li>Name: The name of the site.</li>
 *   <li>X coordinate: The X coordinate of the site.</li>
 *   <li>Y coordinate: The Y coordinate of the site.</li>
 * </ul>
 * Optional: Additional columns may be present for the site's description, CRS, and style.
 * <p>
 * Lines can be marked as comment by starting this line with the # character.
 * The columns within each record are separated by the separator character which can be any character. The reader is
 * configured with the separator as a parameter.
 * If the CSV separator is also used within one of the column values, then it must be enclosed in double quotes.
 */
public class CsvSiteReader {

  /**
   * Prefix used to identify comments in the CSV file.
   */
  private static final String COMMENT_PREFIX = "#";
  private static final int NUM_MANDATORY_COLUMNS = 3;

  /**
   * Reads site data from a CSV file.
   *
   * @param filePath  The path to the CSV file.
   * @param separator The separator used to split the columns in the CSV file.
   * @return A list of `Site` objects representing the site data read from the file.
   * @throws IOException If an I/O error occurs while reading the file.
   */
  public static List<Site> readSites(Path filePath, char separator) throws IOException {
    List<Site> sites = new ArrayList<>();

    try (BufferedReader reader = Files.newBufferedReader(filePath)) {
      List<String> lines = reader.lines().collect(Collectors.toList());

      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i);
        if (line.startsWith(COMMENT_PREFIX)) {
          continue;
        }
        String[] columns = getColumns(separator, line);

        if (columns.length < NUM_MANDATORY_COLUMNS) {
          // Skip if the record doesn't have the minimum required columns
          throw new IOException(String.format("Invalid record @Line %d in CSV file: %d columns are mandatory. "
              + "Correct separator used?", i, NUM_MANDATORY_COLUMNS));
        }

        String name = columns[0];
        double x = Double.parseDouble(columns[1]);
        double y = Double.parseDouble(columns[2]);
        Site site = new Site(name, x, y);
        if (columns.length > NUM_MANDATORY_COLUMNS) {
          site.setDescription(columns[NUM_MANDATORY_COLUMNS]);
        }
        if (columns.length > 4) {
          site.setCrs(columns[4]);
        }
        if (columns.length > 5) {
          site.setStyle(SiteStyle.create(columns[5]));
        }

        sites.add(site);
      }
    }

    return sites;
  }

  /**
   * Splits a line of text into columns using the given separator, but not if the separator appears within a quoted
   * string.
   *
   * @param separator The separator used to split the columns.
   * @param line      The line of text to split.
   * @return An array of strings representing the columns.
   */
  private static String[] getColumns(char separator, String line) {
    List<String> columnList = new ArrayList<>();
    boolean quoted = false;
    StringBuilder currentColumn = new StringBuilder();
    for (char c : line.toCharArray()) {
      if (c == '"') {
        quoted = !quoted;
      } else if ((c == separator) && !quoted) {
        columnList.add(currentColumn.toString());
        currentColumn.setLength(0);
      } else {
        currentColumn.append(c);
      }
    }
    if (currentColumn.length() > 0) {
      columnList.add(currentColumn.toString());
    }
    return columnList.toArray(new String[0]);

  }
}
