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
import org.eomasters.eomtbx.spex.DbSpex;
import org.eomasters.eomtbx.spex.Platform;
import org.eomasters.eomtbx.spex.formula.SpexBand;
import org.eomasters.eomtbx.spex.SpexDb;
import org.eomasters.gui.CollapsiblePanel;
import org.eomasters.gui.DropdownButton;
import org.eomasters.gui.MultiLineText;
import org.eomasters.gui.PopupComponent;
import org.eomasters.gui.UriField;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;

public class SpexInfoPanel extends JPanel {

  private final JTextField nameField;
  private final MultiLineText descriptionField;
  private final MultiLineText deprecationField;
  private final JTextArea formulaField;
  private final UriField referenceUrlEditField;
  private final JTextArea platformsField;
  private final UriField sourceField;
  private final JTable symbolTable;
  private final JTextField domainField;

  public SpexInfoPanel() {
    JTextField textFieldTemplate = new JTextField("");
    textFieldTemplate.setFont(textFieldTemplate.getFont().deriveFont(Font.BOLD));
    textFieldTemplate.setEditable(false);
    nameField = new JTextField("");
    nameField.setFont(nameField.getFont().deriveFont(Font.BOLD));
    nameField.setEditable(false);
    nameField.setToolTipText("The name of the spectral index");
    Collection<AbstractSpex> indices = SpexDb.getInstance().getIndices();
    String[] nameList = indices.stream()
                               .map(AbstractSpex::getName)
                               .sorted()
                               .collect(Collectors.toList())
                               .toArray(String[]::new);
    DropdownButton dropdownButton = new DropdownButton(EomtbxIcons.SPEX_DB.getImageIcon(Icon.SIZE_16));
    JList<String> spexNameList = new JList<>(nameList);
    spexNameList.setVisibleRowCount(10);

    dropdownButton.setPopupComponent(new JScrollPane(spexNameList));
    spexNameList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getClickCount() == 2) {
          String selectedValue = spexNameList.getSelectedValue();
          AbstractSpex index = SpexDb.getInstance().get(selectedValue);
          SpexInfoPanel.this.updateFields(index);
          dropdownButton.showPopup(false);
        }
      }
    });

    formulaField = new MultiLineText("");
    formulaField.setEditable(false);
    formulaField.setColumns(25);
    formulaField.addKeyListener(new TransferFocusOnTabKeyAdapter());
    formulaField.setToolTipText("The formula of the spectral index");
    domainField = new JTextField("");
    domainField.setEditable(false);
    domainField.setToolTipText("The application domain of the spectral index");

    JLabel infoButton = new JLabel(Icons.INFO.getImageIcon(Icon.SIZE_16));
    infoButton.setSize(20, 20);
    infoButton.setToolTipText("Show the symbols used in the formula");
    PopupComponent popupComponent = new PopupComponent(new SymbolHelpPanel());
    popupComponent.installTo(infoButton);

    symbolTable = new JTable();
    symbolTable.setTableHeader(null);
    symbolTable.setBorder(textFieldTemplate.getBorder());
    symbolTable.setRowSelectionAllowed(false);
    symbolTable.setCellSelectionEnabled(false);
    symbolTable.setModel(createSymbolTableModel(null));
    symbolTable.setToolTipText("The symbols used in the formula");
    JLabel tableCellRenderer = (JLabel) symbolTable.getDefaultRenderer(String.class);
    tableCellRenderer.setBackground(textFieldTemplate.getBackground());
    tableCellRenderer.setForeground(textFieldTemplate.getForeground());
    tableCellRenderer.setBorder(textFieldTemplate.getBorder());
    descriptionField = new MultiLineText("");
    descriptionField.setColumns(25);
    descriptionField.setEditable(false);
    descriptionField.setToolTipText("A description of the spectral index");
    deprecationField = new MultiLineText("");
    deprecationField.setColumns(25);
    deprecationField.setEditable(false);
    deprecationField.setToolTipText("Deprecation and possible alternative");
    referenceUrlEditField = new UriField();
    referenceUrlEditField.setEditable(false);
    referenceUrlEditField.setToolTipText("The link to a reference for the spectral index");
    platformsField = new MultiLineText("");
    platformsField.setEditable(false);
    platformsField.setColumns(25);
    platformsField.addKeyListener(new TransferFocusOnTabKeyAdapter());
    platformsField.setToolTipText("List of platforms this index can be applied on");
    sourceField = new UriField();
    sourceField.setEditable(false);
    sourceField.setToolTipText("The source where the index has been taken from");


    JPanel mainInfoPanel = new JPanel(new MigLayout("top, left, gap 4, insets 0", "[grow 0][fill]"));
    mainInfoPanel.add(new JLabel("Name: "));
    mainInfoPanel.add(nameField, "pushx, growx, wrap");
    mainInfoPanel.add(new JLabel("Formula: "));
    mainInfoPanel.add(formulaField, "pushx, growx, wmin 10, wrap");
    mainInfoPanel.add(new JLabel("Domain: "));
    mainInfoPanel.add(domainField, "pushx, growx, wrap");

    JPanel collapsibleContent = new JPanel(new MigLayout("top, left, gap 4, insets 0", "[grow 0][fill]"));

    collapsibleContent.add(new JLabel("Symbols: "));
    collapsibleContent.add(symbolTable, "pushx, growx, wrap");
    collapsibleContent.add(new JLabel("Description: "));
    collapsibleContent.add(descriptionField, "pushx, growx, wmin 10, wrap");
    collapsibleContent.add(new JLabel("Deprecation: "));
    collapsibleContent.add(deprecationField, "pushx, growx, wmin 10, wrap");
    collapsibleContent.add(new JLabel("Reference: "));
    collapsibleContent.add(referenceUrlEditField, "pushx, growx, wrap");
    collapsibleContent.add(new JLabel("Platforms: "));
    collapsibleContent.add(platformsField, "pushx, growx, wmin 10, wrap");
    collapsibleContent.add(new JLabel("Source: "));
    collapsibleContent.add(sourceField, "pushx, growx, wrap");
    CollapsiblePanel collapsible = new CollapsiblePanel("Additional Attributes");
    collapsible.setContent(collapsibleContent);

    setLayout(new MigLayout("top, left, fillx, insets 0"));

    add(mainInfoPanel, "growx, wrap");
    add(collapsible, "growx, wrap");

    updateFields(null);
  }

  public void display(DbSpex index) {
    updateFields(index);
  }

  private void updateFields(AbstractSpex selectedIndex) {
    if (selectedIndex == null) {
      nameField.setText("");
      formulaField.setText("");
      domainField.setText("");
      descriptionField.setText("");
      deprecationField.setText("");
      symbolTable.setModel(createSymbolTableModel(null));
      referenceUrlEditField.setUri(null);
      platformsField.setText("");
      sourceField.setUri(null);
    } else {
      nameField.setText(selectedIndex.getName());
      formulaField.setText(selectedIndex.getFormula());
      domainField.setText(selectedIndex.getDomain().name());
      descriptionField.setText(selectedIndex.getDescription());
      deprecationField.setText(selectedIndex.getDeprecation());
      symbolTable.setModel(createSymbolTableModel(selectedIndex));
      symbolTable.getColumnModel().getColumn(0).setPreferredWidth(60);
      symbolTable.getColumnModel().getColumn(0).setMaxWidth(60);
      referenceUrlEditField.setUri(selectedIndex.getReference());
      platformsField.setText(getPlatformsText(selectedIndex));
      String sourceUrl = selectedIndex.getSourceUrl();
      if (sourceUrl != null) {
        sourceField.setUri(sourceUrl, selectedIndex.getSourceName());
      }
    }
  }

  private static String getPlatformsText(AbstractSpex spex) {
    List<Platform> platforms = spex.getSupportedPlatforms();
    return platforms.stream().map(Platform::getName).collect(Collectors.joining(", "));
  }


  private static DefaultTableModel createSymbolTableModel(AbstractSpex selectedIndex) {
    Vector<Vector<String>> rows = new Vector<>();
    if (selectedIndex != null) {
      List<SpexBand> bands = SpexBand.getBandsUsedInFormula(selectedIndex.getFormula());
      for (SpexBand spexBand : bands) {
        Vector<String> columns = new Vector<>();
        columns.add(spexBand.name());
        Integer minWavelength = spexBand.getMinWavelength();
        Integer maxWavelength = spexBand.getMaxWavelength();
        String wavelengths = "";
        if (minWavelength != null && maxWavelength != null) {
          wavelengths = String.format(" (%d, %d)", minWavelength, maxWavelength);
        }
        columns.add(spexBand.getLongName() + wavelengths);
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
    return new DefaultTableModel(rows, columnNames) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
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
