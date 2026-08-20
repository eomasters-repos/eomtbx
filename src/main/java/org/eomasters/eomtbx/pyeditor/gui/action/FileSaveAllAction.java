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
import java.util.Collection;
import org.eomasters.eomtbx.pyeditor.gui.PyEditor;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Action for saving all modified files in the project.
 */
public class FileSaveAllAction extends AbstractEditorAction {

  private final Collection<Path> modifiedFiles;

  /**
   * Creates a new SaveAllFilesAction with the specified editor.
   *
   * @param editor the PyEditor instance
   */
  public FileSaveAllAction(PyEditor editor) {
    this(editor, null);
  }

  /**
   * Creates a new SaveAllFilesAction with the specified editor.
   *
   * @param editor        the PyEditor instance
   * @param modifiedFiles the modified files to save, if <code>null</code> the modified files are retrieved from the
   *                      editor
   */
  public FileSaveAllAction(PyEditor editor, Collection<Path> modifiedFiles) {
    super(editor, "Save All Files", "Save all modified files in the project",
          FontIcon.of(MaterialDesignC.CONTENT_SAVE_ALL, ICON_SIZE));
    this.modifiedFiles = modifiedFiles;
    setEnabled(false);
  }

  @Override
  public boolean isEnabled() {
    var fileManager = getEditor().getFileManager();
    if (fileManager == null) {
      return false;
    }
    return fileManager.hasModifiedFiles();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    var modifiedFiles = getModifiedFiles();
    getEditor().getFileManager().saveAll(modifiedFiles);
  }

  private Collection<Path> getModifiedFiles() {
    if (modifiedFiles != null) {
      return modifiedFiles;
    }
    var fileManager = getEditor().getFileManager();
    return fileManager.getModifiedFiles();
  }

}
