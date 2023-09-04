/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * --> https://www.eomasters.org/sw/EOMTBX
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
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.wvleditor;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductNode;
import org.esa.snap.rcp.SnapApp;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Action to open the Wavelength Editor.
 */
@ActionID(category = "View", id = "EOM_WvlEditorAction")
@ActionRegistration(
    displayName = "#CTL_WvlEditorAction_MenuText",
    popupText = "#CTL_WvlEditorAction_MenuText",
    iconBase = "org/eomasters/eomtbx/icons/WvlEditor_16.png"
)
@ActionReferences({
    @ActionReference(path = "Menu/Optical", position = 5),
    // [cut,copy,paste,delete] are from 500 to 540 and Properties is at 710, separator-before is at 700
    // But the action is show before anyway
    @ActionReference(path = "Context/Product/Product", position = 1010, separatorBefore = 1000),
})
@NbBundle.Messages({
    "CTL_WvlEditorAction_MenuText=Modify Wavelengths...",
    "CTL_WvlEditorAction_ShortDescription=Open editor to modify multiply wavelengths of one more or products"
})
public class WvlEditorAction extends AbstractAction {

  private final Product product;

  /**
   * Creates a new instance of WvlEditorAction. the node is automatically provided by SNAP (NetBeans Platform).
   *
   * @param node the node of the product which is currently selected
   */
  public WvlEditorAction(ProductNode node) {
    super(Bundle.CTL_WvlEditorAction_MenuText());
    product = node.getProduct();
    putValue(Action.SHORT_DESCRIPTION, Bundle.CTL_WvlEditorAction_ShortDescription());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    WvlEditorDialog wvlEditor = new WvlEditorDialog(SnapApp.getDefault().getMainFrame(), product);
    Product[] allProducts = SnapApp.getDefault().getProductManager().getProducts();
    Product[] otherProducts = Arrays.stream(allProducts).filter(p -> p != product).toArray(Product[]::new);
    wvlEditor.addAdditionalProducts(otherProducts);
    wvlEditor.show();
  }

}
