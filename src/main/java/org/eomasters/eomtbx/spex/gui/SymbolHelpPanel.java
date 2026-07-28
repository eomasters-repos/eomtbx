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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.spex.formula.Constant;
import org.eomasters.eomtbx.spex.formula.SpexBand;

class SymbolHelpPanel extends JPanel {

  public SymbolHelpPanel() {
    super(new MigLayout("top, left, fill, gap 4"));
    JLabel title = new JLabel("Valid Symbols in Formula");
    title.setFont(title.getFont().deriveFont(Font.BOLD).deriveFont(AffineTransform.getScaleInstance(1.3, 1.3)));
    add(title, "gapy 8, wrap");
    add(new JLabel("Bands"), "wrap");
    JTable bandsTable = new JTable(new SpexBandTableModel());
    formatTable(bandsTable, 60);
    JScrollPane bandsScrollPane = wrapInScrollPan(bandsTable);
    add(bandsScrollPane, "wrap");
    bandsScrollPane.setPreferredSize(new Dimension(bandsScrollPane.getPreferredSize().width, 200));

    add(new JLabel("Constants"), "wrap");
    JTable constantTable = new JTable(new ConstantTableModel());
    formatTable(constantTable, 60);
    JScrollPane constantsScrollPane = wrapInScrollPan(constantTable);
    add(constantsScrollPane, "wrap");
    constantsScrollPane.setPreferredSize(new Dimension(constantsScrollPane.getPreferredSize().width, 200));

    add(new JLabel("Functions"), "wrap");
    JTable functionTable = new JTable(new FunctionTableModel());
    formatTable(functionTable, 150);
    JScrollPane functionScrollPane = wrapInScrollPan(functionTable);
    add(functionScrollPane, "wrap");
    functionScrollPane.setPreferredSize(new Dimension(functionScrollPane.getPreferredSize().width, 50));
  }

  private static @Nonnull JScrollPane wrapInScrollPan(JTable bandsTable) {
    JScrollPane bandsScrollPane = new JScrollPane(bandsTable);
    bandsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    bandsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    return bandsScrollPane;
  }

  private static void formatTable(JTable bandsTable, int firstColumnWidth) {
    bandsTable.setDefaultRenderer(String.class, new FirstColumnBoldRenderer());
    bandsTable.setDefaultEditor(String.class, null);
    bandsTable.getColumnModel().getColumn(0).setPreferredWidth(firstColumnWidth);
    bandsTable.getColumnModel().getColumn(0).setMaxWidth(firstColumnWidth);
    bandsTable.setTableHeader(null);
  }

  private static class SpexBandTableModel extends AbstractTableModel {

    private final SpexBand[] spexBands;

    public SpexBandTableModel() {
      spexBands = SpexBand.values();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    @Override
    public int getRowCount() {
      return SpexBand.values().length;
    }

    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      SpexBand spexBand = spexBands[rowIndex];
      switch (columnIndex) {
        case 0:
          return spexBand.name();
        case 1:
          String wavelengths = "";
          Integer minWavelength = spexBand.getMinWavelength();
          Integer maxWavelength = spexBand.getMaxWavelength();
          if (minWavelength != null && maxWavelength != null) {
            wavelengths = String.format(" (%d, %d)", minWavelength, maxWavelength);
          }
          return spexBand.getLongName() + wavelengths;
      }
      return "";
    }
  }

  private static class ConstantTableModel extends AbstractTableModel {

    private final Constant[] constants;

    public ConstantTableModel() {
      constants = Constant.values();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    @Override
    public int getRowCount() {
      return constants.length;
    }

    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      Constant constant = constants[rowIndex];
      switch (columnIndex) {
        case 0:
          return constant.name();
        case 1:
          return String.format("%s - %s", constant.getValue(), constant.getDescription());
      }
      return "";
    }
  }

  private static class FunctionTableModel extends AbstractTableModel {

    private final List<Entry<String, String>> functions = new ArrayList<>();

    public FunctionTableModel() {
      functions.add(Map.entry("bnd(minWvl:maxWvl)", "Band closest to the center of the wavelength range"));
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    @Override
    public int getRowCount() {
      return functions.size();
    }

    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      var entry = functions.get(rowIndex);
      switch (columnIndex) {
        case 0:
          return entry.getKey();
        case 1:
          return entry.getValue();
      }
      return "";
    }
  }

  private static class FirstColumnBoldRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int column) {
      JLabel component = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
          row, column);
      Font font = column == 0 ? new JLabel().getFont().deriveFont(Font.BOLD) : new JLabel().getFont().deriveFont(Font.PLAIN);
      component.setFont(font);
      return component;
    }
  }
}
