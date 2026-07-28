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
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Action for saving a file.
 */
public class FileSaveAction extends AbstractEditorAction {

  private final Path filePath;

  /**
   * Creates a new SaveFileAction with the specified editor.
   *
   * @param editor the PyEditor instance
   */
  public FileSaveAction(PyEditor editor) {
    this(editor, null);
  }

  /**
   * Creates a new SaveFileAction with the specified editor.
   *
   * @param editor   the PyEditor instance
   * @param filePath the file path to save, if <code>null</code> the current file is retrieved from the editor
   */
  public FileSaveAction(PyEditor editor, Path filePath) {
    super(editor, "Save File", "Save the current file", FontIcon.of(MaterialDesignC.CONTENT_SAVE, ICON_SIZE));
    this.filePath = filePath;
    setEnabled(false);
  }

  @Override
  public boolean isEnabled() {
    var fileManager = getEditor().getFileManager();
    if (fileManager == null) {
      return false;
    }
    return fileManager.isFileModified(getSelectedFile());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    var currentFile = getSelectedFile();
    var editor = getEditor();
    editor.getFileManager().saveFile(currentFile, editor.getModifiedText(currentFile));
  }

  private Path getSelectedFile() {
    return filePath != null ? filePath : getEditor().getSelectedFile();
  }

}
