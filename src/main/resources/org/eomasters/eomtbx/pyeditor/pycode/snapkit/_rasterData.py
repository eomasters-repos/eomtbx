import array as pyarray
import jarray
import platform
import snapkit
import sys
from org.esa.snap.core.datamodel import ProductData as JPD

from org.eomasters.eomtbx.pyeditor.snapkit import DataTransfer as JDT


class RasterData:
    """Utility class for efficient raster data transfer between Python and SNAP's Java backend.

    This class provides static methods for transferring raster data to and from SNAP's internal
    Java data structures, handling both raw (unscaled) and geophysical (scaled) data formats.
    It serves as a high-performance bridge between Python arrays and SNAP's raster data nodes,
    optimizing memory usage and transfer speed through direct data transfer mechanisms.

    The class handles automatic loading and unloading of raster data as needed, ensuring
    efficient memory management while providing seamless access to pixel values. It supports
    various Python array types including standard arrays, lists, and NumPy arrays, with
    automatic validation to ensure data integrity during transfer operations.

    All methods in this class are performance-critical and designed to minimize memory overhead
    and data copying operations. The transfer mechanisms bypass Python's standard array
    processing to achieve maximum throughput when working with large raster datasets.

    Note:
        This class cannot be instantiated. All methods are static and should be called
        directly on the class. Performance characteristics have been validated through
        comprehensive timing tests (see RasterDataTest.py and PythonTests.java).

    Example:
        >>> # Get raw pixel data from a raster
        >>> raw_data = RasterData.get_raw(raster)
        >>> print(f"Raw data type: {type(raw_data)}, length: {len(raw_data)}")
        >>> 
        >>> # Get geophysical data with scaling applied
        >>> geo_data = RasterData.get_geophysical(raster)
        >>> 
        >>> # Modify and write back raw data
        >>> modified_data = [x * 2 for x in raw_data[:100]] + list(raw_data[100:])
        >>> RasterData.set_raw(raster, modified_data)
        >>> 
        >>> # Write geophysical data (will be automatically converted to raw)
        >>> import numpy as np
        >>> new_geo_data = np.random.random(raster.width * raster.height)
        >>> RasterData.set_geophysical(raster, new_geo_data)
    """

    # see RasterDataTest.py and PythonTests.java for timing tests.

    def __new__(cls):
        raise TypeError(f"{cls.__name__} cannot be instantiated")

    @staticmethod
    def get_raw(raster):
        """Retrieve raw (unscaled) pixel data from a raster as a Python array.

        This method extracts all raw pixel values from the specified raster and returns
        them as a Python array with the same data type as the raster's native storage format.
        The raw values are returned without any scaling factors or offsets applied, representing
        the actual values stored on disk or in memory.

        The method handles automatic loading and unloading of raster data to optimize memory
        usage. If the raster data was not previously loaded, it will be automatically loaded
        for the transfer and then unloaded afterwards to free memory.

        Args:
            raster (Raster): The Raster object from which to extract raw pixel data.
                Must be a valid Raster instance with accessible underlying SNAP raster data.

        Returns:
            array.array: Python array containing all raw pixel values in row-major order.
                The array type corresponds to the raster's native data type (e.g., 'H' for
                uint16, 'f' for float32). Array length equals width × height.

        Example:
            >>> band = product['B1']
            >>> raw_data = RasterData.get_raw(band)
            >>> print(f"Raw data type: {raw_data.typecode}")  # 'H' for uint16
            >>> print(f"Raw data range: {min(raw_data)} to {max(raw_data)}")
            >>> print(f"First pixel value: {raw_data[0]}")
            >>> 
            >>> # Access pixel at specific coordinates (row=10, col=5)
            >>> pixel_index = 10 * band.width + 5
            >>> pixel_value = raw_data[pixel_index]
        """
        has_data = raster._raster.hasRasterData()
        data_array = pyarray.array(raster.data_type, [0] * raster.width * raster.height)
        dataTransfer = JDT.create(data_array)
        dataTransfer.transferToArray(raster._raster, data_array, False)
        if not has_data:
            raster._raster.unloadRasterData()
        return data_array

    @staticmethod
    def set_raw(raster, data):
        """Write raw (unscaled) pixel data to a raster from a Python array.

        This method transfers pixel values from a Python array-like object to the specified
        raster's underlying SNAP data structure. The data is written as raw values without
        any scaling transformations, directly replacing the raster's stored pixel values.

        The input data is validated to ensure it matches the expected raster dimensions and
        contains valid array-like data. The transfer operation is optimized for performance
        and handles various Python array types including lists, arrays, and NumPy arrays.

        Args:
            raster (Raster): The target Raster object to receive the new pixel data.
                Must be a valid Raster instance with write access.
            data (array-like): Array-like object containing raw pixel values in row-major order.
                Can be a Python list, array.array, or NumPy array. Length must equal
                raster width × height.

        Raises:
            ValueError: If data is None, empty, or doesn't match raster dimensions.

        Example:
            >>> band = product['B1']
            >>> # Get current data and modify it
            >>> current_data = RasterData.get_raw(band)
            >>> modified_data = [min(x * 2, 65535) for x in current_data]  # Double values, cap at uint16 max
            >>> 
            >>> # Write modified data back
            >>> RasterData.set_raw(band, modified_data)
            >>> 
            >>> # Write NumPy array data
            >>> import numpy as np
            >>> new_data = np.random.randint(0, 4096, size=band.width * band.height)
            >>> RasterData.set_raw(band, new_data)
        """
        RasterData.validate(data, raster.width * raster.height)
        dataTransfer = JDT.create(data)
        dataTransfer.transferToRaster(raster._raster, data, False)

    @staticmethod
    def get_geophysical(raster):
        """Retrieve geophysical (scaled) pixel data from a raster as a Python array.

        This method extracts all geophysically calibrated pixel values from the specified
        raster, applying any scaling factors, offsets, and logarithmic transformations
        defined for the raster. The returned values represent the physically meaningful
        measurements (e.g., reflectance, radiance, temperature) rather than raw digital counts.

        The method automatically handles the conversion from raw storage values to geophysical
        values using the raster's scaling parameters. If the raster data was not previously
        loaded, it will be automatically loaded for the transfer and then unloaded afterwards.

        Args:
            raster (Raster): The Raster object from which to extract geophysical pixel data.
                Must be a valid Raster instance with accessible underlying SNAP raster data.

        Returns:
            array.array: Python array containing all geophysical pixel values in row-major order.
                The array type corresponds to the raster's geophysical data type (typically 'f'
                for float32 or 'd' for float64). Array length equals width × height.

        Example:
            >>> band = product['B1']
            >>> geo_data = RasterData.get_geophysical(band)
            >>> print(f"Geophysical data type: {geo_data.typecode}")  # 'f' or 'd'
            >>> print(f"Geophysical range: {min(geo_data):.4f} to {max(geo_data):.4f}")
            >>> print(f"Mean value: {sum(geo_data) / len(geo_data):.4f}")
            >>> 
            >>> # Compare with raw data
            >>> raw_data = RasterData.get_raw(band)
            >>> print(f"Raw vs Geo first pixel: {raw_data[0]} vs {geo_data[0]:.4f}")
        """
        has_data = raster._raster.hasRasterData()
        geo_data_array = pyarray.array(raster.geophysical_data_type, [0] * raster.width * raster.height)
        dataTransfer = JDT.create(geo_data_array)
        dataTransfer.transferToArray(raster._raster, geo_data_array, True)
        if not has_data:
            raster._raster.unloadRasterData()
        return geo_data_array

    @staticmethod
    def set_geophysical(raster, data):
        """Write geophysical (scaled) pixel data to a raster from a Python array.

        This method transfers geophysically calibrated pixel values from a Python array-like
        object to the specified raster. The geophysical values are automatically converted to
        raw storage values using the raster's inverse scaling parameters (scale factors,
        offsets, and logarithmic transformations) before being stored in the raster's data structure.

        This allows you to work with physically meaningful values while the system handles
        the conversion to the appropriate raw storage format. The input data is validated
        to ensure it matches the expected raster dimensions.

        Args:
            raster (Raster): The target Raster object to receive the new geophysical pixel data.
                Must be a valid Raster instance with write access and defined scaling parameters.
            data (array-like): Array-like object containing geophysical pixel values in row-major
                order. Can be a Python list, array.array, or NumPy array. Length must equal
                raster width × height.

        Raises:
            ValueError: If data is None, empty, or doesn't match raster dimensions.

        Example:
            >>> band = product['B1']
            >>> # Create geophysical data (e.g., reflectance values 0.0 to 1.0)
            >>> import numpy as np
            >>> reflectance_data = np.random.random(band.width * band.height)
            >>> 
            >>> # Write geophysical data (automatically converted to raw storage)
            >>> RasterData.set_geophysical(band, reflectance_data)
            >>> 
            >>> # Verify by reading back
            >>> stored_geo_data = RasterData.get_geophysical(band)
            >>> print(f"Mean reflectance: {sum(stored_geo_data) / len(stored_geo_data):.4f}")
        """
        RasterData.validate(data, raster.width * raster.height)
        dataTransfer = JDT.create(data)
        dataTransfer.transferToRaster(raster._raster, data, True)

    @staticmethod
    def validate(data, raster_size):
        """Validate array-like data for raster transfer operations.

        This method performs comprehensive validation of input data to ensure it meets
        the requirements for raster data transfer operations. It checks for null values,
        empty arrays, proper array-like interface, and correct dimensionality to prevent
        errors during data transfer operations.

        The validation ensures that the data can be properly handled by the underlying
        Java data transfer mechanisms and that the array dimensions match the target
        raster's pixel count to prevent buffer overflows or underflows.

        Args:
            data (array-like): Input data to validate. Must be an array-like object
                with a length property (list, array.array, NumPy array, etc.).
            raster_size (int): Expected number of elements in the data array. Should typically
                equal the raster's width × height for full raster operations.

        Raises:
            ValueError: If data is None, empty, not array-like, or doesn't match expected size.
                Specific error messages indicate the validation failure reason.

        Example:
            >>> import array
            >>> import numpy as np
            >>> 
            >>> # Valid data examples
            >>> list_data = [1, 2, 3, 4, 5]
            >>> RasterData.validate(list_data, 5)  # OK
            >>> 
            >>> array_data = array.array('f', [1.0, 2.0, 3.0])
            >>> RasterData.validate(array_data, 3)  # OK
            >>> 
            >>> numpy_data = np.random.random(100)
            >>> RasterData.validate(numpy_data, 100)  # OK
            >>> 
            >>> # Invalid data examples
            >>> try:
            >>>     RasterData.validate(None, 10)  # Raises ValueError
            >>> except ValueError as e:
            >>>     print(f"Error: {e}")
            >>> 
            >>> try:
            >>>     RasterData.validate([1, 2, 3], 5)  # Size mismatch
            >>> except ValueError as e:
            >>>     print(f"Error: {e}")
        """
        if data is None:
                raise ValueError("Data cannot be None")

        # Handle NumPy arrays and other array-like objects
        try:
            data_len = len(data)
        except TypeError:
            raise ValueError("Data must be array-like with a length")

        if data_len == 0:
            raise ValueError("Data cannot be empty")

        if data_len != raster_size:
            raise ValueError(f"Size of data {data_len} does not match the raster size {raster_size}")


# @staticmethod
# def _direct_java_usage(raster):
#     RasterData.ensureloadedData(raster)
#     return raster._raster.getRasterData().getElems()
#
#
# @staticmethod
# def _list_wrapped_java_array(raster):
#     RasterData.ensureloadedData(raster)
#     return list(raster._raster.getRasterData().getElems())

# @staticmethod
# def array_from_bytebuffer(raster):
#   # load data ~200 seconds
#   # computation afterwards ~103 seconds
#   from .raster import Raster
#   RasterData.ensureloadedData(raster)
#   bb = JDT.rasterToDirectBuffer(raster._raster)
#   typecode = Raster._SNAP_TO_PYTHON_TYPE[raster.data_type]
#   # Move to start just in case
#   bb.position(0)
#   java_bytes = jarray.zeros(bb.remaining(), 'b')
#   # Read all bytes once
#   bb.get(java_bytes)  # bulk read into Python-managed buffer
#   # Now build a Python-managed bytearray from the Java byte[]
#   # Build typed Python array in one copy
#   b = bytearray((x & 0xFF) for x in java_bytes)
#   arr = pyarray.array(typecode)
#   arr.frombytes(b)  # interpret with native endianness
#
#   # better if numpy is available
#   # import numpy as np
#   # b = bytearray(java_bytes)
#   # arr = np.frombuffer(b, dtype=np.float64)
#
#   return arr
#
# @staticmethod
# def optimized_array_from_bytebuffer(raster):
#   from .raster import Raster
#   RasterData.ensureloadedData(raster)
#   bb = JDT.rasterToDirectBuffer(raster._raster)
#   typecode = Raster._SNAP_TO_PYTHON_TYPE[raster.data_type]
#
#   bb.position(0)
#   java_bytes = jarray.zeros(bb.remaining(), 'b')
#   bb.get(java_bytes)  # Works because java_bytes is Java array
#
#   # Direct conversion without intermediate bytearray creation
#   python_bytes = bytearray((x & 0xFF) for x in java_bytes)
#   arr = pyarray.array(typecode)
#   arr.frombytes(python_bytes)
#   return arr
#
# @staticmethod
# def optimized_bytebuffer_2(raster):
#   # load data ~185 seconds
#   # computation afterwards ~108 seconds
#   from .raster import Raster
#   RasterData.ensureloadedData(raster)
#
#   # Get data as Java byte array (this works with GraalPython)
#   java_byte_array = JDT.rasterToByteArray(raster._raster)
#   typecode = Raster._SNAP_TO_PYTHON_TYPE[raster.data_type]
#
#   # Convert Java byte array to Python bytearray
#   python_bytes = bytearray((x & 0xFF) for x in java_byte_array)
#
#   # Create typed array from bytes
#   arr = pyarray.array(typecode)
#   arr.frombytes(python_bytes)
#   return arr
#
# @staticmethod
# def ensureloadedData(raster):
#     if not raster._raster.hasRasterData():
#         raster._raster.loadRasterData()
