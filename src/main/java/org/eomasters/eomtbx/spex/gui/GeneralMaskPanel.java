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

import com.bc.ceres.binding.ConversionException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import org.eomasters.gui.FileIo;
import org.eomasters.gui.Highlighter;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.jexp.ParseException;
import org.esa.snap.core.util.PreferencesPropertyMap;
import org.esa.snap.core.util.converters.JtsGeometryConverter;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.ModalDialog;
import org.esa.snap.ui.product.ProductExpressionPane;
import org.locationtech.jts.geom.Geometry;

public class GeneralMaskPanel extends JPanel {

  private final JTextField expressionField;
  private final JButton expressionBtn;
  private final JTextField shapefileField;
  private final JTextField wktField;
  private Product refProduct;

  public GeneralMaskPanel() {
    this(null);
  }

  public GeneralMaskPanel(Product reference) {
    this.refProduct = reference;
    setLayout(new MigLayout("top, left, fillx, gap 4"));
    expressionField = new JTextField();
    expressionField.setToolTipText("Enter a validation expression");
    expressionBtn = createExpressionButton();
    shapefileField = new JTextField();
    shapefileField.setToolTipText("Enter or select a shapefile");
    JButton shapefileButton = createShapefileButton(shapefileField);
    wktField = new JTextField();
    wktField.setToolTipText("Enter geometry as Well-Known Text");

    add(new JLabel("Valid Expression:"), "pushx, span 2, wrap");
    add(expressionField, "pushx, growx");
    add(expressionBtn, "growx, wrap");
    add(new JLabel("Shapefile:"), "pushx, span 2, wrap");
    add(shapefileField, "pushx, growx");
    add(shapefileButton, "growx, wrap");
    add(new JLabel("WKT:"), "pushx, span 2, wrap");
    add(wktField, "pushx, growx, span 2");
  }

  public String getValidExpression() {
    return expressionField.getText();
  }

  public void setValidExpression(String expression) {
    this.expressionField.setText(expression);
  }

  public Path getShapefile() {
    String fieldText = shapefileField.getText();
    if (!fieldText.isBlank()) {
      return Path.of(fieldText);
    }
    return null;
  }

  public void setShapefile(Path shapefile) {
    this.shapefileField.setText(shapefile.toAbsolutePath().toString());
  }

  public String getWkt() {
    return wktField.getText();
  }

  public Geometry getWktGeometry() {
    try {
      return new JtsGeometryConverter().parse(getWkt());
    } catch (ConversionException e) {
      return null;
    }
  }

  public void setWktField(String wkt) {
    this.wktField.setText(wkt);
  }

  public void setReferenceProduct(Product refProduct) {
    this.refProduct = refProduct;
    expressionBtn.setEnabled(refProduct != null);
  }

  private JButton createExpressionButton() {
    final JButton expressionBtn;
    expressionBtn = new JButton("Edit ...");
    expressionBtn.setEnabled(refProduct != null);
    expressionBtn.setToolTipText("Enter a validation expression");
    expressionBtn.addActionListener(e -> {
      ProductExpressionPane pep = ProductExpressionPane.createBooleanExpressionPane(new Product[]{refProduct},
          null, new PreferencesPropertyMap(SnapApp.getDefault().getPreferences()));
      pep.setCode(expressionField.getText());
      int status = pep.showModalDialog(SwingUtilities.getWindowAncestor(this), "Validation Expression Editor");
      if (status == ModalDialog.ID_OK) {
        expressionField.setText(pep.getCode());
      }
    });
    return expressionBtn;
  }

  private JButton createShapefileButton(JTextField shapefileField) {
    JButton shapefileButton = new JButton("Select ...");
    shapefileButton.setToolTipText("Select a shapefile");
    shapefileButton.addActionListener(e -> {
          JFileChooser fileChooser = new JFileChooser();
          fileChooser.setFileFilter(FileIo.createFileFilter("ESRI Shapefile", "shp"));
          fileChooser.setAcceptAllFileFilterUsed(false);
          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          fileChooser.setMultiSelectionEnabled(false);
          if (JFileChooser.APPROVE_OPTION == fileChooser.showDialog(this, "Select")) {
            shapefileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
          }
        }
    );
    return shapefileButton;
  }

  public boolean validateInput() {
    String expression = getValidExpression();
    if (!expression.isBlank() && refProduct != null) {
      try {
        refProduct.parseExpression(expression);
      } catch (ParseException e) {
        String infoMessage = "Invalid Expression:\n" + e.getMessage();
        Highlighter highlighter = new Highlighter(expressionField);
        highlighter.setDuration(3.0);
        highlighter.highlight(infoMessage);
        return false;
      }
    }
    Path shapefilePath = getShapefile();
    if (shapefilePath != null) {
      if (!Files.isReadable(shapefilePath)) {
        Highlighter.error(shapefileField, "File is not readable:\n" + shapefilePath);
        return false;
      }
    }
    String wkt = getWkt();
    if (!wkt.isBlank()) {
      try {
        new JtsGeometryConverter().parse(wkt);
      } catch (ConversionException e) {
        Highlighter.error(wktField, "WKT cannot be parsed:\n" + e.getMessage());

        return false;
      }
    }
    return true;
  }

}
