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

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eomasters.eomtbx.EomtbxRuntime;

public class CaptureTiming {

  private static final Logger LOGGER = EomtbxRuntime.LOGGER;

  public static <V> V logTiming(String name, Callable<V> runnable) throws Exception {
    long start = System.currentTimeMillis();
    LOGGER.log(Level.FINE, "%s: Started".formatted(name));
    try {
      return runnable.call();
    } finally {
      long end = System.currentTimeMillis();
      LOGGER.log(Level.FINE, "%s: %dms".formatted(name, end - start));
    }
  }
}
