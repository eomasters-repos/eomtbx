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

import com.bc.ceres.swing.figure.support.DefaultFigureStyle;
import java.awt.Color;
import org.esa.snap.core.datamodel.CrsGeoCoding;
import org.esa.snap.core.datamodel.PinDescriptor;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Placemark;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.jupiter.api.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

public class SiteStyleTest {

  @Test
  public void testFromText() {
    SiteStyle style = new SiteStyle();

    style.setStyleFromText("stroke:255,0,0;fill:0,0,255;symbol:star");
    assertEquals(Color.RED, style.getStyle(SiteStyle.STROKE));
    assertEquals(Color.BLUE, style.getStyle(SiteStyle.FILL));
    assertEquals("star", style.getStyle(SiteStyle.SYMBOL));

    style.setStyle("stroke-width", 1.2);
    assertEquals(1.2, style.getStyle(SiteStyle.STROKE_WIDTH), 0.01);
    style.setStyle("fill", new Color(255, 0, 160));
    style.setStyle("fill-opacity", 0.6);

    String styleAsText = style.toCssString();
    assertEquals("fill:#ff00a0; fill-opacity:0.6; stroke:#ff0000; stroke-width:1.2; symbol:star", styleAsText);
  }

  @Test
  public void testToText() {
    SiteStyle style = new SiteStyle();

    String styleAsText = style.toCssString();
    assertEquals("fill:#ffff00; fill-opacity:1.0; stroke:#ffffff; stroke-width:0.5; symbol:cross", styleAsText);
  }

  @Test
  void ensureCompatibleWithPlacemark() throws FactoryException, TransformException {
    CrsGeoCoding geoCoding = new CrsGeoCoding(DefaultGeographicCRS.WGS84, 50, 50, 0, 0, 1, 1);
    Placemark placemark = Placemark.createPointPlacemark(PinDescriptor.getInstance(),
        "Name", "label", "description", new PixelPos(10, 5),
        null, geoCoding
        );
    placemark.setStyleCss(new SiteStyle().toCssString());
    DefaultFigureStyle.createFromCss(placemark.getStyleCss());
  }
}
