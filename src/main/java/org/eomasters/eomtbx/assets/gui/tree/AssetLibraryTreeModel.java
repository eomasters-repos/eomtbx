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
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetLibrary;
import org.eomasters.eomtbx.assets.AssetLibrary.Listener;

public class AssetLibraryTreeModel extends DefaultTreeModel {

  private final AssetLibrary library;
  private AssetGroupNode root;
  private GROUPING grouping;
  private String filterText;

  public AssetLibraryTreeModel(AssetLibrary library, GROUPING grouping) {
    this(library, grouping, createRootNode(grouping.getStrategy(), library.getAssets()));
  }

  AssetLibraryTreeModel(AssetLibrary library, GROUPING grouping, AssetGroupNode rootNode) {
    super(rootNode);
    this.library = library;
    root = rootNode;
    this.grouping = grouping;
    library.addListener(new Listener() {
      @Override
      public void onAssetsAdded(List<Asset> assets) {
        for (Asset asset : assets) {
          insertAsset(asset);
        }
        fireTreeStructureChanged(AssetLibraryTreeModel.this, null, null, null);
      }

      @Override
      public void onAssetsRemoved(List<Asset> assets) {
        for (Asset asset : assets) {
          removeAsset(asset);
        }
        fireTreeStructureChanged(AssetLibraryTreeModel.this, null, null, null);
      }

      @Override
      public void onAssetChanged(Asset asset) {
        TreePath pathTo = AssetLibraryTreeModel.this.getPathTo(asset);
        TreePath parentPath = pathTo.getParentPath();
        fireTreeNodesChanged(AssetLibraryTreeModel.this, parentPath.getPath(), null, null);
      }
    });
  }

  private void removeAsset(Asset asset) {
    grouping.getStrategy().remove(asset, root);
  }

  private void insertAsset(Asset asset) {
    grouping.getStrategy().insert(asset, root);
  }

  public void updateModel() {
    List<Asset> assets = library.getAssets();
    List<Asset> filteredAssets = assets;
    if (filterText != null && !filterText.isEmpty()) {
      filteredAssets = assets.parallelStream()
                           .filter(asset -> contains(asset, filterText))
                           .collect(Collectors.toList());
    }
    this.root = createRootNode(grouping.getStrategy(), filteredAssets);
    super.setRoot(root);
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    // ignore
  }

  public void setGrouping(GROUPING grouping) {
    if (!this.grouping.equals(grouping)) {
      this.grouping = grouping;
      updateModel();
    }
  }

  public void setFilter(String text) {
    if (!Objects.equals(filterText, text)) {
      this.filterText = text;
      updateModel();
    }
  }

  private static boolean contains(Asset asset, String filterText) {
    String lowerCaseFilter = filterText.toLowerCase();
    String name = asset.getName();
    String description = asset.getDescription();
    String typeName = asset.getType().getName();
    String typeDescr = asset.getType().getDescription();
    String userNotes = asset.getUserNotes();
    return name.toLowerCase().contains(lowerCaseFilter) || description.toLowerCase().contains(lowerCaseFilter)
        || Arrays.stream(asset.getTags()).anyMatch(s -> s.toLowerCase().contains(lowerCaseFilter))
        || typeName.toLowerCase().contains(lowerCaseFilter) || typeDescr.toLowerCase().contains(lowerCaseFilter)
        || userNotes.toLowerCase().contains(lowerCaseFilter);
  }

  private static AssetGroupNode createRootNode(GroupingStrategy strategy, List<Asset> assets) {
    return new AssetGroupNode("root", strategy.getGroupedAssets(assets));
  }

  public TreePath getPathTo(TreeNode node) {
    List<TreeNode> path = new ArrayList<>();
    if (node != null) {
      TreeNode currentNode = node;
      while (currentNode != null) {
        path.add(0, currentNode);
        currentNode = currentNode.getParent();
      }
    }
    return new TreePath(path.toArray(new TreeNode[0]));
  }

  public TreePath getPathTo(Asset asset) {
    List<TreeNode> path = new ArrayList<>();
    AssetNode node = findNode(root, asset);
    if (node != null) {
      TreeNode currentNode = node;
      while (currentNode != null) {
        path.add(0, currentNode);
        currentNode = currentNode.getParent();
      }
    }
    if (!path.isEmpty()) {
      return new TreePath(path.toArray(new TreeNode[0]));
    } else {
      return null;
    }
  }


  private AssetNode findNode(AssetGroupNode root, Asset asset) {
    Enumeration<? extends TreeNode> children = root.children();
    while (children.hasMoreElements()) {
      TreeNode child = children.nextElement();
      if (child instanceof AssetNode) {
        AssetNode foundNode = (AssetNode) child;
        if (foundNode.getAsset().equals(asset)) {
          return foundNode;
        }
      } else if (child instanceof AssetGroupNode) {
        AssetNode foundNode = findNode((AssetGroupNode) child, asset);
        if (foundNode != null) {
          return foundNode;
        }
      }

    }
    return null;
  }

}
