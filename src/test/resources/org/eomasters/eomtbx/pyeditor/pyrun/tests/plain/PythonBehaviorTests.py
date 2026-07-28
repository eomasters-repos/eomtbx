import jarray
import array as pyarray


def is_java_array(obj):
    t = type(obj)
    # Python arrays have __module__ == 'builtins'
    if t.__module__ != 'builtins':
        return True
    return False


def is_java_array_test():
    python_array = [1, 2, 3]  # Python list
    java_array = jarray.array([1, 2, 3], 'i')  # Java int[]
    if is_java_array(python_array):
        raise AssertionError("The Python array is detected as Java array")

    if not is_java_array(java_array):
        raise AssertionError("The Java array is not detected as Java array")


if __name__ == '__main__':
    is_java_array_test()
