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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.tree.TreeNode;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.icons.Icons;

class ByTagsGroupingStrategy implements GroupingStrategy {

  private static final String NOT_TAGGED = "Not Tagged";

  @Override
  public List<AssetLibraryNode> getGroupedAssets(List<Asset> assets) {
    Set<String> tags = assets.parallelStream()
                             .flatMap(asset -> Arrays.stream(asset.getTags()))
                             .collect(Collectors.toSet());

    List<AssetLibraryNode> groups = new ArrayList<>();
    for (String tag : tags) {
      List<AssetLibraryNode> assetsWithTags = assets.parallelStream()
                                                  .filter(asset -> List.of(asset.getTags()).contains(tag))
                                                  .map(AssetNode::new)
                                                  .collect(Collectors.toList());
      if (!assetsWithTags.isEmpty()) {
        groups.add(new AssetGroupNode(tag, Icons.HASHTAG, assetsWithTags));
      }
    }

    List<AssetLibraryNode> notTaggedAssets = assets.parallelStream()
                                                  .filter(asset -> asset.getTags().length == 0)
                                                  .map(AssetNode::new)
                                                  .collect(Collectors.toList());
    if (!notTaggedAssets.isEmpty()) {
      groups.add(new AssetGroupNode(NOT_TAGGED, Icons.HASHTAG, notTaggedAssets));
    }
    return groups;
  }

  @Override
  public void insert(Asset asset, AssetGroupNode root) {
    Enumeration<? extends TreeNode> children = root.children();
    String[] tags = asset.getTags();
    if (tags.length > 0) {
      for (String tag : tags) {
        AssetGroupNode matchingGroup = null;
        while (children.hasMoreElements()) {
          TreeNode treeNode = children.nextElement();
          if (treeNode instanceof AssetGroupNode) {
            AssetGroupNode group = (AssetGroupNode) treeNode;
            if (tag.equals(group.getName())) {
              matchingGroup = group;
              break;
            }
          }
        }

        // If we didn't find a matching group, create a new one
        if (matchingGroup == null) {
          matchingGroup = new AssetGroupNode(tag, Icons.HASHTAG);
          root.add(matchingGroup);
        }

        matchingGroup.add(new AssetNode(asset));
      }
    } else {
      AssetGroupNode notTagged = getNotTaggedGroup(root);
      if (notTagged == null) {
        notTagged = new AssetGroupNode(NOT_TAGGED, Icons.HASHTAG);
        root.add(notTagged);
      }
      notTagged.add(new AssetNode(asset));
    }
  }

  private AssetGroupNode getNotTaggedGroup(AssetGroupNode root) {
    Enumeration<? extends TreeNode> children = root.children();
    while (children.hasMoreElements()) {
      TreeNode treeNode = children.nextElement();
      if (treeNode instanceof AssetGroupNode) {
        AssetGroupNode group = (AssetGroupNode) treeNode;
        if (group.getName().equals(NOT_TAGGED)) {
          return group;
        }
      }
    }
    return null;
  }

  @Override
  public void remove(Asset asset, AssetGroupNode root) {
    Enumeration<? extends TreeNode> children = root.children();
    String[] tags = asset.getTags();
    if (tags.length > 0) {
      for (String tag : tags) {
        while (children.hasMoreElements()) {
          TreeNode treeNode = children.nextElement();
          if (treeNode instanceof AssetGroupNode) {
            AssetGroupNode group = (AssetGroupNode) treeNode;
            if (tag.equals(group.getName())) {
              group.remove(group.getChildFor(asset));
            }
          }
        }
      }
    } else {
      AssetGroupNode notTagged = getNotTaggedGroup(root);
      if (notTagged != null) {
        notTagged.remove(notTagged.getChildFor(asset));
        if (notTagged.getChildCount() == 0) {
          root.remove(notTagged);
        }
      }
    }
    removeEmptyGroups(root);
  }

}
