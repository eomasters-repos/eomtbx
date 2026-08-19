"""SNAP Graph Processing Module.

This module provides functionality for creating, configuring, and executing
SNAP (Sentinel Application Platform) processing graphs. It offers a Python
interface to SNAP's Java-based graph processing capabilities, allowing users
to build complex remote sensing data processing workflows programmatically.

The main component is the Graph class, which enables:
- Building processing graphs by adding operators
- Connecting operators to create processing chains
- Executing graphs and retrieving results
- Importing/exporting graphs from/to XML format
- Integration with SNAP's extensive operator library

This module is part of the snapkit package, which provides Python bindings
for SNAP functionality in the EOMasters EOMTBX environment.

Example:
    Basic usage for creating a processing chain:

    >>> def progress_callback(progress):
    >>>     print(f"Processing: {progress}%")
    >>> from snapkit.graph import Graph
    >>> graph = Graph()
    >>> read_op = graph.read('/path/to/input.dim')
    >>> calib_op = graph.add_operator('calib', 'Calibration', {'outputSigmaBand': True})
    >>> write_op = graph.write('/path/to/output.dim')
    >>> graph.connect(read_op, calib_op, write_op)
    >>> result = graph.execute(progress_callback=progress_callback, callback_interval=10)
"""
import os
from com.bc.ceres.binding.dom import DefaultDomElement
from com.bc.ceres.core import ProgressMonitor as JProgressMonitor
from java.nio.file import Path, Files
from org.esa.snap.core.gpf.graph import Graph as JGraph
from org.esa.snap.core.gpf.graph import GraphIO, Node, NodeSource
from org.esa.snap.core.gpf.graph import GraphProcessor, GraphContext

from .product import Product, _create_product


class Graph:
    """Creates and executes processing graphs using SNAP operators.

    This class provides an interface for building and executing
    SNAP processing graphs. It allows you to add operators, connect them,
    and execute the entire processing chain.

    Example:
        Basic usage example:

        >>> def progress_callback(progress):
        >>>     print(f"Processing: {progress}%")
        >>> graph = Graph()
        >>> read_id = graph.read('/path/to/input.dim')
        >>> calib_id = graph.add_operator('calib', 'Calibration', {'outputSigmaBand': True})
        >>> write_id = graph.write('/path/to/output.dim')
        >>> graph.connect(read_id, calib_id, write_id)
        >>> result = graph.execute(progress_callback=progress_callback, callback_interval=10)
    """

    def __init__(self, graph_name: str = "graph", java_graph: JGraph = None):
        """Initialize a new graph processor.

        Args:
            graph_name (str, optional): Name of the graph. Defaults to "graph".
            java_graph (JGraph, optional): Existing JGraph object to initialize from.
                If provided, the graph will be initialized from this existing Java graph
                object, extracting operators and connections. Defaults to None.
        """
        if java_graph is not None:
            # Initialize from existing JGraph
            self._graph_name = java_graph.getId()
            self._operators = []
            self._connections = []

            # Extract operators from the JGraph
            for node in java_graph.getNodes():
                operator_data = {
                    'id': node.getId(),
                    'name': node.getOperatorName(),
                    'parameters': {}
                }

                # Extract parameters from node configuration
                config = node.getConfiguration()
                if config is not None:
                    for child in config.getChildren():
                        param_name = child.getName()
                        param_value = child.getValue()
                        operator_data['parameters'][param_name] = param_value

                self._operators.append(operator_data)

                # Extract connections from node sources
                for source in node.getSources():
                    self._connections.append({
                        'source': source.getSourceNodeId(),
                        'target': node.getId(),
                        'source_name': source.getName()
                    })
        else:
            # Initialize new graph
            self._graph_name = graph_name
            self._operators = []
            self._connections = []

    def add_operator(self, operator_id: str, operator_name: str, parameters: dict[str, any] = None) -> str:
        """Add an operator to the graph.

        Args:
            operator_id (str): Unique ID for this operator in the graph.
            operator_name (str): Name of the operator (e.g., 'Read', 'Write', 'Calibration').
            parameters (dict, optional): Dictionary of parameters for the operator.
                Defaults to None, which creates an empty parameter dictionary.

        Returns:
            str: The operator ID for chaining operations.

        Raises:
            ValueError: If operator_id is already in use.

        Example:
            >>> graph = Graph()
            >>> op_id = graph.add_operator('calib1', 'Calibration', {'outputSigmaBand': True})
            >>> print(op_id)
            calib1
        """
        self._operators.append({
            'id': operator_id,
            'name': operator_name,
            'parameters': parameters or {}
        })
        return operator_id

    def connect(self, *node_ids: str, source_name: str = 'sourceProduct'):
        """Connect operators in the graph.

        Can be used to connect two specific operators or chain multiple operators in a sequence.
        Each consecutive pair of node IDs will be connected.

        Args:
            *node_ids (str): Variable number of node IDs to connect. Minimum 2 required.
                The operators will be connected in the order they are provided.
            source_name (str, optional): Name of the source product parameter used for
                connections. Defaults to 'sourceProduct'.

        Raises:
            ValueError: If less than 2 node IDs are provided.

        Examples:
            >>> # Connect two operators:
            >>> graph.connect('read_op', 'calib_op')
            >>> # Connect multiple operators in sequence:
            >>> graph.connect('read_op', 'calib_op', 'filter_op', 'write_op')
            >>> # Connect with custom source name:
            >>> graph.connect('source', 'target', source_name='inputProduct')
        """
        if len(node_ids) < 2:
            raise ValueError(
                "At least two node IDs are required to create a connection")

        # Connect each consecutive pair of nodes
        for i in range(len(node_ids) - 1):
            source_id = node_ids[i]
            target_id = node_ids[i + 1]

            self._connections.append({
                'source': source_id,
                'target': target_id,
                'source_name': source_name
            })

    def clear_connections(self):
        """Clear all stored connections.

        This method resets the connection list to an empty list, ensuring that
        any previously stored connections are removed.
        """
        self._connections = []

    def read(self, file_path: str) -> str:
        """Add a Read operator to the graph.

        Creates and adds a Read operator to the graph with the specified input file path.
        The file path will be converted to an absolute path.

        Args:
            file_path (str): Path to the input file to be read.

        Returns:
            str: The automatically generated operator ID for this Read operation.
                The ID follows the pattern 'read_N' where N is the current number of operators.

        Example:
            >>> graph = Graph()
            >>> read_op = graph.read('/path/to/input.dim')
            >>> print(read_op)
            read_0
        """
        op_id = f"read_{len(self._operators)}"
        file_path = os.path.abspath(file_path)
        self.add_operator(op_id, "Read", {"file": file_path})
        return op_id

    def write(self, file_path: str, format_name: str = None) -> str:
        """Add a Write operator to the graph.

        Creates and adds a Write operator to the graph with the specified output file path
        and optional format. The file path will be converted to an absolute path.

        Args:
            file_path (str): Path to the output file to be written.
            format_name (str, optional): Format name for the output file (e.g., 'BEAM-DIMAP',
                'GeoTIFF', 'NetCDF'). If not specified, SNAP will determine the format
                from the file extension. Defaults to None.

        Returns:
            str: The automatically generated operator ID for this Write operation.
                The ID follows the pattern 'write_N' where N is the current number of operators.

        Example:
            >>> graph = Graph()
            >>> write_op = graph.write('/path/to/output.dim', 'BEAM-DIMAP')
            >>> print(write_op)
            write_0
        """
        op_id = f"write_{len(self._operators)}"
        file_path = os.path.abspath(file_path)
        params = {"file": file_path}
        if format_name:
            params["formatName"] = format_name

        self.add_operator(op_id, "Write", params)
        return op_id

    def export_xml(self, file_path: str):
        """Export the graph data to an XML file.

        This method generates an internal representation of the graph and writes
        it to the specified location as an XML file. The file will be created or
        overwritten in the provided path. The file path will be converted to an
        absolute path.

        Args:
            file_path (str): The absolute or relative file path where the XML
                file will be written. Must be a valid path string.

        Example:
            >>> graph = Graph()
            >>> # Add operators and connections...
            >>> graph.export_xml('/path/to/graph.xml')
        """
        file_path = os.path.abspath(file_path)
        writer = Files.newBufferedWriter(Path.of(file_path))
        graph = self.__build_graph()
        GraphIO.write(graph, writer)

    def execute(self, progress_callback=None, callback_interval=5) -> Product | list[Product]:
        """Execute the graph and return the result.

        Processes the constructed graph by building the internal SNAP graph representation
        and executing it through the GraphProcessor. Returns the output product(s) from
        the last operator in the graph.

        Args:
            progress_callback (callable): Optional callback function that receives
                progress updates during graph execution. The callback should accept a single
                parameter (progress percentage as float). Defaults to None.
            callback_interval (int): Interval in percentage points for progress
                callback notifications. Only calls the callback when progress increases by
                this amount or more. Defaults to 5.

        Returns:
            Product or list[Product]: The output product from the last operator in
                the graph. Returns a single Product if one output, a list of Products if
                multiple outputs, or None if no outputs are produced.

        Raises:
            PyRunnerException: If graph execution fails during processing.
            ValueError: If no operators are defined in the graph.

        Example:
            >>> def my_callback(progress):
            >>>     print(f"Progress: {progress}%")
            >>> graph = Graph()
            >>> # Add operators and connections...
            >>> result = graph.execute(progress_callback=my_callback)
            >>> print(result.getName())
        """

        graph = self.__build_graph()
        return self.__execute_graph(graph, progress_callback, callback_interval)

    @staticmethod
    def execute_xml(file_path: str, progress_callback=None, callback_interval=5) -> Product | list[Product]:
        """Execute an XML-based graph file.

        This method takes the file path of an XML graph file, reads the contents, converts it
        into a Graph object, and then processes the graph. The file path will be converted
        to an absolute path.

        Args:
            file_path (str): The file path of the XML graph file to be executed.
            progress_callback (callable): Optional callback function that receives
                progress updates during graph execution. The callback should accept a single
                float parameter (progress percentage as float). Defaults to None.
            callback_interval (int): Interval in percentage points for progress
                callback notifications. Only calls the callback when progress increases by
                this amount or more. Defaults to 5.

        Returns:
            Product or list[Product]: The processed product(s) from the graph execution.
                Returns a single Product if one output, a list of Products if multiple outputs,
                or None if no outputs are produced.

        Example:
            >>> def my_callback(progress):
            >>>     print(f"Progress: {progress}%")
            >>> result = Graph.execute_xml('/path/to/graph.xml', progress_callback=my_callback)
            >>> print(result.getName())
        """
        file_path = os.path.abspath(file_path)
        reader = Files.newBufferedReader(Path.of(file_path))
        jgraph = GraphIO.read(reader)
        graph = Graph(jgraph)
        return graph.execute(progress_callback, callback_interval)

    def __execute_graph(self, graph: JGraph, progress_callback=None, callback_interval=5) -> Product | list[Product]:
        processor = GraphProcessor()
        graph_context = GraphContext(graph)

        if progress_callback is None:
            progress_monitor = JProgressMonitor.NULL
        else:
            from ._progressMonitor import CallbackProgressMonitor
            progress_monitor = CallbackProgressMonitor(progress_callback, callback_interval)

        jproducts = processor.executeGraph(graph_context, progress_monitor)

        if len(jproducts) == 0:
            return None
        if len(jproducts) == 1:
            return self.__get_or_read_product(jproducts[0])
        else:
            products = []
            for jproduct in jproducts:
                read_product = self.__get_or_read_product(jproduct)
                products.append(_create_product(read_product))
            return products

    def __get_or_read_product(self, jproduct) -> Product:
        writer = jproduct.getProductWriter()
        if (writer is None) or (writer.getOutput() is None):
            return _create_product(jproduct)
        else:
            read = Product.read(writer.getOutput().toString())
            if read is None:
                from org.eomasters.eomtbx.pyeditor.pyrun import PyRunnerException
                raise PyRunnerException(
                    "Product could not be read from " + writer.getOutput().toString())
            else:
                jproduct.dispose()
                return read

    def __build_graph(self) -> JGraph:
        # Create the graph XML
        graph = JGraph(self._graph_name)
        # Add nodes
        for op in self._operators:
            node = Node(op['id'], op['name'])

            # Add parameters
            config = DefaultDomElement("parameters")
            for param_name, param_value in op['parameters'].items():
                config.createChild(param_name).setValue(str(param_value))
            node.setConfiguration(config)

            graph.addNode(node)
        # Add connections
        for conn in self._connections:
            target_node = graph.getNode(conn['target'])
            source = NodeSource(conn['source_name'], conn['source'])
            target_node.addSource(source)
        return graph
