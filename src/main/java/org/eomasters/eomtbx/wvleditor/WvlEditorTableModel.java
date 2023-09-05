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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.table.AbstractTableModel;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;

class WvlEditorTableModel extends AbstractTableModel {

  private static final String[] COLUMN_NAMES = {"Band", "Spectral Band Index", "Wavelength", "Bandwidth"};
  private static final Class<?>[] COLUMN_CLASSES = {String.class, Integer.class, Float.class, Float.class};

  private final Object[][] data;
  private final boolean[] editable;

  public WvlEditorTableModel(Product refProduct) {
    String[] bandNames;
    if (refProduct.getAutoGrouping() != null) {
      bandNames = sortBandNames(refProduct.getBandNames(), refProduct.getAutoGrouping()::indexOf);
    } else {
      bandNames = sortBandNames(refProduct.getBandNames(), s -> s.replaceAll("[0-9]", ""));
    }
    editable = new boolean[bandNames.length];
    data = new Object[bandNames.length][COLUMN_NAMES.length];
    for (int i = 0; i < bandNames.length; i++) {
      String bandName = bandNames[i];
      Band band = refProduct.getBand(bandName);
      data[i][0] = bandName;
      data[i][1] = band.getSpectralBandIndex();
      data[i][2] = band.getSpectralWavelength();
      data[i][3] = band.getSpectralBandwidth();
      editable[i] = band.getSampleCoding() == null;
    }
  }

  static String[] sortBandNames(String[] bandNames, Function<String, Object> genKey) {
    Map<Object, List<String>> map = new LinkedHashMap<>();
    for (String bandName : bandNames) {
      Object key = genKey.apply(bandName);
      if (map.containsKey(key)) {
        map.get(key).add(bandName);
      } else {
        List<String> list = new ArrayList<>();
        list.add(bandName);
        map.put(key, list);
      }
    }
    Collection<List<String>> values = map.values();
    return values.stream().flatMap(Collection::stream).toArray(String[]::new);
  }


  @Override
  public int getRowCount() {
    return data.length;
  }

  public boolean isRowEditable(int row) {
    return editable[row];
  }

  @Override
  public String getColumnName(int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return COLUMN_CLASSES[columnIndex];
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return data[rowIndex][columnIndex];
  }

  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    data[rowIndex][columnIndex] = value;
    fireTableCellUpdated(rowIndex, columnIndex);
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return editable[rowIndex] && columnIndex > 0;
  }

}
