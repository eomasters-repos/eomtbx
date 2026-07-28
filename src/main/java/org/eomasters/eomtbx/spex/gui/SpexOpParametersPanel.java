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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.eomtbx.spex.AbstractSpex;
import org.eomasters.eomtbx.spex.formula.BandMathsExpressionFactory;
import org.eomasters.eomtbx.spex.CustomSpex;
import org.eomasters.eomtbx.spex.DbSpex;
import org.eomasters.eomtbx.spex.SpexDb;
import org.eomasters.eomtbx.spex.gui.UserSpexEditorDialog.Mode;
import org.eomasters.gui.Dialogs;
import org.eomasters.gui.MultiLineTableCellRenderer;
import org.eomasters.icons.Additions;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.eomasters.snap.gui.gpf.ParametersPanel;
import org.eomasters.snap.gui.gpf.ValidationResult;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.descriptor.OperatorDescriptor;
import org.esa.snap.core.gpf.ui.OperatorParameterSupport;
import org.locationtech.jts.geom.Geometry;

public class SpexOpParametersPanel extends JPanel implements ParametersPanel<JPanel> {

  private final OperatorDescriptor spexDescriptor;
  private final SpexDbPanel spexDbPanel;
  private Product sourceProduct;
  private final GeneralMaskPanel generalMaskPanel;
  private JButton removeButton;
  private JButton editButton;
  private JButton addToDbBtn;
  private CostumSpexTableModel cSpeXTableModel;
  private JTable cSpexTable;
  private JScrollPane cSpexScrollPane;

  public static void main(String[] args) {
    final JFrame frame = new JFrame("SpeX Parameters Panel");
    Container contentPane = frame.getContentPane();
    frame.setIconImages(EomtbxIcons.SPEX_DB.getImages(new int[]{16, 32, 48}));
    OperatorSpi spexSpi = GPF.getDefaultInstance()
                             .getOperatorSpiRegistry()
                             .getOperatorSpi("SPEX");
    OperatorDescriptor spexDescriptor = spexSpi.getOperatorDescriptor();

    SpexOpParametersPanel spexParametersPanel = new SpexOpParametersPanel(spexDescriptor);
    contentPane.add(spexParametersPanel);
    frame.setSize(400, 400);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    SwingUtilities.invokeLater(() -> frame.setVisible(true));
  }

  public SpexOpParametersPanel(OperatorDescriptor spexDescriptor) {
    this.spexDescriptor = spexDescriptor;
    setLayout(new MigLayout("top, left, gap 10 10"));
    JButton openDbManageBtn = new JButton(
        EomtbxIcons.SPEX_DB.withAddition(Additions.EYE).getImageIcon(Icon.SIZE_24));
    openDbManageBtn.addActionListener(e -> SpexDbOptionsAction.openManager());
    openDbManageBtn.setToolTipText("Open SpeX DB Manager");
    spexDbPanel = new SpexDbPanel(true, openDbManageBtn, true);
    SpexInfoPanel spexInfoPanel = new SpexInfoPanel();
    spexInfoPanel.setBorder(new TitledBorder("Selected SpeX"));
    spexDbPanel.addTreeSelectionListener(new LeadSelectionChangeListener(spexInfoPanel));
    generalMaskPanel = createGeneralMaskPanel();
    add(spexDbPanel, "spany 2, pushx, growx, shrinkprio 50, top, left");
    add(spexInfoPanel, "pushx, growx, shrinkprio 100, top, left, wrap");
    add(generalMaskPanel, "pushx, growx, shrinkprio 100, top, left, wrap");

    JPanel customSpexPanel = createCustomSpexPanel();
    add(customSpexPanel, "spanx 2, push, grow, shrinkprio 100, top, left");
  }


  @Override
  public void onSourceProductSelectionChanged(HashMap<String, Product> products, String changedKey) {
    setSourceProduct(products.get("sourceProduct"));
  }

  @Override
  public ValidationResult doValidation() {
    String[] allSpexNames = spexDbPanel.getChosenSpexNames();
    for (String name : allSpexNames) {
      AbstractSpex spex = SpexDb.getInstance().get(name);
      if (!BandMathsExpressionFactory.areCompatible(spex, sourceProduct)) {
        return ValidationResult.createInvalidResult(
            String.format("Selected SpeX '%s' is not compatible with the source product", name),
            spexDbPanel);
      }
    }
    CustomSpex[] allCustom = cSpeXTableModel.getAll();
    List<String> allSpexNamesList = List.of(allSpexNames);
    for (CustomSpex customSpex : allCustom) {
      if (allSpexNamesList.contains(customSpex.getName())) {
        return ValidationResult.createInvalidResult(
            String.format("CustomSpectral Index '%s' already selected from SpeX database", customSpex.getName()),
            cSpexScrollPane);
      }
      if (!BandMathsExpressionFactory.areCompatible(customSpex, sourceProduct)) {
        return ValidationResult.createInvalidResult(
            "Formula of spectral index is not compatible with the source product",
            cSpexScrollPane);
      }
    }
    if (allSpexNames.length == 0 && allCustom.length == 0) {
      return ValidationResult.createInvalidResult("No SpeX selected", spexDbPanel);
    }
    return ValidationResult.createValidResult();
  }

  @Override
  public Map<String, Object> getParametersMap() {
    OperatorParameterSupport operatorParameterSupport = new OperatorParameterSupport(spexDescriptor);
    Map<String, Object> parameterMap = operatorParameterSupport.getParameterMap();
    parameterMap.put("spexList", spexDbPanel.getChosenSpexNames());
    parameterMap.put("validExpression", generalMaskPanel.getValidExpression());
    parameterMap.put("shapefile", generalMaskPanel.getShapefile());
    parameterMap.put("wktRegion", generalMaskPanel.getWktGeometry());
    parameterMap.put("customIndices", cSpeXTableModel.getAll());
    return parameterMap;
  }

  public Product getSourceProduct() {
    return sourceProduct;
  }

  public void setSourceProduct(Product sourceProduct) {
    this.sourceProduct = sourceProduct;
    generalMaskPanel.setReferenceProduct(sourceProduct);
    spexDbPanel.setFilterProduct(sourceProduct);
  }

  private GeneralMaskPanel createGeneralMaskPanel() {
    GeneralMaskPanel panel = new GeneralMaskPanel(getSourceProduct());
    panel.setBorder(new TitledBorder("General Masks"));
    return panel;
  }

  private JPanel createCustomSpexPanel() {
    JPanel panel = new JPanel(new MigLayout("top, left, fill, gap 4"));
    panel.setBorder(new TitledBorder("Custom SpeX"));
    cSpeXTableModel = new CostumSpexTableModel();
    cSpexTable = new ToolTipTable(cSpeXTableModel);
    cSpexTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    cSpexTable.setDefaultRenderer(String[].class, new MultiLineTableCellRenderer());
    cSpexTable.setDefaultEditor(Object.class, null);
    TableColumnModel columnModel = cSpexTable.getColumnModel();
    columnModel.getColumn(0).setPreferredWidth(60);
    cSpexTable.getColumnModel().getColumn(0).setMaxWidth(60);
    cSpexTable.getColumnModel().getColumn(0).setMaxWidth(60);
    cSpeXTableModel.addTableModelListener(e -> {
      for (int row = 0; row < cSpexTable.getRowCount(); row++) {
        int rowHeight = cSpexTable.getRowHeight();
        for (int column = 0; column < cSpexTable.getColumnCount(); column++) {
          Component comp = cSpexTable.prepareRenderer(cSpexTable.getCellRenderer(row, column), row, column);
          rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
        }
        cSpexTable.setRowHeight(row, rowHeight);
      }
    });

    cSpexScrollPane = new JScrollPane(cSpexTable);
    cSpexScrollPane.setPreferredSize(new Dimension(cSpexScrollPane.getPreferredSize().width, 100));
    cSpexScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    cSpexScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    panel.add(cSpexScrollPane, "top, left, push, grow");
    JPanel btnPanel = new JPanel(new MigLayout("top, left, gap 4, ins 0"));
    JButton addCustomButton = getAddButton(cSpeXTableModel);
    addCustomButton.setToolTipText("Add Custom SpeX");
    btnPanel.add(addCustomButton, "top, left, wrap");
    removeButton = getRemoveButton(cSpexTable, cSpeXTableModel);
    removeButton.setToolTipText("Remove Custom SpeX");
    btnPanel.add(removeButton, "top, left, wrap");
    editButton = getEditButton(cSpexTable, cSpeXTableModel);
    editButton.setToolTipText("Edit Custom SpeX");
    btnPanel.add(editButton, "top, left, wrap");
    addToDbBtn = getAddToDbButton(cSpexTable, cSpeXTableModel);
    addToDbBtn.setToolTipText("Add Custom SpeX to DB");
    btnPanel.add(addToDbBtn, "top, left, wrap");
    panel.add(btnPanel, "top, left, pushy, grow, wrap");

    cSpexTable.getSelectionModel().addListSelectionListener(e -> {
      ListSelectionModel lsm = (ListSelectionModel) e.getSource();
      if (lsm.getMinSelectionIndex() != -1) {
        CustomSpex customSpex = cSpeXTableModel.get(lsm.getMinSelectionIndex());
        updateButtons(customSpex != null);
      }
    });
    updateButtons(false);
    return panel;
  }

  private JButton getAddToDbButton(JTable cSpexTable, CostumSpexTableModel cSpeXTableModel) {
    JButton addToDbBtn = new JButton(EomtbxIcons.SPEX_DB.withAddition(Additions.PLUS).getImageIcon(Icon.SIZE_24));
    addToDbBtn.addActionListener(e -> {
      Window windowAncestor = SwingUtilities.getWindowAncestor(this);
      UserSpexEditorDialog editorDialog = new UserSpexEditorDialog(windowAncestor, false);
      editorDialog.setLocationRelativeTo(windowAncestor);
      editorDialog.setMode(Mode.ADD);
      int spexIndex = cSpexTable.getSelectedRow();
      editorDialog.initBy(cSpeXTableModel.get(spexIndex));
      editorDialog.setVisible(true);

      CustomSpex createdSpex = editorDialog.getCustomSpex();
      if (createdSpex != null) {
        SpexDb.getInstance().addIndex(new DbSpex(createdSpex));
        boolean openSpeXDbManager = Dialogs.confirmation("SpeX Added to DB", "Open SpeX DB Manager?", this);
        if (openSpeXDbManager) {
          SpexDbOptionsAction.openManager();
        }
      }
    });
    return addToDbBtn;
  }

  private void updateButtons(boolean somethingSelected) {
    removeButton.setEnabled(somethingSelected);
    editButton.setEnabled(somethingSelected);
    addToDbBtn.setEnabled(somethingSelected);
  }

  private JButton getEditButton(JTable cSpexTable, CostumSpexTableModel cSpeXTableModel) {
    JButton editButton = new JButton(Icons.PEN.getImageIcon(Icon.SIZE_24));
    editButton.addActionListener(e -> {
      int spexIndex = cSpexTable.getSelectedRow();
      CustomSpex customSpex = cSpeXTableModel.get(spexIndex);
      if (customSpex == null) {
        return;
      }
      Window windowAncestor = SwingUtilities.getWindowAncestor(SpexOpParametersPanel.this);
      UserSpexEditorDialog editorDialog = new UserSpexEditorDialog(windowAncestor, true);
      editorDialog.setReferenceProduct(getSourceProduct());
      editorDialog.setTitle("Edit Custom SpeX");
      editorDialog.setLocationRelativeTo(windowAncestor);
      editorDialog.initBy(customSpex);
      editorDialog.setVisible(true);
      CustomSpex spex = editorDialog.getCustomSpex();
      if (spex != null) {
        cSpeXTableModel.set(spex, spexIndex);
      }
    });
    return editButton;
  }

  private static JButton getRemoveButton(JTable cSpexTable, CostumSpexTableModel cSpeXTableModel) {
    JButton removeButton = new JButton(Icons.MINUS.getImageIcon(Icon.SIZE_24));
    removeButton.addActionListener(e -> cSpeXTableModel.removeSpexAt(cSpexTable.getSelectedRow()));
    return removeButton;
  }

  private JButton getAddButton(CostumSpexTableModel cSpeXTableModel) {
    JButton addCustomButton = new JButton(Icons.PLUS.getImageIcon(Icon.SIZE_24));
    addCustomButton.addActionListener(e -> {
      Window windowAncestor = SwingUtilities.getWindowAncestor(SpexOpParametersPanel.this);
      UserSpexEditorDialog editorDialog = new UserSpexEditorDialog(windowAncestor, true);
      editorDialog.setReferenceProduct(getSourceProduct());
      editorDialog.setTitle("Create Custom SpeX");
      editorDialog.setLocationRelativeTo(windowAncestor);
      boolean retry;
      do {
        editorDialog.setVisible(true);
        CustomSpex spex = editorDialog.getCustomSpex();
        if (spex != null) {
          if (!cSpeXTableModel.contains(spex)) {
            cSpeXTableModel.add(spex);
            retry = false;
          } else {
            Dialogs.error(editorDialog, editorDialog.getTitle(), "Table contains already a SpeX with this name");
            retry = true;
          }
        } else {
          retry = false;
        }
      } while (retry);
      editorDialog.setVisible(false);
      editorDialog.dispose();

    });
    return addCustomButton;
  }

  private static class LeadSelectionChangeListener implements TreeSelectionListener {

    private final SpexInfoPanel spexInfoPanel;

    public LeadSelectionChangeListener(SpexInfoPanel spexInfoPanel) {
      this.spexInfoPanel = spexInfoPanel;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
      TreePath selectionPath = e.getNewLeadSelectionPath();
      DbSpex selected = null;
      if (selectionPath != null) {
        DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
        Object userObject = lastPathComponent.getUserObject();
        if (userObject instanceof DbSpex) {
          selected = (DbSpex) userObject;
        }
      }
      spexInfoPanel.display(selected);
    }

  }

  private static class ToolTipTable extends JTable {

    private final CostumSpexTableModel cSpeXTableModel;

    public ToolTipTable(CostumSpexTableModel cSpeXTableModel) {
      super(cSpeXTableModel);
      this.cSpeXTableModel = cSpeXTableModel;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
      CustomSpex idx = cSpeXTableModel.get(rowAtPoint(event.getPoint()));
      if (idx == null) {
        return null;
      }
      StringBuilder tooltip = new StringBuilder(String.format("<html>"
              + "<b>Name:</b> %s<br>"
              + "<b>Formula:</b> %s<br>"
              + "<b>Domain:</b> %s<br>"
              + "<b>Description:</b> %s<br>",
          idx.getName(), idx.getFormula(), idx.getDomain(), idx.getDescription()));

      String validExpression = idx.getValidExpression();
      tooltip.append(
          (validExpression != null && !validExpression.isBlank()) ? String.format("<b>Valid Expression:</b> %s<br>",
              validExpression) : "");
      Path shapefile = idx.getShapefile();
      tooltip.append(shapefile != null ? String.format("<b>Shapefile:</b> %s<br>", shapefile) : "");
      Geometry wktRegion = idx.getWktRegion();
      tooltip.append(wktRegion != null ? String.format("<b>WKT Geometry:</b> %s<br>", wktRegion.toText()) : "");
      return tooltip.toString();
    }

  }
}
