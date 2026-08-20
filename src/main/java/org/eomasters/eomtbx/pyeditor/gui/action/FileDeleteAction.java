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

package org.eomasters.eomtbx.pyeditor.gui.action;

import java.awt.event.ActionEvent;
import java.nio.file.Path;
import org.eomasters.eomtbx.pyeditor.gui.PyEditor;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Action for deleting a file or directory.
 */
public class FileDeleteAction extends AbstractEditorAction {

  private final Path filePath;

  /**
   * Creates a new DeleteFileAction with the specified editor.
   *
   * @param editor the PyEditor instance
   */
  public FileDeleteAction(PyEditor editor) {
    this(editor, null);
  }

  /**
   * Creates a new DeleteFileAction with the specified editor.
   *
   * @param editor the PyEditor instance
   */
  public FileDeleteAction(PyEditor editor, Path filePath) {
    super(editor, "Delete File", "Delete the selected file or directory",
          FontIcon.of(MaterialDesignD.DELETE_CIRCLE, ICON_SIZE));
    this.filePath = filePath;
    setEnabled(false);
  }

  @Override
  public boolean isEnabled() {
    var project = getEditor().getProject();
    if (project == null) {
      return false;
    }
    var selectedFile = getSelectedFile();
    return selectedFile != null && !selectedFile.equals(project.getSrcDirectory());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    getEditor().getFileManager().deleteFile(getSelectedFile());
  }

  private Path getSelectedFile() {
    return filePath != null ? filePath : getEditor().getSelectedFile();
  }

}
