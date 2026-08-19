import time
from pathlib import Path
import numpy as np
from snapkit import Product, Raster

testfile = Path(
    __file__).parent.parent.parent.parent.parent / "test_B8B4.znap.zip"
product = Product.read(testfile)
print("NDVI computation started ...")

b8 = product["B8"].fetch(geophysical=True)
b4 = product["B4"].fetch(geophysical=True)

start = time.time()

b8 = np.asarray(b8, dtype=np.float32)
b4 = np.asarray(b4, dtype=np.float32)

den = b8 + b4
with np.errstate(divide='ignore', invalid='ignore'):
    ndvi_data = (b8 - b4) / den
    ndvi_data[den == 0] = np.nan  # match your nan-on-zero-denominator behavior

ndvi = product.add_band("ndvi", Raster.TYPE_FLOAT32)
ndvi.apply(ndvi_data)
print(f"NDVI example numpy sample took {round(time.time() - start, 3)}")
