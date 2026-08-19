"""
SNAP Product Manager interface for centralized product management.

This module provides a Python interface to SNAP's ProductManager functionality,
enabling centralized management of remote sensing products within the SNAP
application environment. The ProductManager serves as a registry for all loaded
products, making them accessible for processing operations and visualization.

The module centers around the ProductManager class, which wraps SNAP's Java
ProductManager and provides Pythonic access to product management operations.
It maintains a centralized collection of Product instances that can be shared
across different components of the SNAP application.

Key Features:
    - Add and remove products from the centralized registry
    - Query the number of products currently managed
    - Retrieve all managed products as a list
    - Access specific products by reference number (index)
    - Integration with SNAP's GUI and processing framework
    - Thread-safe product management operations

Usage:
    Basic product management operations:

        >>> from snapkit import Product, snap
        >>>
        >>> # Get the product manager instance
        >>> pm = snap.product_manager()
        >>>
        >>> # Load and add a product to the manager
        >>> product = Product.read('/path/to/product.dim')
        >>> pm.add(product)
        >>> print(f"Managing {pm.count} products")
        >>>
        >>> # Access all managed products
        >>> all_products = pm.products
        >>> for product in all_products:
        >>>     print(f"Product: {product.name}")
        >>>
        >>> # Get specific product by index
        >>> if pm.count > 0:
        >>>     first_product = pm.get_product(0)
        >>>     print(f"First product: {first_product.name}")
        >>>
        >>> # Remove product from manager
        >>> pm.remove(product)

    Working with multiple products:

        >>> # Add multiple products
        >>> product_files = ['/path/to/prod1.dim', '/path/to/prod2.dim']
        >>> for file_path in product_files:
        >>>     product = Product.read(file_path)
        >>>     pm.add(product)
        >>>
        >>> print(f"Total products managed: {pm.count}")
        >>>
        >>> # Process all managed products
        >>> for i in range(pm.count):
        >>>     product = pm.get_product(i)
        >>>     print(f"Processing product {i}: {product.name}")
        >>>     # ... perform operations on product ...
        >>>
        >>> # Clear all products from manager
        >>> for product in pm.products:
        >>>     pm.remove(product)
        >>> assert pm.count == 0

Note:
    The ProductManager cannot be instantiated directly. It must be obtained through
    the SNAP or GPF interfaces which provide properly initialized instances connected
    to SNAP's application framework.
"""

from org.esa.snap.core.datamodel import ProductManager as JProductManager
from org.esa.snap.rcp import SnapApp

from ._utils import Utils
from .product import Product

class ProductManager:

    """Utility class for managing SNAP products in the application.

    This class provides static methods for managing Product instances within SNAP's
    ProductManager. It acts as a wrapper around SNAP's Java ProductManager functionality.
    If SNAP is not available, it falls back to an empty ProductManager disconnected from SNAP.

    The ProductManager maintains a centralized registry of all products that are currently
    loaded and available for processing within the SNAP application. Products managed by
    this class are accessible across different parts of the application for visualization
    and processing operations.

    Note:
        This class should not be instantiated directly. All methods are static and should be
        called directly on the class. The Java ProductManager instance is managed internally
        through the global _JPM variable.

    Examples:
        >>> from snapkit import Product, product_manager, ProductManager
        >>> product = Product.read('/path/to/product.dim')
        >>> pm = product_manager()
        >>> pm.add(product)
        >>> print(f"Managing {ProductManager.count} products")
        >>> ProductManager.remove(product)

        >>> # Get all managed products
        >>> all_products = ProductManager.products
        >>> for product in all_products:
        >>>     print(f"Product: {product.name}")

        >>> # Get a specific product by reference number
        >>> first_product = ProductManager.get_product(0)
        >>> print(f"First product: {first_product.name}")
    """

    # Private sentinel used to protect constructor access within the package only
    _SENTINEL = object()
    # Package-internal factory: do not export this from the package API.
    def _create_product_manager(jpm):
        return ProductManager(jpm, ProductManager._SENTINEL)

    def __init__(self, jpm, _sentinel: object):
        """Initialize the ProductManager with a Java ProductManager instance. Not Intended for public use.
        """
        if _sentinel is not ProductManager._SENTINEL:
            raise TypeError(
                "ProductManager cannot be instantiated directly. Use retrieve the product manager from SNAP or GPF.")
        Utils.is_instance(jpm, "org.esa.snap.core.datamodel.ProductManager")
        self._JPM = jpm

    def add(self, product: Product):
        """Add a product to the manager.

        This method adds a Product instance to SNAP's ProductManager, making it available
        for processing operations and visualization within the SNAP application.

        Args:
            product (Product): The Product instance to be added to the manager.
                             Must be a valid Product object with an internal _product attribute.

        Raises:
            AttributeError: If the product parameter doesn't have a _product attribute.

        Examples:
            >>> from snapkit import Product, ProductManager
            >>> product = Product.read('/path/to/product.dim')
            >>> ProductManager.add(product)

            >>> # Adding multiple products
            >>> products = [Product.read(f'/path/to/product{i}.dim') for i in range(3)]
            >>> for product in products:
            >>>     ProductManager.add(product)
        """
        self._JPM.addProduct(product._product)

    def remove(self, product: 'Product') -> None:
        """Remove a product from the manager.

        This method removes a Product instance from SNAP's ProductManager, making it no longer
        available for processing operations within the SNAP application. The product object
        itself remains valid but is no longer managed by the ProductManager.

        Args:
            product (Product): The Product instance to be removed from the manager.
                             Must be a valid Product object that is currently managed.

        Raises:
            AttributeError: If the product parameter doesn't have a _product attribute.
            ValueError: If the product is not currently managed by the ProductManager.

        Examples:
            >>> from snapkit import Product, ProductManager
            >>> product = Product.read('/path/to/product.dim')
            >>> ProductManager.add(product)
            >>> ProductManager.remove(product)

            >>> # Remove multiple products
            >>> products = ProductManager.products()
            >>> for product in products:
            >>>     ProductManager.remove(product)
            >>> assert ProductManager.count() == 0
        """
        self._JPM.removeProduct(product._product)

    @property
    def products(self) -> list['Product']:
        """Get all products currently managed by the ProductManager.

        This method retrieves all Product instances that are currently registered with
        SNAP's ProductManager. Each Product is wrapped in a Python Product object for
        convenient access to SNAP functionality.

        Returns:
            List[Product]: A list of all Product instances currently managed by the ProductManager.
                          Returns an empty list if no products are managed or if the
                          ProductManager is not initialized.

        Examples:
            >>> from snapkit import ProductManager
            >>> managed_products = ProductManager.products()
            >>> print(f"Number of products: {len(managed_products)}")

            >>> # Iterate through all managed products
            >>> for product in ProductManager.products():
            >>>     print(f"Product: {product.name}, Size: {product.scene_raster_width}x{product.scene_raster_height}")

            >>> # Check if any products are managed
            >>> if ProductManager.products():
            >>>     print("Products are available for processing")
            >>> else:
            >>>     print("No products currently managed")
        """
        products = []
        for i in range(self._JPM.getProductCount()):
            products.append(Product(None, None, _java_product=self._JPM.getProductAt(i)))
        return products

    @property
    def count(self) -> int:
        """Get the number of products currently managed by the ProductManager.

        This method returns the total count of Product instances that are currently
        registered and managed by SNAP's ProductManager.

        Returns:
            int: The number of products currently managed by the ProductManager.
                Returns 0 if no products are managed or if the ProductManager is not initialized.

        Examples:
            >>> from snapkit import ProductManager
            >>> product_count = ProductManager.count()
            >>> print(f"Currently managing {product_count} products")

            >>> # Check if products are available before processing
            >>> if ProductManager.count() > 0:
            >>>     print(f"Processing {ProductManager.count()} products")
            >>>     products = ProductManager.products()
            >>> else:
            >>>     print("No products available for processing")

            >>> # Monitor product count changes
            >>> initial_count = ProductManager.count()
            >>> # ... add products ...
            >>> final_count = ProductManager.count()
            >>> print(f"Added {final_count - initial_count} products")
        """
        return self._JPM.getProductCount()

    def get_product(self, ref_number: int) -> 'Product':
        """Get a product by its reference number.

        This method retrieves a specific Product instance from SNAP's ProductManager
        using its reference number (index). Reference numbers are assigned sequentially
        starting from 0 for the first product added to the manager.

        Args:
            ref_number (int): The reference number (index) of the product to retrieve.
                            Must be a non-negative integer within the range [0, count()-1].

        Returns:
            Product: The Product instance corresponding to the specified reference number.
                    The returned Product is wrapped for convenient Python access to SNAP functionality.

        Raises:
            IndexError: If the reference number is out of range (negative or >= count()).
            TypeError: If ref_number is not an integer.

        Examples:
            >>> from snapkit import ProductManager
            >>> # Get the first product (if any exist)
            >>> if ProductManager.count() > 0:
            >>>     product = ProductManager.get_product(0)
            >>>     print(f"First product: {product.name}")

            >>> # Get all products by reference number
            >>> for i in range(ProductManager.count()):
            >>>     product = ProductManager.get_product(i)
            >>>     print(f"Product {i}: {product.name}")

            >>> # Safe product retrieval with error handling
            >>> try:
            >>>     product = ProductManager.get_product(5)
            >>>     print(f"Product at index 5: {product.name}")
            >>> except IndexError:
            >>>     print("No product found at index 5")
        """
        if not isinstance(ref_number, int):
            raise TypeError(f"Reference number must be an integer, got {type(ref_number).__name__}")

        if ref_number < 0 or ref_number >= self._JPM.getProductCount():
            raise IndexError(f"Reference number {ref_number} is out of range [0, {self._JPM.getProductCount() - 1}]")

        return Product(None, None, _java_product=self._JPM.getProductAt(ref_number))
