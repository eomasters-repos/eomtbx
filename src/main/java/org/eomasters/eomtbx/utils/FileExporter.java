package org.eomasters.eomtbx.utils;

import java.awt.Component;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.esa.snap.rcp.util.Dialogs;

public class FileExporter {

  private final String title;
  private Component parent;
  private boolean allFileFilterUsed;
  private FileFilter[] fileFilters;

  public FileExporter(String title) {
    this.title = title;
  }
  
  public void setParent(Component parent) {
    this.parent = parent;
  }

  public void setAllFileFilterUsed(boolean allFileFilterUsed) {
    this.allFileFilterUsed = allFileFilterUsed;
  }

  public void setFileFilters(FileFilter... fileFilters) {
    this.fileFilters = fileFilters;
  }


  public void export(Write write) {
    try {
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
}
