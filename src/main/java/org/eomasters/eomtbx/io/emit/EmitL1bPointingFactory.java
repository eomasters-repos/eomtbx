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

import static org.eomasters.eomtbx.io.emit.EmitL1bReaderPlugin.EMIT_L1B_FORMAT;

import org.esa.snap.core.datamodel.AngularDirection;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Pointing;
import org.esa.snap.core.datamodel.PointingFactory;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.RasterDataNode;

public class EmitL1bPointingFactory implements PointingFactory {

  private final Product emitL1b;

  public EmitL1bPointingFactory(Product emitL1b) {
    this.emitL1b = emitL1b;
  }

  @Override
  public String[] getSupportedProductTypes() {
    return new String[]{EMIT_L1B_FORMAT};
  }

  @Override
  public Pointing createPointing(RasterDataNode raster) {
    return new EmitPointing(emitL1b);
  }

  private static class EmitPointing implements Pointing {

    private final Product emitL1b;

    public EmitPointing(Product emitL1b) {
      this.emitL1b = emitL1b;
    }

    @Override
    public GeoCoding getGeoCoding() {
      return emitL1b.getSceneGeoCoding();
    }

    @Override
    public AngularDirection getSunDir(PixelPos pixelPos, AngularDirection angularDirection) {
      int posX = (int) pixelPos.x;
      int posY = (int) pixelPos.y;
      Band sunAzimuthBand = emitL1b.getBand(EmitL1bConstants.OBS_SUN_AZIMUTH);
      Band sunZenithBand = emitL1b.getBand(EmitL1bConstants.OBS_SUN_ZENITH);
      angularDirection.azimuth = sunAzimuthBand.getPixelDouble(posX, posY);
      angularDirection.zenith = sunZenithBand.getPixelDouble(posX, posY);
      return angularDirection;
    }

    @Override
    public AngularDirection getViewDir(PixelPos pixelPos, AngularDirection angularDirection) {
      int posX = (int) pixelPos.x;
      int posY = (int) pixelPos.y;
      Band sensorAzimuthBand = emitL1b.getBand(EmitL1bConstants.OBS_SENSOR_AZIMUTH);
      Band sensorZenithBand = emitL1b.getBand(EmitL1bConstants.OBS_SENSOR_ZENITH);
      angularDirection.azimuth = sensorAzimuthBand.getPixelDouble(posX, posY);
      angularDirection.zenith = sensorZenithBand.getPixelDouble(posX, posY);
      return angularDirection;
    }

    @Override
    public double getElevation(PixelPos pixelPos) {
      Band elevBand = emitL1b.getBand(EmitConstants.ELEV_VARIABLE);
      return elevBand.getPixelDouble((int) pixelPos.x, (int) pixelPos.y);
    }

    @Override
    public boolean canGetSunDir() {
      return true;
    }

    @Override
    public boolean canGetViewDir() {
      return true;
    }

    @Override
    public boolean canGetElevation() {
      return true;
    }
  }
}
