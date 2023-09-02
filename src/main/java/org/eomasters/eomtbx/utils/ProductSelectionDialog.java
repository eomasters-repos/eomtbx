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

package org.eomasters.eomtbx.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;
import net.miginfocom.swing.MigLayout;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.ui.ModalDialog;

/**
 * A dialog for selecting products.
 */
public class ProductSelectionDialog extends ModalDialog {

  private static final String HID_EOMTBX_PRODUCT_SELECTION = "hid_eomtbx.productSelection";
  private final ProductTableModel listModel;

  /**
   * Creates a new dialog for selecting products.
   *
   * @param parent   the parent window
   * @param products the products to select from
   */
  public ProductSelectionDialog(Window parent, List<ProductSelection> products) {
    super(parent, "Product Selection", ModalDialog.ID_OK_CANCEL, HID_EOMTBX_PRODUCT_SELECTION);
    listModel = new ProductTableModel(products);
  }

  @Override
  public int show() {
    setContent(createContentPanel());
    return super.show();
  }

  private JPanel createContentPanel() {
    JTable productSelectionTable = new JTable(listModel);
    productSelectionTable.setShowVerticalLines(false);
    productSelectionTable.getColumnModel().getColumn(0).setMaxWidth(30);
    JScrollPane scrollPane = new JScrollPane(productSelectionTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setMinimumSize(new Dimension(100, 200));
    scrollPane.setPreferredSize(new Dimension(200, 250));

    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(scrollPane, BorderLayout.CENTER);
    JButton selAllBox = new JButton("Select all");
    selAllBox.addActionListener(e -> {
      for (int i = 0; i < listModel.getRowCount(); i++) {
        listModel.products.get(i).setSelected(true);
      }
      listModel.fireTableDataChanged();
    });
    panel.add(selAllBox, BorderLayout.SOUTH);
    JButton invertBox = new JButton("Invert selection");
    invertBox.addActionListener(e -> {
      for (int i = 0; i < listModel.getRowCount(); i++) {
        listModel.products.get(i).setSelected(!listModel.products.get(i).isSelected());
      }
      listModel.fireTableDataChanged();
    });
    JPanel checkBoxPanel = new JPanel(new MigLayout("top, left, flowx, gap 5"));
    checkBoxPanel.add(selAllBox);
    checkBoxPanel.add(invertBox);
    panel.add(checkBoxPanel, BorderLayout.SOUTH);
    return panel;
  }

  private static class ProductTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"Selection", "Product Name"};
    private static final Class<?>[] COLUMN_CLASSES = {Boolean.class, String.class};


    private final List<ProductSelection> products;

    public ProductTableModel(List<ProductSelection> products) {
      this.products = products;
    }

    @Override
    public int getRowCount() {
      return products.size();
    }

    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public String getColumnName(int column) {
      return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return COLUMN_CLASSES[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return columnIndex == 0;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
      if (columnIndex == 0) {
        products.get(rowIndex).setSelected((Boolean) value);
        fireTableCellUpdated(rowIndex, columnIndex);
      }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      ProductSelection productSelection = products.get(rowIndex);
      switch (columnIndex) {
        case 0:
          return productSelection.selected;
        case 1:
          return productSelection.product.getDisplayName();
        default:
          throw new IllegalArgumentException("Invalid column index: " + columnIndex);
      }
    }

  }

  /**
   * A class representing a product selection.
   */
  public static class ProductSelection {

    private final Product product;

    private boolean selected;

    /**
     * Creates a new product selection.
     *
     * @param product  the product
     * @param selected whether the product is selected
     */
    public ProductSelection(Product product, boolean selected) {
      this.product = product;
      this.selected = selected;
    }

    /**
     * Returns the product.
     *
     * @return the product
     */
    public Product getProduct() {
      return product;
    }

    /**
     * Sets whether the product is selected.
     *
     * @param selected whether the product is selected
     */
    public void setSelected(boolean selected) {
      this.selected = selected;
    }

    /**
     * Returns whether the product is selected.
     *
     * @return whether the product is selected
     */
    public boolean isSelected() {
      return selected;
    }
  }
}
