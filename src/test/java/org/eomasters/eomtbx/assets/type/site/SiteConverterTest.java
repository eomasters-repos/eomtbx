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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bc.ceres.binding.ConversionException;
import org.junit.jupiter.api.Test;

class SiteConverterTest {

  @Test
  void testConvert_Simple() throws ConversionException {
    Site site = new Site("test", 0.0, 0.0, "test", "test");
    Site.Converter converter = new Site.Converter();
    String formatted = converter.format(site);

    System.out.println(formatted);

    Site converted = converter.parse(formatted);

    System.out.println(converted);

    assertEquals(site, converted);
  }

  @Test
  void testConvert_WithComma() throws ConversionException {
    Site site = new Site("test", 123.0, 123.45678910, "test, and more words", "test,=");
    site.setStyle(SiteStyle.create("fill:#ffff00; fill-opacity:1.0; stroke:#ffffff; stroke-width:0.5; symbol:cross"));
    Site.Converter converter = new Site.Converter();
    String formatted = converter.format(site);

    System.out.println(formatted);

    Site converted = converter.parse(formatted);

    System.out.println(converted);
    assertEquals("test", converted.getName());
    assertEquals(123.0, converted.getX(), 1.0e-8);
    assertEquals(123.45678910, converted.getY(), 1.0e-8);
    assertEquals("test, and more words", converted.getDescription());
    assertEquals("test,=", converted.getCrs());
    assertEquals("fill:#ffff00; fill-opacity:1.0; stroke:#ffffff; stroke-width:0.5; symbol:cross",
        converted.getStyle().toCssString());

    assertEquals(site, converted);
  }

  @Test
  void testConvert_WithSingleQuote() throws ConversionException {
    Site site = new Site("test", 1, 2, "What's this? Can''t be correct", "CRS");
    site.setStyle(SiteStyle.create("fill:#ffff00; fill-opacity:1.0; stroke:#ffffff; stroke-width:0.5; symbol:cross"));
    Site.Converter converter = new Site.Converter();
    String formatted = converter.format(site);

    System.out.println(formatted);

    Site converted = converter.parse(formatted);

    System.out.println(converted);
    assertEquals("test", converted.getName());
    assertEquals(1, converted.getX(), 1.0e-8);
    assertEquals(2, converted.getY(), 1.0e-8);
    assertEquals("What's this? Can''t be correct", converted.getDescription());
    assertEquals("CRS", converted.getCrs());
    assertEquals("fill:#ffff00; fill-opacity:1.0; stroke:#ffffff; stroke-width:0.5; symbol:cross",
        converted.getStyle().toCssString());

    assertEquals(site, converted);
  }

}
