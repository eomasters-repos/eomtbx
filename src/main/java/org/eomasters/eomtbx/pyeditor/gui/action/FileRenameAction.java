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
import java.nio.file.Files;
import java.nio.file.Path;
import org.eomasters.eomtbx.pyeditor.gui.PyEditor;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.swing.FontIcon;

public class FileRenameAction extends AbstractEditorAction {

  private final Path filePath;

  public FileRenameAction(PyEditor editor, Path filePath) {
    super(editor, "Rename " + (Files.isDirectory(filePath) ? "Directory" : "File"), "Rename a file or directory",
          FontIcon.of(MaterialDesignF.FORM_TEXTBOX, ICON_SIZE));
    this.filePath = filePath;
  }

  @Override
  public boolean isEnabled() {
    return !filePath.equals(getEditor().getProject().getSrcDirectory());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    getEditor().getFileManager().renameFile(filePath);
  }
}
