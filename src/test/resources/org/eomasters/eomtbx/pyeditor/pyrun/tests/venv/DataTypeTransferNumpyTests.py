from snapkit import Product
from snapkit import Raster
import math
import numpy as np

scale_factor = 0.1
scale_offset = -1.0

def compare_fuzzy(type: str, index, expected, actual, accuracy=1e-7):
    if expected is None or math.isnan(expected):
        if actual is not None and (not math.isnan(actual)):
            raise ValueError(f"Raw {type} values do not match at index {index}. Expected None, got {actual}")
    elif actual is None or math.isnan(actual):
        raise ValueError(f"Raw {type} values do not match at index {index}. Expected {expected}, got None")
    elif not math.isclose(actual, expected, rel_tol=accuracy, abs_tol=accuracy):
        raise ValueError(f"Raw {type} values do not match at index {index}. Expected {expected}, got {actual}")


def test_raw(raw_type: str, expected_data, expected_type):
    raw_data = product.rasters[raw_type].fetch()
    raw_data_type = product.rasters[raw_type].data_type
    if raw_data_type != expected_type:
        raise ValueError(
            f"Geophysical Data type for {raw_type} values should be {expected_type}, but got {raw_data_type}")
    for i, (actual, expected) in enumerate(zip(raw_data, expected_data)):
        if raw_data_type == Raster.TYPE_FLOAT32:
            compare_fuzzy(raw_type, i, expected, actual, 1e-6)
        elif raw_data_type == Raster.TYPE_FLOAT64:
            compare_fuzzy(raw_type, i, expected, actual, 1e-8)
        else:
            compare_fuzzy(raw_type, i, expected, actual, 1e-1)


def test_geo(geo_type: str, expected_data, expected_type):
    geo_data = product.rasters[geo_type].fetch(geophysical=True)
    expected_geo_data = tuple((v * scale_factor + scale_offset if v is not None else None) for v in expected_data)
    geo_data_type = product.rasters[geo_type].geophysical_data_type
    if geo_data_type != expected_type:
        raise ValueError(f"Geophysical Data type for {geo_type} values should be {expected_type}, but got {geo_data_type}")
    for i, (actual, expected) in enumerate(zip(geo_data, expected_geo_data)):
        if geo_data_type == Raster.TYPE_FLOAT32:
            compare_fuzzy(geo_type, i, expected, actual, 1e-6)
        elif geo_data_type == Raster.TYPE_FLOAT64:
            compare_fuzzy(geo_type, i, expected, actual, 1e-8)
        else:
            compare_fuzzy(geo_type, i, expected, actual, 1e-0)


def test_data(name, data_type, geo_type, data):
    product.add_band(name, data_type).apply(data)
    product[name].scale_factor = scale_factor
    product[name].scale_offset = scale_offset
    test_raw(name, data, data_type)
    test_geo(name, data, geo_type)
    product.del_raster(name)


if __name__ == '__main__':
    # other tests see PlainPythonTests and DataTypeTransferTests.py
    product = Product("test", "type", (5, 1))

    expected_int8 = (-128, -1, 0, 1, 127)
    expected_int16 = (-32768, -1, 0, 1, 32767)
    expected_int32 = (-2147483648, -1, 0, 1, 2147483647)
    expected_uint8 = (0, 1, 10, 100, 255)
    expected_uint16 = (0, 1, 10, 100, 65535)
    expected_uint32 = (0, 1, 10, 100, 4294967295)
    expected_float = (0.0, 1.1234567, 10.0, 100.0, math.nan)
    expected_double = (0.0, 1.123456789, 10.0, 100.0, math.nan)
    
    test_data("TYPE_INT8", Raster.TYPE_INT8, Raster.TYPE_FLOAT32, np.asarray(expected_int8, dtype=np.int8))
    test_data("TYPE_INT16", Raster.TYPE_INT16, Raster.TYPE_FLOAT32,  np.asarray(expected_int16, dtype=np.int16))
    test_data("TYPE_INT32", Raster.TYPE_INT32, Raster.TYPE_FLOAT64,  np.asarray(expected_int32, dtype=np.int32))
    test_data("TYPE_UINT8", Raster.TYPE_UINT8, Raster.TYPE_FLOAT32,  np.asarray(expected_uint8, dtype=np.uint8))
    test_data("TYPE_UINT16", Raster.TYPE_UINT16, Raster.TYPE_FLOAT32,  np.asarray(expected_uint16, dtype=np.uint16))
    test_data("TYPE_UINT32", Raster.TYPE_UINT32, Raster.TYPE_FLOAT64,  np.asarray(expected_uint32, dtype=np.uint32))
    test_data("TYPE_FLOAT32", Raster.TYPE_FLOAT32, Raster.TYPE_FLOAT32, np.asarray(expected_float, dtype=np.float32))
    test_data("TYPE_FLOAT64", Raster.TYPE_FLOAT64, Raster.TYPE_FLOAT64,  np.asarray(expected_double, dtype=np.float64))


