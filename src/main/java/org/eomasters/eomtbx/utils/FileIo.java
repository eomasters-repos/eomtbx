/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * --> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.utils;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.esa.snap.rcp.util.Dialogs;


/**
 * Utility class for file input/output.
 */
public class FileIo {

  private final String title;
  private Component parent;
  private boolean allFileFilterUsed;
  private FileFilter[] fileFilters;
  private String fileName;

  /**
   * Creates a new FileIO with the given title used by the file chooser dialog.
   *
   * @param title the title
   */
  public FileIo(String title) {
    this.title = title;
  }

  public static FileNameExtensionFilter createFileFilter(String description, String... extensions) {
    String text = description + " " + Arrays.stream(extensions).map(s -> "*." + s).collect(Collectors.toList());
    return new FileNameExtensionFilter(text, extensions);
  }

  private static Path ensureFileExtension(Path path, FileFilter fileFilter) {
    if (fileFilter instanceof FileNameExtensionFilter) {
      String[] extensions = ((FileNameExtensionFilter) fileFilter).getExtensions();
      if (extensions.length > 0) {
        String extension = extensions[0];
        if (!path.toString().endsWith(extension)) {
          path = path.resolveSibling(path.getFileName() + "." + extension);
        }
      }
    }
    return path;
  }

  /**
   * Sets the parent component of the file chooser dialog.
   *
   * @param parent the parent component
   */
  public void setParent(Component parent) {
    this.parent = parent;
  }

  /**
   * Sets the file name used by the file chooser dialog.
   *
   * @param fileName the file name
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Sets whether the all file filter should be used.
   *
   * @param allFileFilterUsed whether the all file filter should be used
   */
  public void setAllFileFilterUsed(boolean allFileFilterUsed) {
    this.allFileFilterUsed = allFileFilterUsed;
  }

  /**
   * Sets the file filters used by the file chooser. The first file filter is used as default.
   *
   * @param fileFilters the file filters
   */
  public void setFileFilters(FileFilter... fileFilters) {
    this.fileFilters = fileFilters;
  }

  /**
   * Loads a file using the given read callback.
   *
   * @param read the read callback
   */
  public void load(Read read) {
    JFileChooser fileChooser = createFileChooser();

    int returnVal = fileChooser.showOpenDialog(parent);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      Path path = fileChooser.getSelectedFile().toPath();
      if (!Files.isReadable(path)) {
        ErrorHandler.showError("File not readable", "The file with the path " + path + " is not readable.");
        return;
      }
      try {
        read.read(Files.newInputStream(path));
      } catch (IOException ex) {
        ErrorHandler.handleException("Could not import file", ex);
      }
    }
  }

  /**
   * Saves a file using the given write callback.
   *
   * @param write the write callback
   */
  public void save(Write write) {
    JFileChooser fileChooser = createFileChooser();

    int returnVal = fileChooser.showSaveDialog(parent);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      Path path = ensureFileExtension(fileChooser.getSelectedFile().toPath(), fileChooser.getFileFilter());
      if (Files.exists(path) && !Dialogs.requestOverwriteDecision("File already exists",
          path.toFile())) {
        return;
      }
      if (!Files.isWritable(path)) {
        ErrorHandler.showError("File not writable", "Cannot write to " + path + ".");
        return;
      }
      try {
        write.write(Files.newOutputStream(path));
      } catch (IOException ex) {
        ErrorHandler.handleException("Could not export file", ex);
      }
    }
  }

  private JFileChooser createFileChooser() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle(title);
    fileChooser.setAcceptAllFileFilterUsed(allFileFilterUsed);
    if (fileFilters != null) {
      for (FileFilter filter : fileFilters) {
        fileChooser.addChoosableFileFilter(filter);
      }
      fileChooser.setFileFilter(fileFilters[0]);
    }
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    if (fileName != null) {
      fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), fileName));
    }
    return fileChooser;
  }

  /**
   * Callback interface for writing a file.
   */
  public interface Write {

    /**
     * Writes the file to the given output stream.
     *
     * @param outputStream the output stream
     * @throws IOException if an I/O error occurs
     */
    void write(OutputStream outputStream) throws IOException;
  }

  /**
   * Callback interface for reading a file.
   */
  public interface Read {

    /**
     * Reads the file from the given input stream.
     *
     * @param inputStream the input stream
     * @throws IOException if an I/O error occurs
     */
    void read(InputStream inputStream) throws IOException;
  }
}
