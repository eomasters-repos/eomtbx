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
 * Action to toggle the visibility of the console in the PyEditor.
 */
public class ConsoleToggleAction extends AbstractEditorAction {

  private final PyEditor pyEditor;

  /**
   * Creates a new ConsoleToggleAction.
   *
   * @param pyEditor the PyEditor instance
   */
  public ConsoleToggleAction(PyEditor pyEditor) {
    super(pyEditor, "Toggle Console", "Toggle console visibility", FontIcon.of(MaterialDesignC.CONSOLE, ICON_SIZE));
    this.pyEditor = pyEditor;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    pyEditor.toggleConsoleVisibility();
  }
}
