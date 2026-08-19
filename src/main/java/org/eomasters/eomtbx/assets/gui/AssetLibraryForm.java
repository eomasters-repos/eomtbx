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

package org.eomasters.eomtbx.assets.gui;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.SwingHelper;
import com.bc.ceres.swing.progress.DialogProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import org.eomasters.gui.LabeledTextField;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.assets.Asset;
import org.eomasters.eomtbx.assets.AssetCreationRegistry;
import org.eomasters.eomtbx.assets.AssetCreationService;
import org.eomasters.eomtbx.assets.AssetException;
import org.eomasters.eomtbx.assets.AssetLibrary;
import org.eomasters.eomtbx.assets.AssetStore;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.AssetTypeRegistry;
import org.eomasters.eomtbx.assets.gui.tree.AssetLibraryTree;
import org.eomasters.eomtbx.assets.gui.tree.AssetLibraryTreeModel;
import org.eomasters.eomtbx.assets.gui.tree.AssetTreeCellEditor;
import org.eomasters.eomtbx.assets.gui.tree.AssetTreeCellRenderer;
import org.eomasters.eomtbx.assets.gui.tree.GROUPING;
import org.eomasters.gui.ClearTextFieldOverlayButton;
import org.eomasters.gui.Dialogs;
import org.eomasters.gui.DropdownButton;
import org.eomasters.gui.FileIo;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.eomasters.utils.ErrorHandler;
import org.esa.snap.core.datamodel.ProductManager;
import org.esa.snap.core.datamodel.ProductManager.Event;
import org.esa.snap.core.datamodel.ProductManager.Listener;
import org.esa.snap.core.util.io.FileUtils;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.ui.ModalDialog;
import org.esa.snap.ui.help.HelpDisplayer;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;

public class AssetLibraryForm extends JPanel {

  public static final String HELP_ID = "eomtbx.assetLibrary";
  private static final Font MENU_LABEL_FONT = UIManager.getFont("Label.font").deriveFont(
      AffineTransform.getScaleInstance(1.15, 1.15));
  private static final String JAF_EXTENSION = "jaf";
  private static final String JAF_DESCRIPTION = "Json Assets File";

  private final AssetLibrary library;
  private final AssetLibraryTreeModel libraryModel;
  private AssetLibraryTree libraryTree;
  private AssetEditPanel assetEditPanel;

  public AssetLibraryForm(AssetLibrary library) {
    this.library = library;
    libraryModel = new AssetLibraryTreeModel(library, GROUPING.BY_TYPES);
    initComponents();
    ProductManager productManager = SnapApp.getDefault().getProductManager();
    productManager.addListener(new ProductManagerChangeListener(productManager));
  }

  private void initComponents() {
    libraryTree = new AssetLibraryTree(libraryModel);
    libraryTree.setInvokesStopCellEditing(true);
    libraryTree.setCellRenderer(new AssetTreeCellRenderer(libraryTree));
    libraryTree.setCellEditor(new AssetTreeCellEditor(libraryTree));
    libraryTree.setEditable(true);
    assetEditPanel = createAssetEditPanel();

    setLayout(new MigLayout(
        "top, left, filly",
        "[fill, grow]",
        "[90%, fill, grow]10:10:20[10%, fill, grow]"
    ));
    add(createOperationsPanel(), "grow, push, wrap");
    add(assetEditPanel, "grow, push");

    assetEditPanel.addChangeListener(this::onAssetModified);
    libraryTree.getSelectionModel().addTreeSelectionListener(this::onTreeSelectionChanged);


  }

  private JPanel createOperationsPanel() {
    final JPanel panel = new JPanel(new MigLayout(
        "top, left",
        "[fill, grow][]",
        "[]0:0:0[]"
    ));
    panel.add(createSearchAndGroupPanel(), "growx, growy 0, pushy 0, wrap");
    panel.add(libraryTree, "growy, push");
    panel.add(createButtonsPanel(), "grow, push");
    return panel;
  }

  private JPanel createSearchAndGroupPanel() {
    final JPanel panel = new JPanel(new MigLayout(
        "top, left, ins 0,",
        "[fill, grow]50:75:n:push[right, grow][right, fill]",
        "[]0"
    ));
    LabeledTextField searchField = new LabeledTextField(Icons.FILTER.getImageIcon(Icon.SIZE_24), "Search: ");
    searchField.getTextField()
               .setFont(searchField.getTextField().getFont().deriveFont(AffineTransform.getScaleInstance(1.2, 1.2)));
    panel.add(searchField, "w 150:250:350:push, growx 50");
    ClearTextFieldOverlayButton.install(searchField.getTextField());
    searchField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        update();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        update();
      }

      private void update() {
        SwingUtilities.invokeLater(() -> {
          Asset selectedAsset = libraryTree.getSelectedAsset();
          libraryModel.setFilter(searchField.getText());
          libraryTree.setSelectedAsset(selectedAsset);
        });
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        // nothing to do here
      }
    });

    EnumComboBoxModel<GROUPING> groupModel = new EnumComboBoxModel<>(GROUPING.class);
    JComboBox<GROUPING> groupingBox = new JComboBox<>(groupModel);
    groupingBox.addActionListener(e -> SwingUtilities.invokeLater(() -> {
      Asset selectedAsset = libraryTree.getSelectedAsset();
      libraryModel.setGrouping(groupModel.getSelectedItem());
      libraryTree.setSelectedAsset(selectedAsset);
    }));
    panel.add(new JLabel("Group:"));
    panel.add(groupingBox, "w 60:100:150:push, growx 15");
    return panel;
  }

  private JPanel createButtonsPanel() {
    final JPanel panel = new JPanel(new MigLayout(
        "",
        "0[fill, grow]0",
        "[][][][]5:n:n:push[grow, push, bottom]0"));
    panel.add(createAddButton(), "wrap");
    panel.add(createRemoveButton(), "wrap");
    panel.add(createImportButton(), "wrap");
    panel.add(createExportButton(), "wrap");
    panel.add(createHelpButton(), "alignx center, growx 0, pushx 0, pushy 100");
    return panel;
  }

  private JButton createIOButton(String title, IOCallable doWithFile) {
    JButton btn = new JButton(title);
    btn.addActionListener(l -> {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileFilter(FileIo.createFileFilter(JAF_DESCRIPTION, JAF_EXTENSION));
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fileChooser.setAcceptAllFileFilterUsed(false);
      fileChooser.setMultiSelectionEnabled(false);
      SwingHelper.centerComponent(fileChooser, this);
      if (JFileChooser.APPROVE_OPTION == fileChooser.showDialog(this, title)) {
        var sw = new ProgressMonitorSwingWorker<Void, Void>(this, title + " Assets") {
          @Override
          protected Void doInBackground(ProgressMonitor pm) {
            doWithFile.executeIO(fileChooser.getSelectedFile());
            return null;
          }
        };
        try {
          sw.executeWithBlocking();
          sw.get();
        } catch (InterruptedException e) {
          // ignore
        } catch (ExecutionException e) {
          Throwable cause = e.getCause();
          ErrorHandler.handleError(title + " Assets", "Not able to " + title.toLowerCase() + " assets", cause);
        }
      }
    });
    return btn;
  }

  // Now you can simplify createExportButton and createImportButton functions
  private JButton createExportButton() {
    String title = "Export";
    return createIOButton(title, file -> {
      File fileExt = FileUtils.ensureExtension(file, "." + JAF_EXTENSION);
      if (fileExt.exists()) {
        String question = "File already exists. Do you want to overwrite it?";
        if (!Dialogs.confirmation(title, question, this)) {
          return;
        }
      }
      try {
        new AssetStore().save(AssetLibrary.getInstance().getAssets(), fileExt.toPath());
      } catch (IOException e) {
        ErrorHandler.handleError(title + " Assets", "Not able to " + title.toLowerCase() + " assets.", e);
      }
    });
  }

  private JButton createImportButton() {
    String title = "Import";
    return createIOButton(title, file -> {
      if (!file.exists()) {
        Dialogs.message(this, title, "File does not exist.");
      }
      try {
        AssetLibrary.getInstance().add(new AssetStore().load(file.toPath()));
      } catch (IOException e) {
        ErrorHandler.handleError(title + " Assets", "Not able to " + title.toLowerCase() + " assets.", e);
      }
    });
  }

  private static JButton createHelpButton() {
    JButton helpBtn = new JButton(Icons.QUESTION_MARK.getImageIcon(Icon.SIZE_24));
    helpBtn.addActionListener(e -> HelpDisplayer.show(HELP_ID));
    return helpBtn;
  }

  // [2024-07-04] No need to use SwingWorker anymore. But keeping it, just in case.
  // Can be fixed by -DTopSecurityManager.disable=true
  // See https://forum.step.esa.int/t/annoyed-by-long-delays-for-file-dialogues-here-is-a-solution/42709
  //
  // private JFileChooser createFileChooser() {
  //   try {
  //     ProgressMonitorSwingWorker<JFileChooser, JFileChooser> sw = new ProgressMonitorSwingWorker<>(this,
  //         "File Chooser") {
  //
  //       @Override
  //       protected JFileChooser doInBackground(ProgressMonitor pm) throws Exception {
  //         pm.beginTask("Initialising the file chooser ...", -1);
  //         try {
  //           JFileChooser fileChooser = new JFileChooser();
  //           fileChooser.setFileFilter(FileIo.createFileFilter(JAF_DESCRIPTION, JAF_EXTENSION));
  //           fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
  //           fileChooser.setAcceptAllFileFilterUsed(false);
  //           fileChooser.setMultiSelectionEnabled(false);
  //           return fileChooser;
  //         } finally {
  //           pm.done();
  //         }
  //       }
  //     };
  //     sw.executeWithBlocking();
  //     return sw.get();
  //   } catch (Exception e) {
  //     throw new IllegalStateException("Not able to create file selection dialog.");
  //   }
  // }

  private JButton createRemoveButton() {
    JButton removeBtn = new JButton("Remove");
    removeBtn.setEnabled(libraryTree.getSelectedAsset() != null);
    libraryTree.addTreeSelectionListener(e -> removeBtn.setEnabled(libraryTree.getSelectedAsset() != null));
    removeBtn.addActionListener(e -> SwingUtilities.invokeLater(() -> {
      TreeNode selectedNode = libraryTree.getSelectedNode();
      TreeNode parent = selectedNode.getParent();
      int index = parent.getIndex(selectedNode);
      library.remove(libraryTree.getSelectedAsset());
      if (parent.getChildCount() > 0) {
        TreeNode newSelection = parent.getChildAt(Math.min(parent.getChildCount() - 1, index));
        libraryTree.makeVisible(libraryModel.getPathTo(newSelection));
        libraryTree.setSelectedNode(newSelection);
      }
    }));
    return removeBtn;
  }

  private DropdownButton createAddButton() {
    JPopupMenu menu = new JPopupMenu();
    AssetTypeRegistry typeRegistry = AssetTypeRegistry.instance();

    for (AssetType type : typeRegistry.getServices()) {
      AssetCreationRegistry factoryRegistry = AssetCreationRegistry.instance();
      List<AssetCreationService> factories = factoryRegistry.getFactories(type);
      if (factories.isEmpty()) {
        continue; // nothing to add to the menu
      }

      if (factories.size() == 1) {
        JMenuItem item = new JMenuItem(type.getName());
        item.setFont(MENU_LABEL_FONT);
        item.setIcon(type.getIcon().getImageIcon(Icon.SIZE_16));
        item.setToolTipText(type.getDescription());
        item.addActionListener(addAssetToLibrary(type, factories.get(0)));
        menu.add(item);
      } else {
        JMenu typeMenu = new JMenu(type.getName());
        typeMenu.setFont(MENU_LABEL_FONT);
        typeMenu.setIcon(type.getIcon().getImageIcon(Icon.SIZE_16));
        typeMenu.setToolTipText(type.getDescription());
        for (AssetCreationService service : factories) {
          typeMenu.add(createMenuItemForCreationService(type, service));
        }
        menu.add(typeMenu);
      }
    }

    return new DropdownButton("Add", null, menu);
  }

  private JMenuItem createMenuItemForCreationService(AssetType type, AssetCreationService service) {
    JMenuItem item = new JMenuItem(service.getName());
    item.setFont(MENU_LABEL_FONT);
    item.setToolTipText(service.getDescription());
    item.addActionListener(addAssetToLibrary(type, service));
    return item;
  }

  private ActionListener addAssetToLibrary(AssetType type, AssetCreationService service) {
    return e -> {
      try {
        Asset asset = promptForAsset(type, service);
        if (asset != null) {
          SwingUtilities.invokeLater(() -> {
            library.add(asset);
            TreePath pathTo = libraryModel.getPathTo(asset);
            if (pathTo != null) {
              libraryTree.makeVisible(pathTo);
              libraryTree.setSelectionPath(pathTo);
            }
          });
        }
      } catch (AssetException ex) {
        Dialogs.error("Error", "Could not create asset.", ex);
      }

    };
  }

  void updateTreeAndModel() {
    libraryModel.updateModel();
    libraryTree.updateUI();
  }


  private Asset promptForAsset(AssetType type, AssetCreationService factory) throws AssetException {
    CreateAssetDialog dialog = new CreateAssetDialog(this, library, type, factory);
    if (dialog.show() == ModalDialog.ID_OK) {
      DialogProgressMonitor pm = new DialogProgressMonitor(null, "Adding asset to the library",
          ModalityType.MODELESS);
      return factory.createAsset(dialog.getAssetName(), dialog.getAssetDescription(), dialog.getAssetTags(),
          dialog.getAssetProperties(), pm);
    }
    return null;
  }

  private AssetEditPanel createAssetEditPanel() {
    AssetEditPanel assetEditPanel = new AssetEditPanel();
    assetEditPanel.setBorder(BorderFactory.createTitledBorder("Asset Properties"));
    return assetEditPanel;
  }

  private void onAssetModified(Asset asset) {
    SwingUtilities.invokeLater(() -> {
      libraryModel.updateModel();
      libraryTree.setSelectedAsset(asset);
    });
  }

  private void onTreeSelectionChanged(TreeSelectionEvent e) {
    assetEditPanel.setEditableAsset(libraryTree.getSelectedAsset());
  }


  private class ProductManagerChangeListener implements Listener {

    private final ProductManager productManager;

    public ProductManagerChangeListener(ProductManager productManager) {
      this.productManager = productManager;
    }

    @Override
    public void productAdded(Event event) {
      updateTree();
    }

    @Override
    public void productRemoved(Event event) {
      updateTree();
    }

    private void updateTree() {
      // only necessary to update the tree, if the number of products changes from zero to one or back
      if (productManager.getProductCount() <= 1) {
        updateTreeAndModel();
      }
    }

  }

  private interface IOCallable {

    void executeIO(File file);
  }
}
