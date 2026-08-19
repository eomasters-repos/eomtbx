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
import org.eomasters.eomtbx.cmap.FlagMask;

public class MaskModel {

  final String name;
  Color color;
  double transparency;
  boolean selected;

  public MaskModel(FlagMask mask) {
    name = mask.getMaskName();
    color = mask.getMaskColor();
    transparency = mask.getMaskTransparency();
    selected = false;
  }

  public String getName() {
    return name;
  }

  public Color getColor() {
    return color;
  }

  public double getTransparency() {
    return transparency;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public void setTransparency(double transparency) {
    this.transparency = transparency;
  }

  public boolean isSelected() {
    return selected;
  }

  public void store() {

  }
}
