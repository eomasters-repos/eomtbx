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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUtilsTest {

  @TempDir
  Path tempDir;

  @Test
  void testMoveDir() throws IOException {
    Path fromDir = tempDir.resolve("fromDir");
    Path toDir = tempDir.resolve("toDir");
    Files.createDirectories(fromDir);
    Files.createDirectories(toDir);
    Path file1 = fromDir.resolve("file1");
    Files.createFile(file1);
    Path file2 = fromDir.resolve("file2");
    Files.createFile(file2);
    Path dir = fromDir.resolve("dir");
    Files.createDirectories(dir);
    Path file3 = dir.resolve("file3");
    Files.createFile(file3);

    FileUtils.moveDir(fromDir, toDir);

    assertFalse(Files.exists(fromDir));

    assertTrue(Files.exists(toDir.resolve("dir").resolve("file3")));
    assertTrue(Files.exists(toDir.resolve("dir")));
    assertTrue(Files.exists(toDir.resolve("file2")));
    assertTrue(Files.exists(toDir.resolve("file1")));

  }

}
