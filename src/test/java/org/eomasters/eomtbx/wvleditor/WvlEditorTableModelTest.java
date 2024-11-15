/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Basic for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2024 Marco Peters
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

package org.eomasters.eomtbx.wvleditor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.esa.snap.core.datamodel.group.BandGroup;
import org.eomasters.eomtbx.TestUtils;
import org.esa.snap.core.datamodel.Product;
import org.junit.jupiter.api.Test;

class WvlEditorTableModelTest {

  private final String[] bandNames = new String[]{"Oa01_radiance", "Oa01_radiance_unc", "Oa02_radiance",
      "Oa02_radiance_unc",
      "Oa03_radiance", "Oa03_radiance_unc", "altitude", "latitude", "longitude", "detector_index", "FWHM_band_1",
      "FWHM_band_2", "FWHM_band_3", "frame_offset", "lambda0_band_1", "lambda0_band_2", "lambda0_band_3",
      "lambda0_band_4", "lambda0_band_5", "lambda0_band_6", "lambda0_band_7", "lambda0_band_8", "lambda0_band_9",
      "lambda0_band_10", "lambda0_band_11", "solar_flux_band_1", "solar_flux_band_3", "solar_flux_band_2",
      "solar_flux_band_4", "quality_flags"};

  @Test
  void sortBandNames() {
    String[] sorted = WvlEditorTableModel.sortBandNames(bandNames, name -> name.replaceAll("[0-9]", ""));
    assertArrayEquals(
        new String[]{"Oa01_radiance", "Oa02_radiance", "Oa03_radiance", "Oa01_radiance_unc", "Oa02_radiance_unc",
            "Oa03_radiance_unc", "altitude", "latitude", "longitude", "detector_index", "FWHM_band_1", "FWHM_band_2",
            "FWHM_band_3", "frame_offset", "lambda0_band_1", "lambda0_band_2", "lambda0_band_3", "lambda0_band_4",
            "lambda0_band_5", "lambda0_band_6", "lambda0_band_7", "lambda0_band_8", "lambda0_band_9", "lambda0_band_10",
            "lambda0_band_11", "solar_flux_band_1", "solar_flux_band_3", "solar_flux_band_2", "solar_flux_band_4",
            "quality_flags"}, sorted);
  }

  @Test
  void sortBandNamesByAutoGrouping() {
    Product product = new Product("name", "type");
    product.setAutoGrouping("radiance_unc:FWMH:radiance:");
    BandGroup bandGroup = product.getAutoGrouping();
    String[] sorted = WvlEditorTableModel.sortBandNames(bandNames, bandGroup::indexOf);
    assertArrayEquals(
        new String[]{"Oa01_radiance", "Oa02_radiance", "Oa03_radiance", "Oa01_radiance_unc", "Oa02_radiance_unc",
            "Oa03_radiance_unc", "altitude", "latitude", "longitude", "detector_index", "FWHM_band_1", "FWHM_band_2",
            "FWHM_band_3", "frame_offset", "lambda0_band_1", "lambda0_band_2", "lambda0_band_3", "lambda0_band_4",
            "lambda0_band_5", "lambda0_band_6", "lambda0_band_7", "lambda0_band_8", "lambda0_band_9", "lambda0_band_10",
            "lambda0_band_11", "solar_flux_band_1", "solar_flux_band_3", "solar_flux_band_2", "solar_flux_band_4",
            "quality_flags"}, sorted);
  }

  @Test
  void testSettingValues() {
    WvlEditorTableModel tableModel = new WvlEditorTableModel(TestUtils.createProduct());
    tableModel.setValueAt("test", 0, 0);
    assertEquals("B1", tableModel.getValueAt(0, 0)); // not editable
    tableModel.setValueAt(1, 0, 1);
    assertEquals(1, tableModel.getValueAt(0, 1));
    tableModel.setValueAt("2.0", 0, 2);
    assertEquals(2.0f, (Float) tableModel.getValueAt(0, 2), 1.0e-6f);
    tableModel.setValueAt(3.0, 0, 3);
    assertEquals(3.0f, (Float) tableModel.getValueAt(0, 3), 1.0e-6f);
  }
}
