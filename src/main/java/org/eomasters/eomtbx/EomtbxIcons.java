/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Basic for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2024 Marco Peters
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

package org.eomasters.eomtbx;

import org.eomasters.icons.Icon;
import org.eomasters.icons.SvgIcon;

/**
 * Eases access to the icons used within the EOMTBX.
 */
public class EomtbxIcons {


  /**
   * The EOMTBX icon, clean without text.
   */
  public static final Icon EOMTBX = new SvgIcon("/org/eomasters/eomtbx/icons/eomtbx_logo", EomtbxIcons.class);
  /**
   * An icon for the Wavelength Editor.
   */
  public static final Icon WVL_EDITOR = new SvgIcon("/org/eomasters/eomtbx/icons/WvlEditor", EomtbxIcons.class);

  protected EomtbxIcons() {
  }

}
