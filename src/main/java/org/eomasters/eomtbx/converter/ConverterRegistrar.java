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

import com.bc.ceres.binding.ConverterRegistry;
import java.nio.file.Path;
import org.esa.snap.core.datamodel.GeoPos;

/**
 * This class registers the known converters.
 */
public class ConverterRegistrar {

  public static void registerConverter() {
    final ConverterRegistry registry = ConverterRegistry.getInstance();
    registry.setConverter(GeoPos.class, new GeoPosConverter());
    registry.setConverter(Path.class, new PathConverter());
  }

  public static void deregisterConverter() {
    final ConverterRegistry registry = ConverterRegistry.getInstance();
    registry.setConverter(GeoPos.class, null);
    registry.setConverter(Path.class, null);
  }

}
