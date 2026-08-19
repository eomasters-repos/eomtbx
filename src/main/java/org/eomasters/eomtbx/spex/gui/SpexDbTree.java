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
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.eomasters.eomtbx.spex.AbstractSpex;
import org.eomasters.gui.CheckBoxTree;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;

public class SpexDbTree extends CheckBoxTree {

  private final CHECKBOX_DISPLAY showCheckbox;
  public enum CHECKBOX_DISPLAY {
    ALL, LEAVES_ONLY, PARENTS_ONLY, NONE
  }

  public SpexDbTree(CHECKBOX_DISPLAY showCheckbox) {
    this.showCheckbox = showCheckbox;
    JPopupMenu popup = new JPopupMenu();
    popup.add(new ExpandAllGroupsAction(this));
    popup.add(new CollapseAllGroupsAction(this));
    setComponentPopupMenu(popup);
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    ToolTipManager.sharedInstance().registerComponent(this); // trees need to be registered if they shall show tooltips
    setCellRenderer(new SpexDbTreeCellRenderer());

    setRootVisible(false);
    setDigIn(false);
    setShowsRootHandles(true);
  }

  @Override
  public boolean isCheckBoxVisible(TreePath path) {
    if(showCheckbox == null) {
      return true;
    }
    switch (showCheckbox) {
      case LEAVES_ONLY:
        return ((DefaultMutableTreeNode) path.getLastPathComponent()).isLeaf();
      case PARENTS_ONLY:
        return !((DefaultMutableTreeNode) path.getLastPathComponent()).isLeaf();
      case NONE:
        return false;
      case ALL:
      default:
        return true;
    }
  }

  private static class ExpandAllGroupsAction extends AbstractAction {

    private final JTree checkBoxTree;

    public ExpandAllGroupsAction(JTree checkBoxTree) {
      this.checkBoxTree = checkBoxTree;
      putValue(Action.NAME, "Expand all groups");
      putValue(Action.SMALL_ICON, Icons.PLUS.getImageIcon(Icon.SIZE_16));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // expand all nodes of checkBoxTree
      int j = checkBoxTree.getRowCount();
      int i = 0;
      while (i < j) {
        checkBoxTree.expandRow(i++);
        j = checkBoxTree.getRowCount();
      }

    }
  }

  private static class CollapseAllGroupsAction extends AbstractAction {

    private final JTree checkBoxTree;

    public CollapseAllGroupsAction(JTree checkBoxTree) {
      this.checkBoxTree = checkBoxTree;
      putValue(Action.NAME, "Collapse all groups");
      putValue(Action.SMALL_ICON, Icons.MINUS.getImageIcon(Icon.SIZE_16));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // collapse all nodes of checkBoxTree
      int j = checkBoxTree.getRowCount();
      int i = 0;
      while (i < j) {
        checkBoxTree.collapseRow(i++);
        j = checkBoxTree.getRowCount();
      }
    }
  }

  private static class SpexDbTreeCellRenderer extends DefaultTreeCellRenderer {

    public SpexDbTreeCellRenderer() {
      setLeafIcon(null);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
        int row, boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      if (leaf && value instanceof SpexDbTreeNode) {
        SpexDbTreeNode treeNode = (SpexDbTreeNode) value;
        AbstractSpex idx = treeNode.getIndex();
        setText(String.format("<html><b>%s</b> - %s (%s)",
            idx.getName(), idx.getDescription(), idx.getDomain()));
        setToolTipText(String.format("<html>"
                + "<b>Name:</b> %s<br>"
                + "<b>Description:</b> %s<br>"
                + "<b>Domain:</b> %s<br>"
                + "<b>Formula:</b> %s<br>"
                + "<b>Source:</b> %s<br>",
            idx.getName(), idx.getDescription(), idx.getDomain(), idx.getFormula(), idx.getSourceName()));
      } else {
        setText(String.format("<html><b>%s</b>", value));
        setToolTipText(null);
      }
      return this;
    }

  }
}
