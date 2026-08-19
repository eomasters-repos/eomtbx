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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import javax.swing.tree.TreeNode;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.icons.Icon;

public class AssetGroupNode extends AssetLibraryNode {


  private final List<AssetLibraryNode> children;

  public AssetGroupNode(String name, List<AssetLibraryNode> children) {
    this(name, EomtbxIcons.RESOURCE_GROUP, children);
  }

  public AssetGroupNode(String name, Icon icon) {
    this(name, icon, new ArrayList<>());
  }

  public AssetGroupNode(String name, Icon icon, List<AssetLibraryNode> children) {
    super(name, icon);
    if (children == null) {
      throw new IllegalArgumentException("Children cannot be null");
    }
    initChildren(children);
    this.children = children;

  }

  private void initChildren(List<AssetLibraryNode> children) {
    for (AssetLibraryNode child : children) {
      child.setParent(this);
    }
    children.sort((node1, node2) -> node1.getName().compareToIgnoreCase(node2.getName()));
  }

  @Override
  public boolean getAllowsChildren() {
    return true;
  }

  @Override
  public TreeNode getChildAt(int childIndex) {
    return children.get(childIndex);
  }

  public AssetLibraryNode getChildFor(Asset asset) {
    Optional<AssetLibraryNode> any = children.stream().filter(node -> node instanceof AssetNode && ((AssetNode) node).getAsset().equals(asset)).findAny();
    return any.orElse(null);
  }

  @Override
  public int getChildCount() {
    return children.size();
  }

  @Override
  public int getIndex(TreeNode node) {
    if (node instanceof AssetLibraryNode) {
      return children.indexOf(node);
    }
    return -1;
  }

  @Override
  public Enumeration<? extends TreeNode> children() {
    return Collections.enumeration(children);
  }


  public void add(AssetLibraryNode node) {
    if (!getAllowsChildren()) {
      throw new IllegalStateException("Children are not allowed.");
    }
    children.add(node);
    node.setParent(this);
  }

  public void remove(AssetLibraryNode node) {
    if (!getAllowsChildren()) {
      throw new IllegalStateException("Children are not allowed.");
    }
    children.remove(node);
    node.setParent(null);
  }

  @Override
  public String toString() {
    return String.format("AssetGroupNode{name='%s', isLeaf=%s, children=%d}", getName(), isLeaf(), children.size());
  }
}
