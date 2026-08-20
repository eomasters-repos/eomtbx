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

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;
import org.apache.commons.lang3.SystemUtils;
import org.eomasters.utils.ErrorHandler;

public class OnnxTempCleaner {

  private static final String TEMP_DIR_PREFIX = "onnxruntime-java";

  public static void doCleaning() {
    doCleaning(SystemUtils.getJavaIoTmpDir().toPath());
  }

  static void doCleaning(Path javaIoTmpDir) {
    try (DirectoryStream<Path> entries = Files.newDirectoryStream(javaIoTmpDir)) {
      for (Path entry : entries) {
        if (isOnnxTempDir(entry)) {
          try {
            Files.walkFileTree(entry, new OnnxTempDirDeleter(entry));
          } catch (AccessDeniedException e) {
            // Directory is locked/protected, skip this ONNX temp directory.
          }
        }
      }
    } catch (IOException e) {
      ErrorHandler.handleError("Cleaning ONNX temp files", e.getMessage(), e);
    }
  }

  static class OnnxTempDirDeleter extends SimpleFileVisitor<Path> {

    private final Path baseDir;
    private final FileDeleter fileDeleter;

    public OnnxTempDirDeleter(Path baseDir) {
      this(baseDir, Files::delete);
    }

    OnnxTempDirDeleter(Path baseDir, FileDeleter fileDeleter) {
      this.baseDir = baseDir;
      this.fileDeleter = fileDeleter;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
      boolean isOnnxDir = isOnnxTempDir(dir);
      boolean isBaseDir = dir.equals(baseDir);
      return isOnnxDir || isBaseDir ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
      if (isOnnxTempDir(file.getParent())) {
        try {
          fileDeleter.delete(file);
        } catch (AccessDeniedException e) {
          // File is locked (e.g., DLL in use on Windows), skip deletion
        } catch (IOException e) {
          // Ignore other IO errors during cleanup
        }
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
      if (exc instanceof AccessDeniedException) {
        // Some temp subdirectories in %TEMP% can be protected/locked by other processes.
        return FileVisitResult.CONTINUE;
      }
      // Keep cleanup best-effort, don't fail startup because of unrelated temp entries.
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
      if (isOnnxTempDir(dir)) {
        try {
          if (isEmpty(dir)) {
            fileDeleter.delete(dir);
          }
        } catch (AccessDeniedException e) {
          // Directory is locked, skip deletion
        } catch (IOException e) {
          // Ignore other IO errors during cleanup
        }
      }
      return FileVisitResult.CONTINUE;
    }

    private boolean isEmpty(Path path) throws IOException {
      if (Files.isDirectory(path)) {
        try (Stream<Path> entries = Files.list(path)) {
          return entries.findFirst().isEmpty();
        }
      }
      return false;
    }

    private static boolean isOnnxTempDir(Path dir) {
      return OnnxTempCleaner.isOnnxTempDir(dir);
    }
  }

  private static boolean isOnnxTempDir(Path path) {
    Path fileName = path.getFileName();
    return fileName != null && fileName.toString().startsWith(TEMP_DIR_PREFIX);
  }

  interface FileDeleter {
    void delete(Path path) throws IOException;
  }

}
