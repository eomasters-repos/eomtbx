from org.esa.snap.core.datamodel import GeoPos as JGeoPos
from org.esa.snap.core.datamodel import PixelPos as JPixelPos

from .placemark import _create_placemark



"""Utility class providing coordinate conversion and placemark management operations for SNAP products.

This class offers static methods for common product operations including coordinate transformations
between pixel and geographic coordinate systems, placemark dictionary creation from SNAP groups,
and group element management. It acts as a helper class for the Product and Raster classes,
providing low-level operations that bridge Python and SNAP's Java API.

The class handles geo-coding operations that allow conversion between pixel coordinates (x, y)
and geographic coordinates (latitude, longitude) using SNAP's geo-coding framework. It also
provides functionality for managing placemark collections and group operations commonly used
in remote sensing product manipulation.

Note:
    This class cannot be instantiated. All methods are static and should be called
    directly on the class. Methods require valid SNAP Java objects as parameters.

Example:
    >>> from ._productUtils import *
    >>> # Convert geographic to pixel coordinates
    >>> geo_coding = product._product.getSceneGeoCoding()
    >>> x, y = get_pixel_pos(geo_coding, 52.5, 13.4)
    >>> print(f"Berlin is at pixel ({x}, {y})")
    >>> 
    >>> # Convert pixel to geographic coordinates
    >>> lat, lon = get_geo_pos(geo_coding, 100, 200)
    >>> print(f"Pixel (100,200) is at {lat}°N, {lon}°E")
    >>> 
    >>> # Get placemarks as dictionary
    >>> pin_group = product._product.getPinGroup()
    >>> pins = get_placemark_dict(pin_group)
    >>> print(f"Found {len(pins)} pins")
"""

def get_pixel_pos(jgeo_coding, lat, lon):
    """Convert geographic coordinates to pixel coordinates using the provided geo-coding.

    This method transforms latitude and longitude coordinates to pixel coordinates (x, y)
    using SNAP's geo-coding functionality. It creates a Java GeoPos object from the input
    coordinates and uses the geo-coding's transformation capabilities to compute the
    corresponding pixel position.

    Args:
        jgeo_coding (org.esa.snap.core.datamodel.GeoCoding): The SNAP geo-coding object
            that provides the coordinate transformation functionality. Must support pixel
            position computation (canGetPixelPos should be True).
        lat (float): Latitude coordinate in decimal degrees.
        lon (float): Longitude coordinate in decimal degrees.

    Returns:
        tuple[float, float] | None: Pixel coordinates as (x, y) tuple where x is the
            column (horizontal) position and y is the row (vertical) position in pixels.
            Returns None if geo-coding is invalid or doesn't support pixel position computation.

    Example:
        >>> geo_coding = product._product.getSceneGeoCoding()
        >>> # Convert Berlin coordinates to pixels
        >>> x, y = get_pixel_pos(geo_coding, 52.5163, 13.3777)
        >>> if (x, y) != (None, None):
        >>>     print(f"Berlin is at pixel ({x:.1f}, {y:.1f})")
        >>>
        >>> # Check if conversion is possible
        >>> pixel_pos = get_pixel_pos(geo_coding, 40.7128, -74.0060)
        >>> if pixel_pos is None:
        >>>     print("Coordinates are outside the product bounds or geo-coding unavailable")
    """
    if not jgeo_coding or not jgeo_coding.canGetPixelPos:
        return None

    jpp = jgeo_coding.get_pixel_pos(JGeoPos(lat, lon), None)
    return jpp.getX(), jpp.getY()

def get_geo_pos(jgeo_coding, x, y):
    """Convert pixel coordinates to geographic coordinates using the provided geo-coding.

    This method transforms pixel coordinates (x, y) to latitude and longitude coordinates
    using SNAP's geo-coding functionality. It creates a Java PixelPos object from the input
    pixel coordinates and uses the geo-coding's transformation capabilities to compute the
    corresponding geographic position.

    Args:
        jgeo_coding (org.esa.snap.core.datamodel.GeoCoding): The SNAP geo-coding object
            that provides the coordinate transformation functionality. Must support geographic
            position computation (canGetGeoPos should be True).
        x (float): Pixel x-coordinate (column position) in the raster.
        y (float): Pixel y-coordinate (row position) in the raster.

    Returns:
        tuple[float, float] | None: Geographic coordinates as (latitude, longitude) tuple
            in decimal degrees. Returns None if geo-coding is invalid or doesn't support
            geographic position computation.

    Example:
        >>> geo_coding = product._product.getSceneGeoCoding()
        >>> # Convert pixel coordinates to geographic coordinates
        >>> lat, lon = get_geo_pos(geo_coding, 1000.5, 2000.5)
        >>> if (lat, lon) != (None, None):
        >>>     print(f"Pixel (1000.5, 2000.5) is at {lat:.4f}°N, {lon:.4f}°E")
        >>>
        >>> # Convert center pixel to coordinates
        >>> center_x, center_y = product.width // 2, product.height // 2
        >>> center_coords = get_geo_pos(geo_coding, center_x, center_y)
        >>> if center_coords is not None:
        >>>     print(f"Product center is at {center_coords[0]:.4f}°N, {center_coords[1]:.4f}°E")
    """
    if not jgeo_coding or not jgeo_coding.canGetGeoPos:
        return None

    jgp = jgeo_coding.get_geo_pos(JPixelPos(x, y), None)
    return jgp.getLat(), jgp.getLon()

def get_placemark_dict(group):
    """Convert a SNAP placemark group to a Python dictionary mapping names to Placemark objects.

    This method extracts all placemarks from a SNAP placemark group (such as pin group or
    GCP group) and converts them into Python Placemark objects organized in a dictionary.
    Each placemark contains both pixel coordinates and geographic coordinates, making them
    useful for reference points, ground control points, and points of interest.

    Args:
        group (org.esa.snap.core.datamodel.PlacemarkGroup): The SNAP placemark group
            containing the placemarks to convert. This can be a pin group, GCP group,
            or any other placemark collection from a SNAP product.

    Returns:
        dict[str, Placemark]: A dictionary where keys are placemark names and values are
            Placemark objects containing the placemark's coordinates and metadata.

    Example:
        >>> # Get pins as dictionary
        >>> pin_group = product._product.getPinGroup()
        >>> pins = get_placemark_dict(pin_group)
        >>> print(f"Found {len(pins)} pins:")
        >>> for name, pin in pins.items():
        >>>     print(f"  {name}: pixel({pin.x}, {pin.y}), geo({pin.lat}, {pin.lon})")
        >>>
        >>> # Get GCPs as dictionary
        >>> gcp_group = product._product.getGcpGroup()
        >>> gcps = get_placemark_dict(gcp_group)
        >>> if gcps:
        >>>     first_gcp = list(gcps.values())[0]
        >>>     print(f"First GCP: {first_gcp.name} at {first_gcp.lat}°N, {first_gcp.lon}°E")
    """
    dict = {}
    for i in range(group.getNodeCount()):
        jplacemark = group.get(i)
        pixel_pos = jplacemark.getPixelPos()
        geo_pos = jplacemark.getGeoPos()
        placemark = _create_placemark(jplacemark.getName(), pixel_pos.getX(), pixel_pos.getY(), geo_pos.getLat(),
                              geo_pos.getLon(), jplacemark.getLabel())
        dict[placemark.name] = placemark
    return dict

def del_from_group(name, group):
    """Remove an element from a SNAP group by name if it exists.

    This method safely removes an element (such as a band, mask, tie-point grid, or
    placemark) from a SNAP group by checking if the element exists first. If the named
    element is found in the group, it is removed; otherwise, no action is taken. This
    prevents errors when attempting to remove non-existent elements.

    Args:
        name (str): The name of the element to remove from the group.
        group (org.esa.snap.core.datamodel.ProductNodeGroup): The SNAP group from which
            to remove the element. This can be a band group, mask group, tie-point grid
            group, placemark group, or any other SNAP product node group.

    Example:
        >>> # Remove a band from the band group
        >>> band_group = product._product.getBandGroup()
        >>> del_from_group("unwanted_band", band_group)
        >>>
        >>> # Remove a mask from the mask group
        >>> mask_group = product._product.getMaskGroup()
        >>> del_from_group("old_mask", mask_group)
        >>>
        >>> # Remove a pin from the pin group
        >>> pin_group = product._product.getPinGroup()
        >>> del_from_group("temp_pin", pin_group)
        >>> print("Element removed if it existed")
    """
    if group.contains(name):
        group.remove(group.get(name))