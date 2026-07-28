from org.esa.snap.core.gpf import GPF
from org.esa.snap.core.gpf.descriptor import OperatorDescriptor as JOperatorDescriptor
from org.esa.snap.core.gpf.descriptor import SourceProductDescriptor, ParameterDescriptor


class OperatorDescriptor:
    """Python wrapper for SNAP OperatorDescriptor providing access to operator metadata.

    This class serves as a Python wrapper around SNAP's Java OperatorDescriptor class,
    providing convenient access to operator metadata including source product requirements,
    parameter definitions, and operator configuration information. It enables inspection
    of SNAP operators to understand their inputs, outputs, and configuration parameters
    before execution.

    The OperatorDescriptor provides access to all metadata associated with a SNAP operator,
    including source product descriptors that define input requirements, parameter descriptors
    that specify configuration options with their types and constraints, and general operator
    information such as name, alias, and description.

    Attributes:
        name (str): The symbolic unambiguous name of the operator.
        alias (str): The operator alias used for identification.
        description (str): Detailed description of the operator's functionality.
        source_product_descriptors (list[dict]): List of source product requirements.
        source_products_descriptor (dict): Source products array descriptor if applicable.
        parameter_descriptors (list[dict]): List of all parameter definitions.

    Example:
        >>> # Get descriptor for a specific operator
        >>> desc = OperatorDescriptor.get_operator_descriptor('Reproject')
        >>> print(f"Operator: {desc.name}")
        >>> print(f"Description: {desc.description}")
        >>> 
        >>> # Examine source product requirements
        >>> for src in desc.source_product_descriptors:
        >>>     print(f"Source: {src['alias']} - {src['description']}")
        >>> 
        >>> # Examine parameters
        >>> for param in desc.parameter_descriptors:
        >>>     print(f"Parameter: {param['alias']} ({param['data_type']})")
        >>>     if param.get('default_value'):
        >>>         print(f"  Default: {param['default_value']}")
    """

    def __init__(self, java_descriptor):
        """Initialize a new OperatorDescriptor wrapper around a Java OperatorDescriptor object.

        This constructor creates a Python wrapper around an existing SNAP Java
        OperatorDescriptor object, providing convenient access to operator metadata
        through Python properties and methods.

        Args:
            java_descriptor (org.esa.snap.core.gpf.descriptor.OperatorDescriptor): 
                The Java OperatorDescriptor object to wrap. Must be a valid SNAP
                operator descriptor containing metadata about the operator.

        Example:
            >>> # Usually called internally by get_operator_descriptor()
            >>> java_desc = spi.getOperatorDescriptor()
            >>> descriptor = OperatorDescriptor(java_desc)
            >>> print(f"Wrapped operator: {descriptor.name}")
        """
        self._descriptor = java_descriptor

    @property
    def name(self):
        """Get the symbolic unambiguous name of the operator.

        Returns:
            str: The symbolic name that uniquely identifies the operator within SNAP.

        Example:
            >>> desc = OperatorDescriptor.get_operator_descriptor('Reproject')
            >>> print(desc.name)  # "Reproject"
        """
        return self._descriptor.getAlias()

    @property
    def alias(self):
        """Get the operator alias used for identification and execution.

        Returns:
            str: The alias name used to reference and execute the operator.

        Example:
            >>> desc = OperatorDescriptor.get_operator_descriptor('Reproject')
            >>> print(desc.alias)  # "Reproject"
        """
        return self._descriptor.getAlias()

    @property
    def description(self):
        """Get the detailed description of the operator's functionality.

        Returns:
            str: Detailed description text explaining what the operator does,
                or empty string if no description is available.

        Example:
            >>> desc = OperatorDescriptor.get_operator_descriptor('Reproject')
            >>> print(desc.description)  # "Reprojection of products..."
        """
        return self._descriptor.getDescription() or ""

    @property
    def source_product_descriptors(self):
        """Get list of source product descriptors defining input requirements.

        This property returns information about all source products required by the
        operator, including their names, descriptions, and expected data types.

        Returns:
            list[dict]: List of dictionaries, each containing:
                - alias (str): Source product alias/name
                - description (str): Description of the source product
                - data_type (str): Expected data type class name

        Example:
            >>> desc = OperatorDescriptor.get_operator_descriptor('BandMaths')
            >>> for src in desc.source_product_descriptors:
            >>>     print(f"Source: {src['alias']}")
            >>>     print(f"  Type: {src['data_type']}")
            >>>     print(f"  Description: {src['description']}")
        """
        descriptors = []
        for desc in self._descriptor.getSourceProductDescriptors():
            descriptors.append({
                'alias': desc.getAlias() or desc.getName(),
                'description': desc.getDescription() or "",
                'data_type': desc.getDataType().getSimpleName() or ""
            })
        return descriptors

    @property
    def source_products_descriptor(self):
        """Get the source products array descriptor if the operator accepts multiple products.

        This property returns information about operators that can accept an array of
        source products rather than individual named products.

        Returns:
            dict: Dictionary containing source products array information:
                - alias (str): Array alias/name
                - description (str): Description of the product array
                - data_type (str): Expected data type class name
                Returns empty dict if no source products array is defined.

        Example:
            >>> desc = OperatorDescriptor.get_operator_descriptor('Mosaic')
            >>> src_array = desc.source_products_descriptor
            >>> if src_array:
            >>>     print(f"Accepts product array: {src_array['alias']}")
            >>>     print(f"Description: {src_array['description']}")
        """
        desc = self._descriptor.getSourceProductsDescriptor()
        if desc:
            return {
                'alias': desc.getAlias() or desc.getName(),
                'description': desc.getDescription() or "",
                'data_type': desc.getDataType().getSimpleName() or ""
            }
        return {}

    @property
    def parameter_descriptors(self):
        """Get list of all parameter descriptors defining operator configuration options.

        This property returns comprehensive information about all parameters that can
        be configured for the operator, including their types, default values, constraints,
        and validation rules.

        Returns:
            list[dict]: List of dictionaries, each containing parameter metadata such as:
                - alias (str): Parameter name/alias
                - description (str): Parameter description
                - data_type (str): Parameter data type
                - default_value: Default parameter value
                - isNotNull (bool): Whether parameter cannot be null
                - isNotEmpty (bool): Whether parameter cannot be empty
                - unit (str): Physical unit if applicable
                - valueSet: Set of allowed values if constrained
                - interval: Valid value range if numeric
                - condition (str): Conditional expression for parameter activation
                - pattern (str): Validation pattern for string parameters
                - format (str): Expected format for parameter values

        Example:
            >>> desc = OperatorDescriptor.get_operator_descriptor('Reproject')
            >>> for param in desc.parameter_descriptors:
            >>>     print(f"Parameter: {param['alias']} ({param['data_type']})")
            >>>     if param.get('default_value'):
            >>>         print(f"  Default: {param['default_value']}")
            >>>     if param.get('unit'):
            >>>         print(f"  Unit: {param['unit']}")
            >>>     print(f"  Description: {param['description']}")
        """
        descriptors = []
        for desc in self._descriptor.getParameterDescriptors():
            param_dict = self.get_parameter_dict(desc)
            descriptors.append(param_dict)
        return descriptors

    def get_parameter_dict(self, desc):
        """Build a parameter dictionary from a SNAP ParameterDescriptor.

        This method extracts all metadata from a SNAP ParameterDescriptor and
        organizes it into a Python dictionary with comprehensive parameter information
        including validation constraints, default values, and structural definitions.

        Args:
            desc (org.esa.snap.core.gpf.descriptor.ParameterDescriptor): The SNAP
                parameter descriptor to extract metadata from.

        Returns:
            dict: Dictionary containing parameter metadata with keys such as:
                - alias (str): Parameter name/alias
                - isDeprecated (bool): Whether parameter is deprecated
                - description (str): Parameter description
                - data_type (str): Parameter data type class name
                - default_value: Default parameter value
                - isNotNull (bool): Whether parameter cannot be null
                - isNotEmpty (bool): Whether parameter cannot be empty
                - unit (str): Physical unit if applicable
                - valueSet: Set of allowed values if constrained
                - interval: Valid value range if numeric
                - condition (str): Conditional expression for parameter activation
                - pattern (str): Validation pattern for string parameters
                - format (str): Expected format for parameter values
                - structure (dict): Nested structure definition if parameter is structured

        Example:
            >>> # Usually called internally by parameter_descriptors property
            >>> param_desc = operator_descriptor.get_parameter_dict(java_param_desc)
            >>> print(f"Parameter: {param_desc['alias']}")
            >>> print(f"Type: {param_desc['data_type']}")
            >>> print(f"Default: {param_desc['default_value']}")
        """
        unit = desc.getUnit()
        value_set = desc.getValueSet()
        interval = desc.getInterval()
        condition = desc.getCondition()
        pattern = desc.getPattern()
        format = desc.getFormat()
        param_dict = {
            'alias': desc.getAlias() or desc.getName(),
            'isDeprecated': desc.isDeprecated(),
            'description': desc.getDescription() or "",
            'data_type': desc.getDataType().getSimpleName(),
            'default_value': desc.getDefaultValue(),
            'isNotNull': desc.isNotNull(),
            'isNotEmpty': desc.isNotEmpty(),
            **({'unit': unit} if unit else {}),  # ** unpacks the dict
            **({'valueSet': value_set} if value_set else {}),
            **({'interval': interval} if interval else {}),
            **({'condition': condition} if condition else {}),
            **({'pattern': pattern} if pattern else {}),
            **({'format': format} if format else {}),
        }
        if desc.isStructure():
            for memDesc in desc.getStructureMemberDescriptors():
                param_dict["structure"] = self.get_parameter_dict(memDesc)
        return param_dict

    @staticmethod
    def get_operator_descriptor(operator_alias):
        """Get an OperatorDescriptor for the given operator alias.

        This static method retrieves an OperatorDescriptor instance for a SNAP operator
        by its alias name. It queries the SNAP operator registry to find the operator
        and returns a Python wrapper around its descriptor, providing access to all
        operator metadata including parameters, source requirements, and documentation.

        Args:
            operator_alias (str): The alias/name of the SNAP operator to retrieve
                (e.g., 'Reproject', 'BandMaths', 'Mosaic').

        Returns:
            OperatorDescriptor: A new OperatorDescriptor instance wrapping the
                Java operator descriptor with all metadata accessible through
                Python properties and methods.

        Raises:
            ValueError: If no operator with the specified alias is found in the
                SNAP operator registry.

        Example:
            >>> # Get descriptor for reprojection operator
            >>> desc = OperatorDescriptor.get_operator_descriptor('Reproject')
            >>> print(f"Operator: {desc.name}")
            >>> print(f"Parameters: {len(desc.parameter_descriptors)}")
            >>> 
            >>> # Get descriptor for band mathematics operator
            >>> math_desc = OperatorDescriptor.get_operator_descriptor('BandMaths')
            >>> for param in math_desc.parameter_descriptors:
            >>>     if param['alias'] == 'expression':
            >>>         print(f"Expression parameter: {param['description']}")
            >>> 
            >>> # Handle non-existent operator
            >>> try:
            >>>     invalid = OperatorDescriptor.get_operator_descriptor('NonExistent')
            >>> except ValueError as e:
            >>>     print(f"Error: {e}")
        """
        gpf = GPF.getDefaultInstance()
        spi = gpf.getOperatorSpiRegistry().getOperatorSpi(operator_alias)
        if spi is None:
            raise ValueError(f"Operator '{operator_alias}' not found")

        descriptor = spi.getOperatorDescriptor()
        return OperatorDescriptor(descriptor)

    def __str__(self):
        """Return a human-readable string representation of the operator descriptor.

        This method provides a comprehensive, formatted string representation that
        includes the operator name, description, source product requirements, and
        parameter definitions. It's useful for displaying operator information in
        a readable format for documentation or debugging purposes.

        Returns:
            str: Multi-line string containing formatted operator information including:
                - Operator name and description
                - Source product requirements with types and descriptions
                - Source products array information if applicable
                - Parameter definitions with types, units, defaults, and descriptions

        Example:
            >>> desc = OperatorDescriptor.get_operator_descriptor('Reproject')
            >>> print(str(desc))
            Operator: Reproject
            Description: Reprojection of products...
            
            Source Products:
              - sourceProduct (Product): The source product to be reprojected
            
            Parameters:
              - crs (String): Coordinate reference system definition
                The target coordinate reference system as WKT, EPSG code, or name
              - resampling (String), default: Nearest: Resampling method
                The method used for resampling the pixel values...
        """
        lines = []
        lines.append(f"Operator: {self.name}")
        lines.append(f"Description: {self.description}")

        if self.source_product_descriptors:
            lines.append("\nSource Products:")
            for desc in self.source_product_descriptors:
                lines.append(
                    f"  - {desc['alias']} ({desc['data_type']}): {desc['description']}")

        if self.source_products_descriptor:
            desc = self.source_products_descriptor
            lines.append(f"\nSource Products Array:")
            lines.append(
                f"  - {desc['alias']} ({desc['data_type']}): {desc['description']}")

        if self.parameter_descriptors:
            lines.append("Parameters:")
            for desc in self.parameter_descriptors:
                default_info = f", default: {desc['default_value']}" if desc[
                    'default_value'] else ""
                unit_info = f" [{desc['unit']}]" if desc.get('unit') else ""
                lines.append(
                    f"  - {desc['alias']} ({desc['data_type']}{unit_info}){default_info}")
                description = desc['description'].replace('\n', ' ')
                import textwrap
                lines.extend(f"    {line}" for line in
                             textwrap.fill(description, width=80-4).splitlines())

        return "\n".join(lines)

    def __repr__(self):
        """Return a string representation of the OperatorDescriptor for debugging.

        This method provides a concise string representation showing key operator
        characteristics for debugging and logging purposes.

        Returns:
            str: String representation in the format:
                "OperatorDescriptor(name='<name>', parameters=<count>, sources=<count>)"

        Example:
            >>> desc = OperatorDescriptor.get_operator_descriptor('Reproject')
            >>> print(repr(desc))
            OperatorDescriptor(name='Reproject', parameters=15, sources=1)
            >>> 
            >>> # Also works with print()
            >>> print(desc)
            OperatorDescriptor(name='Reproject', parameters=15, sources=1)
        """
        return f"OperatorDescriptor(name='{self.name}', parameters={len(self.parameter_descriptors)}, sources={len(self.source_product_descriptors)})"
