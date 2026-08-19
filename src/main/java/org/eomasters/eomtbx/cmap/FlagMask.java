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

import java.awt.Color;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.Mask;
import org.esa.snap.core.datamodel.Mask.BandMathsType;

public class FlagMask {

  private static final CoastalMap MAP = CoastalMap.getInstance();
  private final String flagBandName;
  private final String flagName;
  private final String maskName;
  private final String description;
  private final Color defaultColor;
  private final double defaultTransparency;
  private final int flagMask;
  private final int flagValue;

  public FlagMask(String flagBandName, String flagName, String description, int flagMask, int flagValue, Color defaultColor,
      double defaultTransparency) {
    this.flagBandName = flagBandName;
    this.flagName = flagName;
    this.maskName = "EOM_" + flagName;
    this.description = description;
    this.flagMask = flagMask;
    this.flagValue = flagValue;
    this.defaultColor = defaultColor;
    this.defaultTransparency = defaultTransparency;
  }

  public void addTo(FlagCoding flagCoding) {
    flagCoding.addFlag(flagName, flagMask, flagValue, description);
  }

  public Mask createMask(int width, int height) {
    return BandMathsType.create(maskName, description, width, height, flagBandName + "." + flagName,
        getMaskColor(), getMaskTransparency());
  }

  public String getMaskName() {
    return maskName;
  }

  public Color getMaskColor() {
    return MAP.getMaskColor(maskName, defaultColor);
  }

  public double getMaskTransparency() {
    return MAP.getMaskTransparency(maskName, defaultTransparency);
  }


  @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException", "MethodDoesntCallSuperMethod"})
  @Override
  protected Object clone(){
    return new FlagMask(flagBandName, flagName, description, flagMask, flagValue, defaultColor, defaultTransparency);
  }

}
