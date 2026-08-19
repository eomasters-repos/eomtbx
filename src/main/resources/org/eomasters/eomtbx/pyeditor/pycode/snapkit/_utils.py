import jarray
import java
import platform


class Utils:
    """Utility class providing helper functions for Java integration and platform detection.

    This class offers static utility methods for seamless integration between Python and Java
    environments, particularly within the SNAP (Sentinel Application Platform) ecosystem. It
    provides functionality for Java array creation with proper type handling, Java object
    introspection, platform detection, and type checking operations.

    The class handles the complexities of Python-Java data type conversions, ensuring proper
    mapping between Python array types and their Java equivalents. It also provides robust
    methods for examining Java objects, checking instance relationships, and detecting the
    current operating system platform for cross-platform compatibility.

    All methods are designed to handle edge cases gracefully, returning sensible defaults
    when operations fail rather than raising exceptions, making them suitable for defensive
    programming practices in mixed Python-Java environments.

    Note:
        This class cannot be instantiated. All methods are static and should be called
        directly on the class. Methods are designed to be safe and return reasonable
        defaults on failure.

    Example:
        >>> # Create Java arrays from Python data
        >>> int_array = Utils.create_java_array([1, 2, 3], 'i')
        >>> float_array = Utils.create_java_array([1.0, 2.0, 3.0], 'f')
        >>> 
        >>> # Check Java object types
        >>> is_java = Utils.is_java_array(int_array)
        >>> obj_type = Utils.type(java_object)
        >>> 
        >>> # Platform detection
        >>> if Utils.is_windows():
        >>>     print("Running on Windows")
        >>> elif Utils.is_linux():
        >>>     print("Running on Linux")
        >>> elif Utils.is_mac():
        >>>     print("Running on macOS")
    """

    def __new__(cls):
        raise TypeError(f"{cls.__name__} cannot be instantiated")

    @staticmethod
    def create_java_array(data, data_type):
        """Create a Java array from Python data with proper type conversion.

        This method creates Java arrays from Python data structures, handling the conversion
        between Python array type codes and their Java equivalents. It provides special
        handling for unsigned integer types that are not natively supported in Java by
        promoting them to the next larger signed type.

        The method uses pattern matching to efficiently handle different data types and
        ensures that all conversions result in valid Java arrays that can be used within
        the SNAP framework. Unsigned 64-bit integers are not supported as they have no
        equivalent in Java's type system.

        Args:
            data (list | tuple | array): Python sequence containing the data to convert.
                Can be any sequence type that supports iteration.
            data_type (str): Python array type code specifying the target data type.
                Valid codes: 'b'=int8, 'B'=uint8→int16, 'h'=int16, 'H'=uint16→int32,
                'i'=int32, 'I'=uint32→int64, 'l'=int64, 'd'=float64, 'f'=float32.

        Returns:
            jarray: A Java array of the appropriate type containing the converted data.
                    The returned array can be used directly with Java/SNAP APIs.

        Raises:
            ValueError: If data_type is 'L' (uint64), which is unsupported in Java, or if an
                        unknown data type code is provided.

        Example:
            >>> # Create various Java array types
            >>> byte_array = Utils.create_java_array([1, 2, 3], 'b')
            >>> int_array = Utils.create_java_array([100, 200, 300], 'i')
            >>> float_array = Utils.create_java_array([1.1, 2.2, 3.3], 'f')
            >>> double_array = Utils.create_java_array([1.0, 2.0, 3.0], 'd')
            >>> 
            >>> # Unsigned types are promoted to next larger signed type
            >>> ubyte_data = [255, 128, 64]  # uint8 data
            >>> promoted_array = Utils.create_java_array(ubyte_data, 'B')  # Becomes int16
            >>> 
            >>> # Handle unsupported type
            >>> try:
            >>>     Utils.create_java_array([1, 2, 3], 'L')  # uint64 not supported
            >>> except ValueError as e:
            >>>     print(f"Error: {e}")
        """
        match data_type:
            case 'b' | 'h' | 'i' | 'l' | 'd':
                return jarray.array(data, data_type)
            case 'B':
                return jarray.array(data, 'h')  # shift to next type
            case 'H':
                return jarray.array(data, 'i')
            case 'I':
                return jarray.array(data, 'l')
            case 'L':
                raise ValueError("type 'L' (unit64) not supported in java/snap")
            case _:
                raise ValueError(f"Unknown data type: {data_type}")

    @staticmethod
    def is_java_array(obj):
        """Check if an object is a Java array by examining its module attribute.

        This method determines whether an object originates from Java by checking its
        __module__ attribute. Java objects typically have modules different from
        "builtins", which is used for native Python objects. This provides a simple
        heuristic for distinguishing between Java and Python objects.

        Args:
            obj (object): The object to check for Java array origin.

        Returns:
            bool: True if the object appears to be a Java array (module != "builtins"),
                False if it's likely a Python builtin object.

        Example:
            >>> import array
            >>> python_array = array.array('i', [1, 2, 3])
            >>> java_array = Utils.create_java_array([1, 2, 3], 'i')
            >>> print(Utils.is_java_array(python_array))  # False
            >>> print(Utils.is_java_array(java_array))    # True
        """
        module = getattr(obj, "__module__", "builtins")
        return module != "builtins"

    @staticmethod
    def component_type(java_array):
        """Get the component type of a Java array by traversing its type hierarchy.

        This method examines a Java array object and determines the type of its
        elements by traversing the Java class hierarchy. For multi-dimensional
        arrays, it recursively finds the base component type. The method safely
        handles any errors during type introspection.

        Args:
            java_array (jarray): Java array object to examine.

        Returns:
            str: String representation of the component type (e.g., "class java.lang.Integer"),
                or "unknown" if type determination fails.

        Example:
            >>> int_array = Utils.create_java_array([1, 2, 3], 'i')
            >>> comp_type = Utils.component_type(int_array)
            >>> print(comp_type)  # "int" or similar Java type representation
            >>> 
            >>> # Handle invalid input gracefully
            >>> invalid_type = Utils.component_type("not_an_array")
            >>> print(invalid_type)  # "unknown"
        """
        try:
            c = java_array.getClass()
            while c.isArray():
                c = c.getComponentType()
            return c.toString()
        except:
            return "unknown"

    @staticmethod
    def type(jobject):
        """Get the Java class type of a Java object as a string representation.

        This method examines a Java object and returns its class type information
        as a string. It provides safe introspection of Java objects within the
        Python-Java bridge environment, handling any potential errors gracefully.

        Args:
            jobject (object): Java object to examine for type information.

        Returns:
            str: String representation of the Java object's class type
                (e.g., "class java.lang.String"), or "unknown" if type determination fails.

        Example:
            >>> java_string = java.lang.String("Hello")
            >>> obj_type = Utils.type(java_string)
            >>> print(obj_type)  # "class java.lang.String"
            >>> 
            >>> # Handle non-Java objects
            >>> python_string = "Hello"
            >>> python_type = Utils.type(python_string)
            >>> print(python_type)  # "unknown" or error string
        """
        try:
            c = jobject.getClass()
            return c.toString()
        except:
            return "unknown"

    @staticmethod
    def is_instance(jobj, qualified_path):
        """Check if a Java object is an instance of a specific Java class.

        This method performs instanceof checking between a Java object and a Java
        class specified by its fully qualified name. It safely handles the dynamic
        loading of Java classes and performs the instance check, returning False
        if any errors occur during the process.

        Args:
            jobj (object): Java object to check for instance relationship.
            qualified_path (str): Fully qualified Java class name (e.g.,
                "org.esa.snap.core.datamodel.Product", "java.lang.String").

        Returns:
            bool: True if jobj is an instance of the specified Java class,
                False if not an instance, or if any error occurs during checking.

        Example:
            >>> # Check SNAP product instance
            >>> is_product = Utils.is_instance(java_product, "org.esa.snap.core.datamodel.Product")
            >>> print(is_product)  # True if java_product is a SNAP Product
            >>> 
            >>> # Check with invalid class name
            >>> is_invalid = Utils.is_instance(java_object, "non.existent.Class")
            >>> print(is_invalid)  # False
            >>> 
            >>> # Check Python object against Java class
            >>> is_python = Utils.is_instance("python_string", "java.lang.String")
            >>> print(is_python)  # False
        """
        try:
            my_java_class = java.type(qualified_path)
            return java.instanceof(jobj, my_java_class)
        except:
            return False

    @staticmethod
    def is_linux():
        """Check if the current operating system is Linux.

        This method uses Python's platform module to determine if the code is
        running on a Linux operating system.

        Returns:
            bool: True if running on Linux, False otherwise.

        Example:
            >>> if Utils.is_linux():
            >>>     print("Running on Linux system")
            >>> else:
            >>>     print("Not running on Linux")
        """
        return platform.system() == "Linux"

    @staticmethod
    def is_mac():
        """Check if the current operating system is macOS.

        This method uses Python's platform module to determine if the code is
        running on a macOS operating system. Note that macOS is identified by
        the system name "Darwin".

        Returns:
            bool: True if running on macOS (Darwin), False otherwise.

        Example:
            >>> if Utils.is_mac():
            >>>     print("Running on macOS system")
            >>> else:
            >>>     print("Not running on macOS")
        """
        return platform.system() == "Darwin"

    @staticmethod
    def is_windows():
        """Check if the current operating system is Windows.

        This method uses Python's platform module to determine if the code is
        running on a Windows operating system.

        Returns:
            bool: True if running on Windows, False otherwise.

        Example:
            >>> if Utils.is_windows():
            >>>     print("Running on Windows system")
            >>> else:
            >>>     print("Not running on Windows")
        """
        return platform.system() == "Windows"
