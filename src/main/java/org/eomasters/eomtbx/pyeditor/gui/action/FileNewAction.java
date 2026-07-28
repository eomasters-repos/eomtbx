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
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Action for creating a new file.
 */
public class FileNewAction extends AbstractEditorAction {

  private final Path targetDirectory;

  /**
   * Creates a new NewFileAction with the specified editor.
   *
   * @param editor the PyEditor instance
   */
  public FileNewAction(PyEditor editor) {
    this(editor, null);
  }

  /**
   * Creates a new NewFileAction with the specified editor.
   *
   * @param editor          the PyEditor instance
   * @param targetDirectory the target directory for the new file, if
   *                        <code>null</code> the current target directory is retrieved from the editor
   */
  public FileNewAction(PyEditor editor, Path targetDirectory) {
    super(editor, "New File", "Create a new file", FontIcon.of(MaterialDesignF.FILE_PLUS, ICON_SIZE));
    this.targetDirectory = targetDirectory;
    setEnabled(false);
  }

  @Override
  public boolean isEnabled() {
    return getEditor().getProject() != null;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    getEditor().getFileManager().newFile(getTargetDirectory());
  }

  private Path getTargetDirectory() {
    return targetDirectory != null ? targetDirectory : getEditor().getCurrentTargetDirectory();
  }
}
