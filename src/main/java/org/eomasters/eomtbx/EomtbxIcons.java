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

  public static final Icon COASTAL_MAP = new SvgIcon("/org/eomasters/eomtbx/icons/CoastalMap", EomtbxIcons.class);
  /**
   * An icon with ABCDEF letters.
   */
  public static final Icon ABCDEF = new SvgIcon("/org/eomasters/eomtbx/icons/Abcdef", EomtbxIcons.class);
  /**
   * An icon representing groups or categories.
   */
  public static final Icon GROUPS = new SvgIcon("/org/eomasters/eomtbx/icons/Groups", EomtbxIcons.class);
  /**
   * An icon representing the SpeX database.
   */
  public static final Icon SPEX_DB = new SvgIcon("/org/eomasters/eomtbx/icons/SpexDb", EomtbxIcons.class);
  /**
   * An icon representing the SpeX operator.
   */
  public static final Icon SPEX_OP = new SvgIcon("/org/eomasters/eomtbx/icons/SpexOp", EomtbxIcons.class);
  /**
   * The icon for the Asset Library.
   */
  public static final Icon ASSET_LIBRARY = new SvgIcon("/org/eomasters/eomtbx/icons/AssetLibrary", EomtbxIcons.class);
  /**
   * A generic icon depicting an asset or resource group.
   */
  public static final Icon RESOURCE_GROUP = new SvgIcon("/org/eomasters/eomtbx/icons/Resources", EomtbxIcons.class);
  /**
   * An icon representing geometries.
   */
  public static final Icon GEOMETRIES = new SvgIcon("/org/eomasters/eomtbx/icons/Geometries", EomtbxIcons.class);
  /**
   * An icon representing image masks.
   */
  public static final Icon MASK = new SvgIcon("/org/eomasters/eomtbx/icons/ImageMask", EomtbxIcons.class);
  /**
   * An icon representing mathematical functions.
   */
  public static final Icon MATHS = new SvgIcon("/org/eomasters/eomtbx/icons/Maths", EomtbxIcons.class);

  protected EomtbxIcons() {
  }

}
