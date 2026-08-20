"""
Raster data management and manipulation module.

This module provides a Python interface for working with raster data nodes within
remote sensing products through SNAP's Java API. It offers comprehensive functionality
for accessing, manipulating, and analyzing raster data including spectral bands,
tie-point grids, and masks with support for various data types and scaling operations.

The main component is the Raster class, which serves as a wrapper around SNAP's
Java RasterDataNode class, providing convenient Pythonic access to:
- Raster metadata (dimensions, data types, scaling factors)
- Pixel value sampling and coordinate transformations
- Raw and geophysically calibrated data access
- Data retrieval and modification operations
- Spectral properties for bands (wavelength, bandwidth, solar flux)
- Sample coding for flag bands and classifications

Key Features:
    - Support for multiple data types (8-bit to 64-bit integers and floats)
    - Raw and geophysically calibrated data access with automatic scaling
    - Pixel coordinate to geographic coordinate transformations
    - Efficient pixel sampling and bulk data operations
    - Integration with numpy-compatible array structures
    - Spectral band properties and metadata access
    - Flag band and classification support with sample coding
    - Automatic type conversion between SNAP and Python data types

Supported Data Types:
    - TYPE_INT8 ('b'): 8-bit signed integer
    - TYPE_UINT8 ('B'): 8-bit unsigned integer
    - TYPE_INT16 ('h'): 16-bit signed integer
    - TYPE_UINT16 ('H'): 16-bit unsigned integer
    - TYPE_INT32 ('i'): 32-bit signed integer
    - TYPE_UINT32 ('I'): 32-bit unsigned integer
    - TYPE_FLOAT32 ('f'): 32-bit floating point
    - TYPE_FLOAT64 ('d'): 64-bit floating point

Usage:
    >>> # Basic raster access and properties:
    >>> from snapkit import Product
    >>>
    >>> # Load product and access raster
    >>> product = Product.read('/path/to/product.dim')
    >>> band = product['B1']  # Access band by name
    >>>
    >>> # Check raster properties
    >>> print(f"Band: {band.name}, Size: {band.width}x{band.height}")
    >>> print(f"Data type: {band.data_type}, Unit: {band.unit}")
    >>> print(f"Wavelength: {band.wavelength} nm, Bandwidth: {band.bandwidth} nm")

    >>> # Pixel sampling and data access:
    >>> # Sample individual pixels
    >>> raw_value = band.sample(100, 200)  # Raw pixel value
    >>> geo_value = band.sample(100, 200, geophysical=True)  # Calibrated value
    >>>
    >>> # Get entire raster data as arrays
    >>> raw_data = band.fetch()  # Raw data array
    >>> geo_data = band.fetch(geophysical=True)  # Calibrated data array
    >>> print(f"Data shape: {raw_data.shape}, dtype: {raw_data.dtype}")

    >>> # Coordinate transformations (for geo-coded rasters):
    >>> if band.is_geo_coded:
    >>>     # Convert geographic to pixel coordinates
    >>>     pixel_x, pixel_y = band.pixel_pos(52.5, 13.4)  # Berlin coordinates
    >>>     print(f"Berlin is at pixel ({pixel_x}, {pixel_y})")
    >>>
    >>>     # Convert pixel to geographic coordinates
    >>>     lat, lon = band.geo_pos(100, 200)
    >>>     print(f"Pixel (100,200) is at {lat}°N, {lon}°E")

    >>> # Working with flag bands and sample coding:
    >>> flag_band = product['quality_flags']
    >>> if flag_band.is_flag_band:
    >>>     coding = flag_band.sample_coding()
    >>>     if coding:
    >>>         flag_names = coding.get_flag_names()
    >>>         print(f"Available flags: {flag_names}")

    >>> # Data modification (numpy is only available on Linux):
    >>> import numpy as np
    >>> # Create new data array
    >>> new_data = np.random.random((band.height, band.width))
    >>> # Apply data to raster (as geophysical values)
    >>> band.apply(new_data, geophysical=True)

"""
import array as pyarray
import jarray
from org.esa.snap.core.datamodel import ProductData as JPD
from org.esa.snap.core.datamodel import RasterDataNode as JRaster

from ._productUtils import *
from ._rasterData import RasterData
from ._utils import Utils
from .sampleCoding import SampleCoding

class Raster:
    """Represents a raster data node within a remote sensing product.

    This class serves as a Python wrapper around SNAP's Java RasterDataNode class,
    providing convenient access to raster data including spectral bands, tie-point grids,
    and masks. It offers properties for accessing raster metadata (dimensions, data types,
    scaling factors) and methods for pixel sampling, coordinate transformations, and data
    retrieval/modification.

    The Raster class supports various data types from 8-bit integers to 64-bit floating
    point values, handles both raw and geophysically calibrated data access, and provides
    coordinate transformation capabilities for geo-coded rasters.

    Example:
        >>> # Access raster from product
        >>> product = Product.read('/path/to/product.dim')
        >>> band = product['B1']
        >>>
        >>> # Check raster properties
        >>> print(f"Band: {band.name}, Size: {band.width}x{band.height}")
        >>> print(f"Data type: {band.data_type}, Unit: {band.unit}")
        >>>
        >>> # Sample pixel values
        >>> value = band.sample(100, 200)
        >>> geo_value = band.sample(100, 200, geophysical=True)
        >>>
        >>> # Get entire raster data
        >>> data_array = band.fetch()
        >>> geo_data_array = band.fetch(geophysical=True)
    """

    # Private sentinel used to protect constructor access within the package only
    _SENTINEL = object()
    # Package-internal factory: do not export this from the package API.
    def _create_raster(jraster: JRaster) -> 'Raster':
        return Raster(jraster, Raster._SENTINEL)

    __BAND_CLASS_NAME = 'org.esa.snap.core.datamodel.Band'
    __RDN_CLASS_NAME = 'org.esa.snap.core.datamodel.RasterDataNode'

    TYPE_INT8 = 'b'
    TYPE_UINT8 = 'B'
    TYPE_INT16 = 'h'
    TYPE_UINT16 = 'H'
    TYPE_INT32 = 'i'
    TYPE_UINT32 = 'I'
    # TYPE_INT64 = 'l'
    # TYPE_UINT64 = 'L', # not supported in java/snap
    TYPE_FLOAT32 = 'f'
    TYPE_FLOAT64 = 'd'

    _SNAP_TO_PYTHON_TYPE = {
        JPD.TYPE_INT8: TYPE_INT8,
        JPD.TYPE_UINT8: TYPE_UINT8,
        JPD.TYPE_INT16: TYPE_INT16,
        JPD.TYPE_UINT16: TYPE_UINT16,
        JPD.TYPE_INT32: TYPE_INT32,
        JPD.TYPE_UINT32: TYPE_UINT32,
        # JPD.TYPE_INT64: TYPE_INT64,
        # JPD.TYPE_UINT64: 'L', # not supported in java/snap
        JPD.TYPE_FLOAT32: TYPE_FLOAT32,
        JPD.TYPE_FLOAT64: TYPE_FLOAT64
    }

    _PYTHON_TYPE_TO_SNAP = {
        TYPE_INT8: JPD.TYPE_INT8,
        TYPE_UINT8: JPD.TYPE_UINT8,
        TYPE_INT16: JPD.TYPE_INT16,
        TYPE_UINT16: JPD.TYPE_UINT16,
        TYPE_INT32: JPD.TYPE_INT32,
        TYPE_UINT32: JPD.TYPE_UINT32,
        # TYPE_INT64: JPD.TYPE_INT64,
        # 'L': JPD.TYPE_UINT64, # not supported in java/snap
        TYPE_FLOAT32: JPD.TYPE_FLOAT32,
        TYPE_FLOAT64: JPD.TYPE_FLOAT64
    }

    def __init__(self, java_raster: JRaster, _sentinel: object) -> None:
        """This is NOT intended to be called directly. You can create a new raster using a product.

        Args:
            java_raster (org.esa.snap.core.datamodel.RasterDataNode): The Java
                RasterDataNode object to wrap. This can be a Band, TiePointGrid,
                or Mask from a SNAP Product.

        Example:
            >>> # Usually called internally by Product class
            >>> band = product.add_Band('B1', Raster.TYPE_UINT16))
            >>> print(f"Band name: {band.name}")
            >>> land_mask = product.add_mask('land_mask', "B3 > 0.1"))
            >>> print(f"Mask name: {land_mask.name}")
        """
        if _sentinel is not Raster._SENTINEL:
            raise TypeError(
                "Raster cannot be instantiated directly. Create a new raster using a product.")
        Utils.is_instance(java_raster, Raster.__RDN_CLASS_NAME)
        self._raster = java_raster

    @property
    def name(self) -> str:
        """Get the name of the raster data node.

        Returns:
            str: The name identifier of the raster data node as defined in the product.

        Example:
            >>> band = product['B1']
            >>> print(band.name)  # "B1"
        """
        return self._raster.getName()

    @property
    def description(self) -> str:
        """Get the description text of the raster data node.

        Returns:
            str: Optional description text for the raster, or empty string if not set.

        Example:
            >>> band = product['B1']
            >>> print(band.description)  # "Blue band at 490 nm"
        """
        return self._raster.getDescription()

    @property
    def unit(self) -> str:
        """Get the physical unit of the raster values.

        This property returns the physical unit for band data (e.g., "W/(m^2*sr*μm)"
        for reflectance). Only available for Band objects, returns empty string for
        other raster types like masks or tie-point grids.

        Returns:
            str: Physical unit string for bands, empty string for non-band rasters.

        Example:
            >>> band = product['B1']
            >>> print(band.unit)  # "W/(m^2*sr*μm)"
            >>> mask = product['cloud_mask']
            >>> print(mask.unit)  # ""
        """
        if Utils.is_instance(self._raster, Raster.__BAND_CLASS_NAME):
            return self._raster.getUnit()
        return ""

    @property
    def width(self) -> int:
        """Get the width of the raster in pixels.

        Returns:
            int: Width of the raster data in pixels (columns).

        Example:
            >>> band = product['B1']
            >>> print(f"Band width: {band.width} pixels")
        """
        return self._raster.getRasterWidth()

    @property
    def height(self) -> int:
        """Get the height of the raster in pixels.

        Returns:
            int: Height of the raster data in pixels (rows).

        Example:
            >>> band = product['B1']
            >>> print(f"Band height: {band.height} pixels")
        """
        return self._raster.getRasterHeight()

    @property
    def wavelength(self) -> float:
        """Get the spectral wavelength of the band.

        This property returns the central wavelength for spectral bands. Only
        available for Band objects, returns 0 for other raster types.

        Returns:
            float: Spectral wavelength in nanometers for bands, 0.0 for non-band rasters.

        Example:
            >>> band = product['B1']
            >>> print(f"Band wavelength: {band.wavelength} nm")
            >>> mask = product['cloud_mask']
            >>> print(mask.wavelength)  # 0.0
        """
        if Utils.is_instance(self._raster, Raster.__BAND_CLASS_NAME):
            return self._raster.getSpectralWavelength()
        return 0

    @property
    def bandwidth(self) -> float:
        """Get the spectral bandwidth of the band.

        This property returns the bandwidth (width of the spectral range) for
        spectral bands. Only available for Band objects, returns 0 for other raster types.

        Returns:
            float: Spectral bandwidth in nanometers for bands, 0.0 for non-band rasters.

        Example:
            >>> band = product['B1']
            >>> print(f"Band bandwidth: {band.bandwidth} nm")
            >>> mask = product['cloud_mask']
            >>> print(mask.bandwidth)  # 0.0
        """
        if Utils.is_instance(self._raster, Raster.__BAND_CLASS_NAME):
            return self._raster.getSpectralBandwidth()
        return 0

    @property
    def solar_flux(self) -> float:
        """Get the solar irradiance value for the band.

        This property returns the solar flux (irradiance) value used for atmospheric
        correction calculations. Only available for Band objects, returns 0 for other raster types.

        Returns:
            float: Solar irradiance value in W/(m^2*μm) for bands, 0.0 for non-band rasters.

        Example:
            >>> band = product['B1']
            >>> print(f"Solar flux: {band.solar_flux} W/(m^2*μm)")
            >>> mask = product['cloud_mask']
            >>> print(mask.solar_flux)  # 0.0
        """
        if Utils.is_instance(self._raster, Raster.__BAND_CLASS_NAME):
            return self._raster.getSolarFlux()
        return 0

    @property
    def is_flag_band(self) -> bool:
        """Check if this is a flag/mask band.

        This property indicates whether the band contains flag or mask data rather
        than continuous measurements. Only available for Band objects, returns False
        for other raster types.

        Returns:
            bool: True if this is a flag band, False otherwise.

        Example:
            >>> flag_band = product['quality_flags']
            >>> print(flag_band.is_flag_band)  # True
            >>> data_band = product['B1']
            >>> print(data_band.is_flag_band)  # False
        """
        if Utils.is_instance(self._raster, Raster.__BAND_CLASS_NAME):
            return self._raster.isFlagBand()
        return False

    @property
    def is_index_band(self) -> bool:
        """Check if this is an index band.

        This property indicates whether the band contains index values that reference
        other data structures. Only available for Band objects, returns False for
        other raster types.

        Returns:
            bool: True if this is an index band, False otherwise.

        Example:
            >>> index_band = product['pixel_classif_flags']
            >>> print(index_band.is_index_band)  # True
            >>> data_band = product['B1']
            >>> print(data_band.is_index_band)  # False
        """
        if Utils.is_instance(self._raster, Raster.__BAND_CLASS_NAME):
            return self._raster.isIndexBand()
        return False

    @property
    def data_type(self) -> str:
        """Get the raw data type of the raster values.

        This property returns a Python type code indicating the data type used
        to store raw (unscaled) raster values in memory and on disk.

        Returns:
            str: Python array type code ('b'=int8, 'B'=uint8, 'h'=int16, 'H'=uint16,
                'i'=int32, 'I'=uint32, 'l'=int64, 'f'=float32, 'd'=float64).

        Example:
            >>> band = product['B1']
            >>> print(f"Raw data type: {band.data_type}")  # 'H' for uint16
        """
        return Raster._SNAP_TO_PYTHON_TYPE[self._raster.getDataType()]

    @property
    def geophysical_data_type(self) -> str:
        """Get the geophysical data type of the raster values.

        This property returns a Python type code indicating the data type used
        for geophysically calibrated (scaled) raster values after applying
        scaling factors and offsets.

        Returns:
            str: Python array type code ('b'=int8, 'B'=uint8, 'h'=int16, 'H'=uint16,
                'i'=int32, 'I'=uint32, 'l'=int64, 'f'=float32, 'd'=float64).

        Example:
            >>> band = product['B1']
            >>> print(f"Geophysical data type: {band.geophysical_data_type}")  # 'f' for float32
        """
        return Raster._SNAP_TO_PYTHON_TYPE[
            self._raster.getGeophysicalDataType()]

    @property
    def scale_factor(self) -> float:
        """Get or set the scaling factor applied to convert raw to geophysical values.

        This factor is multiplied with raw pixel values as part of the conversion
        to geophysically meaningful units. The conversion formula is:
        geophysical_value = (raw_value * scale_factor) + scale_offset
        If the log10_scaled property is true, the result is taken to the power of 10 after the actual scaling

        Returns:
            float: Scaling factor multiplier.

        Example:
            >>> band = product['B1']
            >>> print(f"Scale factor: {band.scale_factor}")
            >>> # Set new scaling factor
            >>> band.scale_factor = 0.01
        """

        return self._raster.getScalingFactor()

    @scale_factor.setter
    def scale_factor(self, value: float) -> None:
        """Set the scaling factor used to convert raw to geophysical values."""
        self._raster.setScalingFactor(value)

    @property
    def scale_offset(self) -> float:
        """Get or set the scaling offset applied to convert raw to geophysical values.

        This offset is added to scaled pixel values as part of the conversion
        to geophysically meaningful units. The conversion formula is:
        geophysical_value = (raw_value * scale_factor) + scale_offset
        If the log10_scaled property is true, the result is taken to the power of 10 after the actual scaling

        Returns:
            float: Scaling offset additive term.

        Example:
            >>> band = product['B1']
            >>> print(f"Scale offset: {band.scale_offset}")
            >>> # Set new scaling offset
            >>> band.scale_offset = -1.0
        """

        return self._raster.getScalingOffset()

    @scale_offset.setter
    def scale_offset(self, value: float) -> None:
        """Set the scaling offset used to convert raw to geophysical values."""
        self._raster.setScalingOffset(value)

    @property
    def log10_scaled(self) -> bool:
        """Get or set whether logarithmic scaling is applied to the raster values.

        This property controls whether base-10 logarithmic scaling is applied
        during the conversion from raw to geophysical values. When enabled,
        the conversion formula becomes:
        geophysical_value = 10^((raw_value * scale_factor) + scale_offset)

        Returns:
            bool: True if log10 scaling is applied, False otherwise.

        Example:
            >>> band = product['B1']
            >>> print(f"Log10 scaled: {band.log10_scaled}")
            >>> # Enable logarithmic scaling
            >>> band.log10_scaled = True
        """
        return self._raster.isLog10Scaled()

    @log10_scaled.setter
    def log10_scaled(self, value: bool) -> None:
        """Set whether logarithmic scaling should be applied to the raster values."""
        self._raster.setLog10Scaled(value)

    @property
    def is_scaled(self) -> bool:
        """Check if any scaling is applied to the raster values.

        This property indicates whether scaling (linear or logarithmic) is applied
        during the conversion from raw to geophysical values.

        Returns:
            bool: True if any scaling is applied, False otherwise.

        Example:
            >>> band = product['B1']
            >>> print(f"Is scaled: {band.is_scaled}")
        """
        return self._raster.isScalingApplied()

    @property
    def no_data_value(self) -> float:
        """Get the no-data value for raw raster values.

        This value represents missing or invalid data in the raw (unscaled) raster.
        Pixels with this value should be treated as having no valid measurement.

        Returns:
            float: The no-data value for raw pixel values.

        Example:
            >>> band = product['B1']
            >>> print(f"No-data value: {band.no_data_value}")
        """
        return self._raster.getNoDataValue()

    @property
    def geophysical_no_data_value(self) -> float:
        """Get the no-data value for geophysical raster values.

        This value represents missing or invalid data in the geophysically
        calibrated (scaled) raster. Pixels with this value should be treated
        as having no valid measurement.

        Returns:
            float: The no-data value for geophysical pixel values.

        Example:
            >>> band = product['B1']
            >>> print(f"Geophysical no-data value: {band.geophysical_no_data_value}")
        """
        return self._raster.getGeophysicalNoDataValue()

    @property
    def is_geo_coded(self) -> bool:
        """Check if the raster has valid geo-coding information.

        This property indicates whether the raster has geo-coding that allows
        conversion between pixel coordinates and geographic coordinates.

        Returns:
            bool: True if geo-coding is available, False otherwise.

        Example:
            >>> band = product['B1']
            >>> if band.is_geo_coded:
            >>>     lat, lon = band.geo_pos(100, 200)
            >>>     print(f"Pixel (100,200) is at {lat}°N, {lon}°E")
        """
        jgeo_coding = self._raster.getGeoCoding()
        return False if jgeo_coding is None else True

    def pixel_pos(self, lat: float, lon: float) -> tuple[float, float] | None:
        """Convert geographic coordinates to pixel coordinates.

        This method transforms latitude/longitude coordinates to pixel coordinates
        using the raster's geo-coding information. Requires the raster to have
        valid geo-coding.

        Args:
            lat (float): Latitude in decimal degrees.
            lon (float): Longitude in decimal degrees.

        Returns:
            tuple[float, float] | None: Pixel coordinates as (x, y) tuple in pixels,
                or None if conversion fails or raster lacks geo-coding.

        Example:
            >>> band = product['B1']
            >>> if band.is_geo_coded:
            >>>     pixel_x, pixel_y = band.pixel_pos(52.5, 13.4)
            >>>     print(f"Berlin is at pixel ({pixel_x}, {pixel_y})")
        """
        return get_pixel_pos(self._raster.getGeoCoding(), lat, lon)

    def geo_pos(self, x: float, y: float) -> tuple[float, float] | None:
        """Convert pixel coordinates to geographic coordinates.

        This method transforms pixel coordinates to latitude/longitude coordinates
        using the raster's geo-coding information. Requires the raster to have
        valid geo-coding.

        Args:
            x (float): Pixel x-coordinate (column).
            y (float): Pixel y-coordinate (row).

        Returns:
            tuple[float, float] | None: Geographic coordinates as (latitude, longitude)
                tuple in decimal degrees, or None if conversion fails or raster lacks geo-coding.

        Example:
            >>> band = product['B1']
            >>> if band.is_geo_coded:
            >>>     lat, lon = band.geo_pos(100, 200)
            >>>     print(f"Pixel (100,200) is at {lat}°N, {lon}°E")
        """
        return get_geo_pos(self._raster.getGeoCoding(), x, y)

    def sample_coding(self) -> SampleCoding | None:
        """Get the sample coding information for the raster.

        This method returns the sample coding that maps sample values to meaningful
        labels or categories. Typically used for flag bands or classification results.

        Returns:
            SampleCoding | None: SampleCoding object if available, None otherwise.

        Example:
            >>> flag_band = product['quality_flags']
            >>> coding = flag_band.sample_coding()
            >>> if coding:
            >>>     print(f"Flag definitions: {coding.get_flag_names()}")
        """
        coding = self._raster.getSampleCoding()
        from .sampleCoding import _create_coding
        return None if coding is None else _create_coding(coding)

    def sample(self, x: int | float, y: int | float = None, geophysical: bool = False) -> int | float:
        """Sample a single pixel value from the raster.

        This method retrieves a single pixel value at the specified coordinates.
        It can return either raw or geophysically calibrated values based on the
        geophysical parameter. When y is None, x is treated as a linear index.

        Args:
            x (int | float): Pixel x-coordinate (column) or linear pixel index if y is None.
            y (int | float): Pixel y-coordinate (row). If None, x is
                treated as a linear index. Defaults to None.
            geophysical (bool, optional): If True, return geophysically calibrated values.
                If False, return raw pixel values. Defaults to False.

        Returns:
            int | float: The pixel value at the specified coordinates. Type depends on
                the raster's data type and geophysical parameter.

        Example:
            >>> band = product['B1']
            >>> # Sample pixel at coordinates (100, 200)
            >>> raw_value = band.sample(100, 200)
            >>> geo_value = band.sample(100, 200, geophysical=True)
            >>> # Sample using linear index
            >>> linear_value = band.sample(10000)  # 10000th pixel
        """
        if geophysical:
            y = x // self.width if y is None else y  # // divide and floor
            x = x % self.width if y is None else y
            if self._raster.getGeophysicalDataType() < JPD.TYPE_FLOAT32:
                return self._raster.getPixelInt(x, y)
            else:
                return self._raster.getPixelDouble(x, y)
        else:
            index = y * self.width + x if y is not None else x
            if self._raster.getDataType() < JPD.TYPE_FLOAT32:
                return self._raster.getRasterData().getElemIntAt(index)
            else:
                return self._raster.getRasterData().getElemDoubleAt(index)

    def fetch(self, geophysical: bool = False) -> list | None:
        """Retrieve the entire raster data as a python array.

        This method loads all pixel values from the raster into a python array
        structure. It can return either raw or geophysically calibrated data
        based on the geophysical parameter.

        Args:
            geophysical (bool, optional): If True, return geophysically calibrated values
                with applied scaling factors and offsets. If False, return raw pixel values
                as stored on disk. Defaults to False.

        Returns:
            array: a 1D python array containing all raster pixel values.

        Example:
            >>> band = product['B1']
            >>> # Get raw data
            >>> raw_data = band.fetch()
            >>> print(f"Raw data shape: {raw_data.shape}, dtype: {raw_data.dtype}")
            >>> # Get geophysical data
            >>> geo_data = band.fetch(geophysical=True)
            >>> print(f"Geo data range: {geo_data.min()} to {geo_data.max()}")
        """
        if geophysical:
            return RasterData.get_geophysical(self)
        else:
            return RasterData.get_raw(self)

    def apply(self, data: list, geophysical: bool = False) -> None:
        """Apply new data values to the raster.

        This method replaces the raster's pixel values with the provided data array.
        The data can be written as raw values or geophysically calibrated values
        based on the geophysical parameter.

        Args:
            data (array): The data containing new pixel values. Either a list, a python array, or a Numpy array.
                The Shape must match the raster dimensions (height, width).
            geophysical (bool, optional): If True, treat input data as geophysically
                calibrated values that will be converted to raw values using inverse
                scaling. If False, treat as raw values. Defaults to False.

        Example:
            >>> band = product['B1']
            >>> import numpy as np
            >>> # Create new data
            >>> new_data = np.random.random((band.height, band.width))
            >>> # Apply as geophysical values
            >>> band.apply(new_data, geophysical=True)
            >>> # Apply as raw values
            >>> band.apply((new_data * 1000).astype(np.uint16))
        """
        if geophysical:
            RasterData.set_geophysical(self, data)
        else:
            RasterData.set_raw(self, data)

    def __repr__(self) -> str:
        """Return a string representation of the Raster instance.

        This method provides a concise string representation showing key raster
        characteristics for debugging and logging purposes.

        Returns:
            str: String representation in the format:
                "Raster(name='<name>', width=<w>, height=<h>)"

        Example:
            >>> band = product['B1']
            >>> print(repr(band))
            Raster(name='B1', width=10980, height=10980)
            >>> # Also works with print()
            >>> print(band)
            Raster(name='B1', width=10980, height=10980)
        """
        return f"Raster(name='{self.name}', width={self.width}, height={self.height})"
