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

package org.eomasters.eomtbx.gui;

import java.util.Map;
import org.esa.snap.core.gpf.ui.DefaultOperatorAction;
import org.esa.snap.ui.ModelessDialog;
import org.openide.util.ImageUtilities;


/**
 * Extends the DefaultOperatorAction. The created OperatorDialog is set the icon defined by the 'iconBase' property, if
 * set.
 *
 * @see javax.swing.Action
 */
public class ExtendedOperatorAction extends DefaultOperatorAction {

  protected static final String ICON_BASE_KEY = "iconBase";

  public static ExtendedOperatorAction create(Map<String, Object> properties) {

    ExtendedOperatorAction action = new ExtendedOperatorAction();
    DefaultOperatorAction base = DefaultOperatorAction.create(properties);
    Object[] keys = base.getKeys();
    for (Object key : keys) {
      String keyString = (String) key;
      action.putValue(keyString, base.getValue(keyString));
    }

    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      if (ICON_BASE_KEY.equals(entry.getKey())) {
        action.putValue(entry.getKey(), entry.getValue());
      }
    }
    return action;
  }

  protected ModelessDialog createOperatorDialog() {
    ModelessDialog operatorDialog = super.createOperatorDialog();
    String iconPath = (String) getValue(ICON_BASE_KEY);
    if (iconPath != null) {
      operatorDialog.getJDialog().setIconImage(ImageUtilities.loadImage(iconPath));
    }
    return operatorDialog;
  }

}
