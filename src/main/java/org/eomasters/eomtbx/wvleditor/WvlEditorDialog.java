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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.utils.MultiLineText;
import org.eomasters.eomtbx.utils.ProductSelectionDialog.ProductSelection;
import org.eomasters.eomtbx.utils.ProductSelectionDialog;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.IndexCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.ui.ModalDialog;

class WvlEditorDialog extends ModalDialog {

  public static final String HID_EOMTBX_WvlEditor = "hid_eomtbx.WvlEditor";
  private final Product refProduct;
  private final List<ProductSelection> additionalProducts = new ArrayList<>();


  /**
   * Creates a new WvlEditorDialog.
   *
   * @param parent    the parent frame
   * @param reference the reference product
   */
  public WvlEditorDialog(Frame parent, Product reference) {
    super(parent, "Wavelength Editor", ModalDialog.ID_OK_CANCEL_HELP, HID_EOMTBX_WvlEditor);
    refProduct = reference;
  }

  private JPanel createContentPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    JPanel headerPanel = new JPanel(new MigLayout("top, left, gap 5", "[][fill, grow]"));
    headerPanel.add(new JLabel("Product:"));
    JTextField productNameField = new JTextField(refProduct.getDisplayName());
    productNameField.setEditable(false);
    headerPanel.add(productNameField);

    JTable table = createTable();
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setPreferredSize(new Dimension(300, 400));

    panel.add(headerPanel, BorderLayout.NORTH);
    panel.add(scrollPane, BorderLayout.CENTER);

    if (!additionalProducts.isEmpty()) {
      JPanel footerPanel = new JPanel(new MigLayout("fillx,  bottom"));
      JLabel note = new JLabel("<html><b>Note:</b> Wavelength properties are only editable for bands without sample coding.");
      note.setFont(note.getFont().deriveFont(Font.PLAIN));
      footerPanel.add(note, "span 2, growx, gapbottom 10, left, wrap");

      MultiLineText textField = new MultiLineText("There are " + additionalProducts.size()
          + " other compatible products opened. Shall the changes be applied to those too?");
      textField.setPreferredWidth(300);
      footerPanel.add(textField, "wmin 10, growx");
      JButton productSelectionBtn = new JButton(createButtonText(additionalProducts));
      productSelectionBtn.addActionListener(e -> {
        ProductSelectionDialog selectionDialog = new ProductSelectionDialog(getParent(), additionalProducts);
        selectionDialog.show();
        productSelectionBtn.setText(createButtonText(additionalProducts));
      });
      footerPanel.add(productSelectionBtn, "right");
      panel.add(footerPanel, BorderLayout.SOUTH);
    }
    return panel;
  }

  private String createButtonText(List<ProductSelection> additionalProducts) {
    long numSelectedProducts = additionalProducts.stream().filter(ProductSelection::isSelected).count();
    return "Selected [" + numSelectedProducts + " of " + additionalProducts.size() + "]";
  }

  private JTable createTable() {
    TableModel tableModel = new WvlEditorTableModel(refProduct);
    JTable table = new JTable(tableModel);
    table.setFillsViewportHeight(true);
    TableColumnModel columnModel = table.getColumnModel();
    TableCellRenderer renderer = new DisablingCellRenderer();
    for (int i = 0; i < table.getColumnCount(); i++) {
      if (i == 0) {
        columnModel.getColumn(i).setPreferredWidth(150); // first column is bigger
      } else {
        columnModel.getColumn(i).setCellRenderer(renderer);
        columnModel.getColumn(i).setPreferredWidth(50);
      }
    }
    return table;
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

  public void addAdditionalProducts(Product... additional) {
    for (Product product : additional) {
      additionalProducts.add(new ProductSelection(product, false));
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      Product reference = createCompatibleProduct("");
      WvlEditorDialog wvlEditor = new WvlEditorDialog(null, reference);
      wvlEditor.addAdditionalProducts(createCompatibleProduct("a"), createCompatibleProduct("b"),
          createCompatibleProduct("c"));
      wvlEditor.show();
      System.exit(0);
    });
  }

  private static Product createCompatibleProduct(String id) {
    Product reference = new Product("Test_Product_20230831_" + id, "type", 10, 10);
    reference.addBand("B1", "1");
    reference.addBand("B2", "2");
    reference.addBand("B3", "3");
    reference.addBand("B4", "4");
    Band flags = reference.addBand("flags", "5", ProductData.TYPE_INT8);
    flags.setSampleCoding(new FlagCoding("flags"));
    Band classes = reference.addBand("classes", "11", ProductData.TYPE_INT16);
    classes.setSampleCoding(new IndexCoding("classes"));
    return reference;
  }

  private static class DisablingCellRenderer implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int column) {
      Component renderer = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, isSelected,
          hasFocus, row, column);
      boolean cellEditable = table.isCellEditable(row, column);
      renderer.setEnabled(cellEditable);
      if (renderer instanceof JLabel) {
        ((JLabel) renderer).setHorizontalAlignment(JLabel.RIGHT);
      }
      return renderer;
    }
  }

}
