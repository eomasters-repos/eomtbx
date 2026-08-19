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

import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.eomasters.eomtbx.assets.Asset;

public class AssetLibraryTree extends JTree {

  private final AssetLibraryTreeModel treeModel;

  public AssetLibraryTree(AssetLibraryTreeModel libraryTreeModel) {
    super(libraryTreeModel);
    this.treeModel = libraryTreeModel;
    setRootVisible(false);
    setShowsRootHandles(true);
    // trees need to be registered if they shall show tooltips
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  public void setSelectedNode(TreeNode node) {
    if (node != null) {
      setSelectionPath(treeModel.getPathTo(node));
    }
  }

  public TreeNode getSelectedNode() {
    TreePath selectionPath = getSelectionPath();
    if (selectionPath != null) {
      Object lastPathComponent = selectionPath.getLastPathComponent();
      return (TreeNode) lastPathComponent;
    }
    return null;
  }

  public void setSelectedAsset(Asset asset) {
    if (asset != null) {
      TreePath pathTo = treeModel.getPathTo(asset);
      if (pathTo != null) {
        setSelectionPath(pathTo);
      }
    }
  }

  public Asset getSelectedAsset() {
    TreeNode selectedNode = getSelectedNode();
    if (selectedNode instanceof AssetNode) {
      return ((AssetNode) selectedNode).getAsset();
    }
    return null;
  }

  @Override
  public void updateUI() {
    if (treeModel != null) {
      treeModel.updateModel();
    }
    super.updateUI();
  }
}
