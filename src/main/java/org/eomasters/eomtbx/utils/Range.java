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

package org.eomasters.eomtbx.utils;

/**
 * Represents a numerical range defined by a lower and an upper bound.
 */
public class Range {

  private final double lowerBound;
  private final double upperBound;

  /**
   * Constructs a Range object with a specified lower and upper bound.
   *
   * @param lowerBound The lower bound of the range.
   * @param upperBound The upper bound of the range.
   */
  public Range(double lowerBound, double upperBound) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  /**
   * Retrieves the lower bound of the range.
   *
   * @return the lower bound as a double value.
   */
  public double getLowerBound() {
    return lowerBound;
  }

  /**
   * Retrieves the upper bound of the range.
   *
   * @return the upper bound as a double value.
   */
  public double getUpperBound() {
    return upperBound;
  }

  /**
   * Checks whether the given value is within the range defined by the lower and upper bounds. The comparison is
   * inclusive of the bounds.
   *
   * @param value the floating-point value to check against the range.
   * @return true if the value is within the range (inclusive), false otherwise.
   */
  public boolean isInRange(float value) {
    return value >= lowerBound && value <= upperBound;
  }
}
