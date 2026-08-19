import java
import time
import random
import operator

def iteration_speed_test():
  size = 10_000
  iarr = java.type("int[]")(size)
  start = time.time()
  for i in range(len(iarr)):
    iarr[i] = random.randint(0, 50)
  java_time = time.time() - start

  parr = list(java.type("int[]")(size))
  start = time.time()
  for i in range(len(parr)):
    parr[i] = random.randint(0, 50)
  python_time = time.time() - start

  if python_time > java_time:
    raise AssertionError(f"Use of converted java_array ({round(python_time, 3)}) is slower than directly using it ({round(java_time, 3)})")


def sum_speed_test():
  size = 1_000_000

  arr1 = [random.randint(1, 50) for _ in range(size)]
  arr2 = [random.randint(1, 50) for _ in range(size)]

  start = time.time()
  result = list(map(operator.add, arr1, arr2))
  time_span = time.time() - start
  print(f"map_time ({round(time_span, 3)})")

  start = time.time()
  result = [x + y for x, y in zip(arr1, arr2)]
  # time_span = time.time() - start
  print(f"zip_time ({round(time_span, 3)})")

  if time_span > 1.0:
    raise AssertionError(f"NDVI should be computed in less than 1 second but was {round(time_span, 3)}")


if __name__ == '__main__':
  iteration_speed_test()
  sum_speed_test()