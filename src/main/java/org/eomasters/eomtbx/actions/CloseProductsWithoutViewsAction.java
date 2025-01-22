/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Basic for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2025 Marco Peters
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

package org.eomasters.eomtbx.actions;

import eu.esa.snap.netbeans.docwin.WindowUtilities;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductManager;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.rcp.actions.file.CloseProductAction;
import org.esa.snap.rcp.windows.ProductSceneViewTopComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Action which closes all products which are not used by an opened scene view.
 */
@ActionID(category = "File", id = "EOMTBX_CloseProductsWithoutViewsAction")
@ActionRegistration(displayName = "#CloseProductsWithoutViewsActionName", lazy = false)

@ActionReferences({
    @ActionReference(path = "Menu/File", position = 31), // 31 is after "Close Other Products"
    @ActionReference(path = "Context/Product/Product", position = 81)}) // 81 is after "Close Other Product"
@NbBundle.Messages({"CloseProductsWithoutViewsActionName=Close Products Without a View"})
public final class CloseProductsWithoutViewsAction extends AbstractAction {

  /**
   * Creates a new instance of {@link CloseProductsWithoutViewsAction}.
   */
  public CloseProductsWithoutViewsAction() {
    super(Bundle.CloseProductsWithoutViewsActionName());
    ProductManager productManager = SnapApp.getDefault().getProductManager();
    productManager.addListener(new ProductManagerListener());
    setEnabled(false);
  }


  @Override
  public void actionPerformed(ActionEvent e) {
    Set<Product> collect = Stream.of(SnapApp.getDefault().getProductManager().getProducts())
        .collect(Collectors.toSet());
    Stream<ProductSceneViewTopComponent> openedViews = WindowUtilities.getOpened(ProductSceneViewTopComponent.class);
    openedViews.map(ProductSceneViewTopComponent::getView)
        .forEach(view -> Arrays.stream(view.getRasters()).forEach(raster -> collect.remove(raster.getProduct())));

    CloseProductAction.closeProducts(collect);
  }

  private class ProductManagerListener implements ProductManager.Listener {

    @Override
    public void productAdded(ProductManager.Event event) {
      updateEnableState();
    }

    @Override
    public void productRemoved(ProductManager.Event event) {
      updateEnableState();
    }

    private void updateEnableState() {
      SwingUtilities.invokeLater(() -> setEnabled(SnapApp.getDefault().getProductManager().getProductCount() > 0));
    }
  }

}
