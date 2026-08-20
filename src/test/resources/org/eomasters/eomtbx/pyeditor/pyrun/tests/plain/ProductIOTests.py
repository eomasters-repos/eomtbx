from snapkit import ProductIO


def test_input_formats():
    formats = ProductIO.input_formats()
    if len(formats) < 10:
        raise AssertionError("Too few formats")
    print(formats)

def test_output_formats():
    formats = ProductIO.output_formats()
    if len(formats) < 5:
        raise AssertionError("Too few formats")
    print(formats)

if __name__ == '__main__':
    test_input_formats()
    test_output_formats()
