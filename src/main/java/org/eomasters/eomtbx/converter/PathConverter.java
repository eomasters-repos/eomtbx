/*-
 * ========================LICENSE_START=================================
 * EOM Commons SNAP - Library of common utilities for ESA SNAP
 * -> https://www.eomasters.org/
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
import java.nio.file.Path;

// TODO - Can be removed with SNAP 11
public class PathConverter implements Converter<Path> {

  @Override
  public Class<Path> getValueType() {
    return Path.class;
  }

  @Override
  public Path parse(String text) {
    if (text.isEmpty()) {
      return null;
    }
    return Path.of(text);
  }

  @Override
  public String format(Path value) {
    if (value == null) {
      return "";
    }
    return value.toAbsolutePath().toString();
  }
}
