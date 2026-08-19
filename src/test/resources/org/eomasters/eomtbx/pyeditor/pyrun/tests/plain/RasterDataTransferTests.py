from snapkit import Product
from snapkit._rasterData import RasterData
import array as pyarray
import jarray
import time
import random


def test_get_transfer(get_func, raster):
    raster._raster.unloadRasterData()
    start = time.time()
    arr = get_func(raster)
    print(function_name(get_func), "test: ", round(time.time() - start, 3), "sec")

    start = time.time()
    data = []
    from math import nan
    for x in zip(arr):
        data.append(x if x != 0 else nan)
    print("  computation afterwards: ", round(time.time() - start, 3), "sec")

    return arr


def test_set_transfer(set_func, raster, data):
    start = time.time()
    set_func(raster, data)
    print(function_name(set_func), "test: ", round(time.time() - start, 3), "sec")


def function_name(func):
    return getattr(func, "__qualname__", getattr(func, "__name__", type(func).__name__))


def assertSample(expected_sample, actual_sample, accuracy=6):
    if round(expected_sample, accuracy) != round(actual_sample, accuracy):
        raise AssertionError(f"Expected sample != actual sample: {expected_sample} != {actual_sample}")


if __name__ == '__main__':
    # using os.path, pathlib has issue in 25.0.0: https://github.com/oracle/graalpython/issues/549
    import os
    file_path = os.path.abspath(__file__)
    print("file_path:", file_path)
    one = os.path.dirname(file_path)
    two = os.path.dirname(one)
    three = os.path.dirname(two)
    four = os.path.dirname(three)
    five = os.path.dirname(four)
    test_file = os.path.join(five, "test_B8B4.znap.zip")
    product = Product.read(test_file)
    b4 = product["B4"]
    random.seed(1234)

    arr = test_get_transfer(RasterData.get_raw, b4)
    b4._raster.loadRasterData()
    assertSample(b4.fetch()[250], arr[250])

    arr = test_get_transfer(RasterData.get_geophysical, b4)
    raster_sample = b4._raster.scale(b4.fetch()[250])
    assertSample(raster_sample, arr[250])

    print("Generating random data...")
    data = pyarray.array(b4.data_type, [random.randint(1000, 2500) for _ in range(b4.width * b4.height)])
    test_set_transfer(RasterData.set_raw, b4, data)
    assertSample(data[250], b4.fetch()[250])

    geodata = pyarray.array(b4.geophysical_data_type, [random.uniform(0, 1) for _ in range(b4.width * b4.height)])
    test_set_transfer(RasterData.set_geophysical, b4, geodata)
    geodata_250 = geodata[250]
    retrieved_geo_250 = b4.fetch(True)[250]
    assertSample(geodata_250, retrieved_geo_250, 4)
    raw_from_inverse = b4._raster.scaleInverse(geodata_250)
    retrieved_raw = b4.fetch()[250]
    assertSample(raw_from_inverse, retrieved_raw,0)


    # not good
    # test_transfer(RasterData._direct_java_usage, b4)
    # test_transfer(RasterData._list_wrapped_java_array, b4)

    # more than 100 seconds
    # test_transfer(RasterData.array_from_bytebuffer, b4)
    # test_transfer(RasterData.optimized_array_from_bytebuffer, b4)
    # test_transfer(RasterData.optimized_bytebuffer_2, b4)
