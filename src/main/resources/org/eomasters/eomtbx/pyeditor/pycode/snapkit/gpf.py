"""Graph Processing Framework (GPF) module for SNAP integration.

This module provides Python wrappers for SNAP's Graph Processing Framework (GPF),
enabling Python scripts to interact with SNAP operators and product management.
The module provides a GPF class with static methods for accessing registered operators and their descriptors.

Key Features:
    - List all available GPF operators from the registry
    - Get detailed operator descriptors with parameters and metadata
    - Access GPF product manager for product operations
    - Integration with SNAP's Graph Processing Framework

GPF Class Methods:
    GPF.operators: List all available operators from the GPF registry
    GPF.describe_operator: Get detailed information about a specific operator
    GPF.product_manager: Get the GPF product manager for product operations

Usage:
    Basic GPF operations:

        >>> from snapkit.gpf import GPF
        >>> 
        >>> # List all available operators
        >>> operators = GPF.operators()
        >>> print(f"Found {len(operators)} operators")
        >>> 
        >>> # Get operator information
        >>> reproject_desc = GPF.describe_operator('Reproject')
        >>> print(f"Operator: {reproject_desc.name}")
        >>> 
        >>> # Access product manager
        >>> pm = GPF.product_manager()

Example:
    Working with GPF operators and product manager:

        >>> from snapkit.gpf import GPF
        >>> from snapkit import ProductIO
        >>> 
        >>> # Get list of all operators
        >>> all_ops = GPF.operators()
        >>> print(f"Available operators: {len(all_ops)}")
        >>> 
        >>> # Get detailed operator information
        >>> calib_desc = GPF.describe_operator('Calibration')
        >>> print(f"Parameters: {len(calib_desc.parameters)}")
        >>> 
        >>> # Use GPF product manager
        >>> pm = GPF.product_manager()
        >>> product = ProductIO.read('/path/to/product.dim')
        >>> pm.add(product)
"""

from org.esa.snap.core.gpf import GPF as JGPF

from ._operatorDescriptor import OperatorDescriptor
from .product_manager import ProductManager


class GPF:
    """Graph Processing Framework (GPF) utilities for SNAP integration.
    
    This class provides static methods for GPF functionality for SNAP operators 
    and product management through SNAP's Java GPF API.
    """

    def __new__(cls):
        """Class should not be instantiated as it only provides static methods."""
        raise TypeError(f"{cls.__name__} class should not be instantiated - use static methods only")

    @staticmethod
    def operators() -> list[str]:
        """List all available operators from the GPF registry.

        Returns:
            A sorted list of operator aliases available in the GPF registry.
            
        Example:
            >>> from snapkit.gpf import GPF
            >>> operators = GPF.operators()
            >>> print(f"Found {len(operators)} operators")
            >>> print(operators[:5])  # Show first 5 operators
        """
        gpf = JGPF.getDefaultInstance()
        registry = gpf.getOperatorSpiRegistry()
        operators = []

        for spi in registry.getOperatorSpis():
            operators.append(spi.getOperatorAlias())

        return sorted(operators)

    @staticmethod
    def describe_operator(operator: str) -> 'OperatorDescriptor':
        """Get the descriptor of a specific operator.

        Args:
            operator: The operator alias/name to describe. Must be a valid
                operator name registered in the GPF registry.

        Returns:
            The operator descriptor containing detailed information about the
            operator including parameters, inputs, and outputs.

        Raises:
            ValueError: If the specified operator is not found in the GPF registry
                or if the operator name is invalid.
                
        Example:
            >>> from snapkit.gpf import GPF
            >>> desc = GPF.describe_operator('Reproject')
            >>> print(f"Operator: {desc.name}")
            >>> print(f"Parameters: {len(desc.parameters)}")
        """
        return OperatorDescriptor.get_operator_descriptor(operator)

    @staticmethod
    def product_manager() -> 'ProductManager':
        """Get the GPF product manager.

        Returns:
            The ProductManager instance for managing products in GPF.
            
        Example:
            >>> from snapkit.gpf import GPF
            >>> from snapkit import ProductIO
            >>> pm = GPF.product_manager()
            >>> product = ProductIO.read('/path/to/product.dim')
            >>> pm.add(product)
        """
        from .product_manager import _create_product_manager
        return _create_product_manager(JGPF.getDefaultInstance().getProductManager())
