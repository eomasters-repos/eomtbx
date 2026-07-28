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

package org.eomasters.eomtbx.s2superres.onnx;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eomasters.eomtbx.s2superres.onnx.OnnxTempCleaner.OnnxTempDirDeleter;
import org.junit.jupiter.api.Test;

public class TempFilesCleanerTest {

  @Test
  public void testDoCleaning() throws IOException {
    try (FileSystem fileSystem = Jimfs.newFileSystem("onnx-test")) {
      Path temp = Files.createDirectory(fileSystem.getPath("temp"));
      Path remainingTestFile = Files.createFile(temp.resolve("test.txt"));
      Path remainingTestDir = Files.createDirectory(temp.resolve("other"));
      Path onnxTempDir = temp.resolve("onnxruntime-java78571197692704194");
      Files.createDirectory(onnxTempDir);
      Path onnxDll1 = Files.createFile(onnxTempDir.resolve("onnxruntime.dll"));
      Path onnxDll2 = Files.createFile(onnxTempDir.resolve("onnxruntime4j_jni.dll"));

      OnnxTempCleaner.doCleaning(temp);

      assertTrue(Files.exists(remainingTestFile));
      assertTrue(Files.exists(remainingTestDir));

      assertFalse(Files.exists(onnxTempDir));
      assertFalse(Files.exists(onnxDll1));
      assertFalse(Files.exists(onnxDll2));

    }
  }

  @Test
  public void testDoCleaningWithLockedFile() throws IOException {
    try (FileSystem fileSystem = Jimfs.newFileSystem("onnx-locked-test")) {
      Path temp = Files.createDirectory(fileSystem.getPath("temp"));
      Path onnxTempDir = temp.resolve("onnxruntime-java-locked");
      Files.createDirectory(onnxTempDir);
      Path lockedFile = onnxTempDir.resolve("onnxruntime.dll");
      Files.createFile(lockedFile);

      OnnxTempDirDeleter deleter = new OnnxTempDirDeleter(temp, path -> {
        if (path.equals(lockedFile)) {
          throw new AccessDeniedException(path.toString());
        }
        Files.delete(path);
      });

      Files.walkFileTree(temp, deleter);

      assertTrue(Files.exists(lockedFile), "Locked file should still exist");
      assertTrue(Files.exists(onnxTempDir), "Directory containing locked file should still exist");
    }
  }

  @Test
  public void testVisitFileFailedContinuesOnAccessDenied() throws IOException {
    try (FileSystem fileSystem = Jimfs.newFileSystem("onnx-visit-failed-test")) {
      Path temp = Files.createDirectory(fileSystem.getPath("temp"));
      OnnxTempDirDeleter deleter = new OnnxTempDirDeleter(temp);

      FileVisitResult result = deleter.visitFileFailed(temp.resolve("crssync-kpSrUI"),
                                                       new AccessDeniedException("denied"));

      assertEquals(FileVisitResult.CONTINUE, result);
    }
  }

}
