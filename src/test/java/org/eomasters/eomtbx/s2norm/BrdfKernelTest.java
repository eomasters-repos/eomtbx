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

package org.eomasters.eomtbx.s2norm;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BrdfKernelTest {

  @Test
  public void kgeo_single() {
    // expected values taken from real data, computed by python sen2nbar
    assertEquals(-0.43788963, BrdfKernel.kgeoSingle(19.3322, 0, -103.786, 1.0, 2.0), 1.0e-8);
    assertEquals(-0.44329278, BrdfKernel.kgeoSingle(19.3322, 0.874544, -103.786, 1.0, 2.0), 1.0e-8);
    assertEquals(-0.42568178, BrdfKernel.kgeoSingle(18.8124, 0, -174.915, 1.0, 2.0), 1.0e-8);
    assertEquals(-0.48963829, BrdfKernel.kgeoSingle(18.8124, 2.70079, -174.915, 1.0, 2.0), 1.0e-8);
  }

  @Test
  public void kvol_single() {
    // expected values taken from real data, computed by python sen2nbar
    assertEquals(-0.01627151, BrdfKernel.kvolSingle(19.3322, 0, -103.786), 1.0e-8);
    assertEquals(-0.01705795, BrdfKernel.kvolSingle(19.3322, 0.874544, -103.786), 1.0e-8);
    assertEquals(-0.01555761, BrdfKernel.kvolSingle(18.8124, 0, -174.915), 1.0e-8);
    assertEquals(-0.02525587, BrdfKernel.kvolSingle(18.8124, 2.70079, -174.915), 1.0e-8);
  }

}
