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

package org.eomasters.eomtbx.spex;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;

/**
 * Simple implementation of {@link AbstractPreferences} for testing. Preferences are not stored across JVM invocations
 * only during the lifetime of the current JVM.
 */
public class TestPreferences extends AbstractPreferences {

  private final Map<String, String> map;

  public TestPreferences() {
    super(null, "");
    map = Collections.synchronizedMap(new HashMap<>());
  }

  @Override
  protected void putSpi(String key, String value) {
    map.put(key, value);
  }

  @Override
  protected String getSpi(String key) {
    return map.get(key);
  }

  @Override
  protected void removeSpi(String key) {
    map.remove(key);
  }

  @Override
  protected void removeNodeSpi() {
    map.clear();
  }

  @Override
  protected String[] keysSpi() {
    return map.keySet().toArray(new String[0]);
  }

  @Override
  protected String[] childrenNamesSpi() {
    return new String[0];
  }

  @Override
  protected AbstractPreferences childSpi(String name) {
    return new TestPreferences();
  }


  @Override
  protected void syncSpi() {
  }

  @Override
  protected void flushSpi() {
  }
}
