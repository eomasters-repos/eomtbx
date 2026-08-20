"""
Sample coding support for flag bands and index bands in remote sensing data.

This module provides Python interfaces for working with SNAP's sample coding functionality,
which maps integer values in raster data to meaningful names and descriptions. Sample codings
are essential components for interpreting flag bands (quality indicators,
cloud masks) and index bands (classification results, land cover categories).

The module centers around the SampleCoding class, which serves as a unified wrapper around
SNAP's Java FlagCoding and IndexCoding classes. It automatically detects the coding type
and provides consistent Python access to sample definitions regardless of the underlying
Java implementation.

Key Features:
    - Unified interface for both FlagCoding and IndexCoding
    - Automatic detection of coding type (flag vs index)
    - Dictionary-based access to sample definitions
    - Integration with Raster class for flag and index bands
    - Type-safe construction through factory pattern
    - Support for both flag masks and index values

Sample Coding Types:
    - FlagCoding: Maps bit patterns to flag names (e.g., cloud detection, quality flags)
    - IndexCoding: Maps integer indices to category names (e.g., land cover classes)

Usage:
    Accessing sample coding from flag bands:

        >>> from snapkit import Product
        >>>
        >>> # Load product and access flag band
        >>> product = Product.read('/path/to/sentinel2.dim')
        >>> flag_band = product['quality_flags']
        >>>
        >>> # Get sample coding information
        >>> coding = flag_band.sample_coding()
        >>> if coding:
        >>>     print(f"Coding type: {'Flag' if coding.isFlagCoding else 'Index'}")
        >>>     print(f"Coding name: {coding.name}")
        >>>     print(f"Description: {coding.description}")

    Working with flag definitions:

        >>> # Get all sample definitions
        >>> samples = coding.samples()
        >>> print(f"Available samples: {list(samples.keys())}")
        >>>
        >>> # Access specific flag masks
        >>> cloud_mask = samples.get('CLOUD', 0)
        >>> water_mask = samples.get('WATER', 0)
        >>> print(f"Cloud flag mask: {cloud_mask:08b}")  # Binary representation
        >>> print(f"Water flag mask: {water_mask:08b}")

    Working with index bands:

        >>> # Access classification band
        >>> class_band = product['classification']
        >>> class_coding = class_band.sample_coding()
        >>>
        >>> if class_coding and not class_coding.isFlagCoding:
        >>>     # Get classification categories
        >>>     categories = class_coding.samples()
        >>>     print("Land cover classes:")
        >>>     for name, index in categories.items():
        >>>         print(f"  {index}: {name}")

    Checking pixel values against sample coding:

        >>> # Sample a pixel and interpret its meaning
        >>> pixel_value = flag_band.sample(100, 200)
        >>> samples = coding.samples()
        >>>
        >>> # For flag coding - check which flags are set
        >>> if coding.isFlagCoding:
        >>>     active_flags = []
        >>>     for flag_name, flag_mask in samples.items():
        >>>         if pixel_value & flag_mask:  # Bitwise AND operation
        >>>             active_flags.append(flag_name)
        >>>     print(f"Active flags at pixel (100,200): {active_flags}")
        >>>
        >>> # For index coding - find category name
        >>> else:
        >>>     category_name = next((name for name, idx in samples.items() if idx == pixel_value), "Unknown")
        >>>     print(f"Category at pixel (100,200): {category_name}")
"""

from ._utils import Utils


# Private sentinel used to protect constructor access within the package only
_SENTINEL = object()
# Package-internal factory: do not export this from the package API.
def _create_coding(jsample_coding) -> 'SampleCoding':
    return SampleCoding(jsample_coding, _SENTINEL)

class SampleCoding:
    """Wrapper for SNAP's sample coding functionality providing access to flag and index definitions.

    This class serves as a Python wrapper around SNAP's Java sample coding classes
    (FlagCoding and IndexCoding), providing convenient access to sample definitions
    that map integer values to meaningful names and descriptions. Sample codings are
    commonly used in remote sensing for flag bands (quality indicators, cloud masks)
    and index bands (classification results, land cover types).

    The class automatically detects whether the underlying Java object is a FlagCoding
    or IndexCoding instance and provides unified access to their sample definitions
    through a common Python interface.

    Example:
        >>> # Access sample coding from a flag band
        >>> flag_band = product['quality_flags']
        >>> coding = flag_band.sample_coding()
        >>> 
        >>> # Check coding type and get samples
        >>> if coding.isFlagCoding:
        >>>     print("This is flag coding")
        >>> else:
        >>>     print("This is index coding")
        >>> 
        >>> # Get all sample definitions
        >>> samples = coding.samples()
        >>> print(f"Available samples: {list(samples.keys())}")
        >>> print(f"Cloud flag mask: {samples.get('CLOUD', 'Not found')}")
    """
    def __init__(self, jsample_coding, _sentinel: object) -> None:
        """This is not intended to be called directly. Get sample-coding from a raster.

        This constructor creates a Python wrapper around an existing SNAP Java
        sample coding object (either FlagCoding or IndexCoding), automatically
        detecting the type and providing unified access to sample definitions.

        Args:
            jsample_coding (org.esa.snap.core.datamodel.FlagCoding | org.esa.snap.core.datamodel.IndexCoding):
                The Java sample coding object to wrap. Must be either a FlagCoding
                or IndexCoding instance from SNAP's data model.

        Raises:
            ValueError: If the provided object is neither a FlagCoding nor IndexCoding instance.
        """
        if _sentinel is not _SENTINEL:
            raise TypeError("SampleCoding cannot be instantiated directly. Retrieve it from a product.")
        self.isFlagCoding = Utils.is_instance(jsample_coding, "org.esa.snap.core.datamodel.FlagCoding")
        isIndexCoding = Utils.is_instance(jsample_coding, "org.esa.snap.core.datamodel.IndexCoding")
        if not (self.isFlagCoding or isIndexCoding):
            raise ValueError(f"Unknown sample coding instance: {Utils.type(jsample_coding)}. "
                             f"Only FlagCoding and IndexCoding are supported")
        self._jsample_coding = jsample_coding

    @property
    def name(self) -> str:
        """Get the name of the sample coding.

        Returns:
            str: The name identifier of the sample coding as defined in the product.

        Example:
            >>> coding = band.sample_coding()
            >>> print(f"Coding name: {coding.name}")
        """
        return self._jsample_coding.getName()

    @property
    def description(self) -> str:
        """Get the description text of the sample coding.

        Returns:
            str: Optional description text for the sample coding, or empty string if not set.

        Example:
            >>> coding = band.sample_coding()
            >>> print(f"Coding description: {coding.description}")
        """
        return self._jsample_coding.getDescription()

    def samples(self) -> dict[str, int]:
        """Get all sample definitions as a dictionary mapping names to values.

        This method retrieves all sample definitions from the underlying sample coding
        and returns them as a convenient dictionary structure. For FlagCoding, values
        are flag masks (bit patterns), while for IndexCoding, values are simple indices.

        Returns:
            dict[str, int]: A dictionary where keys are sample names (str) and values are
                their corresponding flag masks (for FlagCoding) or index values (for IndexCoding).

        Example:
            >>> coding = flag_band.sample_coding()
            >>> samples = coding.samples()
            >>> print(f"Available samples: {list(samples.keys())}")
            >>> # For flag coding
            >>> print(f"Cloud mask: {samples.get('CLOUD', 0):08b}")  # Binary representation
            >>> # For index coding
            >>> print(f"Water class index: {samples.get('WATER', -1)}")
        """
        sample_names = self._get_sample_names()
        samples_dict = {}
        for name in sample_names:
            samples_dict[name] = self._get_sample_value(name)
        return samples_dict

    def _get_sample_names(self) -> list[str]:
        """Get all sample names from the underlying sample coding.

        This private method retrieves the list of sample names, handling both
        FlagCoding and IndexCoding types appropriately.

        Returns:
            list[str]: List of sample names defined in the sample coding.
        """
        if self.isFlagCoding:
            return self._jsample_coding.getFlagNames()
        else:
            return self._jsample_coding.getIndexNames()

    def _get_sample_value(self, sample_name: str) -> int:
        """Get the value for a specific sample name from the underlying sample coding.

        This private method retrieves the integer value associated with a sample name,
        handling both FlagCoding (mask values) and IndexCoding (index values) appropriately.

        Args:
            sample_name (str): The name of the sample to retrieve the value for.

        Returns:
            int: The flag mask value (for FlagCoding) or index value (for IndexCoding)
                associated with the given sample name.
        """
        if self.isFlagCoding:
            return self._jsample_coding.getFlagMask(sample_name)
        else:
            return self._jsample_coding.getIndexValue(sample_name)
