package org.eomasters.eomtbx.utils;

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.esa.snap.rcp.util.Dialogs;


/**
 * Utility class for file input/output.
 */
public class FileIO {

  private final String title;
  private Component parent;
  private boolean allFileFilterUsed;
  private FileFilter[] fileFilters;

  /**
   * Creates a new FileIO with the given title used by the file chooser dialog.
   *
   * @param title the title
   */
  public FileIO(String title) {
    this.title = title;
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
    try {
      JFileChooser fileChooser = createFileChooser();

      int returnVal = fileChooser.showOpenDialog(parent);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        Path path = fileChooser.getSelectedFile().toPath();
        read.read(Files.newInputStream(path));
      }
    } catch (IOException ex) {
      ErrorHandler.handle("Could not import file", ex);
    }
  }

  /**
   * Saves a file using the given write callback.
   *
   * @param write the write callback
   */
  public void save(Write write) {
    try {
      JFileChooser fileChooser = createFileChooser();

      int returnVal = fileChooser.showSaveDialog(parent);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        Boolean doWrite = true;
        Path path = ensureFileExtension(fileChooser.getSelectedFile().toPath(), fileChooser.getFileFilter());
        if (Files.exists(path)) {
          doWrite = Dialogs.requestOverwriteDecision("File already exists",
              path.toFile());
        }
        if (doWrite) {
          write.write(Files.newOutputStream(path));
        }
      }
    } catch (IOException ex) {
      ErrorHandler.handle("Could not export file", ex);
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
    return fileChooser;
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

  public interface Write {

    void write(OutputStream outputStream) throws IOException;
  }

  public interface Read {

    void read(InputStream inputStream) throws IOException;
  }
}
