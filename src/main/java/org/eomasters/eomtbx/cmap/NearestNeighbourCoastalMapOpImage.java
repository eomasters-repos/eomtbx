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

package org.eomasters.eomtbx.cmap;

import java.io.IOException;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.datamodel.SceneFactory;
import org.esa.snap.core.image.RasterDataNodeSampleOpImage;
import org.esa.snap.core.image.ResolutionLevel;

class NearestNeighbourCoastalMapOpImage extends RasterDataNodeSampleOpImage {

  private final GeoCoding geoCoding;
  private final CoastalDataProvider provider;

  public NearestNeighbourCoastalMapOpImage(RasterDataNode rasterDataNode) throws IOException {
    super(rasterDataNode, ResolutionLevel.MAXRES);
    CoastalMap map = CoastalMap.getInstance();
    provider = new CoastalDataProvider(map);
    provider.init(SceneFactory.createScene(rasterDataNode));

    geoCoding = rasterDataNode.getGeoCoding();

  }

  @Override
  protected double computeSample(int x, int y) {
    try {
      GeoPos geoPos = geoCoding.getGeoPos(new PixelPos(x + 0.5, y + 0.5), null);
      return provider.getMapValue(geoPos.lon, geoPos.lat);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }
}
