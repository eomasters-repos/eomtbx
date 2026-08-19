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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;
import org.eomasters.eomtbx.assets.Asset;

public interface GroupingStrategy {

  List<AssetLibraryNode> getGroupedAssets(List<Asset> assets);

  void insert(Asset asset, AssetGroupNode root);

  void remove(Asset asset, AssetGroupNode root);

  default void removeEmptyGroups(AssetGroupNode root) {
    Enumeration<? extends TreeNode> children = root.children();
    List<AssetGroupNode> toRemove = new ArrayList<>();
    while (children.hasMoreElements()) {
      TreeNode treeNode = children.nextElement();
      if(treeNode instanceof AssetGroupNode && treeNode.getChildCount() == 0) {
        toRemove.add((AssetGroupNode) treeNode);
      }
    }
    toRemove.forEach(root::remove);
  }
}
