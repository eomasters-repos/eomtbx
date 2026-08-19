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

package org.eomasters.eomtbx.cmap.gui;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eomasters.eomtbx.cmap.CoastalMap;
import org.eomasters.eomtbx.cmap.FlagMask;

public class MaskOptionsModel implements Iterable<MaskModel> {

  private static final CoastalMap MAP = CoastalMap.getInstance();

  Map<String, MaskModel> masks;
  private boolean isChanged;

  public MaskOptionsModel(FlagMask[] mask) {
    masks = new LinkedHashMap<>();
    for (FlagMask flagMask : mask) {
      masks.put(flagMask.getMaskName(), new MaskModel(flagMask));
    }
  }

  public Color getColor(String maskName) {
    return masks.get(maskName).getColor();
  }

  public double getTransparency(String maskName) {
    return masks.get(maskName).getTransparency();
  }

  public void setColor(String maskName, Color color) {
    masks.get(maskName).setColor(color);
    isChanged = true;
  }

  public void setTransparency(String maskName, double transparency) {
    masks.get(maskName).setTransparency(transparency);
    isChanged = true;
  }

  @Override
  public Iterator<MaskModel> iterator() {
    return masks.values().iterator();
  }

  public void store() {
    for (MaskModel maskModel : this) {
      MAP.updatePreferences(maskModel);
    }
  }

  public boolean isChanged() {
    return isChanged;
  }
}
