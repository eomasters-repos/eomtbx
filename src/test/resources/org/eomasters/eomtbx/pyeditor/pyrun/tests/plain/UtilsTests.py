from snapkit._utils import Utils
import array as pyarray
import jarray
import java


def creating_java_array_test():
    python_array = pyarray.array('H', [1, 2, 3, 300, 400, 500])
    java_array = Utils.create_java_array(python_array, 'H')
    type = Utils.component_type(java_array)
    if type != 'int':
        raise AssertionError("not int")
    if java_array[0] != 1:
        raise AssertionError("conversion error")
    if java_array[5] != 500:
        raise AssertionError("conversion error")


def is_java_array_test():
    if Utils.is_java_array([]):
        raise AssertionError("not a java array")

    if not Utils.is_java_array(passed_java_array):
        raise AssertionError("This should be a java array")

    java_short_array = java.type("short[]")(10)
    if not Utils.is_java_array(java_short_array):
        raise AssertionError("This is surprisingly not java array")

    java_byte_array = jarray.array([1, 2, 3], 'b')
    if not Utils.is_java_array(java_byte_array):
        raise AssertionError("This should be a java array")

    python_array = pyarray.array('i', [1, 2, 3, 300, 400, 500])
    java_int_array = Utils.create_java_array(python_array, 'i')
    if not Utils.is_java_array(java_int_array):
        raise AssertionError("This should be a java array")


def component_type_test():
    if "unknown" != Utils.component_type([]):
        raise AssertionError("not a java array")
    java_short_array = java.type("short[]")(10)
    if 'short' != Utils.component_type(java_short_array):
        raise AssertionError("This should be a short array")
    import jarray
    java_byte_array = jarray.array([1, 2, 3], 'b')
    if 'byte' != Utils.component_type(java_byte_array):
        raise AssertionError("This should be a byte array")
    python_array = pyarray.array('i', [1, 2, 3, 300, 400, 500])
    java_int_array = Utils.create_java_array(python_array, 'i')
    if 'int' != Utils.component_type(java_int_array):
        raise AssertionError("This should be an int array")


def is_instance_test():
    pixel_pos = java.type("org.esa.snap.core.datamodel.PixelPos")()
    if not (Utils.is_instance(pixel_pos, "org.esa.snap.core.datamodel.PixelPos")):
        raise AssertionError("Should be a PixelPos instance")


if __name__ == '__main__':
    creating_java_array_test()
    is_java_array_test()
    component_type_test()
    is_instance_test()
