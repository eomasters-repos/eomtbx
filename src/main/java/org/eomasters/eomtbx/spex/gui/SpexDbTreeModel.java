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

package org.eomasters.eomtbx.spex.gui;

import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.eomasters.eomtbx.spex.AbstractSpex;
import org.eomasters.eomtbx.spex.SpexDb;
import org.eomasters.utils.ProgressManager;
import org.eomasters.utils.ProgressTask;

public class SpexDbTreeModel extends DefaultTreeModel {

  private Map<String, Filter<SpexDbTreeNode>> filters;

  public enum GROUPING {
    NONE(null),
    NAME(node -> String.valueOf(node.getIndex().getName().charAt(0)).toUpperCase()),
    DOMAIN(node -> node.getIndex().getDomain().name()),
    SOURCE(node -> node.getIndex().getSourceName().toUpperCase());

    private final GroupKeyProvider provider;

    GROUPING(GroupKeyProvider provider) {
      this.provider = provider;
    }

    private void groupTree(DefaultMutableTreeNode root, Collection<SpexDbTreeNode> indices) {
      Stream<SpexDbTreeNode> sorted = indices.stream()
                                             .sorted(Comparator.comparing(node -> node.getIndex().getName().toLowerCase()));
      if (provider != null) {
        TreeMap<String, DefaultMutableTreeNode> groupMap = new TreeMap<>();
        sorted.forEach(index -> {
          String groupKey = provider.provide(index);
          DefaultMutableTreeNode groupNode = groupMap.computeIfAbsent(groupKey, DefaultMutableTreeNode::new);
          groupNode.add(index);
        });
        groupMap.keySet().stream().sorted().forEach(key -> root.add(groupMap.get(key)));
      } else {
        sorted.forEach(root::add);
      }
    }
  }

  private final SpexDb spexDb;
  private GROUPING grouping;

  public SpexDbTreeModel() {
    super(new DefaultMutableTreeNode("SPEX DB"));
    grouping = GROUPING.NAME;
    filters = new HashMap<>();
    spexDb = SpexDb.getInstance();
    spexDb.addChangeListener(this::updateModel);
    updateModel();
  }

  public SpexDbTreeNode getNodeForName(String name) {
    Set<SpexDbTreeNode> allLeafNodes;
    synchronized (this) {
      allLeafNodes = getIndexNodes(getRoot());
    }
    return allLeafNodes.stream()
                       .parallel()
                       .filter(node -> node.getIndex().getName().equals(name))
                       .findFirst()
                       .orElse(null);
  }

  public DefaultMutableTreeNode[] getNodesForNames(String[] names) {
    Set<SpexDbTreeNode> allLeafNodes;
    synchronized (this) {
      allLeafNodes = getIndexNodes(getRoot());
    }
    return allLeafNodes.stream().parallel().filter(node -> {
      for (String name : names) {
        if (node.getIndex().getName().equals(name)) {
          return true;
        }
      }
      return false;
    }).toArray(DefaultMutableTreeNode[]::new);
  }

  private Set<SpexDbTreeNode> getIndexNodes(DefaultMutableTreeNode node) {
    Set<SpexDbTreeNode> leafNodes = new HashSet<>();
    if (node.isLeaf()) {
      if (node instanceof SpexDbTreeNode) {
        leafNodes.add((SpexDbTreeNode) node);
      }
    } else {
      Enumeration<javax.swing.tree.TreeNode> children = node.children();
      while (children.hasMoreElements()) {
        javax.swing.tree.TreeNode treeNode = children.nextElement();
        if (treeNode instanceof DefaultMutableTreeNode) {
          leafNodes.addAll(getIndexNodes((DefaultMutableTreeNode) treeNode));
        }
      }
    }
    return leafNodes;
  }

  /**
   * Sets the grouping of the tree. Call {@link #updateModel()} to apply the grouping.
   *
   * @param grouping the grouping to set
   */
  public void setGrouping(GROUPING grouping) {
    this.grouping = grouping;
  }

  /**
   * Sets the filters of the tree. Call {@link #updateModel()} to apply the filters.
   *
   * @param filters the filters to set
   */
  public void setFilters(Map<String, Filter<SpexDbTreeNode>> filters) {
    this.filters = filters;
  }

  @Override
  public DefaultMutableTreeNode getRoot() {
    return (DefaultMutableTreeNode) super.getRoot();
  }

  @Override
  public DefaultMutableTreeNode getChild(Object parent, int index) {
    return (DefaultMutableTreeNode) super.getChild(parent, index);
  }

  public void updateModel() {
    DefaultMutableTreeNode root = getRoot();
    synchronized (this) {
      root.removeAllChildren();
      Collection<AbstractSpex> indices = spexDb.getIndices();
      List<SpexDbTreeNode> treeNodes = indices.stream()
                                              .parallel()
                                              .map(SpexDbTreeNode::new)
                                              .collect(Collectors.toList());
      Collection<SpexDbTreeNode> filteredNodes = doFiltering(treeNodes);
      grouping.groupTree(root, filteredNodes);
      fireTreeStructureChanged(this, null, null, null);
    }
  }

  private Collection<SpexDbTreeNode> doFiltering(List<SpexDbTreeNode> treeNodes) {
    List<SpexDbTreeNode> filteredList = treeNodes;
    for (Entry<String, Filter<SpexDbTreeNode>> filterEntry : filters.entrySet()) {
      try (ProgressTask task = ProgressManager.registerTask(filterEntry.getKey(), filteredList.size())) {
        filteredList = filteredList.stream().parallel().filter(objectToMatch -> {
          boolean matches = filterEntry.getValue().matches(objectToMatch);
          task.worked(1);
          return matches;
        }).collect(Collectors.toList());
      }
    }
    return filteredList;
  }


  private interface GroupKeyProvider {

    String provide(SpexDbTreeNode index);
  }

}
