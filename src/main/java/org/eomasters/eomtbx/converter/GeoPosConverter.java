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

package org.eomasters.eomtbx.converter;

import com.bc.ceres.binding.Converter;
import java.util.Locale;
import org.esa.snap.core.datamodel.GeoPos;

/**
 * A converter implementation for the class {@link GeoPos}. The latitude and longitude values are converted as comma
 * separated tuple of doubles (lat, lon). Example, '23.8948, -114.96'. If a {@code null} object is provided it will be
 * represented as 'N/A'. In case the geo-position is invalid the text 'INV' will be used.
 */
public class GeoPosConverter implements Converter<GeoPos> {

  public static final String NOT_AVAILABLE = "N/A";
  private static final String INVALID = "INV";
  private static final GeoPos INVALID_GEOPOS;

  static {
    INVALID_GEOPOS = new GeoPos();
    INVALID_GEOPOS.setInvalid();
  }

  @Override
  public Class<? extends GeoPos> getValueType() {
    return GeoPos.class;
  }

  @Override
  public GeoPos parse(final String text) {
    if (NOT_AVAILABLE.equals(text)) {
      return null;
    }
    if (INVALID.equals(text)) {
      return INVALID_GEOPOS;
    }
    final String[] split = text.split(",");
    double lat = Double.parseDouble(split[0].trim());
    double lon = Double.parseDouble(split[1].trim());
    return new GeoPos(lat, lon);
  }

  @Override
  public String format(final GeoPos value) {
    if (value == null) {
      return NOT_AVAILABLE;
    }
    if (!value.isValid()) {
      return INVALID;
    }
    final double lat = value.getLat();
    final double lon = value.getLon();
    return String.format(Locale.ENGLISH, "%f, %f", lat, lon);
  }
}
