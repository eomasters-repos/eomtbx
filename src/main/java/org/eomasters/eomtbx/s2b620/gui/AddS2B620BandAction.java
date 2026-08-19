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

/*
 * Copyright (C) 2015 by Array Systems Computing Inc. http://www.array.ca
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.eomasters.eomtbx.s2b620.gui;


import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.binding.PropertyPane;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.eomtbx.s2b620.S2B620;
import org.eomasters.eomtbx.s2b620.S2B620Operator.Spi;
import org.eomasters.eomtbx.utils.BandUtils;
import org.eomasters.gui.Dialogs;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductNode;
import org.esa.snap.core.gpf.descriptor.OperatorDescriptor;
import org.esa.snap.core.gpf.ui.OperatorParameterSupport;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.ui.AbstractDialog;
import org.esa.snap.ui.ModalDialog;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

@ActionID(
    category = "Tools",
    id = "AddS2B620BandAction"
)
@ActionRegistration(
    displayName = "#TXT_MENU_AddS2B620BandAction",
    popupText = "#TXT_MENU_AddS2B620BandAction",
    lazy = false // must be not lazy in order to show icon in context menu
)
@ActionReference(
    path = "Context/Product/Product",
    position = 25
)
@NbBundle.Messages({
    "TXT_DIALOG_AddS2B620BandAction=Add Band at 620",
    "TXT_MENU_AddS2B620BandAction=Add Band at 620",
})
public class AddS2B620BandAction extends AbstractAction implements ContextAwareAction, LookupListener,
    Presenter.Popup, HelpCtx.Provider {

  private static final String DIALOGS_TITLE = "Add Band at 620";
  private static final String HELP_ID = "eomtbx.s2b620.band";

  private final OperatorDescriptor operatorDescriptor;
  private final Lookup lkp;

  private Product product;
  private Band band665;

  public static void main(String[] args) throws Exception {
    final JFrame frame = new JFrame("Add S2620 Band");
    Container contentPane = frame.getContentPane();
    JButton openDialogBtn = new JButton("Open Dialog");
    openDialogBtn.addActionListener(e -> new AddS2B620BandAction().actionPerformed(null));
    contentPane.setLayout(new BorderLayout());
    contentPane.add(openDialogBtn, BorderLayout.CENTER);
    frame.setSize(100, 50);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    SwingUtilities.invokeLater(() -> frame.setVisible(true));
  }


  public AddS2B620BandAction() {
    this(Utilities.actionsGlobalContext());
  }

  public AddS2B620BandAction(Lookup lkp) {
    super(Bundle.TXT_MENU_AddS2B620BandAction());
    putValue(NAME, Bundle.TXT_MENU_AddS2B620BandAction());
    putValue(SMALL_ICON, EomtbxIcons.EOMTBX.getImageIcon(Icon.SIZE_16));
    putValue(LARGE_ICON_KEY, EomtbxIcons.EOMTBX.getImageIcon(Icon.SIZE_24));
    this.lkp = lkp;
    Lookup.Result<ProductNode> lkpContext = lkp.lookupResult(ProductNode.class);
    lkpContext.addLookupListener(WeakListeners.create(LookupListener.class, this, lkpContext));
    setEnableState();
    operatorDescriptor = new Spi().getOperatorDescriptor();
  }

  @Override
  public Action createContextAwareInstance(Lookup actionContext) {
    return new AddS2B620BandAction(actionContext);
  }

  @Override
  public void resultChanged(LookupEvent ev) {
    setEnableState();
  }

  private void setEnableState() {
    ProductNode productNode = lkp.lookup(ProductNode.class);
    boolean state = false;
    if (productNode != null) {
      product = productNode.getProduct();
      band665 = BandUtils.findClosestToCenterBand(product.getBands(), 664, 666);
      state = band665 != null;
    }
    setEnabled(state);
  }

  @Override
  public HelpCtx getHelpCtx() {
    return new HelpCtx(HELP_ID);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    if (product.containsBand(S2B620.B620_NAME)) {
      String question = String.format("<html>The product contains already a band with the name '%s'.<br>"
                                          + "Do you want to replace it?", S2B620.B620_NAME);
      if (Dialogs.confirmation(DIALOGS_TITLE, question, null)) {
        product.removeBand(product.getBand(S2B620.B620_NAME));
      } else {
        return;
      }
    }

    OperatorParameterSupport parameterSupport = new OperatorParameterSupport(operatorDescriptor);
    PropertySet propertySet = parameterSupport.getPropertySet();
    propertySet.removeProperty(propertySet.getProperty("includeSourceBands"));
    BindingContext bindingContext = new BindingContext(propertySet);
    PropertyPane parametersPane = new PropertyPane(bindingContext);
    JPanel parametersPanel = parametersPane.createPanel();

    ModalDialog dialog = new ModalDialog(null, Bundle.TXT_DIALOG_AddS2B620BandAction(), parametersPanel,
                                         ModalDialog.ID_OK_CANCEL_HELP, HELP_ID);
    JDialog jDialog = dialog.getJDialog();
    jDialog.setPreferredSize(new Dimension(400, 200));
    jDialog.setIconImage(EomtbxIcons.EOMTBX.getImageIcon(Icon.SIZE_16).getImage());
    dialog.getButton(AbstractDialog.ID_HELP).setText(null);
    dialog.getButton(AbstractDialog.ID_HELP).setIcon(Icons.QUESTION_MARK.getImageIcon(Icon.SIZE_16));

    if (ModalDialog.ID_OK != dialog.show()) {
      return;
    }
    try {
      ProgressMonitorSwingWorker<Band, Band> worker = new S2B620SwingWorker(product, band665, propertySet);
      worker.executeWithBlocking();
      Band b620 = worker.get();
      int indexB665 = product.getBandGroup().indexOf(band665);
      product.getBandGroup().add(indexB665, b620);
      ProductUtils.copyGeoCoding(band665, b620);
    } catch (Exception e) {
      Dialogs.error(DIALOGS_TITLE, "The band at 620 could not be added.", e);
    }
  }

  @Override
  public JMenuItem getPopupPresenter() {
    return new JMenuItem(this);
  }

  private class S2B620SwingWorker extends ProgressMonitorSwingWorker<Band, Band> {


    private final Product srcProduct;
    private final Band band666;
    private final PropertySet propertySet;

    public S2B620SwingWorker(Product product, Band band665, PropertySet propertySet) {
      super(null, DIALOGS_TITLE);
      srcProduct = product;
      band666 = band665;
      this.propertySet = propertySet;
    }

    @Override
    protected Band doInBackground(ProgressMonitor pm) {
      try {
        pm.beginTask("Deriving Band 620", 1);
        S2B620 s2b620 = new S2B620(srcProduct);
        s2b620.setLimitInputRange(propertySet.getValue("limitInputRange"));
        s2b620.setValidExpression(propertySet.getValue("validExpression"));
        s2b620.setShapefile(propertySet.getValue("shapefile"));
        s2b620.setWktRegion(propertySet.getValue("wktRegion"));
        return s2b620.deriveBand620From(band666);
      } finally {
        pm.done();
      }
    }

  }

}
