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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.util.ProductUtils;

public final class BandUtils {

  private BandUtils() {
  }


  /**
   * Finds the band closest to the center wavelength within a specified wavelength range.
   *
   * @param bands An array of Band objects to search within.
   * @param minWvl The minimum wavelength defining the range.
   * @param maxWvl The maximum wavelength defining the range.
   * @return The Band object closest to the center wavelength of the specified range.
   *         If no valid bands are found within the range, returns null.
   */
  public static Band findClosestToCenterBand(Band[] bands, double minWvl, double maxWvl) {
    List<Band> validBands = findBandsInRange(bands, minWvl, maxWvl);
    if (validBands.isEmpty()) {
      return null;
    }
    double centerWvl = (minWvl + maxWvl) / 2;
    double minDelta = Math.abs(centerWvl - validBands.get(0).getSpectralWavelength());
    int index = 0;
    for (int i = 1; i < validBands.size(); ++i) {
      final double delta = Math.abs(centerWvl - validBands.get(i).getSpectralWavelength());
      if (delta < minDelta) {
        minDelta = delta;
        index = i;
      }
    }
    return validBands.get(index);
  }

  /**
   * Filters an array of Band objects to find those within a specified wavelength range,
   * sorts the matching bands in ascending order by their wavelength, and returns them as a list.
   *
   * @param bands An array of Band objects to filter and sort.
   * @param minWvl The minimum wavelength defining the range (inclusive).
   * @param maxWvl The maximum wavelength defining the range (inclusive).
   * @return A list of Band objects within the specified wavelength range, sorted in ascending order
   *         by wavelength. Returns an empty list if no matching bands are found or if the input array is empty.
   */
  public static List<Band> findBandsInRange(Band[] bands, double minWvl, double maxWvl) {
    if (bands.length == 0) {
      return Collections.emptyList();
    }
    // exclude all below minWvl and maxWvl and sort bands by wavelength from min to max, store the result in a list
    return Arrays.stream(bands)
                 .filter(band -> band.getSpectralWavelength() >= minWvl)
                 .filter(band -> band.getSpectralWavelength() <= maxWvl)
                 .sorted((b1, b2) -> Float.compare(b1.getSpectralWavelength(), b2.getSpectralWavelength()))
                 .collect(Collectors.toList());
  }

  /**
   * Finds the band with the minimum wavelength within a specified range.
   *
   * @param bands An array of Band objects to search within.
   * @param minWvl The minimum wavelength defining the range (inclusive).
   * @param maxWvl The maximum wavelength defining the range (inclusive).
   * @return The Band object with the minimum wavelength within the specified range.
   *         Returns null if no valid bands are found within the range.
   */
  public static Band findMinimumBandInRange(Band[] bands, double minWvl, double maxWvl) {
    List<Band> validBands = findBandsInRange(bands, minWvl, maxWvl);
    if (validBands == null) {
      return null;
    }
    return validBands.get(0);
  }

  public static void copyBands(Product source, Product target, boolean copySourceImages) {
    for (Band band : source.getBands()) {
      ProductUtils.copyBand(band.getName(), source, target, copySourceImages);
    }
  }
}
