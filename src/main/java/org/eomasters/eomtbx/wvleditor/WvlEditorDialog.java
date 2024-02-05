/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2024 Marco Peters
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
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
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.gui.MultiLineText;
import org.eomasters.gui.NumberCellEditor;
import org.eomasters.gui.TablePasteAdapter;
import org.eomasters.icons.Icon.SIZE;
import org.eomasters.snap.gui.ProductSelectionDialog;
import org.eomasters.snap.gui.ProductSelectionDialog.ProductSelection;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.IndexCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.ui.ModalDialog;

class WvlEditorDialog extends ModalDialog {

  public static final String HID_EOMTBX_WvlEditor = "eomtbxWvlEditor";
  private final Product refProduct;
  private final List<ProductSelection> compatibleProducts = new ArrayList<>();
  private final WvlEditorTableModel tableModel;


  /**
   * Creates a new WvlEditorDialog.
   *
   * @param parent    the parent frame
   * @param reference the reference product
   */
  public WvlEditorDialog(Frame parent, Product reference) {
    super(parent, "Wavelength Editor", ModalDialog.ID_OK_CANCEL_HELP, HID_EOMTBX_WvlEditor);
    refProduct = reference;
    tableModel = new WvlEditorTableModel(refProduct);
  }

  private JPanel createContentPanel() {
    JPanel headerPanel = new JPanel(new MigLayout("top, left, gap 5", "[][fill, grow]"));
    headerPanel.add(new JLabel("Product:"));
    JTextField productNameField = new JTextField();
    productNameField.setEditable(false);
    productNameField.setColumns(60);
    productNameField.setText(refProduct.getDisplayName());
    productNameField.setToolTipText(refProduct.getDisplayName());
    headerPanel.add(productNameField);

    JTable table = createTable();
    table.requestFocusInWindow();
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setPreferredSize(new Dimension(300, 400));

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(headerPanel, BorderLayout.NORTH);
    panel.add(scrollPane, BorderLayout.CENTER);

    if (!compatibleProducts.isEmpty()) {
      JPanel footerPanel = new JPanel(new MigLayout("fillx,  bottom"));
      JLabel note = new JLabel(
          "<html><b>Note:</b> Wavelength properties are only editable for bands without sample coding.");
      note.setFont(note.getFont().deriveFont(Font.PLAIN));
      footerPanel.add(note, "span 2, growx, gapbottom 10, left, wrap");

      MultiLineText textField = new MultiLineText("There are " + compatibleProducts.size()
          + " other compatible products opened. Shall the changes be applied to those too?");
      textField.setPreferredWidth(300);
      footerPanel.add(textField, "wmin 10, growx");
      JButton productSelectionBtn = new JButton(createButtonText(compatibleProducts));
      productSelectionBtn.addActionListener(e -> {
        ProductSelectionDialog selectionDialog = new ProductSelectionDialog(getParent(), compatibleProducts, HID_EOMTBX_WvlEditor);
        selectionDialog.getJDialog().setIconImage(EomtbxIcons.WVL_EDITOR.getImageIcon(SIZE.S16).getImage());
        selectionDialog.show();
        productSelectionBtn.setText(createButtonText(compatibleProducts));
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
    JTable table = new JTable(tableModel);
    table.addKeyListener(new TablePasteAdapter(table));
    table.setCellSelectionEnabled(true);
    table.setFillsViewportHeight(true);
    table.setFont(table.getFont().deriveFont(table.getFont().getSize2D() * 1.1f));
    TableColumnModel columnModel = table.getColumnModel();
    TableCellRenderer renderer = new DisabledCellRenderer();
    for (int i = 0; i < table.getColumnCount(); i++) {
      if (i == 0) {
        columnModel.getColumn(i).setPreferredWidth(120); // first column is bigger
      } else {
        columnModel.getColumn(i).setCellRenderer(renderer);
        columnModel.getColumn(i).setPreferredWidth(40);
        // noinspection unchecked
        NumberCellEditor cellEditor = new NumberCellEditor((Class<? extends Number>) tableModel.getColumnClass(i));
        columnModel.getColumn(i).setCellEditor(cellEditor);
      }
    }
    return table;
  }

  @Override
  public int show() {
    setContent(createContentPanel());
    getJDialog().setIconImage(EomtbxIcons.WVL_EDITOR.getImageIcon(SIZE.S16).getImage());
    return super.show();
  }

  @Override
  protected void onOK() {
    List<ProductSelection> applyToProduct = new ArrayList<>();
    Collections.copy(compatibleProducts, applyToProduct);
    applyToProduct.add(0, new ProductSelection(refProduct, true));
    int rowCount = tableModel.getRowCount();

    for (ProductSelection productSelection : applyToProduct) {
      if (!productSelection.isSelected()) {
        continue;
      }
      Product product = productSelection.getProduct();
      for (int i = 0; i < rowCount; i++) {
        if (!tableModel.isRowEditable(i)) {
          continue;
        }
        String bandName = (String) tableModel.getValueAt(i, 0);
        int spectralIndex = (int) tableModel.getValueAt(i, 1);
        float wavelength = (float) tableModel.getValueAt(i, 2);
        float bandwidth = (float) tableModel.getValueAt(i, 3);
        product.getBand(bandName).setSpectralBandIndex(spectralIndex);
        product.getBand(bandName).setSpectralWavelength(wavelength);
        product.getBand(bandName).setSpectralBandwidth(bandwidth);
      }
    }
    super.onOK();
  }

  public void addAdditionalProducts(Product... additional) {
    String[] refBandNames = refProduct.getBandNames();
    Stream.of(additional).filter(p1 -> isCompatible(p1, refBandNames))
        .forEach(p -> compatibleProducts.add(new ProductSelection(p, false)));
  }

  private boolean isCompatible(Product p, String[] refBandNames) {
    String[] bandNames = p.getBandNames();
    if (refBandNames.length != bandNames.length) {
      return false;
    }
    for (int i = 0; i < refBandNames.length; i++) {
      if (!refBandNames[i].equals(bandNames[i])) {
        return false;
      }
    }
    return true;
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

  private static class DisabledCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int column) {
      Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
          column);
      rendererComponent.setEnabled(table.isCellEditable(row, column));
      if (rendererComponent instanceof JLabel) {
        ((JLabel) rendererComponent).setHorizontalAlignment(JLabel.RIGHT);
      }
      return rendererComponent;
    }
  }

}
