from pathlib import Path
from snapkit import Product, Raster
from org.esa.snap.core.datamodel import ProductData as JPD


def test(raster):
    ensure('B4', raster.name)
    ensure(5000, raster.width)
    ensure(5000, raster.height)
    ensure('dl', raster.unit)
    ensure('Reflectance in band B4', raster.description)
    ensure(665.0, raster.wavelength)
    ensure(30.0, raster.bandwidth)
    ensure(0, raster.solar_flux)
    ensure(False, raster.is_flag_band)
    ensure(False, raster.is_index_band)
    ensure('H', raster.data_type)
    ensure('f', raster.geophysical_data_type)
    ensure(0.0001, raster.scale_factor)
    ensure(-0.1, raster.scale_offset)
    ensure(False, raster.log10_scaled)
    ensure(0.0, raster.no_data_value)
    ensure(-0.1, raster.geophysical_no_data_value)


def ensure(expected, actual):
    if not actual == expected:
        raise AssertionError(f"Expected[{expected}] but was [{actual}]")


if __name__ == '__main__':
    testfile = Path(__file__).parent.parent.parent.parent.parent / "test_B8B4.znap.zip"
    product = Product.read(testfile)
    test(product["B4"])
