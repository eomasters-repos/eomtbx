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

package org.eomasters.eomtbx.spex.gui;

import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.eomtbx.spex.AbstractSpex;
import org.eomasters.eomtbx.spex.formula.Constant;
import org.eomasters.eomtbx.spex.CustomSpex;
import org.eomasters.eomtbx.spex.DbSpex;
import org.eomasters.eomtbx.spex.Domain;
import org.eomasters.eomtbx.spex.formula.SpexBand;
import org.eomasters.eomtbx.spex.SpexDb;
import org.eomasters.eomtbx.spex.gui.UserSpexEditorDialog.Mode;
import org.eomasters.gui.CollapsiblePanel;
import org.eomasters.gui.DropdownButton;
import org.eomasters.gui.Highlighter;
import org.eomasters.gui.OrderedFocusTraversalPolicy;
import org.eomasters.gui.PopupComponent;
import org.eomasters.gui.UriField;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;

public class SpexEditorPanel extends JPanel {

  private final JTextField nameField;
  private final JTextField descriptionField;
  private final JTextArea formulaField;
  private final UriField referenceUrlEditField;
  private final JTable symbolTable;
  private final JComboBox<Domain> domainBox;
  private Mode mode;

  public SpexEditorPanel() {
    boolean editable = true;
    mode = Mode.EDIT;
    setLayout(new MigLayout("fillx, top, left, gap 4"));
    JTextField textFieldTemplate = new JTextField("");
    textFieldTemplate.setFont(textFieldTemplate.getFont().deriveFont(Font.BOLD));
    nameField = new JTextField("");
    nameField.setFont(nameField.getFont().deriveFont(Font.BOLD));
    nameField.setToolTipText("The name of the spectral index");
    Collection<AbstractSpex> indices = SpexDb.getInstance().getIndices();
    String[] nameList = indices.stream()
                               .map(AbstractSpex::getName)
                               .sorted()
                               .collect(Collectors.toList())
                               .toArray(String[]::new);
    DropdownButton dropdownButton = new DropdownButton(EomtbxIcons.SPEX_DB.getImageIcon(Icon.SIZE_16));
    dropdownButton.setToolTipText("Select an index to prefill the dialog");
    JList<String> spexNameList = new JList<>(nameList);
    spexNameList.setVisibleRowCount(10);
    spexNameList.setToolTipText("Click to select an index");
    dropdownButton.setPopupComponent(new JScrollPane(spexNameList));
    spexNameList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent evt) {
          String selectedValue = spexNameList.getSelectedValue();
          AbstractSpex index = SpexDb.getInstance().get(selectedValue);
          SpexEditorPanel.this.updateFields(index);
          dropdownButton.showPopup(false);
        }
    });
    formulaField = new JTextArea("");
    formulaField.setBackground(textFieldTemplate.getBackground());
    formulaField.setForeground(textFieldTemplate.getForeground());
    formulaField.setBorder(textFieldTemplate.getBorder());
    formulaField.setLineWrap(true);
    formulaField.setColumns(25);
    formulaField.addKeyListener(new TransferFocusOnTabKeyAdapter());
    formulaField.setToolTipText("The formula of the spectral index");
    // noinspection unchecked
    domainBox = new JComboBox<Domain>(new EnumComboBoxModel<Domain>(Domain.class));
    domainBox.setEnabled(true);
    domainBox.setEditable(false);
    domainBox.setSelectedIndex(-1);
    domainBox.setToolTipText("The application domain of the spectral index");

    JLabel infoButton = new JLabel(Icons.INFO.getImageIcon(Icon.SIZE_16));
    infoButton.setSize(20, 20);
    infoButton.setToolTipText("Show the symbols used in the formula");
    PopupComponent popupComponent = new PopupComponent(new SymbolHelpPanel());
    popupComponent.installTo(infoButton);

    symbolTable = new JTable();
    symbolTable.setTableHeader(null);
    symbolTable.setToolTipText("The symbols used in the formula");
    JLabel tableCellRenderer = (JLabel) symbolTable.getDefaultRenderer(String.class);
    tableCellRenderer.setBackground(textFieldTemplate.getBackground());
    tableCellRenderer.setForeground(textFieldTemplate.getForeground());
    tableCellRenderer.setBorder(textFieldTemplate.getBorder());
    descriptionField = new JTextField("");
    descriptionField.setEditable(editable);
    descriptionField.setToolTipText("A description of the spectral index");
    referenceUrlEditField = new UriField();
    referenceUrlEditField.setEditable(editable);
    referenceUrlEditField.setToolTipText("The link to a reference for the spectral index");

    add(new JLabel("Name: "));
    add(nameField, String.format("%s%s", "pushx, growx", !editable ? ", wrap" : ", split 2"));
    add(dropdownButton, "wrap");

    add(new JLabel("Formula: "));
    add(formulaField, String.format("%s%s", "pushx, growx, wmin 10", !editable ? ", wrap" : ", split 2"));
    add(infoButton, "wrap");
    add(new JLabel("Domain: "));
    add(domainBox, "pushx, growx, wrap");

    JPanel additionalPanel = new JPanel(new MigLayout("fillx, top, left"));

    additionalPanel.add(new JLabel("Description: "));
    additionalPanel.add(descriptionField, "pushx, growx, wrap");
    additionalPanel.add(new JLabel("Reference: "));
    additionalPanel.add(referenceUrlEditField, "pushx, growx, wrap");

    CollapsiblePanel collapsible = new CollapsiblePanel("Additional Attributes");
    collapsible.setContent(additionalPanel);
    add(collapsible, "pushx, growx, wrap, spanx 2");

    setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(List.of(nameField, formulaField, domainBox,
        descriptionField, referenceUrlEditField)));
    updateFields(null);
  }

  public void display(DbSpex index) {
    updateFields(index);
  }

  private void updateFields(AbstractSpex selectedIndex) {
    if (selectedIndex == null) {
      if (nameField != null) {
        nameField.setText("");
      }
      formulaField.setText("");
      if (domainBox != null) {
        domainBox.setSelectedItem(Domain.Undefined);
      }
      descriptionField.setText("");
      symbolTable.setModel(createSymbolTableModel(null));
      referenceUrlEditField.setUri(null);
    } else {
      if (nameField != null) {
        nameField.setText(selectedIndex.getName());
      }
      formulaField.setText(selectedIndex.getFormula());
      if (domainBox != null) {
        domainBox.setSelectedItem(selectedIndex.getDomain());
      }
      descriptionField.setText(selectedIndex.getDescription());
      symbolTable.setModel(createSymbolTableModel(selectedIndex));
      symbolTable.getColumnModel().getColumn(0).setPreferredWidth(60);
      symbolTable.getColumnModel().getColumn(0).setMaxWidth(60);
      referenceUrlEditField.setUri(selectedIndex.getReference());
    }
  }

  private static DefaultTableModel createSymbolTableModel(AbstractSpex selectedIndex) {
    Vector<Vector<String>> rows = new Vector<>();
    if (selectedIndex != null) {
      List<SpexBand> bands = SpexBand.getBandsUsedInFormula(selectedIndex.getFormula());
      for (SpexBand spexBand : bands) {
        Vector<String> columns = new Vector<>();
        columns.add(spexBand.name());
        columns.add(String.format("%s (%d, %d)", spexBand.getLongName(), spexBand.getMinWavelength(),
            spexBand.getMaxWavelength()));
        rows.add(columns);
      }
      String formula = selectedIndex.getFormula();
      List<Constant> constants = Constant.getConstantsUsedInFormula(formula);
      for (Constant constant : constants) {
        Vector<String> columns = new Vector<>();
        columns.add(constant.name());
        columns.add(String.format("%f (%s)", constant.getValue(), constant.getDescription()));
        rows.add(columns);
      }
    } else {
      Vector<String> cols = new Vector<>();
      cols.add("");
      cols.add("");
      rows.add(cols);
    }
    Vector<Object> columnNames = new Vector<>();
    columnNames.add("Symbol");
    columnNames.add("Description");
    return new DefaultTableModel(rows, columnNames);
  }

  public boolean validateInput() {
    // name should not be empty
    String name = nameField.getText();
    if (name.isBlank()) {
      Highlighter.error(nameField, "Name should not be empty");
      return false;
    }
    if(mode == Mode.ADD) {
      if (SpexDb.getInstance().get(name) != null) {
        Highlighter.error(nameField, "Name already exists");
        return false;
      }
    }
    // formula should not be empty
    String formula = formulaField.getText();
    if (formula.isBlank()) {
      Highlighter.error(formulaField, "Formula should not be empty");
      return false;
    }

    return true;
  }

  public AbstractSpex getSpex() {
    CustomSpex index = new CustomSpex();
    String name = nameField.getText();
    index.setName(name);
    index.setFormula(formulaField.getText());
    index.setDomain((Domain) domainBox.getSelectedItem());
    index.setDescription(descriptionField.getText());
    index.setReference(referenceUrlEditField.getUri());
    return index;
  }

  public void setSpex(AbstractSpex spex) {
    nameField.setText(spex.getName());
    if (domainBox != null) {
      domainBox.setSelectedItem(spex.getDomain());
    }
    formulaField.setText(spex.getFormula());
    descriptionField.setText(spex.getDescription());
    referenceUrlEditField.setUri(spex.getReference());
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  private static class TransferFocusOnTabKeyAdapter extends KeyAdapter {

    @Override
    public void keyTyped(KeyEvent e) {
      if (e.getKeyChar() == KeyEvent.VK_TAB) {
        e.getComponent().transferFocus();
        e.consume();
      }
    }

    @Override
    public void keyPressed(KeyEvent e) {
      if (e.getKeyChar() == KeyEvent.VK_TAB) {
        e.consume();
      }
    }

    @Override
    public void keyReleased(KeyEvent e) {
      if (e.getKeyChar() == KeyEvent.VK_TAB) {
        e.consume();
      }
    }
  }

}
