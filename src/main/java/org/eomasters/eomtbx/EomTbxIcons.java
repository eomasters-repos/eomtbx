/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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
import org.eomasters.icons.RasterIcon;

/**
 * Eases access to the icons used within the EOMTBX.
 */
public final class EomTbxIcons {

  /**
   * An icon with ABCDEF letters.
   */
  public static final Icon ABCDEF = new RasterIcon("/org/eomasters/eomtbx/icons/Abcdef");
  /**
   * An icon showing document with an arrow pointing upwards.
   */
  public static final Icon DOC_ARROW_UP = new RasterIcon("/org/eomasters/eomtbx/icons/DocArrowUp");
  /**
   * The EOMTBX icon.
   */
  public static final Icon EOMTBX = new RasterIcon("/org/eomasters/eomtbx/icons/EomToolbox");
  /**
   * An icon representing groups or categories.
   */
  public static final Icon GROUPS = new RasterIcon("/org/eomasters/eomtbx/icons/Groups");
  /**
   * An icon for the System Report.
   */
  public static final Icon SYS_REPORT = new RasterIcon("/org/eomasters/eomtbx/icons/SysReport");
  /**
   * An icon for the Wavelength Editor.
   */
  public static final Icon WVL_EDITOR = new RasterIcon("/org/eomasters/eomtbx/icons/WvlEditor");

  private EomTbxIcons() {
  }

}
