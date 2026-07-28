from snapkit import SNAP
from snapkit import Product
from snapkit import Raster

p = Product("test", "type", (5, 1))
band = p.add_band("a_band", Raster.TYPE_FLOAT32)

if band != SNAP._get_raster(band):
    raise ValueError("Retrieved band should be the same as the one passed to _get_raster")

if band.name != SNAP._get_raster("a_band", p).name:
    raise ValueError("_get_raster should return raster with name 'a_band'")
