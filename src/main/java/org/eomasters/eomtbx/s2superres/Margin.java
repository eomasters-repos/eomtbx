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

package org.eomasters.eomtbx.s2superres;

import java.awt.Rectangle;

/**
 * Represents the margins (top, left, right, bottom) around a region of interest (ROI) within
 * a given scene. The margin values are typically calculated to ensure a specified minimum
 * distance between the ROI and the boundaries of the containing scene.
 */
public class Margin {

  private int top;
  private int left;
  private int right;
  private int bottom;


  private Margin() {
    this.top = 0;
    this.left = 0;
    this.right = 0;
    this.bottom = 0;
  }

  /**
   * Creates a {@code Margin} object representing the computed margins (top, left, right, bottom)
   * for a specified region of interest (ROI) within a scene, ensuring that all margins meet
   * a specified minimum value.
   *
   * @param scene the rectangle representing the entire scene, which should always originate at (0, 0)
   * @param roi the rectangle representing the region of interest within the scene
   * @param minMargin the minimum allowed margin value. Any margin smaller than this will be adjusted to meet or exceed this value
   * @return a {@code Margin} object representing the calculated margin values
   * @throws IllegalArgumentException if the scene origin is not at (0, 0) or if the scene does not fully contain the ROI
   */
  public static Margin create(Rectangle scene, Rectangle roi, int minMargin) {
    if (scene.x != 0 || scene.y != 0) {
      throw new IllegalArgumentException("Scene origin must be at [0,0]");
    }
    if (!scene.contains(roi)) {
      throw new IllegalArgumentException("Scene must contain roi");
    }

    Margin margin = new Margin();
    int topMargin = roi.y - scene.x;
    if (topMargin < minMargin) {
      margin.top = minMargin - topMargin;
    }

    int leftMargin = roi.x - scene.y;
    if (leftMargin < minMargin) {
      margin.left = minMargin - leftMargin;
    }

    int rightMargin = (scene.x + scene.width) - (roi.x + roi.width);
    if (rightMargin < minMargin) {
      margin.right = minMargin - rightMargin;
    }

    int bottomMargin = (scene.y + scene.height) - (roi.y + roi.height);
    if (bottomMargin < minMargin) {
      margin.bottom = minMargin - bottomMargin;
    }

    return margin;
  }

  public int getTop() {
    return top;
  }

  public int getLeft() {
    return left;
  }

  public int getRight() {
    return right;
  }

  public int getBottom() {
    return bottom;
  }

}
