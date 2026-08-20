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

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import javax.swing.BorderFactory;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.esa.snap.rcp.SnapApp;

public abstract class AssetTreeCellBase extends JPanel {

  private static final Color defForeground = UIManager.getColor("Tree.foreground");
  private static final Color defBackground = UIManager.getColor("Tree.background");
  private static final Color selForeground = UIManager.getColor("Tree.selectionForeground");
  private static final Color selBackground = UIManager.getColor("Tree.selectionBackground");
  private static final Font TREE_LABEL_FONT = UIManager.getFont("Label.font").deriveFont(
      AffineTransform.getScaleInstance(1.2, 1.2));

  protected final JLabel label;
  protected final JButton addButton;
  protected final AssetLibraryTree libraryTree;

  public AssetTreeCellBase(AssetLibraryTree libraryTree) {
    this.libraryTree = libraryTree;
    label = new JLabel();
    label.setFont(TREE_LABEL_FONT);
    ImageIcon addIcon = Icons.PLUS.getImageIcon(Icon.SIZE_16);
    addButton = new JButton(addIcon);
    addButton.setDisabledIcon(createDisabledIcon(addIcon));
    addButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    addButton.setContentAreaFilled(false);
    this.setLayout(new MigLayout("",
        "0[grow, fill]10[right]0",
        "0[]0"));
    this.add(label);
    this.add(addButton);
  }

  protected AssetTreeCellBase getAssetTreeCellComponent(JTree tree, Object value, boolean selected,
      boolean expanded, boolean leaf, int row, boolean hasFocus) {
    AssetLibraryNode node = getAsNode(value);
    setupComponentColors(selected);
    if (node != null) {
      label.setText(node.getName());
      label.setIcon(node.getIcon().getImageIcon(Icon.SIZE_24));
      if (node.isLeaf()) {
        addButton.setEnabled(SnapApp.getDefault().getProductManager().getProductCount() > 0);
        addButton.setVisible(true);
      } else {
        addButton.setVisible(false);
      }
    } else {
      label.setText("");
      label.setIcon(null);
      addButton.setVisible(false);
    }
    invalidate();
    return this;
  }

  public ImageIcon createDisabledIcon(ImageIcon icon) {
    Image img = icon.getImage();
    Image grayImage = GrayFilter.createDisabledImage(img);
    return new ImageIcon(grayImage);
  }

  static AssetLibraryNode getAsNode(Object value) {
    if (value instanceof AssetLibraryNode) {
      return (AssetLibraryNode) value;
    }
    return null;
  }

  protected void setupComponentColors(boolean selected) {
    if (selected) {
      this.setBackground(selBackground);
      label.setBackground(selBackground);
      label.setForeground(selForeground);
      addButton.setBackground(selBackground);
      addButton.setForeground(selForeground);
    } else {
      this.setBackground(defBackground);
      label.setBackground(defBackground);
      label.setForeground(defForeground);
      addButton.setBackground(defBackground);
      addButton.setForeground(defForeground);
    }
  }

}
