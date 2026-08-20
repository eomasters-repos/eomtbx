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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.eomasters.eomtbx.spex.CustomSpex;
import org.eomasters.utils.TextUtils;
import org.esa.snap.core.util.converters.JtsGeometryConverter;

class CostumSpexTableModel extends AbstractTableModel {

  private static final String[] COLUMN_NAMES = {"Name", "Formula", "Masks"};
  private static final Class<?>[] COLUMN_CLASSES = {String.class, String.class, String[].class};
  private static final int DEFAULT_ROW_COUNTS = 8;
  private final List<CustomSpex> spexList;
  private final JtsGeometryConverter wktConverter;

  public CostumSpexTableModel() {
    spexList = new ArrayList<>();
    wktConverter = new JtsGeometryConverter();
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return COLUMN_CLASSES[columnIndex];
  }

  public boolean contains(CustomSpex spex) {
    return spexList.stream().parallel().anyMatch(customSpex -> customSpex.getName().equalsIgnoreCase(spex.getName()));
  }

  public void add(CustomSpex spex) {
    boolean exists = spexList.stream()
                             .parallel()
                             .anyMatch(customSpex -> customSpex.getName().equalsIgnoreCase(spex.getName()));
    if (exists) {
      throw new IllegalArgumentException("A custom SpeX with this name already exists");
    }
    spexList.add(spex);
    int row = spexList.size() - 1;
    fireTableRowsUpdated(row, row);
  }

  public CustomSpex get(int index) {
    if (index < spexList.size()) {
      return spexList.get(index);
    }
    return null;
  }

  public CustomSpex[] getAll() {
    return spexList.toArray(new CustomSpex[0]);
  }

  public void set(CustomSpex spex, int spexIndex) {
    if (spexIndex >= spexList.size()) {
      add(spex);
    } else {
      spexList.set(spexIndex, spex);
      fireTableRowsUpdated(spexIndex, spexIndex);
    }

  }

  public void removeSpexAt(int index) {
    if (index < spexList.size()) {
      spexList.remove(index);
      fireTableRowsUpdated(index, spexList.size());
    }
  }

  @Override
  public String getColumnName(int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public int getRowCount() {
    return Math.max(DEFAULT_ROW_COUNTS, spexList.size());
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex >= spexList.size()) {
      return "";
    } else {
      CustomSpex spex = spexList.get(rowIndex);
      switch (columnIndex) {
        case 0:
          return spex.getName();
        case 1:
          return spex.getFormula();
        case 2:
          ArrayList<String> masks = new ArrayList<>();
          if (spex.getValidExpression() != null && !spex.getValidExpression().isBlank()) {
            String escapedExpression = TextUtils.escapeHtml(spex.getValidExpression());
            masks.add(String.format("<html><b>Valid Expression:</b> %s", escapedExpression));
          }
          if (spex.getShapefile() != null) {
            Path shapefile = spex.getShapefile();
            masks.add(String.format("<html><b>Shapefile shp:</b> %s", shapefile));
          }
          if (spex.getWktRegion() != null) {
            masks.add(String.format("<html><b>WKT Geometry:</b> %s", wktConverter.format(spex.getWktRegion())));
          }
          return masks.toArray(new String[0]);
        default:
          return "";
      }
    }
  }

}
