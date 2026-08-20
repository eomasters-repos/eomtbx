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

package org.eomasters.eomtbx.utils;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import java.text.MessageFormat;
import java.util.Objects;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.esa.snap.core.datamodel.ImageInfo;
import org.esa.snap.core.datamodel.RGBChannelDef;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.util.PreferencesPropertyMap;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.rcp.actions.window.OpenImageViewAction;
import org.esa.snap.rcp.actions.window.OpenRGBImageViewAction;
import org.esa.snap.ui.UIUtils;
import org.esa.snap.ui.product.ProductSceneImage;
import org.esa.snap.ui.product.ProductSceneView;

@SuppressWarnings({"unused", "Used in SNAPKIT"})
public class SceneViewUtils {

  private SceneViewUtils() {
  }

  public static void openRaster(RasterDataNode raster) {
    OpenImageViewAction.openImageView(raster);
  }

  public static void openRgb(String name, RasterDataNode red, RasterDataNode green, RasterDataNode blue) {

    String viewName = Objects.requireNonNullElseGet(name, () -> "RGB_" + red.getName() + "_" + green.getName() + "_"
        + blue.getName());

    var snapApp = SnapApp.getDefault();
    SwingWorker<ProductSceneImage, Object> worker = new ProgressMonitorSwingWorker<>(
        snapApp.getMainFrame(), "Opening RGB image...") {

      @Override
      protected ProductSceneImage doInBackground(com.bc.ceres.core.ProgressMonitor pm) {
        final var preferences = snapApp.getPreferences();
        final var preferencesPropertyMap = new PreferencesPropertyMap(preferences);

        var productSceneImage = new ProductSceneImage(viewName, red, green, blue,
                                                      preferencesPropertyMap, ProgressMonitor.NULL);
        productSceneImage.initVectorDataCollectionLayer();
        productSceneImage.initMaskCollectionLayer();

        var rasterDataNodes = new String[]{red.getName(), green.getName(), blue.getName()};
        var rgbBands = new RasterDataNode[]{red, green, blue};
        final RGBChannelDef userRgbChannelDef = new RGBChannelDef(rasterDataNodes);
        for (int i = 0; i < rgbBands.length; i++) {
          RasterDataNode rgbBand = rgbBands[i];
          var imageInfo = rgbBand.getImageInfo(ProgressMonitor.NULL);
          var colorPaletteDef = imageInfo.getColorPaletteDef();
          userRgbChannelDef.setMinDisplaySample(i, colorPaletteDef.getFirstPoint().getSample());
          userRgbChannelDef.setMaxDisplaySample(i, colorPaletteDef.getLastPoint().getSample());
        }
        final ImageInfo imageInfo = new ImageInfo(userRgbChannelDef);
        productSceneImage.setImageInfo(imageInfo);
        return productSceneImage;
      }

      @Override
      public void done() {
        SwingUtilities.invokeLater(() -> {
          UIUtils.setRootFrameDefaultCursor(snapApp.getMainFrame());
          snapApp.setStatusBarMessage("");
          try {
            ProductSceneView productSceneView = new ProductSceneView(get());
            OpenRGBImageViewAction.openDocumentWindow(productSceneView);
          } catch (Exception e) {
            snapApp.handleError(MessageFormat.format("Failed to open image view.\n\n{0}", e.getMessage()), e);
          }
        });
      }
    };
    worker.execute();
  }
}
