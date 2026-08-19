#!/usr/bin/env python3
"""
Python Documentation Generator for SnapKit

This script generates HTML documentation for the SnapKit Python package.
It processes Python source files and creates JavaHelp-compatible HTML documentation.
"""

import ast
import os
import re
import sys
from pathlib import Path

whitelisted_methods = ('__init__', '__getitem__')


class GoogleDocstringParser:
    """Parser for Google-style docstrings."""

    def __init__(self):
        # Google style sections that we recognize
        self.sections = ['Args', 'Arguments', 'Parameters', 'Param', 'Returns', 'Return', 'Yields', 'Yield',
                         'Raises', 'Raise', 'Note', 'Notes', 'Example', 'Examples', 'See Also', 'Attributes']
        # Regex pattern to match section headers
        self.section_pattern = re.compile(r'^(' + '|'.join(self.sections) + r'):\s*$', re.MULTILINE)

    def parse(self, docstring: str) -> dict[str, str | dict | bool | None]:
        """Parse a Google-style docstring into structured sections.
        
        Args:
            docstring: The docstring to parse
            
        Returns:
            Dict containing parsed sections and validation status
            
        Raises:
            ValueError: If docstring is not in Google style format
        """
        if not docstring:
            return {'description': '', 'sections': {}, 'is_valid': True, 'error': None}

        try:
            # Split docstring into lines and clean up
            lines = docstring.strip().split('\n')

            # Find section headers
            section_matches = list(self.section_pattern.finditer(docstring))

            if not section_matches:
                # No sections found - could be just a description
                return {
                    'description': docstring.strip(),
                    'sections': {},
                    'is_valid': True,
                    'error': None
                }

            # Extract description (everything before the first section)
            first_section_pos = section_matches[0].start()
            description_text = docstring[:first_section_pos].strip()

            # Parse sections
            sections = {}
            for i, match in enumerate(section_matches):
                section_name = match.group(1).lower()
                section_start = match.end()

                # Find the end of this section (start of next section or end of docstring)
                if i + 1 < len(section_matches):
                    section_end = section_matches[i + 1].start()
                else:
                    section_end = len(docstring)

                section_content = docstring[section_start:section_end].strip()
                sections[section_name] = self._parse_section_content(section_name, section_content)

            # Validate the structure
            validation_result = self._validate_sections(sections)

            return {
                'description': description_text,
                'sections': sections,
                'is_valid': validation_result['is_valid'],
                'error': validation_result.get('error')
            }

        except Exception as e:
            return {
                'description': docstring,
                'sections': {},
                'is_valid': False,
                'error': f"Failed to parse docstring: {str(e)}"
            }

    def _parse_section_content(self, section_name: str, content: str) -> dict[str, str | list]:
        """Parse the content of a specific section."""
        if section_name in ['args', 'arguments', 'parameters', 'param', 'attributes']:
            return self._parse_args_section(content)
        elif section_name in ['returns', 'return', 'yields', 'yield']:
            return self._parse_returns_section(content)
        elif section_name in ['raises', 'raise']:
            return self._parse_raises_section(content)
        else:
            return {'type': 'text', 'content': content}

    def _parse_args_section(self, content: str) -> dict[str, str | list]:
        """Parse Args/Arguments section."""
        items = []
        current_item = None

        lines = content.split('\n')
        for i, line in enumerate(lines):
            original_line = line
            line = line.strip()
            if not line:
                # Preserve empty lines in descriptions
                if current_item and current_item['description']:
                    current_item['description'] += '\n'
                continue

            # Check if this line starts a new argument (contains a colon)
            if ':' in line and not line.startswith(' '):
                if current_item:
                    items.append(current_item)

                parts = line.split(':', 1)
                arg_part = parts[0].strip()
                desc_part = parts[1].strip() if len(parts) > 1 else ''

                # Parse argument name and type if present
                if '(' in arg_part and ')' in arg_part:
                    # Format: arg_name (type): description
                    name = arg_part.split('(')[0].strip()
                    type_part = arg_part.split('(')[1].split(')')[0].strip()
                else:
                    # Format: arg_name: description
                    name = arg_part
                    type_part = None

                current_item = {
                    'name': name,
                    'type': type_part,
                    'description': desc_part
                }
            else:
                # Continuation of previous argument description
                if current_item:
                    if current_item['description']:
                        current_item['description'] += '\n' + original_line
                    else:
                        current_item['description'] = original_line

        if current_item:
            items.append(current_item)

        return {'type': 'args', 'items': items}

    def _parse_returns_section(self, content: str) -> dict[str, str | None]:
        """Parse Returns/Return section."""
        # Check if it starts with a type specification
        lines = [line.strip() for line in content.split('\n') if line.strip()]
        if not lines:
            return {'type': 'returns', 'return_type': None, 'description': ''}

        first_line = lines[0]
        if ':' in first_line and not first_line.startswith(' '):
            # Format: Type: description
            parts = first_line.split(':', 1)
            return_type = parts[0].strip()
            description = parts[1].strip()
            if len(lines) > 1:
                description += ' ' + ' '.join(lines[1:])
        else:
            # Just description
            return_type = None
            description = ' '.join(lines)

        return {'type': 'returns', 'return_type': return_type, 'description': description}

    def _parse_raises_section(self, content: str) -> dict[str, str | list]:
        """Parse Raises/Raise section."""
        items = []
        current_item = None

        for line in content.split('\n'):
            line = line.strip()
            if not line:
                continue

            # Check if this line starts a new exception (contains a colon)
            if ':' in line and not line.startswith(' '):
                if current_item:
                    items.append(current_item)

                parts = line.split(':', 1)
                exception_type = parts[0].strip()
                description = parts[1].strip() if len(parts) > 1 else ''

                current_item = {
                    'exception': exception_type,
                    'description': description
                }
            else:
                # Continuation of previous exception description
                if current_item:
                    if current_item['description']:
                        current_item['description'] += ' ' + line
                    else:
                        current_item['description'] = line

        if current_item:
            items.append(current_item)

        return {'type': 'raises', 'items': items}

    def _validate_sections(self, sections: dict[str, dict]) -> dict[str, bool | None]:
        """Validate that the sections follow Google style conventions."""
        # For now, we accept any structure that was parseable
        # In the future, we could add stricter validation rules
        return {'is_valid': True, 'error': None}


class DocGenerator:
    def __init__(self, source_dir: str, output_dir: str):
        self.source_dir = Path(source_dir)
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.docstring_parser = GoogleDocstringParser()
        self.validation_errors = []

    def generate_docs(self):
        """Generate documentation for all public Python modules."""
        print(f"Generating documentation from {self.source_dir} to {self.output_dir}")

        # Get all public Python files (excluding private modules starting with _)
        python_files = [f for f in self.source_dir.glob("*.py")
                        if not f.name.startswith("_") and f.name != "__init__.py"]

        # Parse __init__.py for package documentation
        init_file = self.source_dir / "__init__.py"
        package_info = self.parse_init_file(init_file)

        # Generate module documentation
        modules = {}
        for py_file in python_files:
            module_info = self.parse_python_file(py_file)
            if module_info:
                modules[py_file.stem] = module_info
                self.generate_module_html(py_file.stem, module_info)

        # Generate index.html
        self.generate_index_html(package_info, modules)

        # Generate CSS file
        self.generate_css()

        # Check for validation errors and fail if found
        if self.validation_errors:
            print(f"\nValidation failed with {len(self.validation_errors)} error(s):")
            for error in self.validation_errors:
                print(f"  - {error}")
            print("\nDocumentation generation failed due to Google-style docstring validation errors.")
            sys.exit(1)

        print(f"Documentation generated successfully in {self.output_dir}")

    def parse_init_file(self, init_file: Path) -> dict[str, str | list]:
        """Parse __init__.py file to extract package documentation."""
        try:
            with open(init_file, 'r', encoding='utf-8') as f:
                content = f.read()

            tree = ast.parse(content)
            docstring = ast.get_docstring(tree)

            # Extract __all__ if present
            all_items = []
            for node in ast.walk(tree):
                if isinstance(node, ast.Assign):
                    for target in node.targets:
                        if isinstance(target, ast.Name) and target.id == '__all__':
                            if isinstance(node.value, ast.List):
                                all_items = [elt.s for elt in node.value.elts if isinstance(elt, ast.Str)]

            return {
                'docstring': docstring or "SnapKit Python Package",
                'all_items': all_items
            }
        except Exception as e:
            print(f"Error parsing {init_file}: {e}")
            return {'docstring': "SnapKit Python Package", 'all_items': []}

    def parse_python_file(self, py_file: Path) -> dict[str, str | list] | None:
        """Parse a Python file to extract class and method documentation."""
        try:
            with open(py_file, 'r', encoding='utf-8') as f:
                content = f.read()

            tree = ast.parse(content)
            module_docstring = ast.get_docstring(tree)

            classes = []
            functions = []

            for node in ast.walk(tree):
                if isinstance(node, ast.ClassDef):
                    # Only document public classes (not starting with _)
                    if not node.name.startswith('_'):
                        class_info = self.parse_class(node)
                        if class_info:
                            classes.append(class_info)
                elif isinstance(node, ast.FunctionDef):
                    # Only document public functions at module level (not starting with _)
                    if not node.name.startswith('_') and node.col_offset == 0:
                        func_info = self.parse_function(node)
                        if func_info:
                            functions.append(func_info)

            return {
                'name': py_file.stem,
                'docstring': module_docstring,
                'classes': classes,
                'functions': functions
            }
        except Exception as e:
            print(f"Error parsing {py_file}: {e}")
            return None

    def parse_class(self, class_node: ast.ClassDef) -> dict[str, str | list]:
        """Parse a class node to extract documentation."""
        class_docstring = ast.get_docstring(class_node)

        methods = []
        properties = []
        setters = {}  # Map property names to setter info

        # First pass: collect all methods, properties, and setters
        for node in class_node.body:
            if isinstance(node, ast.FunctionDef):
                # Document public methods and constructors (__init__), but exclude other private methods
                if not node.name.startswith('_') or node.name in whitelisted_methods:
                    method_info = self.parse_function(node, is_method=True)
                    if method_info:
                        # Check if it's a property
                        if any(isinstance(decorator, ast.Name) and decorator.id == 'property'
                               for decorator in node.decorator_list):
                            properties.append(method_info)
                        # Check if it's a setter
                        elif any(isinstance(decorator, ast.Attribute) and
                                 decorator.attr == 'setter'
                                 for decorator in node.decorator_list):
                            # Extract property name from decorator
                            prop_name = node.name
                            setters[prop_name] = method_info
                        else:
                            methods.append(method_info)

        # Second pass: merge setter info into properties
        for prop in properties:
            prop_name = prop['name']
            if prop_name in setters:
                setter_info = setters[prop_name]
                # Add setter parameter information to property
                prop['has_setter'] = True
                prop['setter_args'] = setter_info['args']
                # You could also merge setter docstring if needed

        return {
            'name': class_node.name,
            'docstring': class_docstring,
            'methods': methods,
            'properties': properties
        }

    def parse_function(self, func_node: ast.FunctionDef, is_method: bool = False) -> dict[str, str | list | bool | dict | None]:
        """Parse a function node to extract documentation."""
        func_docstring = ast.get_docstring(func_node)

        # Extract arguments
        args = []
        for arg in func_node.args.args:
            if not (is_method and arg.arg == 'self') and not arg.arg.startswith(
                '_'):  # Skip 'self' for methods and parameters starting with '_'
                args.append(arg.arg)

        # Parse and validate Google-style docstring
        parsed_docstring = None
        if func_docstring:
            parsed_docstring = self.docstring_parser.parse(func_docstring)
            if not parsed_docstring['is_valid']:
                error_msg = f"Function '{func_node.name}' has invalid Google-style docstring: {parsed_docstring['error']}"
                self.validation_errors.append(error_msg)
                print(f"ERROR: {error_msg}")
        else:
            # For public methods with arguments, require docstring in Google style
            if args and not func_node.name.startswith('_'):
                error_msg = f"Public function '{func_node.name}' with arguments must have Google-style docstring"
                self.validation_errors.append(error_msg)
                print(f"ERROR: {error_msg}")

        return {
            'name': func_node.name,
            'docstring': func_docstring,
            'parsed_docstring': parsed_docstring,
            'args': args,
            'is_static': any(isinstance(decorator, ast.Name) and decorator.id == 'staticmethod'
                             for decorator in func_node.decorator_list),
            'is_classmethod': any(isinstance(decorator, ast.Name) and decorator.id == 'classmethod'
                                  for decorator in func_node.decorator_list)
        }

    def generate_index_html(self, package_info: dict[str, str | list], modules: dict[str, dict[str, str | list]]):
        """Generate the main index.html file."""
        html_content = f'''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SnapKit API Documentation</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        <h1>SnapKit API Documentation</h1>
        <div class="package-description"><pre>{self.escape_html(package_info['docstring'])}</pre></div>
        
        <h2>Modules</h2>
        <div class="module-list">
'''

        for module_name, module_info in sorted(modules.items()):
            description = module_info.get('docstring', 'No description available.')
            if description:
                # Take first line of docstring for brief description
                brief = description.split('\n')[0].strip()
            else:
                brief = 'No description available.'

            html_content += f'''            <div class="module-item">
                <a href="{module_name}.html"><span class="module-name">{module_name}</span> - <span class="module-short-description">{self.escape_html(brief)}</span></a>
            </div>
'''

        html_content += '''        </div>
    </div>
</body>
</html>'''

        with open(self.output_dir / "index.html", 'w', encoding='utf-8') as f:
            f.write(html_content)

    def generate_module_html(self, module_name: str, module_info: dict[str, str | list]):
        """Generate HTML documentation for a single module."""
        html_content = f'''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{module_name} - SnapKit API Documentation</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        <div class="navigation">
            <a href="index.html">← Back to Index</a>
        </div>
        
        <h1>Module: {module_name}</h1>
'''

        if module_info.get('docstring'):
            cleaned_docstring = self.fix_code_example_indentation(module_info['docstring'])
            html_content += f'''        <div class="module-description"><pre>{(self.escape_html(cleaned_docstring))}</pre></div>
'''

        # Generate class documentation
        for class_info in module_info.get('classes', []):
            html_content += self.generate_class_html(class_info)

        # Generate function documentation
        for func_info in module_info.get('functions', []):
            html_content += self.generate_function_html(func_info, is_module_function=True)

        html_content += '''    </div>
</body>
</html>'''

        with open(self.output_dir / f"{module_name}.html", 'w', encoding='utf-8') as f:
            f.write(html_content)

    def generate_class_html(self, class_info: dict[str, str | list]) -> str:
        """Generate HTML for a class."""
        class_name = class_info['name']
        class_doc = class_info.get('docstring', '')

        html = f'''
        <div class="class-section" id="{class_name}">
            <h2>Class: {class_name}</h2>
'''

        if class_doc:
            cleaned_docstring = self.fix_code_example_indentation(class_doc)
            html += f'''        <div class="module-description"><pre>{(self.escape_html(cleaned_docstring))}</pre></div>'''

        # Add overview section with links to properties and methods
        properties = class_info.get('properties', [])
        methods = class_info.get('methods', [])

        if properties or methods:
            html += '''            <h3>Overview</h3>
            <div class="overview-section">
'''
            if properties:
                html += '''                <h4>Properties</h4>
                <div class="overview-list">
'''
                for prop in properties:
                    prop_name = prop['name']
                    html += f'''                    <span class="overview-item">&nbsp;<a href="#{class_name}-{prop_name}">{prop_name}</a>&nbsp;</span>
'''
                html += '''                </div>
'''

            if methods:
                html += '''                <h4>Methods</h4>
                <div class="overview-list">
'''
                for method in methods:
                    method_name = method['name']
                    html += f'''                    <span class="overview-item">&nbsp;<a href="#{class_name}-{method_name}">{method_name}</a>&nbsp;</span>
'''
                html += '''                </div>
'''

            html += '''            </div>
'''

        # Properties
        if properties:
            html += '''            <h3>Properties</h3>
            <div class="properties-section">
'''
            for prop in properties:
                html += self.generate_function_html(prop, is_property=True, class_name=class_name)
            html += '''            </div>
'''

        # Methods
        methods = class_info.get('methods', [])
        if methods:
            html += '''            <h3>Methods</h3>
            <div class="methods-section">
'''
            for method in methods:
                html += self.generate_function_html(method, class_name=class_name)
            html += '''            </div>
'''

        html += '''        </div>
'''
        return html

    def generate_function_html(self, func_info: dict[str, str | list | bool | dict | None], is_property: bool = False,
        is_module_function: bool = False, class_name: str = None) -> str:
        """Generate HTML for a function or method."""
        func_name = func_info['name']
        func_doc = func_info.get('docstring', '')
        args = func_info.get('args', [])

        # Build signature
        if is_property:
            signature = f"{func_name}"
            func_type = "property"
        elif func_name == "__init__":
            signature = f"{func_name}({', '.join(args)})"
            func_type = "constructor"
        elif func_info.get('is_static'):
            signature = f"{func_name}({', '.join(args)})"
            func_type = "static method"
        elif func_info.get('is_classmethod'):
            signature = f"{func_name}({', '.join(args)})"
            func_type = "class method"
        elif is_module_function:
            signature = f"{func_name}({', '.join(args)})"
            func_type = "function"
        else:
            signature = f"{func_name}({', '.join(args)})"
            func_type = "method"

        # Add anchor ID if class_name is provided
        anchor_id = f' id="{class_name}-{func_name}"' if class_name else ''

        html = f'''                <div class="function-item"{anchor_id}>
                    <h4 class="function-signature">{signature}</h4>
                    <span class="function-type">{func_type}</span>
'''

        # Use parsed Google-style docstring if available
        parsed_docstring = func_info.get('parsed_docstring')
        if parsed_docstring and parsed_docstring['is_valid']:
            html += '''                    <div class="function-description">
'''
            # Add description
            if parsed_docstring['description']:
                cleaned_description = self.fix_code_example_indentation(parsed_docstring['description'])
                html += f'''                        <div class="docstring-description">
                            <pre>{self.escape_html(cleaned_description)}</pre>
                        </div>
'''

            # Add sections
            sections = parsed_docstring.get('sections', {})

            # Arguments section
            if 'args' in sections or 'arguments' in sections or 'parameters' in sections or 'param' in sections:
                args_section = (sections.get('args') or sections.get('arguments') or
                                sections.get('parameters') or sections.get('param'))
                if args_section and args_section.get('items'):
                    html += '''                        <div class="docstring-section">
                            <h5>Arguments:</h5>
                            <ul class="args-list">
'''
                    for item in args_section['items']:
                        type_info = f" ({item['type']})" if item['type'] else ""
                        cleaned_arg_description = self.fix_code_example_indentation(item['description']) if item[
                            'description'] else ""
                        html += f'''                                <li>
                                    <strong>{self.escape_html(item['name'])}{type_info}</strong>: <pre>{self.escape_html(cleaned_arg_description)}</pre>
                                </li>
'''
                    html += '''                            </ul>
                        </div>
'''

            # Returns section
            if 'returns' in sections or 'return' in sections:
                returns_section = sections.get('returns') or sections.get('return')
                if returns_section:
                    # Determine header based on whether property has setter
                    if is_property and func_info.get('has_setter'):
                        header = "Returns/Argument:"
                    else:
                        header = "Returns:"

                    html += f'''                        <div class="docstring-section">
                            <h5>{header}</h5>
                            <div class="returns-info">
'''
                    if returns_section.get('return_type'):
                        html += f'''                                <strong>{self.escape_html(returns_section['return_type'])}</strong>: '''
                    cleaned_return_description = self.fix_code_example_indentation(
                        returns_section.get('description', ''))
                    html += f'''<pre>{self.escape_html(cleaned_return_description)}</pre>
                            </div>
                        </div>
'''

            # Raises section
            if 'raises' in sections or 'raise' in sections:
                raises_section = sections.get('raises') or sections.get('raise')
                if raises_section and raises_section.get('items'):
                    html += '''                        <div class="docstring-section">
                            <h5>Raises:</h5>
                            <ul class="raises-list">
'''
                    for item in raises_section['items']:
                        cleaned_raises_description = self.fix_code_example_indentation(item['description']) if item[
                            'description'] else ""
                        html += f'''                                <li>
                                    <strong>{self.escape_html(item['exception'])}</strong>: <pre>{self.escape_html(cleaned_raises_description)}</pre>
                                </li>
'''
                    html += '''                            </ul>
                        </div>
'''

            # Other sections (Note, Example, etc.)
            for section_name, section_data in sections.items():
                if section_name not in ['args', 'arguments', 'parameters', 'param', 'returns', 'return', 'raises',
                                        'raise']:
                    if section_data.get('content'):
                        display_name = section_name.title()
                        # Fix indentation for Example sections
                        if section_name.lower() in ['example', 'examples']:
                            content = self.fix_code_example_indentation(section_data['content'])
                        else:
                            content = section_data['content']
                        html += f'''                        <div class="docstring-section">
                            <h5>{display_name}:</h5>
                            <div class="section-content">
                                <pre>{self.escape_html(content)}</pre>
                            </div>
                        </div>
'''

            html += '''                    </div>
'''
        elif func_doc:
            # Fall back to raw docstring display
            html += f'''                    <div class="function-description">
                        <pre>{self.escape_html(func_doc)}</pre>
                    </div>
'''
        else:
            html += '''                    <div class="function-description">
                        <em>No documentation available.</em>
                    </div>
'''

        html += '''                </div>
'''
        return html

    def generate_css(self):
        """Generate CSS stylesheet."""
        css_content = '''/* SnapKit API Documentation Styles */
body {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    line-height: 1.6;
    margin: 0;
    padding: 0;
    background-color: #f8f9fa;
    color: #333;
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    background-color: white;
    box-shadow: 0 0 10px rgba(0,0,0,0.1);
}

.navigation {
    margin-bottom: 20px;
    padding: 10px 0;
    border-bottom: 1px solid #dee2e6;
}

.navigation a {
    color: #007bff;
    text-decoration: none;
    font-size: 14px;
}

.navigation a:hover {
    text-decoration: underline;
}

h1 {
    color: #2c3e50;
    border-bottom: 3px solid #3498db;
    padding-bottom: 10px;
    margin-bottom: 30px;
}

h2 {
    color: #34495e;
    border-bottom: 2px solid #ecf0f1;
    padding-bottom: 8px;
    margin-top: 40px;
    margin-bottom: 20px;
}

h3 {
    color: #2c3e50;
    margin-top: 30px;
    margin-bottom: 15px;
}

h4 {
    color: #3498db;
    margin-top: 20px;
    margin-bottom: 10px;
}

.module-list, .class-list {
    display: grid;
    gap: 10px;
    margin-bottom: 30px;
}

.module-item, .class-item {
    background-color: #ffffff;
    border: 1px solid #dee2e6;
    border-radius: 8px;
    padding: 10px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    transition: box-shadow 0.3s ease;
}

.module-item:hover, .class-item:hover {
    box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.module-item h3, .class-item h4 {
    margin-top: 0;
    margin-bottom: 10px;
}

.module-item a, .class-item a {
    color: #2c3e50;
    text-decoration: none;
}

.module-item a:hover, .class-item a:hover {
    color: #3498db;
    text-decoration: underline;
}

.module-name {
    font-weight: bold;
    font-size: 1.2em;
}

.module-short-description {
    font-weight: normal;
    font-size: 1em;
}

.class-section {
    background-color: #ffffff;
    border: 1px solid #dee2e6;
    border-radius: 8px;
    padding: 25px;
    margin-bottom: 30px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.class-description, .module-description, .package-description {
    background-color: #f8f9fa;
    padding: 8px;
    border-radius: 5px;
    margin-bottom: 10px;
    border-left: 4px solid #3498db;
}

.class-description pre, .module-description pre, .package-description pre {
    margin: 0;
    font-family: inherit;
    line-height: 1.4;
    white-space: pre-wrap;
    word-wrap: break-word;
}

/* Overview section styles */
.overview-section {
    background-color: #f8f9fa;
    border: 1px solid #dee2e6;
    border-radius: 5px;
    padding: 15px;
    margin-bottom: 20px;
    margin-left: 20px;
}

.overview-section h4 {
    color: #2c3e50;
    margin-top: 0;
    margin-bottom: 10px;
    font-size: 16px;
}

.overview-list {
    padding: 0;
    margin: 0 0 15px 0;
}

.overview-item {
    background-color: #ffffff;
    border: 1px solid #dee2e6;
    border-radius: 3px;
    padding: 4px 8px;
    margin: 0 8px 8px 0;
    display: inline-block;
}

.overview-item a {
    color: #007bff;
    text-decoration: none;
    font-size: 14px;
    font-weight: bold;
    font-style: italic;
}

.overview-item a:hover {
    text-decoration: underline;
    color: #0056b3;
}

.properties-section, .methods-section {
    margin-left: 20px;
}

.function-item {
    background-color: #f8f9fa;
    border: 1px solid #e9ecef;
    border-radius: 5px;
    padding: 15px;
    margin-bottom: 15px;
}

.function-signature {
    font-family: 'Courier New', monospace;
    color: #e74c3c;
    margin: 0 0 5px 0;
    font-size: 16px;
}

.function-type {
    background-color: #6c757d;
    color: white;
    padding: 2px 8px;
    border-radius: 3px;
    font-size: 12px;
    text-transform: uppercase;
}

.function-description {
    margin-top: 10px;
    padding: 10px;
    background-color: white;
    border-radius: 3px;
}

.function-description pre {
    white-space: pre-wrap;
    word-wrap: break-word;
    margin: 0;
    font-family: inherit;
    line-height: 1.4;
}

.function-description em {
    color: #6c757d;
    font-style: italic;
}

/* Google-style docstring sections */
.docstring-description {
    margin-bottom: 15px;
}

.docstring-section {
    margin: 15px 0;
    padding: 10px;
    background-color: #f8f9fa;
    border-left: 3px solid #007bff;
    border-radius: 3px;
}

.docstring-section h5 {
    margin: 0 0 10px 0;
    color: #007bff;
    font-size: 14px;
    font-weight: bold;
    text-transform: uppercase;
}

.args-list, .raises-list {
    list-style-type: none;
    padding: 0;
    margin: 0;
}

.args-list li, .raises-list li {
    margin: 8px 0;
    padding: 5px 0;
    border-bottom: 1px solid #e9ecef;
}

.args-list li:last-child, .raises-list li:last-child {
    border-bottom: none;
}

.returns-info {
    padding: 8px 0;
}

.section-content {
    margin: 5px 0;
}

.section-content pre {
    background-color: #ffffff;
    border: 1px solid #dee2e6;
    padding: 10px;
    border-radius: 3px;
    margin: 0;
    font-size: 13px;
    line-height: 1.4;
}

/* Responsive design */
@media (max-width: 768px) {
    .container {
        padding: 10px;
    }
    
    .module-list, .class-list {
        grid-template-columns: 1fr;
    }
    
    .function-signature {
        font-size: 14px;
        word-break: break-all;
    }
}
'''

        with open(self.output_dir / "styles.css", 'w', encoding='utf-8') as f:
            f.write(css_content)

    def escape_html(self, text: str) -> str:
        """Escape HTML characters in text."""
        if not text:
            return ""
        return (text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace('"', "&quot;")
                .replace("'", "&#x27;"))

    def fix_code_example_indentation(self, text: str) -> str:
        """Fix inconsistent indentation in code examples and remove >>> markers."""
        if not text:
            return ""

        lines = text.split('\n')
        fixed_lines = []

        for line in lines:
            # If line starts with >>>, remove the >>> marker and maintain indentation
            if line.strip().startswith('>>>'):
                content = line.strip()
                if content.startswith('>>> '):
                    # Remove '>>> ' and keep the rest of the content
                    code_content = content[4:]  # Remove '>>> '
                    fixed_lines.append('    ' + code_content)
                elif content == '>>>':
                    # Empty >>> line, just add indentation
                    fixed_lines.append('    ')
                else:
                    # Remove '>>>' and keep the rest
                    code_content = content[3:]  # Remove '>>>'
                    fixed_lines.append('    ' + code_content)
            else:
                fixed_lines.append(line)

        return '\n'.join(fixed_lines)


def main():
    if len(sys.argv) != 3:
        print("Usage: python generate_python_docs.py <source_dir> <output_dir>")
        sys.exit(1)

    source_dir = sys.argv[1]
    output_dir = sys.argv[2]

    if not os.path.exists(source_dir):
        print(f"Error: Source directory '{source_dir}' does not exist")
        sys.exit(1)

    generator = DocGenerator(source_dir, output_dir)
    generator.generate_docs()


if __name__ == "__main__":
    main()
