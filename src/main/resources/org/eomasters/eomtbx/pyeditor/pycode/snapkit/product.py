"""
Remote sensing product management and manipulation module.

This module provides a Python interface for working with remote sensing products
wrapping SNAP's Java API. It offers comprehensive functionality for loading, creating,
manipulating, and saving remote sensing datasets with their associated metadata,
raster data, and geospatial information.

The main component is the Product class, which serves as a wrapper around SNAP's
Java Product class, providing convenient Pythonic access to:
- Raster data (bands, tie-point grids, masks)
- Product metadata and properties
- Geolocation and coordinate transformations
- Placemarks (pins and ground control points)
- Virtual band creation and mathematical operations
- Product I/O operations in various formats

Key Features:
    - Load products from various formats (BEAM-DIMAP, GeoTIFF, NetCDF, etc.)
    - Create new products from scratch with custom dimensions
    - Add and manipulate raster bands, including virtual bands with expressions
    - Manage metadata and product properties
    - Handle coordinate transformations between pixel and geographic coordinates
    - Create and manage placemarks for reference points
    - Save products in multiple output formats

Usage:
    Basic product operations:

        >>> from snapkit.product import Product
        >>>
        >>> # Load an existing product
        >>> product = Product.read('/path/to/product.dim')
        >>> print(f"Product: {product.name}, Size: {product.width}x{product.height}")
        >>>
        >>> # Access raster data
        >>> band1 = product['B1']  # Get specific band
        >>> all_bands = product.rasters  # Get all bands as dictionary
        >>>
        >>> # Work with coordinates (if geo-coded)
        >>> if product.is_geo_coded:
        >>>     pixel_x, pixel_y = product.pixel_pos(52.5, 13.4)  # Berlin coordinates
        >>>     lat, lon = product.geo_pos(100, 200)  # Convert pixel to geo
        >>>
        >>> # Add placemarks
        >>> product.add_pin("POI", 100, 200, "Point of Interest")
        >>> product.add_gcp("GCP1", 150, 250, 52.5, 13.4, "Control Point")
        >>>
        >>> # Create virtual bands
        >>> ndvi = product.add_virtual_band("NDVI", "(B8 - B4) / (B8 + B4)")
        >>>
        >>> # Save in different formats
        >>> product.save_as('/path/to/output.dim')  # BEAM-DIMAP format
        >>> product.save_as('/path/to/output.tif', 'GeoTIFF')

    Creating new products:

        >>> # Create empty product
        >>> new_product = Product("MyProduct", "Custom")
        >>>
        >>> # Create product with dimensions
        >>> sized_product = Product("SizedProduct", "Custom", [1024, 1024])
        >>>
        >>> # Add bands to new product
        >>> data_band = sized_product.add_band("temperature", 'd')  # double precision
        >>> class_band = sized_product.add_band("classification", 'b')  # byte
        >>>
        >>> # Add masks
        >>> cloud_mask = sized_product.add_mask("clouds", "temperature > 273",
        >>>                                     "Cloud pixels", (255, 255, 255, 128))

"""
from org.esa.snap.core.datamodel import GcpDescriptor as JGcpDescriptor
from org.esa.snap.core.datamodel import GeoPos as JGeoPos
from org.esa.snap.core.datamodel import PinDescriptor as JPinDescriptor
from org.esa.snap.core.datamodel import PixelPos as JPixelPos
from org.esa.snap.core.datamodel import Product as JProduct
from org.esa.snap.core.datamodel import ProductData as JProductData

from org.eomasters.eomtbx.utils import MetadataUtils as JMetadataUtils
from ._productUtils import *
from .placemark import Placemark
from .raster import Raster


# Package-internal factory: do not export this from the package API.
def _create_product(jproduct: JProduct) -> 'Product':
    return Product(None, None, size=None, _java_product=jproduct)


class Product:
    """Represents a remote sensing product with raster data, metadata, and geolocation information.

    This class serves as a Python wrapper around SNAP's Java Product class, providing
    convenient access to remote sensing product data including raster bands, metadata,
    and geolocation information. Products can be created from scratch or loaded from
    existing files, and support various operations like adding/removing raster data,
    managing placemarks (pins and GCPs), and saving to different formats.

    The Product class acts as a container for all data associated with a remote sensing
    dataset, including multiple raster bands, tie-point grids, masks, and associated
    metadata. It provides both low-level access to the underlying SNAP functionality
    and high-level convenience methods for common operations.

    Example:
        >>> # Create a new product
        >>> product = Product("MyProduct", "Custom", [512, 512])
        >>> 
        >>> # Load an existing product
        >>> product = Product.read('/path/to/product.dim')
        >>> 
        >>> # Access product properties
        >>> print(f"Product: {product.name}, Size: {product.width}x{product.height}")
        >>> 
        >>> # Work with rasters
        >>> band = product['band_1']  # Access band by name
        >>> all_bands = product.rasters  # Get all bands as dictionary
    """

    def __init__(self, name: str, product_type: str, size: [int, int] = None, _java_product: JProduct = None) -> None:
        """Initialize a new Product instance or wrap an existing Java Product.Creates a new SNAP Product with a name,
        a product type, and an optional size.

        Args:
            name (str): The name identifier for the product.
            product_type (str): The type/format identifier of the product (e.g., 'Sentinel-2', 'Landsat-8').
            size (list[int]): Raster dimensions as [width, height] in pixels.
                If None, creates a product without predefined dimensions. Defaults to None.

        Example:
            >>> # Create a new empty product
            >>> product = Product("MyProduct", "Custom")
            >>> 
            >>> # Create a product with specific dimensions
            >>> product = Product("MyProduct", "Custom", [1024, 1024])
        """
        if _java_product is not None:
            self._product = _java_product
        else:
            if size is None:
                self._product = JProduct(name, product_type)
            elif len(size) < 2:
                raise ValueError("Size must be a list of two integers")
            else:
                self._product = JProduct(name, product_type, size[0], size[1])

    def __exit__(self, exc_type: type, exc_val: Exception, exc_tb: any):
        """Clean up resources when exiting a context manager.

        This method is called when the Product is used as a context manager and the
        context is exited. It properly disposes of the underlying Java Product object
        to free memory and resources.

        Args:
            exc_type (type): Exception type if an exception occurred.
            exc_val (Exception): Exception instance if an exception occurred.
            exc_tb (traceback): Traceback object if an exception occurred.

        Example:
            >>> with Product.read('/path/to/product.dim') as product:
            >>>     print(f"Product: {product.name}")
            # Product is automatically disposed when exiting the with block
        """
        self._product.dispose()
        self._product = None

    @property
    def name(self) -> str:
        """Get the name of the product.

        Returns:
            str: The name of the product as set during creation or loading.

        Example:
            >>> product = Product("Sentinel2_L1C", "S2_MSI_Level-1C")
            >>> print(product.name)  # "Sentinel2_L1C"
        """
        return self._product.getName()

    @property
    def width(self):
        """Get the width of the product's scene raster.

        Returns:
            int: The width of the scene raster in pixels.

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> print(f"Product width: {product.width} pixels")
        """
        return self._product.getSceneRasterWidth()

    @property
    def height(self):
        """Get the height of the product's scene raster.

        Returns:
            int: The height of the scene raster in pixels.

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> print(f"Product height: {product.height} pixels")
        """
        return self._product.getSceneRasterHeight()

    @property
    def is_geo_coded(self):
        """Check if the product has valid geo-coding information.

        Returns:
            bool: True if the product has geo-coding information that allows conversion
                between pixel and geographic coordinates, False otherwise.

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> if product.is_geo_coded:
            >>>     lat_lon = product.geo_pos(100, 100)
            >>>     print(f"Pixel (100,100) is at {lat_lon}")
        """
        jgeo_coding = self._product.getSceneGeoCoding()
        return False if jgeo_coding is None else True

    @property
    def rasters(self):
        """Get all raster data nodes as a dictionary mapping names to Raster objects.

        This property provides access to all raster data nodes (bands, tie-point grids,
        masks, etc.) contained in the product as a convenient dictionary structure.

        Returns:
            dict[str, Raster]: A dictionary where keys are raster names and values are
                Raster objects wrapping the underlying SNAP raster data nodes.

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> rasters = product.rasters
            >>> print(f"Available rasters: {list(rasters.keys())}")
            >>> band1 = rasters['B1']  # Access specific band
            >>> for name, raster in rasters.items():
            >>>     print(f"Raster {name}: {raster.width}x{raster.height}")
        """
        raster_dict = {}
        for raster in self._product.getRasterDataNodes():
            raster_dict[raster.getName()] = Raster._create_raster(raster)
        return raster_dict

    @property
    def file_location(self) -> str:
        """Get the file location of the product.

        Returns:
            str: The file location of the product as a string.

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> file = product.file_location  # Get file location as string
            >>> product.save_as(file)
        """
        return self._product.getFileLocation().toString()

    def __getitem__(self, key):
        """Access a raster data node by name using dictionary-like syntax.

        This method enables dictionary-style access to raster data nodes (bands, masks,
        tie-point grids) within the product using square bracket notation.

        Args:
            key (str): The name of the raster data node to retrieve.

        Returns:
            Raster: A Raster object wrapping the requested raster data node.

        Raises:
            KeyError: If no raster data node with the specified name exists in the product.

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> band1 = product['B1']  # Access band named 'B1'
            >>> mask = product['cloud_mask']  # Access mask named 'cloud_mask'
            >>> print(f"Band shape: {band1.width}x{band1.height}")
        """
        raster = self._product.getRasterDataNode(key)
        if raster is None:
            raise KeyError(f"Raster '{key}' not found")
        return Raster._create_raster(raster)

    def pixel_pos(self, lat, lon) -> [float, float]:
        """Convert geographic coordinates to pixel coordinates.

        This method transforms latitude/longitude coordinates to pixel coordinates
        using the product's geo-coding information. Requires the product to have
        valid geo-coding.

        Args:
            lat (float): Latitude in decimal degrees.
            lon (float): Longitude in decimal degrees.

        Returns:
            tuple[float, float]: Pixel coordinates as (x, y) tuple in pixels,
                or None if conversion fails or product lacks geo-coding.

        Example:
            >>> product = Product.read('/path/to/geocoded_product.dim')
            >>> if product.is_geo_coded:
            >>>     pixel_x, pixel_y = product.pixel_pos(52.5, 13.4)
            >>>     print(f"Berlin is at pixel ({pixel_x}, {pixel_y})")
        """
        return get_pixel_pos(self._product.getSceneGeoCoding(), lat, lon)

    def geo_pos(self, x, y) -> [float, float]:
        """Convert pixel coordinates to geographic coordinates."""
        """Convert pixel coordinates to geographic coordinates.

        This method transforms pixel coordinates to latitude/longitude coordinates
        using the product's geo-coding information. Requires the product to have
        valid geo-coding.

        Args:
            x (float): Pixel x-coordinate (column).
            y (float): Pixel y-coordinate (row).

        Returns:
            tuple[float, float]: Geographic coordinates as (latitude, longitude)
                tuple in decimal degrees, or None if conversion fails or product lacks geo-coding.

        Example:
            >>> product = Product.read('/path/to/geocoded_product.dim')
            >>> if product.is_geo_coded:
            >>>     lat, lon = product.geo_pos(100, 200)
            >>>     print(f"Pixel (100,200) is at {lat}°N, {lon}°E")
        """
        return get_geo_pos(self._product.getSceneGeoCoding(), x, y)

    def add_pin(self, name, x, y, label=None) -> Placemark:
        """Add a pin placemark to the product at the specified pixel coordinates.

        Pins are point placemarks that mark specific pixel locations within the product
        for reference or analysis purposes. They are stored in the product's pin group
        and can be accessed later through the pins property.

        Args:
            name (str): Unique name identifier for the pin.
            x (float): Pixel x-coordinate (column) where to place the pin.
            y (float): Pixel y-coordinate (row) where to place the pin.
            label (str): Optional descriptive label for the pin.
                If None, the name will be used as the label. Defaults to None.

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> product.add_pin("POI1", 100, 200, "Point of Interest 1")
            >>> product.add_pin("Center", product.width//2, product.height//2)
            >>> print(f"Added {len(product.pins)} pins")
        """
        pin = JPlacemark.createPointPlacemark(JPinDescriptor.getInstance(), name, label, None,
                                              JPixelPos(x, y), None, self._product.getSceneGeoCoding())
        self._product.getPinGroup().addPlacemark(pin)
        return pin

    def add_gcp(self, name, x, y, lat, lon, label=None) -> Placemark:
        """Add a ground control point (GCP) to the product with pixel and geographic coordinates.

        Ground Control Points are placemarks that contain both pixel coordinates and
        corresponding geographic coordinates. They are essential for geo-referencing,
        orthorectification, and accuracy assessment of remote sensing products.

        Args:
            name (str): Unique name identifier for the GCP.
            x (float): Pixel x-coordinate (column) of the GCP location.
            y (float): Pixel y-coordinate (row) of the GCP location.
            lat (float): Latitude of the GCP in decimal degrees.
            lon (float): Longitude of the GCP in decimal degrees.
            label (str): Optional descriptive label for the GCP.
                If None, the name will be used as the label. Defaults to None.

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> # Add GCP for Berlin Brandenburg Gate
            >>> product.add_gcp("Berlin_Gate", 1250, 800, 52.5163, 13.3777, "Brandenburg Gate")
            >>> # Add GCP for London Big Ben
            >>> product.add_gcp("London_BigBen", 2100, 1200, 51.4994, -0.1245, "Big Ben")
            >>> print(f"Added {len(product.gcps)} GCPs")
        """
        gcp = JPlacemark.createPointPlacemark(JGcpDescriptor.getInstance(), name, label, None,
                                              JPixelPos(x, y), JGeoPos(lat, lon), self._product.getSceneGeoCoding())
        self._product.getGcpGroup().addPlacemark(gcp)
        return gcp

    @property
    def pins(self):
        """Get all pin placemarks as a dictionary mapping names to placemark objects.

        Pins are point placemarks that mark specific pixel locations within the product.
        They are useful for marking points of interest or reference locations.

        Returns:
            dict[str, object]: A dictionary where keys are pin names and values are
                placemark objects representing the pins.

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> product.add_pin("POI1", 100, 200, "Point of Interest")
            >>> pins = product.pins
            >>> print(f"Available pins: {list(pins.keys())}")
        """
        return get_placemark_dict(self._product.getPinGroup())

    @property
    def gcps(self):
        """Get all ground control point placemarks as a dictionary mapping names to placemark objects.

        Ground Control Points (GCPs) are placemarks that contain both pixel coordinates
        and geographic coordinates, used for geo-referencing and accuracy assessment.

        Returns:
            dict[str, object]: A dictionary where keys are GCP names and values are
                placemark objects representing the ground control points.

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> product.add_gcp("GCP1", 100, 200, 52.5, 13.4, "Control Point")
            >>> gcps = product.gcps
            >>> print(f"Available GCPs: {list(gcps.keys())}")
        """
        return get_placemark_dict(self._product.getGcpGroup())

    def get_metadata(self, path) -> str:
        """Retrieve metadata attribute value from the product using a hierarchical path.

        This method searches the product's metadata tree using a slash-separated path
        to locate and retrieve specific metadata attributes. The path follows the
        hierarchical structure of the metadata tree.

        Args:
            path (str): Slash-separated path to the metadata attribute (e.g.,
                'General_Info/Product_Info.PRODUCT_TYPE' or 'Processing_Info/PROCESSING_LEVEL').

        Returns:
            str: The metadata value as a string if found and not None,
                otherwise None if the attribute doesn't exist or has no value.

        Example:
            >>> product = Product.read('/path/to/sentinel2_product.dim')
            >>> product_type = product.get_metadata('General_Info.Product_Info.PRODUCT_TYPE')
            >>> print(f"Product type: {product_type}")
            >>> 
            >>> processing_level = product.get_metadata('Processing_Info/PROCESSING_LEVEL')
            >>> if processing_level:
            >>>     print(f"Processing level: {processing_level}")
        """
        attribute = JMetadataUtils.getAttributeFromPath(self._product.getMetadataRooo(), path)
        return attribute.toString() if attribute is not None else None

    def add_virtual_band(self, name, expression):
        """Add a virtual band to the product using a mathematical expression.

        Virtual bands are computed on-the-fly using mathematical expressions that can
        reference other bands and constants. They don't store data directly but calculate
        values dynamically when accessed, saving memory and storage space.

        Args:
            name (str): Unique name for the new virtual band.
            expression (str): Mathematical expression defining how the band values are computed.
                Can reference existing bands using their names (e.g., 'B1 + B2', 'sqrt(B3 * B4)').

        Returns:
            Raster: A Raster object wrapping the newly created virtual band.

        Example:
            >>> product = Product.read('/path/to/sentinel2_product.dim')
            >>> # Create NDVI virtual band
            >>> ndvi = product.add_virtual_band("NDVI", "(B8 - B4) / (B8 + B4)")
            >>> # Create a custom ratio band
            >>> ratio = product.add_virtual_band("Green_Red_Ratio", "B3 / B2")
            >>> print(f"Added virtual band: {ndvi.name}")
        """
        return Raster._create_raster(self._product.addBand(name, expression))

    def add_band(self, name, data_type=Raster.TYPE_FLOAT32, size: tuple[int, int] = None):
        """Add a new empty band to the product with specified data type.

        This method creates a new raster band with the same dimensions as the product's
        scene raster. The band is initially filled with no-data values and can be
        populated with data after creation.

        Args:
            name (str): Unique name for the new band.
            data_type (str, optional): Data type for the band values. Valid options:
                    TYPE_BYTE = 'b' = byte (8-bit signed),
                    TYPE_UBYTE = 'B' = unsigned byte (8-bit unsigned),
                    TYPE_SHORT = 'h' = short (16-bit signed),
                    TYPE_USHORT = 'H' = unsigned short (16-bit unsigned),
                    TYPE_INT = 'i' = int (32-bit signed),
                    TYPE_UINT 'I' = unsigned int (32-bit unsigned),
                    TYPE_FLOAT= 'f' = float (32-bit),
                    TYPE_DOUBLE = 'd' = double (64-bit).
                Defaults to 'f'.
            size (list[int]): Raster dimensions as [width, height] in pixels.
                If None, uses the scene raster size of the product. Defaults to None.

        Returns:
            Raster: A Raster object wrapping the newly created band.

        Raises:
            ValueError: If size is None and product has no scene raster size or of data type is not supported.

        Example:
            >>> product = Product("MyProduct", "Custom", [512, 512])
            >>> # Add a single precision band
            >>> data_band = product.add_band("temperature", 'f', [10, 10])
            >>> # Add a byte band for classification results
            >>> class_band = product.add_band("classification", Raster.TYPE_BYTE))
            >>> print(f"Added band: {data_band.name} with type {data_band.data_type}")
        """

        if data_type not in Raster._PYTHON_TYPE_TO_SNAP:
            raise ValueError(f"Data type '{data_type}' is not supported")
        snap_type = Raster._PYTHON_TYPE_TO_SNAP[data_type]
        if size is None:
            if self._product.getSceneRasterSize() is None:
                raise ValueError("Product has no scene raster size, cannot add band. Provide size parameter instead.")
            return Raster._create_raster(self._product.addBand(name, snap_type))
        else:
            return Raster._create_raster(self._product.addBand(JBand(name, snap_type, size[0], size[1])))

    def add_mask(self, name, expression, description="", color=(255, 0, 0, 128)):
        """Add a mask to the product using a boolean expression.

        Masks are boolean raster layers that identify pixels meeting certain criteria.
        They are commonly used for cloud masking, water detection, quality flags, etc.
        Masks can be visualized with custom colors and transparency.

        Args:
            name (str): Unique name for the new mask.
            expression (str): Boolean expression defining the mask condition. Should evaluate
                to true/false for each pixel (e.g., 'B1 > 0.3', 'B4 < 0.1 AND B3 > 0.2').
            description (str, optional): Descriptive text explaining the mask purpose.
                Defaults to empty string.
            color (tuple[int, int, int] | tuple[int, int, int, int], optional): RGB or RGBA
                color tuple for mask visualization. Values should be 0-255. If RGB provided,
                transparency defaults to 128. Defaults to (255, 0, 0, 128) - semi-transparent red.

        Returns:
            object: The newly created mask object from the underlying SNAP API.

        Raises:
            ValueError: If color tuple has less than 3 elements.

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> # Add cloud mask with blue color
            >>> cloud_mask = product.add_mask("clouds", "B1 > 0.3", "Cloud pixels", (0, 0, 255, 100))
            >>> # Add water mask with cyan color
            >>> water_mask = product.add_mask("water", "B3 < 0.1", "Water bodies", (0, 255, 255))
            >>> print(f"Added mask: {cloud_mask.getName()}")
        """
        if len(color) < 3:
            raise ValueError("color must be a tuple of length 3 or 4")
        transparency = (color[3] if color[3] else 128) / 255.0
        return self._product.addMask(name, expression, description, color[:3], transparency)

    def del_raster(self, name):
        """Remove a raster data node from the product by name.

        This method removes raster data nodes (bands, masks, or tie-point grids) from
        the product. It searches all raster groups and removes the first match found.
        The removal is permanent and cannot be undone.

        Args:
            name (str): Name of the raster data node to remove.

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> # Remove a band
            >>> product.del_raster("B1")
            >>> # Remove a mask
            >>> product.del_raster("cloud_mask")
            >>> print(f"Remaining rasters: {list(product.rasters.keys())}")
        """
        del_from_group(name, self._product.getBandGroup())
        del_from_group(name, self._product.getMaskGroup())
        del_from_group(name, self._product.getTiePointGridGroup())

    def del_pin(self, name):
        """Remove a pin placemark from the product by name.

        This method removes a pin placemark from the product's pin group. The removal
        is permanent and cannot be undone.

        Args:
            name (str): Name of the pin placemark to remove.

        Returns:
            object: The removed pin placemark object if found and removed,
                None if no pin with the specified name exists.

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> product.add_pin("temp_pin", 100, 100)
            >>> removed_pin = product.del_pin("temp_pin")
            >>> if removed_pin:
            >>>     print("Pin successfully removed")
            >>> print(f"Remaining pins: {list(product.pins.keys())}")
        """
        group = self._product.getPinGroup()
        return group.remove(group.get(name))

    def del_gcp(self, name) -> Placemark:
        """Remove a ground control point (GCP) placemark from the product by name.

        This method removes a GCP placemark from the product's GCP group. The removal
        is permanent and cannot be undone.

        Args:
            name (str): Name of the GCP placemark to remove.

        Returns:
            Placemark: The removed GCP placemark object if found and removed,
                None if no GCP with the specified name exists.

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> product.add_gcp("temp_gcp", 100, 100, 52.5, 13.4)
            >>> removed_gcp = product.del_gcp("temp_gcp")
            >>> if removed_gcp:
            >>>     print("GCP successfully removed")
            >>> print(f"Remaining GCPs: {list(product.gcps.keys())}")
        """
        group = self._product.getGcpGroup()
        return group.remove(group.get(name))

    def save_as(self, file_path, format_name=None):
        """Save the product to a file in the specified format.

        This method saves the product to disk using SNAP's ProductIO functionality.
        The output format is determined by the format_name parameter or defaults
        to BEAM-DIMAP format if not specified.

        Args:
            file_path (str): Path where the product file will be written. The file
                extension should match the chosen format.
            format_name (str): Name of the output format (e.g.,
                'BEAM-DIMAP', 'GeoTIFF', 'NetCDF'). If None, defaults to 'BEAM-DIMAP'.
                Use ProductIO.output_formats() to see available formats.

        Example:
            >>> product = Product.read('/path/to/input.dim')
            >>> # Save in BEAM-DIMAP format (default)
            >>> product.save_as('/path/to/output.dim')
            >>> # Save in GeoTIFF format
            >>> product.save_as('/path/to/output.tif', 'GeoTIFF')
            >>> # Save in NetCDF format
            >>> product.save_as('/path/to/output.nc', 'NetCDF')
        """
        from product_io import ProductIO
        ProductIO.save(self, file_path, format_name)

    @staticmethod
    def read(file_path):
        """Read a product from a file using SNAP's ProductIO functionality.

        This static method creates a new Product instance by loading data from the
        specified file path. The format is automatically detected based on the file
        content and extension.

        Args:
            file_path (str): Path to the product file to be read. Can be any format
                supported by SNAP's reader plugins (e.g., .dim, .nc, .tif, .zip).

        Returns:
            Product: A new Product instance wrapping the loaded data.

        Raises:
            FileNotFoundError: If the file cannot be read or does not exist.

        Example:
            >>> # Read various product formats
            >>> sentinel2 = Product.read('/path/to/sentinel2.zip')
            >>> landsat = Product.read('/path/to/landsat.tar.gz')
            >>> dimap = Product.read('/path/to/product.dim')
            >>> geotiff = Product.read('/path/to/raster.tif')
            >>> print(f"Loaded product: {sentinel2.name}")
        """
        from .product_io import ProductIO
        return ProductIO.read(file_path)

    def __repr__(self):
        """Return a string representation of the Product instance.

        This method provides a concise string representation showing key product
        characteristics for debugging and logging purposes.

        Returns:
            str: String representation in the format:
                "Product(name='<name>', width=<w>, height=<h>, rasters=<count>)"

        Example:
            >>> product = Product.read('/path/to/product.dim')
            >>> print(repr(product))
            Product(name='S2A_MSIL1C_20230415T103031', width=10980, height=10980, rasters=13)
            >>> 
            >>> # Also works with print()
            >>> print(product)
            Product(name='S2A_MSIL1C_20230415T103031', width=10980, height=10980, rasters=13)
        """
        return f"Product(name='{self.name}', width={self.width}, height={self.height}, rasters={len(self.rasters)})"
