"""
Simple test script to verify the OperatorDescriptor implementation.
"""

from snapkit import GPF
from snapkit._operatorDescriptor import OperatorDescriptor
import random


def test_GPF_operators():
    print("Testing GPF.operators()...")
    operators = GPF.operators()
    if not operators or len(operators) == 0:
        raise AssertionError("GPF.operators() returned empty list")

    print(f"Found {len(operators)} operators")
    if operators:
        print(f"First few operators: {operators[:5]}")


def test_read_op_description():
    _describe_op('Read')


def test_random_op_description():
    operators = GPF.operators()
    rnd_op = operators[random.randint(0, len(operators) - 1)]
    _describe_op(rnd_op)


def _describe_op(rnd_op):
    print(f"\nDescribing '{rnd_op}' operator...")
    desc = GPF.describe_operator(rnd_op)
    print(f"Operator descriptor: {repr(desc)}")
    print("\nDetailed description:")
    print(str(desc))


if __name__ == '__main__':
    test_GPF_operators()
    test_read_op_description()
    test_random_op_description()
