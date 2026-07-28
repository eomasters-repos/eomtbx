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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CsvSiteReaderTest {

  @Test
  public void testReadSites() throws Exception {
    @SuppressWarnings("DataFlowIssue")
    Path filePath = Paths.get( getClass().getResource("test_csv_TechSites.txt").toURI());

    List<Site> sites = CsvSiteReader.readSites(filePath, ',');

    assertNotNull(sites);
    assertEquals(6, sites.size());

    // Check the first site
    Site site = sites.getFirst();
    assertEquals("Google", site.getName());
    assertEquals(37.4220, site.getX(), 0.0001);
    assertEquals(-122.0841, site.getY(), 0.0001);
    assertEquals("Large tech company", site.getDescription());
    assertEquals("EPSG:4326", site.getCrs());
    assertEquals(SiteStyle.create("fill:#c80014; fill-opacity:1.0; stroke:#ff0000; stroke-width:0.5; symbol:cross"), site.getStyle());

    // Check the second site
    site = sites.get(1);
    assertEquals("Facebook", site.getName());
    assertEquals(37.4842, site.getX(), 0.0001);
    assertEquals(-122.1484, site.getY(), 0.0001);
    assertEquals("Social media giant", site.getDescription());
    assertEquals("EPSG:4326", site.getCrs());

    // Check the third site
    site = sites.get(2);
    assertEquals("Amazon", site.getName());
    assertEquals(47.6062, site.getX(), 0.0001);
    assertEquals(-122.3321, site.getY(), 0.0001);

    // Check the fourth site
    site = sites.get(3);
    assertEquals("Netflix_WGS", site.getName());
    assertEquals(37.2350, site.getX(), 0.0001);
    assertEquals(-121.9623, site.getY(), 0.0001);
    assertEquals("Streaming service", site.getDescription());
    assertEquals("EPSG:4326", site.getCrs());
    assertEquals(SiteStyle.create("stroke:127,127,0;fill:0,0,255;symbol:circle"), site.getStyle());

    // Check the fifth site
    site = sites.get(4);
    assertEquals("Netflix_UTM", site.getName());
    assertEquals(4121447, site.getX(), 0.0001);
    assertEquals(592047, site.getY(), 0.0001);
    assertEquals("Streaming service", site.getDescription());
    assertEquals("EPSG:32610", site.getCrs());
    assertEquals(SiteStyle.create("stroke:127,127,0;fill:0,0,255;symbol:cross"), site.getStyle());

    // Check the sixth site
    site = sites.get(5);
    assertEquals("Apple", site.getName());
    assertEquals(37.3229, site.getX(), 0.0001);
    assertEquals(-122.0322, site.getY(), 0.0001);
    assertEquals("Consumer electronics company", site.getDescription());
    assertEquals("EPSG:4326", site.getCrs());
    assertEquals(SiteStyle.create("stroke:255,0,0;fill:0,0,255;symbol:star"), site.getStyle());
  }

  @Test
  public void testWithWrongSeparator() throws Exception {
    @SuppressWarnings("DataFlowIssue") Path filePath = Paths.get( getClass().getResource("test_csv_TechSites.txt").toURI());

    try {
      CsvSiteReader.readSites(filePath, '\t');
      fail("Should have thrown an exception; uses wrong separator");
    } catch (IOException e) {
      assertTrue(e.getMessage().toLowerCase().contains("correct separator"));
    }
  }
}
