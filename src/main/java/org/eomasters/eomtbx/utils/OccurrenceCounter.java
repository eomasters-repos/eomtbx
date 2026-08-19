/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/eomtbx
 * ======================================================================
 * Copyright (C) 2023 - 2026 Marco Peters
 * ======================================================================
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * -> http://www.gnu.org/licenses/gpl-3.0.html
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * The OccurrenceCounter class is used to count the occurrences of objects of type T. It uses a HashMap to store the
 * items and their counts.
 *
 * @param <T> The type of objects to count occurrences for.
 */
public class OccurrenceCounter<T> {

  HashMap<T, Integer> map = new HashMap<>();

  /**
   * Adds an item to the OccurrenceCounter and updates its count. If the item already exists, the count is incremented
   * by 1. If the item does not exist, it is added with a count of 1.
   *
   * @param item The item to be added to the OccurrenceCounter.
   */
  public void add(T item) {
    if (map.containsKey(item)) {
      map.put(item, map.get(item) + 1);
    } else {
      map.put(item, 1);
    }
  }

  /**
   * Adds all the items from the given list to the OccurrenceCounter and updates their counts.
   *
   * @param values The list of items to be added to the OccurrenceCounter.
   * @see #add(T)
   */
  public void addAll(List<T> values) {
    for (T value : values) {
      add(value);
    }
  }

  /**
   * Removes an item from the OccurrenceCounter and updates its count. If the item exists in the OccurrenceCounter, the
   * count is decremented by 1. If the count becomes 0, the item is removed from the OccurrenceCounter.
   *
   * @param item The item to be removed from the OccurrenceCounter.
   */
  public void remove(T item) {
    if (map.containsKey(item)) {
      map.put(item, map.get(item) - 1);
    }
    if (map.get(item) == 0) {
      map.remove(item);
    }
  }

  /**
   * Removes all the items from the OccurrenceCounter and updates their counts.
   *
   * @param items The list of items to be removed from the OccurrenceCounter.
   * @see #remove(T)
   */
  public void removeAll(List<T> items) {
    for (T item : items) {
      remove(item);
    }
  }

  public void clear() {
    map.clear();
  }

  /**
   * Checks if the OccurrenceCounter contains the specified item.
   *
   * @param item The item to check for.
   * @return True if the OccurrenceCounter contains the item, false otherwise.
   */
  public boolean contains(T item) {
    return map.containsKey(item);
  }

  /**
   * Returns the count of occurrences for the specified item in the OccurrenceCounter.
   *
   * @param item The item to get the count for.
   * @return The count of occurrences for the specified item. If the item does not exist in the OccurrenceCounter, 0 is
   * returned.
   */
  public int getCount(T item) {
    return map.getOrDefault(item, 0);
  }

  /**
   * Returns a set of values stored in the OccurrenceCounter.
   *
   * @return A set of values stored in the OccurrenceCounter.
   */
  public Set<T> getValues() {
    return map.keySet();
  }

}
