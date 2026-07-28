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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.esa.snap.core.datamodel.GeoPos;
import org.junit.jupiter.api.Test;

class GeoPosConverterTest {

  @Test
  public void testParse() {
    GeoPosConverter converter = new GeoPosConverter();
    GeoPos geoPos = converter.parse("23.8948, -114.96");
    assertNotNull(geoPos);
    assertEquals(23.8948, geoPos.getLat());
    assertEquals(-114.96, geoPos.getLon());
  }

  @Test
  public void testFormat() {
    GeoPosConverter converter = new GeoPosConverter();
    GeoPos geoPos = new GeoPos(23.8948, -114.96);
    String formatted = converter.format(geoPos);
    assertEquals("23.894800, -114.960000", formatted);
  }

  @Test
  public void testFormatNull() {
    GeoPosConverter converter = new GeoPosConverter();
    String formatted = converter.format(null);
    assertEquals("N/A", formatted);
  }

  @Test
  public void testParseNotAvailable() {
    GeoPosConverter converter = new GeoPosConverter();
    GeoPos geopos = converter.parse("N/A");
    assertNull(geopos);
  }

  @Test
  public void testFormatINvalid() {
    GeoPosConverter converter = new GeoPosConverter();
    GeoPos geoPos = new GeoPos();
    geoPos.setInvalid();
    String formatted = converter.format(geoPos);
    assertEquals("INV", formatted);
  }

  @Test
  public void testParseInvalid() {
    GeoPosConverter converter = new GeoPosConverter();
    GeoPos geopos = converter.parse("INV");
    assertFalse(geopos.isValid());
  }

  @Test
  public void testGetValueType() {
    GeoPosConverter converter = new GeoPosConverter();
    assertEquals(GeoPos.class, converter.getValueType());
  }
}
