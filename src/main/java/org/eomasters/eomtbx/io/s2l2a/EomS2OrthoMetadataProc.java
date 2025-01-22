/*-
 * ========================LICENSE_START=================================
 * EOMTBXP Toolbox Module - Toolbox Module for the EOMasters Pro Toolbox for SNAP
 * -> https://www.eomasters.org/eomtbx/modules/eomtbxp-toolbox
 * ======================================================================
 * Copyright (C) 2023 - 2025 Marco Peters
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

package org.eomasters.eomtbx.io.s2l2a;

import static java.awt.Color.getHSBColor;

import eu.esa.opt.dataio.s2.S2IndexBandInformation;
import eu.esa.opt.dataio.s2.S2MetadataProc;
import eu.esa.opt.dataio.s2.S2SpatialResolution;
import eu.esa.opt.dataio.s2.ortho.filepatterns.S2OrthoGranuleDirFilename;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by obarrile on 06/07/2016.
 */
public class EomS2OrthoMetadataProc extends S2MetadataProc {

  public static S2IndexBandInformation makeTileInformation(S2SpatialResolution resolution,
                                                           EomS2OrthoSceneLayout sceneDescription) {
    List<S2IndexBandInformation.S2IndexBandIndex> indexList = new ArrayList<>();
    int numberOfTiles = sceneDescription.getOrderedTileIds().size();
    int index = 1;
    for (String tileId : sceneDescription.getOrderedTileIds()) {
      float f;
      f = (index - 1) * (float) 1.0 / (numberOfTiles + 1);
      f = (float) 0.75 - f;
      if (f < 0) {
        f++;
      }
      if (S2OrthoGranuleDirFilename.create(tileId).tileNumber != null) {
        indexList.add(S2IndexBandInformation.makeIndex(index, getHSBColor(f, (float) 1.0, (float) 1.0),
                                                       S2OrthoGranuleDirFilename.create(tileId).getTileID(), tileId));
      } else {
        indexList.add(
            S2IndexBandInformation.makeIndex(index, getHSBColor(f, (float) 1.0, (float) 1.0), tileId, tileId));
      }
      index++;
    }
    return new S2IndexBandInformation("tile_id_" + resolution.resolution + "m", resolution, "", "Tile ID", "",
                                      indexList, "tile_" + resolution.resolution + "m_");
  }
}
