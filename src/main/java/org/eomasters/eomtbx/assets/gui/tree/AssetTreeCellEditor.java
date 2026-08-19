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

import eu.esa.snap.netbeans.docwin.WindowUtilities;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.EventObject;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.gui.ScrollableMenu;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductManager;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.rcp.nodes.PNode;
import org.esa.snap.rcp.windows.ProductExplorerTopComponent;
import org.openide.nodes.Node;

public class AssetTreeCellEditor extends AssetTreeCellBase implements TreeCellEditor {

  private static final Font MENU_LABEL_FONT = UIManager.getFont("Label.font").deriveFont(
      AffineTransform.getScaleInstance(1.15, 1.15));

  private static final boolean SELECTED_WHEN_EDITING = true;
  private final JPopupMenu targetMenu;

  public AssetTreeCellEditor(AssetLibraryTree libraryTree) {
    super(libraryTree);
    targetMenu = new JPopupMenu();
    ScrollableMenu.install(targetMenu);
    addButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
          onButtonClicked(e.getLocationOnScreen());
        }
      }
    });
    addButton.isFocusOwner();
  }

  public Component getTreeCellEditorComponent(JTree tree, Object value,
      boolean isSelected, boolean expanded, boolean leaf, int row) {
    return getAssetTreeCellComponent(tree, value, SELECTED_WHEN_EDITING, expanded, leaf, row, false);
  }

  @Override
  public Object getCellEditorValue() {
    return null;
  }

  public boolean isCellEditable(EventObject anEvent) {
    if (anEvent instanceof MouseEvent) {
      MouseEvent mouseEvent = (MouseEvent) anEvent;
      return getAsset(mouseEvent.getLocationOnScreen()) != null;
    }
    return false;
  }

  @Override
  public boolean shouldSelectCell(EventObject anEvent) {
    return true;
  }

  @Override
  public boolean stopCellEditing() {
    return true;
  }

  @Override
  public void cancelCellEditing() {

  }

  @Override
  public void addCellEditorListener(CellEditorListener l) {

  }

  @Override
  public void removeCellEditorListener(CellEditorListener l) {

  }

  private void onButtonClicked(Point locationOnScreen) {
    ProductManager productManager = SnapApp.getDefault().getProductManager();
    int productCount = productManager.getProductCount();
    if (productCount == 0) {
      return;
    }
    Asset asset = getAsset(locationOnScreen);
    if (asset == null) {
      return;
    }
    if (productCount == 1) {
      Product product = productManager.getProduct(0);
      var addAssetAction = new AddAssetToProductAction(libraryTree.getParent(), product.getDisplayName(), asset, product);
      addAssetAction.execute();
    } else if (productCount > 1) {
      initTargetMenu(productManager, asset);
      targetMenu.show(addButton, 0, addButton.getHeight());
    }
  }

  private void initTargetMenu(ProductManager productManager, Asset asset) {
    targetMenu.removeAll();
    Product selectedProduct = getSelectedProduct();
    if (selectedProduct != null) {
      // special menu entry for the selected product
      String title = String.format("Selected Product (%s)", getCappedDisplayName(selectedProduct));

      var addAssetAction = new AddAssetToProductAction(libraryTree.getParent(), title, asset, selectedProduct);
      JMenuItem menuItem = new JMenuItem(addAssetAction);
      menuItem.setFont(MENU_LABEL_FONT);
      targetMenu.add(menuItem);
      targetMenu.add(new JSeparator());
    }
    Product[] products = productManager.getProducts();
    for (Product product : products) {
      var addAssetAction = new AddAssetToProductAction(libraryTree.getParent(), product.getDisplayName(), asset, product);
      targetMenu.add(new JMenuItem(addAssetAction));
    }
  }

  private static String getCappedDisplayName(Product selectedProduct) {
    int maxLength = 25;
    String displayName = selectedProduct.getDisplayName();
    String ellipsis = "...";
    if (displayName.length() > maxLength + ellipsis.length()) {
      return displayName.substring(0, maxLength) + ellipsis;
    }
    return displayName;
  }

  private static Product getSelectedProduct() {
    // SnapApp.getDefault().getSelectedProduct(); does not work properly
    ProductExplorerTopComponent productExplorerTopComponent = WindowUtilities.getOpened(
        ProductExplorerTopComponent.class).findFirst().orElse(null);
    if (productExplorerTopComponent != null) {
      Node[] selectedNodes = productExplorerTopComponent.getExplorerManager().getSelectedNodes();

      for (Node selectedNode : selectedNodes) {
        if (selectedNode instanceof PNode) {
          return ((PNode) selectedNode).getProduct();
        }
      }
    }
    return null;
  }

  private Asset getAsset(Point locationOnScreen) {
    Point treePoint = locationOnScreen.getLocation();
    SwingUtilities.convertPointFromScreen(treePoint, libraryTree);
    TreePath pathForLocation = libraryTree.getPathForLocation(treePoint.x, treePoint.y);
    if (pathForLocation != null) {
      Object lastNode = pathForLocation.getLastPathComponent();
      if (lastNode instanceof AssetNode) {
        AssetNode assetNode = (AssetNode) lastNode;
        return assetNode.getAsset();
      }
    }
    return null;
  }

}
