import time
from pathlib import Path
from org.esa.snap.core.datamodel import RasterDataNode as JRaster
from snapkit import Product, Raster

testfile = Path(
    __file__).parent.parent.parent.parent.parent / "test_B8B4.znap.zip"
product = Product.read(testfile)
print("NDVI fetching data ...")

b8 = product["B8"].fetch(geophysical=True)
b4 = product["B4"].fetch(geophysical=True)

start = time.time()
from math import nan

print("NDVI computation started ...")
# NDVI = (B8 - B4) / (B8 + B4).
ndvi_data = []
for x, y in zip(b8, b4):
    d = x + y
    ndvi_data.append((x - y) / d if d != 0 else nan)

print(f"Computation of NDVI took {round(time.time() - start, 3)}")

ndvi = product.add_band("ndvi", Raster.TYPE_FLOAT64)
ndvi.apply(ndvi_data)
print(f"NDVI example took {round(time.time() - start, 3)}")

