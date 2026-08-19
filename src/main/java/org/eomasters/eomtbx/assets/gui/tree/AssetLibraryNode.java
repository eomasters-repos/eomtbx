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

package org.eomasters.eomtbx.assets.gui.tree;

import javax.swing.tree.TreeNode;
import org.eomasters.icons.Icon;

public abstract class AssetLibraryNode implements TreeNode {

  private final String name;
  private final Icon icon;
  private AssetLibraryNode parent;

  public AssetLibraryNode(String name, Icon icon) {
    if (name == null) {
      throw new IllegalArgumentException("Name cannot be null");
    }
    if (icon == null) {
      throw new IllegalArgumentException("Icon cannot be null");
    }
    this.name = name;
    this.icon = icon;
  }

  public String getName() {
    return name;
  }

  public Icon getIcon() {
    return icon;
  }

  void setParent(AssetLibraryNode parent) {
    this.parent = parent;
  }

  @Override
  public AssetLibraryNode getParent() {
    return parent;
  }

  @Override
  public boolean isLeaf() {
    return getChildCount() == 0;
  }

}
