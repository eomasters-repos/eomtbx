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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.esa.snap.core.datamodel.Band;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BandUtilsTest {

  @Test
  public void testFindClosestBand() {
    Band band0 = Mockito.mock(Band.class);
    Mockito.when(band0.getSpectralWavelength()).thenReturn(512.0f);
    Band band1 = Mockito.mock(Band.class);
    Mockito.when(band1.getSpectralWavelength()).thenReturn(700.0f);
    Band band2 = Mockito.mock(Band.class);
    Mockito.when(band2.getSpectralWavelength()).thenReturn(705.0f);
    Band band3 = Mockito.mock(Band.class);
    Mockito.when(band3.getSpectralWavelength()).thenReturn(680.0f);
    Band band4 = Mockito.mock(Band.class);
    Mockito.when(band4.getSpectralWavelength()).thenReturn(735.0f);
    Band band5 = Mockito.mock(Band.class);
    Mockito.when(band5.getSpectralWavelength()).thenReturn(790.0f);
    Band band6 = Mockito.mock(Band.class);
    Mockito.when(band6.getSpectralWavelength()).thenReturn(695.0f);

    Band[] bands = new Band[]{band0, band1, band2, band3, band4, band5, band6};
    Band result = BandUtils.findClosestToCenterBand(bands, 700.0, 700.0 + 5.0);
    assertNotNull(result);
    assertEquals(700.0, result.getSpectralWavelength());

    result = BandUtils.findClosestToCenterBand(bands, 725.0, 725.0 + 25.0);
    assertNotNull(result);
    assertEquals(735.0, result.getSpectralWavelength());

    result = BandUtils.findClosestToCenterBand(bands, 700.0, 700.0 + 0.0);
    assertEquals(700.0, result.getSpectralWavelength());

    result = BandUtils.findClosestToCenterBand(bands, 650.0, 650.0 + 5.0);
    assertNull(result);
  }

  @Test
  public void testFindMinimumInRangeBand() {
    Band band0 = Mockito.mock(Band.class);
    Mockito.when(band0.getSpectralWavelength()).thenReturn(512.0f);
    Band band1 = Mockito.mock(Band.class);
    Mockito.when(band1.getSpectralWavelength()).thenReturn(700.0f);
    Band band2 = Mockito.mock(Band.class);
    Mockito.when(band2.getSpectralWavelength()).thenReturn(705.0f);
    Band band3 = Mockito.mock(Band.class);
    Mockito.when(band3.getSpectralWavelength()).thenReturn(716.0f);
    Band band4 = Mockito.mock(Band.class);
    Mockito.when(band4.getSpectralWavelength()).thenReturn(735.0f);

    Band[] bands = new Band[]{band0, band1, band3, band2, band4};
    Band result = BandUtils.findMinimumBandInRange(bands, 705.0 - 15.0, 705.0 + 15.0);
    assertNotNull(result);
    assertEquals(700.0, result.getSpectralWavelength());
  }

  @Test
  public void testFindBandsInRange() {
    Band band0 = Mockito.mock(Band.class);
    Mockito.when(band0.getSpectralWavelength()).thenReturn(512.0f);
    Band band1 = Mockito.mock(Band.class);
    Mockito.when(band1.getSpectralWavelength()).thenReturn(700.0f);
    Band band2 = Mockito.mock(Band.class);
    Mockito.when(band2.getSpectralWavelength()).thenReturn(705.0f);
    Band band3 = Mockito.mock(Band.class);
    Mockito.when(band3.getSpectralWavelength()).thenReturn(680.0f);
    Band band4 = Mockito.mock(Band.class);
    Mockito.when(band4.getSpectralWavelength()).thenReturn(735.0f);
    Band band5 = Mockito.mock(Band.class);
    Mockito.when(band5.getSpectralWavelength()).thenReturn(790.0f);
    Band band6 = Mockito.mock(Band.class);
    Mockito.when(band6.getSpectralWavelength()).thenReturn(695.0f);

    Band[] bands = new Band[]{band0, band1, band2, band3, band4, band5, band6};
    List<Band> result = BandUtils.findBandsInRange(bands, 695.0 - 20.0, 695.0 + 20.0);
    assertNotNull(result);
    assertEquals(4, result.size());
    assertTrue(result.contains(band1));
    assertTrue(result.contains(band2));
    assertTrue(result.contains(band3));
    assertTrue(result.contains(band6));

    result = BandUtils.findBandsInRange(bands, 701.0 - 5.0, 701.0 + 5.0);
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.contains(band1));
    assertTrue(result.contains(band2));
  }
}
