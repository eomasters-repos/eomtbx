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
import java.util.stream.Collectors;
import javax.swing.tree.TreeNode;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.AssetTypeRegistry;

class ByTypesGroupingStrategy implements GroupingStrategy {

  @Override
  public List<AssetLibraryNode> getGroupedAssets(List<Asset> assets) {
    List<AssetLibraryNode> groups = new ArrayList<>();
    List<AssetType> types = AssetTypeRegistry.instance().getServices();
    for (AssetType type : types) {
      List<AssetLibraryNode> typeAssets = assets.parallelStream()
                                                   .filter(asset -> asset.getType().equals(type))
                                                   .map(AssetNode::new)
                                                   .collect(Collectors.toList());
      if (!typeAssets.isEmpty()) {
        groups.add(new AssetGroupNode(type.getName(), type.getIcon(), typeAssets));
      }
    }
    return groups;
  }

  @Override
  public void insert(Asset asset, AssetGroupNode root) {
    Enumeration<? extends TreeNode> children = root.children();
    boolean added = false;
    AssetType assetType = asset.getType();
    while (children.hasMoreElements()) {
      TreeNode treeNode = children.nextElement();
      if (treeNode instanceof AssetGroupNode) {
        AssetGroupNode group = (AssetGroupNode) treeNode;
        if (group.getName().equals(assetType.getName())) {
          group.add(new AssetNode(asset));
          added = true;
          break;
        }
      }
    }
    // If we didn't find a matching group, create a new one
    if (!added) {
      AssetGroupNode newGroup = new AssetGroupNode(assetType.getName(), assetType.getIcon());
      newGroup.add(new AssetNode(asset));
      root.add(newGroup);
    }
  }

  @Override
  public void remove(Asset asset, AssetGroupNode root) {
    Enumeration<? extends TreeNode> children = root.children();
    while (children.hasMoreElements()) {
      TreeNode treeNode = children.nextElement();
      if (treeNode instanceof AssetGroupNode) {
        AssetGroupNode group = (AssetGroupNode) treeNode;
        if (group.getName().equals(asset.getType().getName())) {
          group.remove(group.getChildFor(asset));
        }
      }
    }
    removeEmptyGroups(root);
  }
}
