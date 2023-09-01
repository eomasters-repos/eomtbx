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

public class ProductSelectionDialog extends ModalDialog {

  public static final String HID_EOMTBX_PRODUCT_SELECTION = "hid_eomtbx.productSelection";
  private final ProductTableModel listModel;

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
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    JTable productSelectionTable = new JTable(listModel);
    productSelectionTable.getColumnModel().getColumn(0).setMaxWidth(30);
    JScrollPane scrollPane = new JScrollPane(productSelectionTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setMinimumSize(new Dimension(100, 300));
    scrollPane.setPreferredSize(new Dimension(200, 350));
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
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if (columnIndex == 0) {
        products.get(rowIndex).setSelected((Boolean) aValue);
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

  public static class ProductSelection {

    private final Product product;

    private boolean selected;

    public ProductSelection(Product product, boolean selected) {
      this.product = product;
      this.selected = selected;
    }

    public Product getProduct() {
      return product;
    }

    public void setSelected(boolean selected) {
      this.selected = selected;
    }

    public boolean isSelected() {
      return selected;
    }
  }
}
