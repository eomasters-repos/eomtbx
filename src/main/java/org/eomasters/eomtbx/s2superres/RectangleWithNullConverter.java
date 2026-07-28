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

import com.bc.ceres.binding.ConversionException;
import com.bc.ceres.binding.Converter;
import java.awt.Rectangle;
import org.esa.snap.core.util.StringUtils;

// Copy of org.esa.snap.core.util.converters.RectangleConverter but allows a null value
public class RectangleWithNullConverter implements Converter<Rectangle> {


  private static final String EXCEPTION_FORMAT_PATTERN = "Invalid Rectangle '%s'. should be in form of x,y,width,height";

  @Override
  public Class<Rectangle> getValueType() {
    return Rectangle.class;
  }

  @Override
  public Rectangle parse(String text) throws ConversionException {
    if (text == null) {
      return null;
    }
    text = text.trim();
    if (text.isEmpty()) {
      return null;
    }
    final String[] coordinates = StringUtils.csvToArray(text);
    if (coordinates.length != 4) {
      throw new ConversionException(String.format(EXCEPTION_FORMAT_PATTERN, text));
    }
    return new Rectangle(Integer.parseInt(coordinates[0].trim()), Integer.parseInt(coordinates[1].trim()),
                         Integer.parseInt(coordinates[2].trim()), Integer.parseInt(coordinates[3].trim()));
  }

  @Override
  public String format(final Rectangle r) {
    if (r == null) {
      return null;
    }
    return String.format("%d,%d,%d,%d", r.x, r.y, r.width, r.height);
  }
}
