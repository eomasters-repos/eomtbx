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

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.logging.Level;
import org.apache.commons.io.file.PathUtils;
import org.eomasters.eomtbx.EomtbxRuntime;

public class FileUtils {

  public static Path getPath(URI uri) throws IOException {
    if ("jar".equals(uri.getScheme())) {
      try {
        return Path.of(uri);
      } catch (FileSystemNotFoundException e) {
        try {
          FileSystems.newFileSystem(uri, Collections.emptyMap());
        } catch (FileSystemAlreadyExistsException ignored) {
          // already exists
        }
        return Path.of(uri);
      }
    }
    return Path.of(uri);
  }

  public static void moveDir(Path from, Path to) {
    try {
      PathUtils.copyDirectory(from, to);
    } catch (IOException e) {
      EomtbxRuntime.LOGGER.log(Level.WARNING, "Could not copy old directory", e);
    }
    try {
      PathUtils.deleteDirectory(from);
    } catch (IOException e) {
      EomtbxRuntime.LOGGER.log(Level.WARNING, "Could not delete old directory", e);
    }
  }
}
