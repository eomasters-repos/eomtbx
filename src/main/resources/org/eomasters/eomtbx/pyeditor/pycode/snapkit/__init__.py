# snapkit/__init__.py

"""
SNAPKIT - A Python Package for SNAP (Sentinel Application Platform) Integration.

This package provides a comprehensive Python interface for working with SNAP's
remote sensing data processing capabilities. It offers convenient access to
SNAP's Graph Processing Framework (GPF), product I/O operations, and various
data manipulation tools for satellite imagery and geospatial data analysis.

Key Features:
    - Product I/O operations for various remote sensing formats
    - Graph Processing Framework (GPF) integration for automated processing chains
    - Raster data access and manipulation
    - Product metadata and georeferencing utilities
    - Integration with SNAP Desktop application when available

Main Classes and Functions:
    Graph: Building and executing SNAP processing graphs
    Product: Remote sensing product representation with raster data and metadata
    ProductManager: Management of products within SNAP application context
    Raster: Access to raster data nodes within products
    SampleCoding: Sample coding and flag interpretation utilities
    
    Graph Processing Framework Functions (via GPF class):
    GPF.operators: List all available operators from the GPF registry
    GPF.describe_operator: Get detailed information about a specific operator
    GPF.product_manager: Get the GPF product manager for product operations
    
    Product I/O Functions (via ProductIO class):
    ProductIO.read: Read a product from a file using ProductIO
    ProductIO.save: Save a product to a file in the specified format
    ProductIO.input_formats: Get all available input formats and their file extensions
    ProductIO.output_formats: Get all available output formats and their file extensions

    SNAP Desktop Integration Functions (via Snap class):
    Snap.is_available: Check if SNAP Desktop is available and running
    Snap.selected_product: Get currently selected product from Product Explorer
    Snap.product_manager: Get SNAP Desktop product manager
    Snap.view_raster: Open raster visualization in SNAP Desktop
    Snap.view_rgb: Open RGB composite visualization in SNAP Desktop

Example:
    Basic usage for reading and processing a satellite product:
    
    >>> from snapkit import Product, Graph, product_io
    >>> 
    >>> # Read a product from file
    >>> product = product_io.read('/path/to/sentinel2.zip')
    >>> print(f"Product: {product.name}, Size: {product.width}x{product.height}")
    >>> 
    >>> # Create a processing graph
    >>> graph = Graph()
    >>> read_id = graph.read('/path/to/input.zip')
    >>> calib_id = graph.add_operator('calib', 'Calibration', {'outputSigmaBand': True})
    >>> write_id = graph.write('/path/to/output.dim')
    >>> graph.connect(read_id, calib_id, write_id)
    >>> result = graph.execute()

Requirements:
    - SNAP Desktop application or SNAP Engine installation
    - Java runtime environment
    - Graalpython environment for Python-Java integration

Note:
    This package is designed to work within a the SNAP application and using Graalpython environment.
    Some functionality may require an active SNAP Desktop session.

Version: 0.5
Author: Marco Peters
Email: info@eomasters.org
"""

# Make classes available at package level
from . import gpf
from .gpf import GPF
from .graph import Graph
from .placemark import Placemark
from .product import Product
from .product_io import ProductIO
from .product_manager import ProductManager
from .raster import Raster
from .sampleCoding import SampleCoding
from .snap import SNAP

# Define what gets imported with "from snapkit import *"
__all__ = ['GPF', 'Graph', 'Placemark', 'Product', 'ProductIO', 'ProductManager', 'Raster', 'SampleCoding', 'SNAP']

# Package metadata
__version__ = '0.5'
__author__ = 'Marco Peters'
__email__ = 'info@eomasters.org'
