package org.eomasters.eomtbx.wvleditor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.Product.AutoGrouping;
import org.junit.jupiter.api.Test;

class WvlEditorTableModelTest {

  private final String[] bandNames = new String[]{"Oa01_radiance", "Oa01_radiance_unc", "Oa02_radiance", "Oa02_radiance_unc",
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
    AutoGrouping autoGrouping = product.getAutoGrouping();
    String[] sorted = WvlEditorTableModel.sortBandNames(bandNames, autoGrouping::indexOf);
    assertArrayEquals(
        new String[]{"Oa01_radiance", "Oa02_radiance", "Oa03_radiance", "Oa01_radiance_unc", "Oa02_radiance_unc",
            "Oa03_radiance_unc", "altitude", "latitude", "longitude", "detector_index", "FWHM_band_1", "FWHM_band_2",
            "FWHM_band_3", "frame_offset", "lambda0_band_1", "lambda0_band_2", "lambda0_band_3", "lambda0_band_4",
            "lambda0_band_5", "lambda0_band_6", "lambda0_band_7", "lambda0_band_8", "lambda0_band_9", "lambda0_band_10",
            "lambda0_band_11", "solar_flux_band_1", "solar_flux_band_3", "solar_flux_band_2", "solar_flux_band_4",
            "quality_flags"}, sorted);
  }
}