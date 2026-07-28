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

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

public class AssetTreeCellRenderer extends AssetTreeCellBase implements TreeCellRenderer {

  public AssetTreeCellRenderer(AssetLibraryTree libraryTree) {
    super(libraryTree);
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
      boolean leaf, int row, boolean hasFocus) {
    AssetTreeCellBase assetTreeCellComponent = getAssetTreeCellComponent(tree, value, selected, expanded,
        leaf, row, hasFocus);
    AssetLibraryNode node = getAsNode(value);
    if (node instanceof AssetNode) {
      setToolTip(((AssetNode) node).getAsset().getDescription());
    } else {
      setToolTip(null);
    }
    return assetTreeCellComponent;
  }

  private void setToolTip(String description) {
    if (description != null) {
      setToolTipText("<html>" + description);
    } else {
      setToolTipText(null);
    }
  }

}
