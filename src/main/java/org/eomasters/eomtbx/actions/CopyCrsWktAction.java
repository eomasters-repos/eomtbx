/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Basic for SNAP
 * -> https://www.eomasters.org/eomtbx
 * ======================================================================
 * Copyright (C) 2023 - 2025 Marco Peters
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

package org.eomasters.eomtbx.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.eomasters.icons.Icons;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductNode;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

@ActionID(category = "Tools", id = "CopyCrsWktAction")
@ActionRegistration(displayName = "#CopyCrsWktAction", popupText = "#CopyCrsWktAction", lazy = false)
@ActionReferences({
    // 220 is detach pixel-geocoding
    @ActionReference(path = "Menu/Tools", position = 221,separatorBefore = 220),
    // group nodes by type: position = 30,separatorAfter = 35,separatorBefore = 25
    @ActionReference(path = "Context/Product/Product", position = 36, separatorAfter = 37)})
@Messages({"CopyCrsWktAction=Copy CRS WKT to Clipboard"})
public class CopyCrsWktAction extends AbstractAction implements ContextAwareAction, LookupListener {

  private final Lookup lkp;

  public CopyCrsWktAction() {
    this(Utilities.actionsGlobalContext());
  }

  private CopyCrsWktAction(Lookup lkp) {
    super(Bundle.CopyCrsWktAction());
    this.lkp = lkp;
    Lookup.Result<ProductNode> lkpContext = lkp.lookupResult(ProductNode.class);
    lkpContext.addLookupListener(WeakListeners.create(LookupListener.class, this, lkpContext));
    putValue(Action.SHORT_DESCRIPTION, Bundle.CopyCrsWktAction());
    setEnableState();
  }

  @Override
  public Action createContextAwareInstance(Lookup actionContext) {
    return new CopyCrsWktAction(actionContext);
  }

  @Override
  public void resultChanged(LookupEvent ev) {
    setEnableState();
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {

    GeoCoding geoCoding = getGeoCoding(lkp.lookup(ProductNode.class));
    CoordinateReferenceSystem mapCrs = geoCoding.getMapCRS();
    String mapCrsWKT = mapCrs.toWKT();
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    StringSelection stringSelection = new StringSelection(mapCrsWKT);
    clipboard.setContents(stringSelection, null);

    NotificationDisplayer.getDefault().notify("Successful",
        Icons.THUMBS_UP.getImageIcon(16), "Copied CRS WKT to the clipboard.", null);
  }

  private void setEnableState() {
    ProductNode productNode = lkp.lookup(ProductNode.class);
    GeoCoding geoCoding = getGeoCoding(productNode);
    setEnabled(geoCoding != null);
  }

  private static GeoCoding getGeoCoding(ProductNode productNode) {
    if (productNode != null) {
      if (productNode instanceof RasterDataNode) {
        RasterDataNode raster = (RasterDataNode) productNode;
        return raster.getGeoCoding();
      } else {
        Product product = productNode.getProduct();
        return product.getSceneGeoCoding();
      }
    }
    return null;
  }


}
