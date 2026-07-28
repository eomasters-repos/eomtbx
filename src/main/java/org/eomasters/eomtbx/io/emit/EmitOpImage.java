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
import org.esa.snap.core.image.ResolutionLevel;
import org.esa.snap.dataio.netcdf.util.NetcdfOpImage;
import ucar.nc2.Variable;

public class EmitOpImage extends NetcdfOpImage {

  public EmitOpImage(Variable variable, int[] imageOrigin, boolean flipY, Object readLock, int dataBufferType,
                        int sourceWidth, int sourceHeight, Dimension tileSize,
                        ResolutionLevel level) {
    super(variable, imageOrigin, flipY, readLock, dataBufferType, sourceWidth, sourceHeight, tileSize, level,
          ArrayConverter.IDENTITY, new DimensionIndices(1, 0, 0));
  }
}
