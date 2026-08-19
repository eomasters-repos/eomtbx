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
import org.eomasters.eomtbx.pyeditor.gui.PyEditor;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Action for managing projects.
 */
public class ProjectCloseAction extends AbstractEditorAction {

  /**
   * Creates a new CloseProjectsAction with the specified editor.
   *
   * @param editor the PyEditor instance
   */
  public ProjectCloseAction(PyEditor editor) {
    super(editor, "Close Project", "Close the current project",
          FontIcon.of(MaterialDesignC.CLOSE_BOX_OUTLINE, ICON_SIZE));
  }

  @Override
  public boolean isEnabled() {
    return getEditor().getProject() != null;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    var editor = getEditor();
    if (!editor.getFileManager().promptForUnsavedFiles()) {
      return;
    }
    ExecutionManager.remove(editor.getProject());
    editor.closeProject();
    editor.updateToolbar();
  }
}
