/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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

package org.eomasters.eomtbx.wvleditor;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.icons.Icon.SIZE;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductNode;
import org.esa.snap.rcp.SnapApp;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 * Action to open the Wavelength Editor.
 */
@ActionID(category = "View", id = "EOMTBX_WvlEditorAction")
@ActionRegistration(
    displayName = "#TXT_WvlEditorAction",
    popupText = "#TXT_WvlEditorAction"
)
// [cut,copy,paste,delete] are from 500 to 540 and Properties is at 710, separator-before is at 700
// But the action is show before anyway
@ActionReferences({
    @ActionReference(path = "Menu/Optical", position = 5),
    @ActionReference(path = "Context/Product/Product", position = 1010, separatorBefore = 1000),
    @ActionReference(path = "Context/Product/RasterDataNode", position = 1010, separatorBefore = 1000),
})
@NbBundle.Messages({
    "TXT_WvlEditorAction=Modify Wavelengths...",
    "DSC_WvlEditorAction=Open editor to modify multiply wavelengths of one more or products"
})
public class WvlEditorAction extends AbstractAction implements Presenter.Popup {

  private final Result<ProductNode> result;
  @SuppressWarnings("FieldCanBeLocal")
  private final LookupListener lookupListener;

  /**
   * Creates a new instance of WvlEditorAction.
   *
   */
  public WvlEditorAction() {
    putValue(Action.NAME, Bundle.TXT_WvlEditorAction());
    putValue(Action.SHORT_DESCRIPTION, Bundle.DSC_WvlEditorAction());
    putValue(Action.SMALL_ICON, EomtbxIcons.WVL_EDITOR.getImageIcon(SIZE.S16));
    putValue(Action.LARGE_ICON_KEY, EomtbxIcons.WVL_EDITOR.getImageIcon(SIZE.S24));

    result = Utilities.actionsGlobalContext().lookupResult(ProductNode.class);
    this.lookupListener = ev -> EventQueue.invokeLater(() -> setEnabled(!result.allInstances().isEmpty()));
    result.addLookupListener(WeakListeners.create(LookupListener.class, this.lookupListener, result));
    this.lookupListener.resultChanged(null);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Optional<? extends ProductNode> any = result.allInstances().stream().findAny();
    if (any.isPresent()) {
      Product product = any.get().getProduct();
      WvlEditorDialog wvlEditor = new WvlEditorDialog(SnapApp.getDefault().getMainFrame(), product);
      Product[] allProducts = SnapApp.getDefault().getProductManager().getProducts();
      Product[] otherProducts = Arrays.stream(allProducts).filter(p -> p != product).toArray(Product[]::new);
      wvlEditor.addAdditionalProducts(otherProducts);
      wvlEditor.show();
    }
  }

  @Override
  public JMenuItem getPopupPresenter() {
    return new JMenuItem(this);
  }
}
