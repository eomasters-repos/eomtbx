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

package org.eomasters.eomtbx.io.emit;

import java.awt.Dimension;
import java.time.format.DateTimeFormatter;

public class EmitConstants {

  static String CROSSTRACK_DIMENSION = "crosstrack";
  static String DOWNTRACK_DIMENSION = "downtrack";
  static String UNITS_ATTRIBUTE = "units";
  static String LOCATION_GROUP = "location";
  static String SENSOR_BAND_PARAMETERS_GROUP = "sensor_band_parameters";
  static String WAVELENGTHS_VARIABLE = "wavelengths";
  static String FWHM_VARIABLE = "fwhm";
  static String LON_VARIABLE = "lon";
  static String LAT_VARIABLE = "lat";
  static String ELEV_VARIABLE = "elev";
  static String TITLE_ATTRIBUTE = "title";
  static String TIME_COVERAGE_START_ATTRIBUTE = "time_coverage_start";
  static String TIME_COVERAGE_END_ATTRIBUTE = "time_coverage_end";
  static double RASTER_RESOLUTION_IN_KM = 0.06; // 60m
  static Dimension PREFERRED_TILE_SIZE = new Dimension(128, 128);
  static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
}
