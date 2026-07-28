"""SNAP Placemark Module for representing geographic points of interest.

This module provides the Placemark class for representing geographic points of interest
in SNAP (Sentinel Application Platform). Placemarks are used to mark specific locations on Earth observation data,
combining both raster coordinates and geographic coordinates for precise location reference.

Example:
    Creating a ground control point from a product location:
    
    >>> product = Product.read('/path/to/product.dim')
    >>> gcp = product.gcps["GCP_1"]
    >>> print(f"GCP: {gcp.name} - {gcp.label} at {gcp.x}, {gcp.y} / {gcp.lat}, {gcp.lon} ")
    >>> pin = product.gcps["PIN_1"]
    >>> print(f"PIN: {pin.name} - {pin.label} at {pin.x}, {pin.y} / {pin.lat}, {pin.lon}")
    >>> product.
"""
# Private sentinel used to protect constructor access within the package only
_SENTINEL = object()
# Package-internal factory: do not export this from the package API.
def _create_placemark(self, name: str, x: float, y: float, lat: float = None, lon: float = None,
    label: str = None) -> 'Placemark':
    return Placemark(name, x, y, lat, lon, label, _SENTINEL)


class Placemark:
    """Represents a placemark with geographic and display information.

    This class is used to represent pins and gcps of a SNAP product. It defines a point of interest or location,
    including its name, coordinates on a 2D plane, geographic latitude and longitude, and an optional label.

    Attributes:
        name (str): The name of the placemark.
        x (float): The x-coordinate of the placemark in a raster.
        y (float): The y-coordinate of the placemark in a raster.
        lat (float): The latitude of the placemark in geographic coordinates.
        lon (float): The longitude of the placemark in geographic coordinates.
        label (str): The label associated with the placemark.
    """

    def __init__(self, name: str, x: float, y: float, lat: float = None, lon: float = None, label: str = None,
        _sentinel: object = None) -> None:
        """This is not intended to be called directly. Get pins and gcps as placemark from a product.

        Args:
            name (str): The name of the placemark.
            x (float): The x-coordinate of the placemark in a raster.
            y (float): The y-coordinate of the placemark in a raster.
            lat (float): The latitude of the placemark in geographic coordinates.
            lon (float): The longitude of the placemark in geographic coordinates.
            label (str): The label associated with the placemark.
        """
        if _sentinel is not _SENTINEL:
            raise TypeError("Placemark cannot be instantiated directly. Get or create a new placemark using a product.")
        self.name = name
        self.x = x
        self.y = y
        self.lat = lat
        self.lon = lon
        self.label = label if label is not None else name
