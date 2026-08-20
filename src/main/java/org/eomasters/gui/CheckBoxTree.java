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

package org.eomasters.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * A JTree with checkboxes for nodes. This is a replacement for the JIDE CheckBoxTree.
 */
public class CheckBoxTree extends JTree {

    private final Map<TreePath, Boolean> checkedPaths = new HashMap<>();
    private final CheckBoxTreeSelectionModel checkBoxTreeSelectionModel;
    private final CheckBoxTreeCellRenderer checkBoxTreeCellRenderer;
    private TreeCellRenderer delegateRenderer;
    private boolean digIn = true;

    /**
     * Creates a new CheckBoxTree with the specified model.
     */
    public CheckBoxTree() {
        super();
        checkBoxTreeSelectionModel = new CheckBoxTreeSelectionModel(this);
        checkBoxTreeCellRenderer = new CheckBoxTreeCellRenderer();
        delegateRenderer = new DefaultTreeCellRenderer();
        super.setCellRenderer(checkBoxTreeCellRenderer);
        addMouseListener(new CheckBoxTreeMouseListener());
    }

    @Override
    public void setCellRenderer(TreeCellRenderer x) {
        // During super-construction the wrapper renderer is not initialized yet.
        if (checkBoxTreeCellRenderer == null) {
            super.setCellRenderer(x);
            return;
        }

        if (x == checkBoxTreeCellRenderer) {
            super.setCellRenderer(x);
            return;
        }

        delegateRenderer = x != null ? x : new DefaultTreeCellRenderer();
        // Keep the wrapper renderer installed; only update delegate to avoid
        // recursion with LAF/UI updates that also call setCellRenderer(...).
        if (super.getCellRenderer() != checkBoxTreeCellRenderer) {
            super.setCellRenderer(checkBoxTreeCellRenderer);
        }
    }

    /**
     * Sets whether to propagate selection to child nodes.
     *
     * @param digIn true to propagate selection to child nodes, false otherwise
     */
    public void setDigIn(boolean digIn) {
        this.digIn = digIn;
    }

    /**
     * Returns whether a checkbox should be visible for the given path.
     * This method can be overridden by subclasses to control checkbox visibility.
     *
     * @param path the path to check
     * @return true if a checkbox should be visible, false otherwise
     */
    public boolean isCheckBoxVisible(TreePath path) {
        return true;
    }

    /**
     * Returns the CheckBoxTreeSelectionModel used by this tree.
     *
     * @return the CheckBoxTreeSelectionModel
     */
    public CheckBoxTreeSelectionModel getCheckBoxTreeSelectionModel() {
        return checkBoxTreeSelectionModel;
    }

    /**
     * A TreeSelectionModel that manages checkbox selections.
     */
    public static class CheckBoxTreeSelectionModel {
        private final CheckBoxTree tree;
        private final List<TreeSelectionListener> listeners = new ArrayList<>();

        public CheckBoxTreeSelectionModel(CheckBoxTree tree) {
            this.tree = tree;
        }

        /**
         * Adds a TreeSelectionListener to this model.
         *
         * @param listener the listener to add
         */
        public void addTreeSelectionListener(TreeSelectionListener listener) {
            listeners.add(listener);
        }

        /**
         * Removes a TreeSelectionListener from this model.
         *
         * @param listener the listener to remove
         */
        public void removeTreeSelectionListener(TreeSelectionListener listener) {
            listeners.remove(listener);
        }

        /**
         * Sets the selection paths.
         *
         * @param paths the paths to select
         */
        public void setSelectionPaths(TreePath[] paths) {
            if (paths == null) {
                return;
            }

            // Clear existing selections
            tree.checkedPaths.clear();

            // Add new selections
            for (TreePath path : paths) {
                tree.checkedPaths.put(path, true);

                // If digIn is enabled, select all child nodes
                if (tree.digIn) {
                    selectDescendants(path);
                }
            }

            // Notify listeners
            fireValueChanged(paths, true);
            tree.repaint();
        }

        /**
         * Sets a single selection path.
         *
         * @param path the path to select
         */
        public void setSelectionPath(TreePath path) {
            if (path == null) {
                return;
            }

            setSelectionPaths(new TreePath[]{path});
        }

        /**
         * Selects all descendants of the given path.
         *
         * @param path the parent path
         */
        private void selectDescendants(TreePath path) {
            Object node = path.getLastPathComponent();
            if (node instanceof DefaultMutableTreeNode treeNode) {
                for (int i = 0; i < treeNode.getChildCount(); i++) {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
                    TreePath childPath = path.pathByAddingChild(child);
                    tree.checkedPaths.put(childPath, true);
                    selectDescendants(childPath);
                }
            }
        }

        /**
         * Notifies listeners that the selection has changed.
         *
         * @param paths the paths that changed
         * @param isAddedPath whether the paths were added or removed
         */
        private void fireValueChanged(TreePath[] paths, boolean isAddedPath) {
            if (listeners.isEmpty()) {
                return;
            }

            for (TreePath path : paths) {
                TreeSelectionEvent event = new TreeSelectionEvent(this, path, isAddedPath, path, path);
                for (TreeSelectionListener listener : listeners) {
                    listener.valueChanged(event);
                }
            }
        }
    }

    /**
     * A TreeCellRenderer that renders checkboxes next to tree nodes.
     */
    private class CheckBoxTreeCellRenderer implements TreeCellRenderer {
        private final JCheckBox checkBox = new JCheckBox();
        private final JPanel panel = new JPanel(new BorderLayout());

        public CheckBoxTreeCellRenderer() {
            panel.setOpaque(false);
            checkBox.setOpaque(false);
        }

        int getCheckBoxWidth() {
            return checkBox.getPreferredSize().width;
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                     boolean leaf, int row, boolean hasFocus) {
            Component rendererComponent = delegateRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            TreePath path = tree.getPathForRow(row);
            if (path != null && isCheckBoxVisible(path)) {
                checkBox.setSelected(checkedPaths.getOrDefault(path, false));
                panel.removeAll();
                panel.add(checkBox, BorderLayout.WEST);
                panel.add(rendererComponent, BorderLayout.CENTER);
                return panel;
            } else {
                return rendererComponent;
            }
        }
    }

    /**
     * A MouseListener that handles checkbox clicks.
     */
    private class CheckBoxTreeMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int row = getRowForLocation(e.getX(), e.getY());
            if (row < 0) {
                return;
            }

            TreePath path = getPathForRow(row);
            if (path != null && isCheckBoxVisible(path)) {
                Rectangle pathBounds = getPathBounds(path);
                int checkBoxWidth = checkBoxTreeCellRenderer.getCheckBoxWidth();
                if (pathBounds != null && e.getX() >= pathBounds.x && e.getX() < pathBounds.x + checkBoxWidth) {
                    boolean currentState = checkedPaths.getOrDefault(path, false);
                    checkedPaths.put(path, !currentState);

                    // If digIn is enabled, update all child nodes
                    if (digIn) {
                        updateChildCheckStates(path, !currentState);
                    }

                    // Notify listeners
                    checkBoxTreeSelectionModel.fireValueChanged(new TreePath[]{path}, !currentState);
                    repaint();
                }
            }
        }

        /**
         * Updates the check state of all child nodes.
         *
         * @param parentPath the parent path
         * @param state the new state
         */
        private void updateChildCheckStates(TreePath parentPath, boolean state) {
            Object node = parentPath.getLastPathComponent();
            if (node instanceof DefaultMutableTreeNode treeNode) {
                for (int i = 0; i < treeNode.getChildCount(); i++) {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
                    TreePath childPath = parentPath.pathByAddingChild(child);
                    checkedPaths.put(childPath, state);
                    updateChildCheckStates(childPath, state);
                }
            }
        }
    }
}
