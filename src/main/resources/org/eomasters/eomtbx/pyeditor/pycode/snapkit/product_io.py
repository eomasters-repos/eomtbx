"""
Product Input/Output utilities for remote sensing data.

This module provides I/O functionality for remote sensing products through SNAP's
Java ProductIO API. It serves as a bridge between Python and SNAP's comprehensive
file format support, enabling reading from and writing to various remote sensing
data formats including BEAM-DIMAP, GeoTIFF, NetCDF, HDF5, and many others.

The module provides a ProductIO class with static methods for product I/O operations. 
It abstracts SNAP's complex Java I/O system into simple Python class methods while 
maintaining full access to format-specific capabilities and metadata.

Key Features:
    - Read products from 50+ supported formats (Sentinel, Landsat, MODIS, etc.)
    - Write products to various output formats with format-specific options
    - Automatic format detection based on file content and extensions
    - Query available input/output formats and their file extensions
    - Seamless integration with Product class for data manipulation workflows

Supported Formats (examples):
    Input formats: BEAM-DIMAP (.dim), GeoTIFF (.tif/.tiff), NetCDF (.nc),
                   HDF5 (.h5/.hdf5), Sentinel SAFE (.zip), Landsat (.tar.gz),
                   ENVISAT (.N1), MODIS (.hdf), and many more
    Output formats: BEAM-DIMAP (.dim), GeoTIFF (.tif), NetCDF (.nc),
                    PNG (.png), JPEG (.jpg), BigGeoTIFF (.btf), and others

ProductIO Class Methods:
    ProductIO.read: Read a product from a file using ProductIO
    ProductIO.save: Save a product to a file in the specified format
    ProductIO.input_formats: Get all available input formats and their file extensions
    ProductIO.output_formats: Get all available output formats and their file extensions

Usage:
    Reading products from various formats:

        >>> from snapkit import ProductIO
        >>>
        >>> # Read different format types
        >>> sentinel2 = ProductIO.read('/path/to/S2A_MSIL1C_product.zip')
        >>> landsat = ProductIO.read('/path/to/landsat_scene.tar.gz')
        >>> geotiff = ProductIO.read('/path/to/raster_data.tif')
        >>> dimap = ProductIO.read('/path/to/product.dim')
        >>>
        >>> print(f"Loaded: {sentinel2.name}")

    Saving products in different formats:

        >>> # Save in various formats
        >>> ProductIO.saveAs(product, '/output/product.dim')  # Default BEAM-DIMAP
        >>> ProductIO.saveAs(product, '/output/raster.tif', 'GeoTIFF')
        >>> ProductIO.saveAs(product, '/output/data.nc', 'NetCDF')
        >>> ProductIO.saveAs(product, '/output/image.png', 'PNG')

    Querying available formats:

        >>> # Check supported input formats
        >>> input_formats = ProductIO.input_formats()
        >>> print("GeoTIFF extensions:", input_formats.get('GeoTIFF', []))
        >>>
        >>> # Check supported output formats
        >>> output_formats = ProductIO.output_formats()
        >>> available = list(output_formats.keys())
        >>> print(f"Available output formats: {len(available)}")

Example:
    Working with Product I/O functions:

        >>> from snapkit import ProductIO
        >>> 
        >>> # Read a product
        >>> product = ProductIO.read('/path/to/product.dim')
        >>> print(f"Product: {product.name}, Size: {product.width}x{product.height}")
        >>> 
        >>> # Query available formats
        >>> formats = ProductIO.output_formats()
        >>> print(f"Available output formats: {len(formats)}")
        >>> 
        >>> # Save product in different formats
        >>> ProductIO.saveAs(product, '/path/to/output.tif', 'GeoTIFF')

"""
import os
from org.esa.snap.core.dataio import ProductIO as JProductIO
from org.esa.snap.core.dataio import ProductIOPlugInManager as JPluginManager

from .product import Product


class ProductIO:
    """Product Input/Output utilities for remote sensing data.
    
    This class provides static methods for I/O functionality for remote sensing 
    products through SNAP's Java ProductIO API.
    """

    def __new__(cls):
        """Class should not be instantiated as it only provides static methods."""
        raise TypeError(f"{cls.__name__} class should not be instantiated - use static methods only")

    @staticmethod
    def read(file_path: str) -> Product:
        """Read a product from a file using ProductIO.

        This static method reads a remote sensing product from the specified file path using
        SNAP's IO functionality. The file path is automatically converted to an
        absolute path before processing.

        Args:
            file_path (str): Path to the product file to be read.

        Returns:
            Product: A new Product instance wrapping the loaded Java product.

        Raises:
            FileNotFoundError: If the product file cannot be read or does not exist.

        Example:
            >>> from snapkit import ProductIO
            >>> product = ProductIO.read('/path/to/product.nc')
            >>> print(product.name)
        """
        file_path = os.path.abspath(file_path)
        java_product = JProductIO.readProduct(file_path)
        if java_product is None:
            raise FileNotFoundError(f"Could not read product from '{file_path}'")
        from .product import _create_product
        return _create_product(java_product)

    @staticmethod
    def save(product: Product, file_path: str, format_name: str = "BEAM-DIMAP"):
        """Save a product to a file in the specified format.

        This static method saves a Product instance to the specified file path using the given
        format. The file path is automatically converted to an absolute path before
        processing.
        Which output formats are supported depends on the installed SNAP plugins. You can get the
        available formats using the output_formats() static method.

        Args:
            product (Product): The Product instance to be saved.
            file_path (str): Path where the output file will be written.
            format_name (str, optional): Format name for the output file (e.g., 'BEAM-DIMAP',
                'GeoTIFF', 'NetCDF'). Defaults to "BEAM-DIMAP".

        Example:
            >>> from snapkit import ProductIO
            >>> product = ProductIO.read('/path/to/input.dim')
            >>> ProductIO.saveAs(product, '/path/to/output.dim', 'BEAM-DIMAP')
        """
        from java.io import File
        abs_path = os.path.abspath(file_path)
        JProductIO.writeProduct(product._product, File(abs_path), format_name, True)

    @staticmethod
    def input_formats() -> dict[str, list[str]]:
        """Get all available input formats and their associated file extensions.

        This static method queries SNAP's ProductIOPlugInManager to retrieve all registered
        reader plugins and returns their format names along with supported file extensions
        as a dictionary.

        Returns:
            dict[str, list[str]]: A dictionary mapping format names to lists of supported
                file extensions. The keys are format names (e.g., 'BEAM-DIMAP', 'GeoTIFF')
                and values are lists of file extensions (e.g., ['.dim', '.data']).

        Example:
            >>> from snapkit import ProductIO
            >>> formats = ProductIO.input_formats()
            >>> print(formats['BEAM-DIMAP'])
            ['.dim']
        """
        plugins = JPluginManager.getInstance().getAllReaderPlugIns()
        return ProductIO._io_formats_dict(plugins)

    @staticmethod
    def output_formats() -> dict[str, list[str]]:
        """Get all available output formats and their associated file extensions.

        This static method queries SNAP's ProductIOPlugInManager to retrieve all registered
        writer plugins and returns their format names along with supported file extensions
        as a dictionary.

        Returns:
            dict[str, list[str]]: A dictionary mapping format names to lists of supported
                file extensions. The keys are format names (e.g., 'BEAM-DIMAP', 'GeoTIFF')
                and values are lists of file extensions (e.g., ['.dim', '.data']).

        Example:
            >>> from snapkit import ProductIO
            >>> formats = ProductIO.output_formats()
            >>> print(formats['GeoTIFF'])
            ['.tif', '.tiff', '.gtif', '.btf', '.zip']
        """
        plugins = JPluginManager.getInstance().getAllWriterPlugIns()
        return ProductIO._io_formats_dict(plugins)

    @staticmethod
    def _io_formats_dict(plugins) -> dict[str, list[str]]:
        """Internal helper static method to convert plugin information to format dictionary."""
        result = {}
        for plugin in plugins:
            names = plugin.getFormatNames()
            extensions = plugin.getDefaultFileExtensions()
            if names and len(names) > 0:
                # Convert Java arrays to Python lists if needed
                result[names[0]] = list(extensions)
        return result
