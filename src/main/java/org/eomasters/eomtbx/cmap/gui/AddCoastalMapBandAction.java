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
package org.eomasters.eomtbx.cmap.gui;


import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.SubProgressMonitor;
import com.bc.ceres.multilevel.MultiLevelModel;
import com.bc.ceres.multilevel.support.DefaultMultiLevelImage;
import com.bc.ceres.multilevel.support.DefaultMultiLevelSource;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.media.jai.OpImage;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.eomtbx.cmap.CoastalMap;
import org.eomasters.eomtbx.cmap.FlagsAndMasks;
import org.eomasters.eomtbx.cmap.ImageUtils;
import org.eomasters.gui.Dialogs;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductNode;
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
    id = "AddCoastalMapBandAction"
)
@ActionRegistration(
    displayName = "#TXT_MENU_AddCoastalMapAction",
    popupText = "#TXT_MENU_AddCoastalMapAction",
    lazy = false // must be not lazy in order to show icon in context menu
)
@ActionReference(
    path = "Context/Product/Product",
    position = 25
)
@NbBundle.Messages({
    "TXT_DIALOG_AddCoastalMapAction=Add EOMasters Coastal Map Band",
    "TXT_MENU_AddCoastalMapAction=Add Coastal Map Band",
})
public class AddCoastalMapBandAction extends AbstractAction implements ContextAwareAction, LookupListener,
    Presenter.Popup, HelpCtx.Provider {
  private static final String DIALOGS_TITLE = "EOMasters Coastal Map";

  static final String HELP_ID = "eomtbx.cmap.band";
  private final Lookup lkp;
  private Product product;

  public static void main(String[] args) throws Exception {
    final JFrame frame = new JFrame("Add EOM CM Band");
    Container contentPane = frame.getContentPane();
    JButton openDialogBtn = new JButton("Open Dialog");
    openDialogBtn.addActionListener(e -> new AddCoastalMapBandAction().actionPerformed(null));
    contentPane.setLayout(new BorderLayout());
    contentPane.add(openDialogBtn, BorderLayout.CENTER);
    frame.setSize(100, 50);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    SwingUtilities.invokeLater(() -> frame.setVisible(true));
  }


  public AddCoastalMapBandAction() {
    this(Utilities.actionsGlobalContext());
  }

  public AddCoastalMapBandAction(Lookup lkp) {
    super(Bundle.TXT_MENU_AddCoastalMapAction());
    putValue(NAME, Bundle.TXT_MENU_AddCoastalMapAction());
    putValue(SMALL_ICON, EomtbxIcons.COASTAL_MAP.getImageIcon(Icon.SIZE_16));
    putValue(LARGE_ICON_KEY, EomtbxIcons.COASTAL_MAP.getImageIcon(Icon.SIZE_24));
    this.lkp = lkp;
    Lookup.Result<ProductNode> lkpContext = lkp.lookupResult(ProductNode.class);
    lkpContext.addLookupListener(WeakListeners.create(LookupListener.class, this, lkpContext));
    setEnableState();
  }

  @Override
  public Action createContextAwareInstance(Lookup actionContext) {
    return new AddCoastalMapBandAction(actionContext);
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
      state = product.getSceneGeoCoding() != null;
    }
    setEnabled(state);
  }

  @Override
  public HelpCtx getHelpCtx() {
    return new HelpCtx(HELP_ID);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CoastalMapBandSettingsPanel bandSettings = new CoastalMapBandSettingsPanel();
    ModalDialog dialog = new ModalDialog(null, Bundle.TXT_DIALOG_AddCoastalMapAction(), bandSettings,
        ModalDialog.ID_OK_CANCEL_HELP, HELP_ID);
    dialog.getJDialog().setIconImage(EomtbxIcons.COASTAL_MAP.getImageIcon(Icon.SIZE_16).getImage());
    dialog.getButton(AbstractDialog.ID_HELP).setText(null);
    dialog.getButton(AbstractDialog.ID_HELP).setIcon(Icons.QUESTION_MARK.getImageIcon(Icon.SIZE_16));

    if (ModalDialog.ID_OK != dialog.show()) {
      return;
    }

    if (product != null) {
      try {
        if (product.containsBand(FlagsAndMasks.getFlagBandName())) {
          String question = String.format("<html>The product contains already a band with the name '%s'.<br>"
              + "Do you want to replace it?", FlagsAndMasks.getFlagBandName());
          if (Dialogs.confirmation(DIALOGS_TITLE, question, null)) {
            product.removeBand(product.getBand(FlagsAndMasks.getFlagBandName()));
          } else {
            return;
          }
        }

        ProgressMonitorSwingWorker<Void, Void> worker = new CoastalMapSwingWorker(bandSettings.getSampling(),
            bandSettings.areMasksEnabled());
        worker.executeWithBlocking();
        worker.get();
      } catch (Exception e) {
        Dialogs.error(DIALOGS_TITLE, "The map band could not be added.", e);
      }
    }
  }

  @Override
  public JMenuItem getPopupPresenter() {
    return new JMenuItem(this);
  }

  private class CoastalMapSwingWorker extends ProgressMonitorSwingWorker<Void, Void> {

    private final String sampling;
    private final boolean addMasks;

    public CoastalMapSwingWorker(String sampling, boolean addMasks) {
      super(null, DIALOGS_TITLE);
      this.sampling = sampling;
      this.addMasks = addMasks;
    }

    @Override
    protected Void doInBackground(ProgressMonitor pm) throws Exception {
      try {
        pm.beginTask("Loading coastal map data", 12);
        final Band band = CoastalMap.addCoastMapFlagBand(product);
        MultiLevelModel levelModel = band.createMultiLevelModel();
        OpImage mapImage = CoastalMap.getCoastMapImage(band, CoastalMapBandSettingsPanel.AGGREGATE.equals(sampling));
        pm.worked(1);
        BufferedImage bufferedImage = ImageUtils.getAsBufferedImage(mapImage, SubProgressMonitor.create(pm, 10));
        if (pm.isCanceled()) {
          product.removeBand(band);
        }
        band.setSourceImage(new DefaultMultiLevelImage(new DefaultMultiLevelSource(bufferedImage, levelModel)));
        if (addMasks) {
          CoastalMap.addCoastalMapMasks(product);
        }
      } finally {
        pm.done();
      }
      return null;
    }

  }

}
