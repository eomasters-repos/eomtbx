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

package org.eomasters.eomtbx.ciop.gui;

import com.bc.ceres.binding.ConversionException;
import java.awt.Container;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import net.miginfocom.swing.MigLayout;
import org.eomasters.gui.FileIo;
import org.eomasters.snap.gui.gpf.ParametersPanel;
import org.eomasters.snap.gui.gpf.ValidationResult;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.descriptor.OperatorDescriptor;
import org.esa.snap.core.gpf.ui.OperatorParameterSupport;
import org.esa.snap.core.jexp.ParseException;
import org.esa.snap.core.util.PreferencesPropertyMap;
import org.esa.snap.core.util.converters.JtsGeometryConverter;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.ModalDialog;
import org.esa.snap.ui.product.ProductExpressionPane;

public class CyanoIndexParametersPanel extends JPanel implements ParametersPanel<JPanel> {

  private final OperatorDescriptor ciOpDescriptor;
  private Product sourceProduct;
  private final JTextField expressionField;
  private final JButton expressionBtn;
  private final JTextField shapefileField;
  private final JTextField wktField;

  public static void main(String[] args) {
    final JFrame frame = new JFrame("Cyano Index Parameters Panel");
    Container contentPane = frame.getContentPane();
    OperatorSpi spexSpi = GPF.getDefaultInstance()
                             .getOperatorSpiRegistry()
                             .getOperatorSpi("CyanoIndex");
    OperatorDescriptor spexDescriptor = spexSpi.getOperatorDescriptor();

    CyanoIndexParametersPanel spexParametersPanel = new CyanoIndexParametersPanel(spexDescriptor);
    contentPane.add(spexParametersPanel);
    frame.setSize(400, 400);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    SwingUtilities.invokeLater(() -> frame.setVisible(true));
  }

  public CyanoIndexParametersPanel(OperatorDescriptor cyanoOpDescriptor) {
    this.ciOpDescriptor = cyanoOpDescriptor;
    setLayout(new MigLayout("top, left, gap 10 10"));
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

  public Path getShapefile() {
    String fieldText = shapefileField.getText();
    if (!fieldText.isBlank()) {
      return Path.of(fieldText);
    }
    return null;
  }

  public String getWkt() {
    return wktField.getText();
  }


  private JButton createExpressionButton() {
    final JButton expressionBtn;
    expressionBtn = new JButton("Edit ...");
    expressionBtn.setEnabled(sourceProduct != null);
    expressionBtn.setToolTipText("Enter a validation expression");
    expressionBtn.addActionListener(e -> {
      ProductExpressionPane pep = ProductExpressionPane.createBooleanExpressionPane(new Product[]{sourceProduct},
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


  @Override
  public void onSourceProductSelectionChanged(HashMap<String, Product> products, String changedKey) {
    this.sourceProduct = products.get("sourceProduct");
    expressionBtn.setEnabled(sourceProduct != null);
  }

  @Override
  public ValidationResult doValidation() {
    String expression = expressionField.getText();
    if (!expression.isBlank() && sourceProduct != null) {
      try {
        sourceProduct.parseExpression(expression);
      } catch (ParseException e) {
        return ValidationResult.createInvalidResult("Invalid Expression:\n" + e.getMessage(), expressionField);
      }
    }
    Path shapefilePath = getShapefile();
    if (shapefilePath != null) {
      if (!Files.isReadable(shapefilePath)) {
        return ValidationResult.createInvalidResult("File is not readable:\n" + shapefilePath, shapefileField);
      }
    }
    String wkt = getWkt();
    if (!wkt.isBlank()) {
      try {
        new JtsGeometryConverter().parse(wkt);
      } catch (ConversionException e) {
        return ValidationResult.createInvalidResult("WKT cannot be parsed:\n" + e.getMessage(), wktField);
      }
    }
    return ValidationResult.createValidResult();
  }

  @Override
  public Map<String, Object> getParametersMap() {
    OperatorParameterSupport operatorParameterSupport = new OperatorParameterSupport(ciOpDescriptor);
    Map<String, Object> parameterMap = operatorParameterSupport.getParameterMap();
    parameterMap.put("validExpression", getValidExpression());
    parameterMap.put("shapefile", getShapefile());
    parameterMap.put("wktRegion", getWkt());
    return parameterMap;
  }


}
