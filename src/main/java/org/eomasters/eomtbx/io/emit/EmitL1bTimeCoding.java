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

import java.awt.Rectangle;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.ProductData.UTC;
import org.esa.snap.core.datamodel.TimeCoding;

public class EmitL1bTimeCoding implements TimeCoding {

  private final Band timeBand;
  private final Rectangle rasterSize;
  private final long startDateMillis;

  public EmitL1bTimeCoding(Band timeBand) {
    this.timeBand = timeBand;
    this.rasterSize = new Rectangle(timeBand.getRasterSize());
    UTC startTime = timeBand.getProduct().getStartTime();
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    calendar.setTime(startTime.getAsDate());
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
    calendar.set(java.util.Calendar.MINUTE, 0);
    calendar.set(java.util.Calendar.SECOND, 0);
    calendar.set(java.util.Calendar.MILLISECOND, 0);
    startDateMillis = calendar.getTime().getTime();
  }

  @Override
  public double getMJD(PixelPos pixelPos) {
    if (!this.rasterSize.contains(pixelPos)) {
      return Double.NaN;
    }
    double timeOfDay = timeBand.getPixelDouble((int) pixelPos.x, (int) pixelPos.y);

    long timeOfDayInMillis = (long) (timeOfDay * 3600 * 1000);
    UTC dateTime = new UTC(startDateMillis + timeOfDayInMillis);
    return dateTime.getMJD();
  }


}
