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

package org.eomasters.eomtbx.ciop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.esa.snap.core.datamodel.Band;
import org.junit.jupiter.api.Test;

public class CyanoIndexOpImageTest {

  @Test
  void testCalcWvlFactor() {
    Band low = mock(Band.class);
    Band center = mock(Band.class);
    Band high = mock(Band.class);

    when(low.getSpectralWavelength()).thenReturn(665.0f);
    when(center.getSpectralWavelength()).thenReturn(681.3f);
    when(high.getSpectralWavelength()).thenReturn(708.8f);

    double actual = CyanoIndexOpImage.calcWvlFactor(low, center, high);

    assertEquals(0.372146, actual, 1.0e-6);
  }


  @Test
  void testCalcIndex() {
    double actual;
    // OLCI wavelength
    double wvlFactor = ((681.3 - 665.0) / (708.8 - 665.0));
    // Radiance values OLCI L1
    actual = CyanoIndexOpImage.calcIndex(17.572433, 15.923867, 13.218795, wvlFactor);
    assertEquals(0.02837652, actual, 1.0e-6);

    actual = CyanoIndexOpImage.calcIndex(20.300737, 18.107557, 19.506292, wvlFactor);
    assertEquals(1.8975304, actual, 1.0e-6);

    actual = CyanoIndexOpImage.calcIndex(14.521041, 13.075184, 10.426209, wvlFactor);
    assertEquals(-0.07801859, actual, 1.0e-6);

    // Rayleigh corrected OLCI values
    actual = CyanoIndexOpImage.calcIndex(0.017739542,	0.0169237,	0.0146973515, wvlFactor);
    assertEquals(-3.1629702E-4, actual, 1.0e-6);

    actual = CyanoIndexOpImage.calcIndex(0.024091005,	0.023089293,	0.02179511, wvlFactor);
    assertEquals(1.4730368E-4, actual, 1.0e-6);

    actual = CyanoIndexOpImage.calcIndex(0.032731537,	0.030179193,	0.044876494, wvlFactor);
    assertEquals(0.007072042, actual, 1.0e-6);

    // L2 Water OLCI values
    actual = CyanoIndexOpImage.calcIndex(0.0012024301, 0.0012207412, 0.0012939856, wvlFactor);
    assertEquals(1.5760952E-5, actual, 1.0e-6);

    actual = CyanoIndexOpImage.calcIndex(-0.017273476, -0.018683432, 0.0010376301, wvlFactor);
    assertEquals(0.0082243625, actual, 1.0e-6);

    actual = CyanoIndexOpImage.calcIndex(0.003234963, 0.0029969185, 0.0013672301, wvlFactor);
    assertEquals(-4.570251E-4, actual, 1.0e-6);
  }
}
