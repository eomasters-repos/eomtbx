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

public class EmitL1bConstants extends EmitConstants {

  static String OBS_PATH_LENGTH = "obs_path_length";
  static String OBS_SENSOR_AZIMUTH = "obs_sensor_azimuth";
  static String OBS_SENSOR_ZENITH = "obs_sensor_zenith";
  static String OBS_SUN_AZIMUTH = "obs_sun_azimuth";
  static String OBS_SUN_ZENITH = "obs_sun_zenith";
  static String OBS_SOLAR_PHASE = "obs_solar_phase";
  static String OBS_SLOPE = "obs_slope";
  static String OBS_ASPECT = "obs_aspect";
  static String OBS_COSINE = "obs_cosine";
  static String OBS_TIME = "obs_time";
  static String OBS_SUN_DISTANCE = "obs_sun_distance";
  static String[] OBSERVATION_BAND_NAMES = new String[]{OBS_PATH_LENGTH, OBS_SENSOR_AZIMUTH, OBS_SENSOR_ZENITH,
      OBS_SUN_AZIMUTH, OBS_SUN_ZENITH, OBS_SOLAR_PHASE, OBS_SLOPE, OBS_ASPECT, OBS_COSINE, OBS_TIME, OBS_SUN_DISTANCE};

  static String OBS_VARIABLE = "obs";
  static String RADIANCE_VARIABLE = "radiance";
  static String OBSERVATION_BANDS_VARIABLE = "observation_bands";
}
