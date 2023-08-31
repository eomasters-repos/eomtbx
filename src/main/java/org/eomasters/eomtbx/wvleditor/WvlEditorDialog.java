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

import java.awt.BorderLayout;
import java.awt.Frame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.IndexCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.ui.ModalDialog;

class WvlEditorDialog extends ModalDialog {

  public static final String HID_EOMTBX_WvlEditor = "hid_eomtbx.WvlEditor";
  private Product refProduct;
  private Product[] otherProducts;

  /**
   * Creates a new WvlEditorDialog.
   *
   * @param parent the parent frame
   */
  public WvlEditorDialog(Frame parent) {
    super(parent, "Wavelength Editor", ModalDialog.ID_OK_CANCEL_HELP, HID_EOMTBX_WvlEditor);
  }

  private JPanel createContentPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    JPanel headerPanel = new JPanel(new MigLayout("top, left, gap 5", "[][fill, grow]"));
    headerPanel.add(new JLabel("Product:"));
    JTextField productNameField = new JTextField(refProduct.getDisplayName());
    productNameField.setEditable(false);
    headerPanel.add(productNameField);
    JScrollPane scrollPane = new JScrollPane(new JTable(new DefaultTableModel(refProduct.getNumBands(), 4)));
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setPreferredSize(new java.awt.Dimension(400, 500));
    JPanel footerPanel = new JPanel();
    panel.add(headerPanel, BorderLayout.NORTH);
    panel.add(scrollPane, BorderLayout.CENTER);
    panel.add(footerPanel, BorderLayout.SOUTH);
    return panel;
  }

  @Override
  public int show() {
    setContent(createContentPanel());
    return super.show();
  }

  @Override
  protected boolean verifyUserInput() {
    return super.verifyUserInput();
  }

  @Override
  protected void onOK() {
    super.onOK();
  }

  public void setProducts(Product reference, Product[] other) {
    refProduct = reference;
    otherProducts = other;
  }

  public static void main(String[] args) {
    WvlEditorDialog wvlEditor = new WvlEditorDialog(null);
    Product reference = new Product("Test_Product_20230831", "type", 10, 10);
    reference.addBand("B1", "1");
    reference.addBand("B2", "2");
    reference.addBand("B3", "3");
    reference.addBand("B4", "4");
    Band flags = reference.addBand("flags", "5", ProductData.TYPE_INT8);
    flags.setSampleCoding(new FlagCoding("flags"));
    Band classes = reference.addBand("classes", "11", ProductData.TYPE_INT16);
    classes.setSampleCoding(new IndexCoding("classes"));

    wvlEditor.setProducts(reference, null);
    wvlEditor.show();
  }
}
