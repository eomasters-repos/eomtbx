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

import java.awt.Color;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.eomasters.eomtbx.pyeditor.gui.PyEditor;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * Abstract base class for editor actions. Implements the javax.swing.Action interface and provides common
 * functionality.
 */
public abstract class AbstractEditorAction extends AbstractAction {

  protected static final int ICON_SIZE = 24;
  private static final String SMALL_DISABLED_ICON = "SmallDisabledIcon";

  private final PyEditor editor;

  /**
   * Creates a new AbstractEditorAction with the specified name, description, and icon.
   *
   * @param editor      the PyEditor instance
   * @param name        the name of the action
   * @param description the description of the action
   * @param icon        the icon for the action
   */
  public AbstractEditorAction(PyEditor editor, String name, String description, FontIcon icon) {
    super(name);
    this.editor = editor;
    putValue(Action.SHORT_DESCRIPTION, description);
    setIcon(icon);
  }

  public PyEditor getEditor() {
    return editor;
  }

  public void setIconSize(int size) {
    var icon = getIcon();
    if (icon != null) {
      icon.setIconSize(size);
      setIcon(icon);
    }
    var disabledIcon = getDisabledIcon();
    if (disabledIcon != null) {
      disabledIcon.setIconSize(size);
      setDisabledIcon(disabledIcon);
    }
  }

  public void setIconColor(Color color) {
    var icon = getIcon();
    if (icon != null) {
      icon.setIconColor(color);
      setIcon(icon);
    }
  }

  public void setIcon(FontIcon icon) {
    putValue(Action.SMALL_ICON, icon);
  }

  public FontIcon getIcon() {
    var value = getValue(Action.SMALL_ICON);
    if (value instanceof FontIcon) {
      return (FontIcon) value;
    }
    return null;
  }


  public void setDisabledIcon(FontIcon icon) {
    putValue(SMALL_DISABLED_ICON, icon);
  }

  public FontIcon getDisabledIcon() {
    var value = getValue(SMALL_DISABLED_ICON);
    if (value == null) {
      var icon = getIcon();
      if (icon != null) {
        value = FontIcon.of(icon.getIkon(), Color.GRAY);
        putValue(SMALL_DISABLED_ICON, value);
      }
    }
    return (FontIcon) value;
  }

  public void updateState() {
    setEnabled(isEnabled());
  }

  /**
   * Gets the name of the action.
   *
   * @return the name of the action
   */
  public String getName() {
    return (String) getValue(Action.NAME);
  }

  /**
   * Gets the description of the action.
   *
   * @return the description of the action
   */
  public String getDescription() {
    return (String) getValue(Action.SHORT_DESCRIPTION);
  }
}
