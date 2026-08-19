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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import org.eomasters.eomtbx.pyeditor.gui.PyEditor;
import org.kordamp.ikonli.materialdesign2.MaterialDesignW;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Action for configuring a project. This action opens the project configuration dialog. It is only enabled when a
 * project is loaded.
 */
public class ProjectConfigAction extends AbstractEditorAction {

  /**
   * Creates a new ProjectConfigAction with the specified editor.
   *
   * @param editor the PyEditor instance
   */
  public ProjectConfigAction(PyEditor editor) {
    super(editor, "Configure Project", "Open the project configuration dialog",
          FontIcon.of(MaterialDesignW.WRENCH_OUTLINE, ICON_SIZE));
  }

  @Override
  public boolean isEnabled() {
    return getEditor().getProject() != null;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Frame parentFrame = getEditor().getParentFrame();
    ProjectConfigDialog dialog = new ProjectConfigDialog((JFrame) parentFrame, getEditor().getProject());
    dialog.showDialog();
  }

}
