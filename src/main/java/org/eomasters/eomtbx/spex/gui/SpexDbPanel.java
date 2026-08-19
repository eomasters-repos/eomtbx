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

import java.awt.Dimension;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.eomtbx.spex.AbstractSpex;
import org.eomasters.eomtbx.spex.DbSpex;
import org.eomasters.eomtbx.spex.formula.BandMathsExpressionFactory;
import org.eomasters.eomtbx.spex.gui.SpexDbTree.CHECKBOX_DISPLAY;
import org.eomasters.eomtbx.spex.gui.SpexDbTreeModel.GROUPING;
import org.eomasters.gui.CheckBoxTree;
import org.eomasters.gui.ClearTextFieldOverlayButton;
import org.eomasters.gui.LabeledTextField;
import org.eomasters.gui.MultiStateButton;
import org.eomasters.gui.OverlayProgressSwingWorker;
import org.eomasters.gui.State;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.eomasters.utils.ProgressManager;
import org.eomasters.utils.ProgressTask;
import org.esa.snap.core.datamodel.Product;

public class SpexDbPanel extends JPanel {

  private final SpexDbTreeModel treeModel;
  private final SpexDbTree spexTree;
  private final MultiStateButton groupingButton;
  private final LabeledTextField filterField;
  private final SortedSet<String> chosenSpexNameSet = new TreeSet<>();
  private final boolean checkboxesUsed;
  private JTextField chosenTextField;

  private Product filterProduct;

  public SpexDbPanel(boolean useCheckboxes, JButton additionalButton, boolean allowProductFiltering) {
    setLayout(new MigLayout("top, left, fillx, gapy 5, insets 0"));
    setPreferredSize(new Dimension(400, 500));
    setMinimumSize(new Dimension(300, 250));
    checkboxesUsed = useCheckboxes;
    filterField = new LabeledTextField(Icons.FILTER.getImageIcon(Icon.SIZE_24), "Filter: ") {
      @Override
      protected AbstractButton createButton() {
        if (allowProductFiltering) {
          JToggleButton toggleButton = new JToggleButton(Icons.DATABASE.getImageIcon(Icon.SIZE_24));
          toggleButton.setToolTipText("Filter by product compatibility");
          toggleButton.addActionListener(e -> updateUi());
          return toggleButton;
        }
        return null;
      }
    };
    ClearTextFieldOverlayButton.install(filterField.getTextField());

    add(filterField, "pushx, growx 100, gapx 2");
    State domainGrouping = new State(GROUPING.DOMAIN.name(), EomtbxIcons.GROUPS.getImageIcon(Icon.SIZE_24));
    domainGrouping.setToolTip("Group by app. domain");
    State nameGrouping = new State(GROUPING.NAME.name(), EomtbxIcons.ABCDEF.getImageIcon(Icon.SIZE_24));
    nameGrouping.setToolTip("Group by name");
    State sourceGrouping = new State(GROUPING.SOURCE.name(), Icons.PAPER.getImageIcon(Icon.SIZE_24));
    sourceGrouping.setToolTip("Group by source");
    State noGrouping = new State(GROUPING.NONE.name(), Icons.CANCEL.getImageIcon(Icon.SIZE_24));
    noGrouping.setToolTip("No grouping");
    groupingButton = new MultiStateButton(domainGrouping, nameGrouping, sourceGrouping, noGrouping);
    add(groupingButton, "gapx 2" + (additionalButton == null ? ", wrap" : ""));
    if (additionalButton != null) {
      add(additionalButton, "wrap");
    }
    spexTree = new SpexDbTree(checkboxesUsed ? CHECKBOX_DISPLAY.LEAVES_ONLY : CHECKBOX_DISPLAY.NONE);
    treeModel = new SpexDbTreeModel();
    treeModel.addTreeModelListener(new TreeUpdater());
    spexTree.setModel(treeModel);
    JScrollPane scrollPane = new JScrollPane(spexTree);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    add(scrollPane,
        "growx, growy, push, spanx " + (additionalButton == null ? "2" : "3") + (checkboxesUsed ? ", wrap" : ""));

    if (checkboxesUsed) {
      JPanel selectionPanel = new JPanel(new MigLayout("top, left, fillx, insets 0"));
      JLabel chosenLabel = new JLabel("Selected:");
      selectionPanel.add(chosenLabel, "");
      chosenTextField = new JTextField();
      chosenTextField.setEditable(false);
      selectionPanel.add(chosenTextField, "pushx, growx");
      spexTree.getCheckBoxTreeSelectionModel()
              .addTreeSelectionListener(e -> {
                    TreePath[] changedPaths = e.getPaths();
                    for (TreePath selectedPath : changedPaths) {
                      DbSpex index = (DbSpex) ((DefaultMutableTreeNode) selectedPath.getLastPathComponent()).getUserObject();
                      if (index != null) {
                        if (e.isAddedPath(selectedPath)) {
                          chosenSpexNameSet.add(index.getName());
                        } else {
                          chosenSpexNameSet.remove(index.getName());
                        }
                      }
                    }
                    chosenTextField.setText(String.join(", ", chosenSpexNameSet.toArray(String[]::new)));
                  });
      add(selectionPanel, "pushx, growx, spanx " + (additionalButton == null ? "2" : "3"));
    }
    groupingButton.addStateListener((oldState, newState) -> updateUi());
    filterField.getTextField().getDocument().addDocumentListener(new FilterFieldListener());
    updateUi(spexTree, groupingButton, filterField);
  }

  public DbSpex getSelectedSpex() {
    TreePath[] selectedPaths = spexTree.getSelectionPaths();
    if (selectedPaths != null && selectedPaths.length == 1) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPaths[0].getLastPathComponent();
      Object userObject = node.getUserObject();
      if (userObject instanceof DbSpex) {
        return (DbSpex) userObject;
      }
    }
    return null;
  }

  String[] getChosenSpexNames() {
    if (chosenTextField != null) {
      String chosenSpex = chosenTextField.getText();
      if (chosenSpex != null && !chosenSpex.isBlank()) {
        return chosenSpex.split(", ");
      }
    }
    return new String[0];
  }

  public void setFilterProduct(Product product) {
    this.filterProduct = product;
    updateUi();
  }

  void addTreeSelectionListener(TreeSelectionListener listener) {
    spexTree.getSelectionModel().addTreeSelectionListener(listener);
  }

  void removeTreeSelectionListener(TreeSelectionListener listener) {
    spexTree.getSelectionModel().removeTreeSelectionListener(listener);
  }

  private void updateUi() {
    updateUi(spexTree, groupingButton, filterField);
  }

  private void updateUi(CheckBoxTree tree, MultiStateButton multiStateButton, LabeledTextField filterField) {
    if (filterField.getButton() != null) {
      filterField.getButton().setEnabled(filterProduct != null);
      if (filterProduct == null) {
        filterField.getButton().setSelected(false);
      }
    }

    // preserve the selection state before updating the model
    String[] selectedSpexNames = chosenSpexNameSet.toArray(String[]::new);

    HashMap<String, Filter<SpexDbTreeNode>> filters = setupFilters(filterField);
    treeModel.setFilters(filters);
    treeModel.setGrouping(GROUPING.valueOf(multiStateButton.getCurrentState().getId()));
    ProgressTask task = ProgressManager.registerTask("SpexDbModelUpdate", 10)
                                       .with("SpexDbTextFilter", 1)
                                       .with("SpexDbProductFilter", 9);
    task.setRunnable(treeModel::updateModel);
    SwingWorker<Void, Void> worker = new OverlayProgressSwingWorker(this, task) {
      @Override
      protected void done() {
        super.done();
        if (selectedSpexNames != null && selectedSpexNames.length > 0) {
          DefaultMutableTreeNode[] nodesForNames = treeModel.getNodesForNames(selectedSpexNames);
          TreePath[] preservedSelectionPaths = Arrays.stream(nodesForNames)
                                                     .map(node -> new TreePath(treeModel.getPathToRoot(node)))
                                                     .toArray(TreePath[]::new);
          chosenSpexNameSet.addAll(List.of(selectedSpexNames));
          tree.getCheckBoxTreeSelectionModel().setSelectionPaths(preservedSelectionPaths);
        }
      }
    };
    worker.execute();
  }

  private HashMap<String, Filter<SpexDbTreeNode>> setupFilters(LabeledTextField filterField) {
    HashMap<String, Filter<SpexDbTreeNode>> filters = new HashMap<>();
    String filterText = filterField.getText();
    if (filterText != null && !filterText.isBlank()) {
      filters.put("SpexDbTextFilter", objectToMatch -> objectToMatch.containsText(filterText));
    }

    if (filterProduct != null && filterField.getButton() != null && filterField.getButton().isSelected()) {
      filters.put("SpexDbProductFilter", objectToMatch -> {
        DbSpex index = (DbSpex) objectToMatch.getUserObject();
        return BandMathsExpressionFactory.areCompatible(index, filterProduct);
      });
    }
    return filters;
  }

  public void setSelectedSpex(AbstractSpex spex) {
    if (spex != null) {
      SpexDbTreeNode nodeForSpex = treeModel.getNodeForName(spex.getName());
      if (nodeForSpex != null) {
        TreePath path = new TreePath(treeModel.getPathToRoot(nodeForSpex));
        spexTree.getSelectionModel().setSelectionPath(path);
        if (checkboxesUsed) {
          spexTree.getCheckBoxTreeSelectionModel().setSelectionPath(path);
        }
        spexTree.scrollPathToVisible(path);
      }
    }
  }

  private class FilterFieldListener implements DocumentListener {


    public FilterFieldListener() {
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
      updateUi();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      updateUi();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      // nothing to do here
    }
  }

  private class TreeUpdater implements TreeModelListener {

    @Override
    public void treeNodesChanged(TreeModelEvent e) {
      SwingUtilities.invokeLater(spexTree::revalidate);
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
      SwingUtilities.invokeLater(spexTree::revalidate);
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
      SwingUtilities.invokeLater(spexTree::revalidate);
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
      SwingUtilities.invokeLater(spexTree::revalidate);
    }
  }
}
