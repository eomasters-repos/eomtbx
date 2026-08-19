"""
SNAP Desktop integration and control module.

This module provides a Python interface for interacting with the SNAP (Sentinel Application
Platform) Desktop application, enabling programmatic control of product management,
visualisation operations, and GUI components from within Python scripts. It serves as
a bridge between Python code and the SNAP Desktop environment, allowing seamless
integration of custom processing workflows with SNAP's powerful visualisation capabilities.

The module provides a Snap class with static methods for checking SNAP Desktop availability, 
managing products in the application's product registry, opening raster visualisations, and
accessing currently selected products. All operations require an active SNAP Desktop session 
to function properly.

Key Features:
    - Check SNAP Desktop availability and running status
    - Access the currently selected product from the Product Explorer
    - Manage products through SNAP's ProductManager interface
    - Open individual rasters and RGB composites for visualisation
    - Control scene view operations programmatically
    - Integration with SNAP Desktop's GUI components and windows

Desktop Integration:
    - Automatic detection of SNAP Desktop availability
    - Access to Product Explorer selections and state
    - Scene view control for raster and RGB visualisation
    - Product registry management through SNAP's framework
    - Window and component management utilities

Snap Class Methods:
    SNAP.is_available: Check if SNAP Desktop is available and running
    SNAP.selected_product: Get the currently selected product from the Product Explorer
    SNAP.product_manager: Get the SNAP Desktop product manager
    SNAP.view_raster: Open individual rasters for visualisation
    SNAP.view_rgb: Open RGB composite visualisations

Usage:
    Basic SNAP Desktop interaction:

        >>> from snapkit.snap import SNAP
        >>> from snapkit import Product
        >>>
        >>> # Check if SNAP Desktop is available
        >>> if SNAP.is_available():
        >>>     print("SNAP Desktop is running")
        >>> else:
        >>>     print("SNAP Desktop not available")
        >>>
        >>> # Get currently selected product
        >>> current_product = SNAP.selected_product()
        >>> if current_product:
        >>>     print(f"Selected: {current_product.name}")
        >>> else:
        >>>     print("No product selected")

    Product management and visualization:

        >>> # Load and manage products
        >>> product = Product.read('/path/to/sentinel2.dim')
        >>> pm = SNAP.product_manager()
        >>> pm.add(product)
        >>>
        >>> # Open raster visualizations
        >>> SNAP.view_raster('B4', product)  # Open single band
        >>> SNAP.view_rgb('B4', 'B3', 'B2', product, 'True Color')  # RGB composite
        >>>
        >>> # Work with selected products
        >>> selected = SNAP.selected_product()
        >>> if selected:
        >>>     SNAP.view_raster('B8', selected)

    Error handling for headless environments:

        >>> try:
        >>>     pm = SNAP.product_manager()
        >>>     pm.add(product)
        >>> except RuntimeError as e:
        >>>     print(f"SNAP Desktop not available: {e}")
        >>>     # Fall back to headless processing

"""
import java
from eu.esa.snap.netbeans.docwin import WindowUtilities as JWindowUtilities
from java.awt import GraphicsEnvironment as JGraphicsEnvironment
from org.esa.snap.rcp import SnapApp as JSnapApp

from org.eomasters.eomtbx.utils import SceneViewUtils as JSceneViewUtils
from .product import Product
from .product_manager import ProductManager
from .raster import Raster



class SNAP:
    """SNAP Desktop integration and control utilities.
    
    This class provides static methods for SNAP Desktop integration functionality
    including availability checking, product management, and visualization operations.
    """
    _IS_AVAILABLE: bool = (JSnapApp.getDefault().getAppContext().getApplicationWindow().isShowing()
                            ) if not JGraphicsEnvironment.isHeadless() else False

    def __new__(cls):
        """Class should not be instantiated as it only provides static methods."""
        raise TypeError(f"{cls.__name__} class should not be instantiated - use static methods only")

    @staticmethod
    def is_available() -> bool:
        """Check if SNAP Desktop is available and running.

        Returns:
            True if SNAP Desktop is available and the application window is showing,
            False if running in headless mode or if the application is not available.

        Example:
            >>> from snapkit.snap import SNAP
            >>> if SNAP.is_available():
            >>>     print("SNAP Desktop is running and available")
            >>>     # Proceed with SNAP operations
            >>> else:
            >>>     print("SNAP Desktop is not available")
            >>>     # Handle offline or headless mode
        """
        return SNAP._IS_AVAILABLE

    @staticmethod
    def selected_product() -> Product:
        """Get the currently selected product from SNAP Desktop's Product Explorer.

        Returns:
            The currently selected Product object if a product is selected
            in the Product Explorer, None if no product is selected or if the Product
            Explorer is not accessible.

        Example:
            >>> from snapkit.snap import SNAP
            >>> # Check what product is currently selected
            >>> current_product = SNAP.selected_product()
            >>> if current_product:
            >>>     print(f"Selected product: {current_product.name}")
            >>>     print(f"Dimensions: {current_product.width}x{current_product.height}")
            >>> else:
            >>>     print("No product is currently selected")
        """
        top_component_class = java.type("org.esa.snap.rcp.windows.ProductExplorerTopComponent")
        top_component = JWindowUtilities.getOpened(top_component_class).findFirst().orElse(None)
        if top_component:
            top_component_lookup = top_component.getLookup()
            if top_component_lookup:
                product_node_class = java.type("org.esa.snap.core.datamodel.ProductNode")
                product_node = top_component_lookup.lookup(product_node_class)
                if product_node:
                    return Product(None, None, _java_product=product_node.getProduct())
        return None

    @staticmethod
    def product_manager() -> ProductManager:
        """Get the SNAP Desktop product manager.

        Returns:
            The ProductManager instance for managing products in SNAP Desktop.

        Raises:
            RuntimeError: If SNAP Desktop is not available or running in headless mode.
        """
        SNAP._check_snap_available()
        return ProductManager._create_product_manager(JSnapApp.getDefault().getAppContext().getProductManager())

    @staticmethod
    def view_raster(raster: str | Raster, product: Product = None):
        """Opens a view on a raster in SNAP Desktop.

        Opens the specified raster in SNAP Desktop's scene view for visualization.
        The raster can be specified either as a string (raster name) or as a Raster object.

        Args:
            raster (str | Raster): The raster to open. Can be either a string specifying the raster name
                   or a Raster object directly.
            product: The product containing the raster. Required if raster is specified
                    as a string, optional if raster is a Raster object.

        Raises:
            RuntimeError: If SNAP Desktop is not available or running in headless mode.
            ValueError: If product is None when raster is specified as a string, or if
                       raster is neither a string nor a Raster object.
            KeyError: If the specified raster name is not found in the product.

        Example:
            >>> from snapkit.snap import SNAP
            >>> # Open raster by name
            >>> product = SNAP.selected_product()
            >>> SNAP.view_raster('B1', product)
            >>>
            >>> # Open raster object directly
            >>> raster_obj = product.rasters['B2']
            >>> SNAP.view_raster(raster_obj)
        """
        SNAP._check_snap_available()
        the_raster = SNAP._get_raster(raster, product)
        JSceneViewUtils.openRaster(the_raster._raster)

    @staticmethod
    def view_rgb(raster_r: str | Raster, raster_g: str, raster_b: str, product: Product = None,
        name: str = None):
        """Open an RGB composite view in SNAP Desktop.

        Creates an RGB composite from three rasters (red, green, blue channels) and opens
        it for visualization in SNAP Desktop's scene view.

        Args:
            raster_r (str | Raster): The raster or the raster name for the red channel.
            raster_g (str | Raster): The raster or the raster name for the green channel.
            raster_b (str | Raster): The raster or the raster name for the blue channel.
            product (Product): The product containing the rasters. Required if any raster is specified
                               as a string.
            name (str): Optional name for the RGB composite. If None, a default name will be used.

        Raises:
            RuntimeError: If SNAP Desktop is not available or running in headless mode.
            ValueError: If product is None when raster names are specified as strings, or if
                       raster parameters are of incorrect types.
            KeyError: If any of the specified raster names are not found in the product.

        Example:
            >>> from snapkit.snap import SNAP
            >>> # Create RGB composite using raster names
            >>> product = SNAP.selected_product()
            >>> SNAP.view_rgb('B4','B3','B2',product=product,name='True Color')
            >>>
            >>> # Create RGB composite with raster object for red channel
            >>> red_raster = product.rasters['B8']
            >>> SNAP.view_rgb(red_raster,'B4','B3',product=product,name='False Color')
        """
        SNAP._check_snap_available()
        red = SNAP._get_raster(raster_r, product)
        green = SNAP._get_raster(raster_g, product)
        blue = SNAP._get_raster(raster_b, product)
        JSceneViewUtils.openRgb(name, red._raster, green._raster, blue._raster)

    @staticmethod
    def _get_raster(raster: str | Raster, product: Product = None) -> Raster:
        """Get a Raster object from either a string name or Raster instance.

        Internal helper static method that resolves a raster specification (either string name
        or Raster object) to a Raster object, with validation.

        Args:
            raster (str, Raster): The raster specification. Can be either a string specifying the raster
                   name or a Raster object directly.
            product: The product containing the raster. Required if raster is specified
                    as a string.

        Returns:
            The resolved Raster object.

        Raises:
            ValueError: If product is None when raster is specified as a string, or if
                       raster is neither a string nor a Raster object.
            KeyError: If the specified raster name is not found in the product.
        """
        if isinstance(raster, str):
            if product is None:
                raise ValueError("Product must be provided if raster is a string")
            else:
                the_raster = product.rasters[raster]
                if the_raster is None:
                    raise KeyError(f"Raster '{raster}' not found in product")
        elif isinstance(raster, Raster):
            the_raster = raster
        else:
            raise ValueError("Raster must be a string or a Raster object")
        return the_raster

    @staticmethod
    def _check_snap_available() -> None:
        """Check SNAP Desktop availability and raise RuntimeError if not available.

        Raises:
            RuntimeError: If SNAP Desktop is not available or running in headless mode.
        """
        if not SNAP._IS_AVAILABLE:
            raise RuntimeError("SNAP Desktop is not available")