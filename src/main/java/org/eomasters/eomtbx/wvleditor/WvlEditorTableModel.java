package org.eomasters.eomtbx.wvleditor;

import javax.swing.table.AbstractTableModel;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;

class WvlEditorTableModel extends AbstractTableModel {
  private static final String[] COLUMN_NAMES = {"Band", "Band Index", "Wavelength", "Bandwidth"};
  private static final Class<?>[] COLUMN_CLASSES = {String.class, Integer.class, Float.class, Float.class};

  private final Object[][] data;
  private final boolean[] editable;

  public WvlEditorTableModel(Product refProduct) {
    String[] bandNames = refProduct.getBandNames();
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

  @Override
  public int getRowCount() {
    return data.length;
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
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    data[rowIndex][columnIndex] = aValue;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return editable[rowIndex] && columnIndex > 0;
  }

}
