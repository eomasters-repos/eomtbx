/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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

package org.eomasters.eomtbx.vbkmrks;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.grender.Viewport;
import eu.esa.snap.netbeans.docwin.DocumentWindowManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MouseInputAdapter;
import org.eomasters.eomtbx.preferences.EomtbxOptionsPanelController;
import org.eomasters.eomtbx.utils.Dialogs;
import org.eomasters.eomtbx.utils.ErrorHandler;
import org.eomasters.eomtbx.vbkmrks.ViewBookmarks.Bookmark;
import org.eomasters.eomtbx.vbkmrks.ViewBookmarks.BookmarkRoi;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.rcp.windows.ProductSceneViewTopComponent;
import org.esa.snap.ui.product.ProductSceneImage;
import org.esa.snap.ui.product.ProductSceneView;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.windows.WindowManager;

/**
 * The class inserts the View Bookmarks menu into the menu bar and the context menu of the scene view .
 */
@ActionID(category = "View", id = "EOMTBX_ViewBookmarksAction")
@ActionRegistration(displayName = "#ViewBookmarksActionMenuText", menuText = "#ViewBookmarksActionMenuText", lazy = false)
@ActionReferences({@ActionReference(path = "Menu/View", position = 400),
    @ActionReference(path = "Context/ProductSceneView", position = 70)})
@NbBundle.Messages({"ViewBookmarksActionMenuText=View-Bookmarks", "OpenOptionsMenuText=Open Options",
    "AddBookmarkMenuText=Add Bookmark"})
public final class ViewBookmarksAction extends AbstractAction implements LookupListener, Presenter.Menu,
    Presenter.Popup {

  @SuppressWarnings("FieldCanBeLocal")
  private final Lookup.Result<ProductSceneView> result;
  private final JMenu mainMenu;
  private final JMenu contextMenu;

  /**
   * Creates a new ViewBookmarksAction.
   */
  @SuppressWarnings("unused")
  public ViewBookmarksAction() {
    this(Utilities.actionsGlobalContext());
  }

  /**
   * Creates a new ViewBookmarksAction.
   *
   * @param lookup the lookup
   */
  public ViewBookmarksAction(Lookup lookup) {
    super(Bundle.ViewBookmarksActionMenuText());
    result = lookup.lookupResult(ProductSceneView.class);
    result.addLookupListener(WeakListeners.create(LookupListener.class, this, result));
    contextMenu = new JMenu(Bundle.ViewBookmarksActionMenuText());
    contextMenu.addMouseListener(new MenuUpdater(contextMenu));
    mainMenu = new JMenu(Bundle.ViewBookmarksActionMenuText());
    mainMenu.addMouseListener(new MenuUpdater(mainMenu));
  }

  @Override
  public JMenuItem getMenuPresenter() {
    return updateMenu(mainMenu);
  }

  @Override
  public JMenuItem getPopupPresenter() {
    return updateMenu(contextMenu);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // do nothing
  }

  @Override
  public void resultChanged(LookupEvent ev) {
    setEnabled(SnapApp.getDefault().getSelectedProductSceneView() != null);
  }

  private static JMenu updateMenu(JMenu menu) {
    JComponent[] menuItems = createMenuItems();
    menu.removeAll();
    for (JComponent menuItem : menuItems) {
      menu.add(menuItem);
    }

    return menu;
  }

  private static JComponent[] createMenuItems() {
    List<JComponent> items = new ArrayList<>();
    ProductSceneView sceneView = SnapApp.getDefault().getSelectedProductSceneView();

    items.add(createAddAction(sceneView));

    items.add(new JPopupMenu.Separator());
    JComponent[] bookmarkItems = createApplyBookmarkItems(sceneView);
    Collections.addAll(items, bookmarkItems);
    items.add(new JPopupMenu.Separator());

    items.add(createOptionsItem());

    return items.toArray(new JComponent[0]);
  }

  private static JMenuItem createOptionsItem() {
    JMenuItem menuItem = new JMenuItem(Bundle.OpenOptionsMenuText());
    menuItem.addActionListener(e -> OptionsDisplayer.getDefault().open(EomtbxOptionsPanelController.OPTIONS_ID));
    return menuItem;
  }

  private static JMenuItem createAddAction(ProductSceneView sceneView) {
    JMenuItem addAction = new JMenuItem(Bundle.AddBookmarkMenuText());
    if (sceneView != null) {
      GeoCoding geoCoding = sceneView.getRaster().getGeoCoding();
      boolean usableGeoCoding = geoCoding != null && geoCoding.canGetGeoPos();
      addAction.setEnabled(usableGeoCoding);
      if (!usableGeoCoding) {
        addAction.setToolTipText("Scene view must be geocoded to add a bookmark");
      }
      boolean canAddBookmark = ViewBookmarks.getInstance().getBookmarks().size() < ViewBookmarks.MAX_BOOKMARKS;
      addAction.setEnabled(addAction.isEnabled() && canAddBookmark);
      if (!canAddBookmark) {
        addAction.setToolTipText("Number of bookmarks reached maximum of " + ViewBookmarks.MAX_BOOKMARKS);
      }
    } else {
      addAction.setEnabled(false);
      addAction.setToolTipText("No scene view selected");
    }

    addAction.addActionListener(e -> {
      GeoCoding geoCoding = sceneView.getRaster().getGeoCoding();
      if (geoCoding == null || !geoCoding.canGetGeoPos()) {
        return;
      }
      String bookmarkName = Dialogs.input("Create Bookmark", "Enter name for bookmark");
      if (bookmarkName == null || bookmarkName.isEmpty()) {
        return;
      }

      Viewport viewport = sceneView.getViewport();
      Rectangle viewBounds = sceneView.getViewport().getViewBounds();
      Rectangle visibleImageBounds = sceneView.getVisibleImageBounds();

      Point ulLocation = visibleImageBounds.getLocation();
      double visImageX = ulLocation.getX();
      double visImageY = ulLocation.getY();
      double width = visibleImageBounds.getWidth();
      double height = visibleImageBounds.getHeight();
      GeoPos upperLeft = geoCoding.getGeoPos(new PixelPos(visImageX, visImageY), new GeoPos());
      GeoPos upperRight = geoCoding.getGeoPos(new PixelPos(visImageX + width, visImageY), new GeoPos());
      GeoPos lowerRight = geoCoding.getGeoPos(new PixelPos(visImageX + width, visImageY + height), new GeoPos());
      GeoPos lowerLeft = geoCoding.getGeoPos(new PixelPos(visImageX, visImageY + height), new GeoPos());
      Bookmark bookmark = new Bookmark(bookmarkName, viewBounds,
          new BookmarkRoi(upperLeft, upperRight, lowerRight, lowerLeft), sceneView.getOrientation());
      try {
        ViewBookmarks.getInstance().add(bookmark);
      } catch (Exception ex) {
        ErrorHandler.handleError("Create Bookmark", "Could not add bookmark", ex);
      }
    });
    return addAction;
  }

  private static JComponent[] createApplyBookmarkItems(ProductSceneView sceneView) {
    List<JComponent> items = new ArrayList<>();
    ViewBookmarks viewBookmarks = ViewBookmarks.getInstance();
    ArrayList<Bookmark> bookmarks = viewBookmarks.getBookmarks();
    if (bookmarks.isEmpty()) {
      JMenuItem menuItem = new JMenuItem("<No Bookmarks yet>");
      menuItem.setToolTipText("Please use the menu to add bookmarks");
      menuItem.setEnabled(false);
      items.add(menuItem);
    }
    for (Bookmark bookmark : bookmarks) {
      JMenuItem menuItem = new JMenuItem("Apply " + bookmark.getName());
      GeoCoding geoCoding = null;
      if (sceneView != null) {
        geoCoding = sceneView.getRaster().getGeoCoding();
        if (geoCoding == null || !geoCoding.canGetPixelPos()) {
          createAddAction(sceneView).setToolTipText("Scene view must be geocoded to add a bookmark");
          menuItem.setEnabled(false);
        } else {
          menuItem.setToolTipText(bookmark.getRoi() + " ;  Orientation: " + bookmark.getRotation());
        }
        menuItem.addActionListener(new ApplyBookmarkAction(sceneView, bookmark));
      } else {
        menuItem.setEnabled(false);
        menuItem.setToolTipText("No scene view selected");
      }
      items.add(menuItem);
    }
    return items.toArray(new JComponent[0]);
  }

  private static class ApplyBookmarkAction implements ActionListener {

    private final ProductSceneView sceneView;
    private final Bookmark bookmark;

    public ApplyBookmarkAction(ProductSceneView sceneView, Bookmark bookmark) {
      this.sceneView = sceneView;
      this.bookmark = bookmark;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      GeoCoding geoCoding = null;
      if (sceneView != null) {
        geoCoding = sceneView.getRaster().getGeoCoding();
      }
      if (geoCoding == null || !geoCoding.canGetPixelPos()) {
        return;
      }
      // ProductSceneViewTopComponent psvTopComponent = WindowUtilities.getOpened(ProductSceneViewTopComponent.class)
      //     .filter(psvTc -> sceneView == psvTc.getView()).findFirst().orElse(null);
      // if (psvTopComponent == null) {
      //   return;
      // }

      WindowManager wm = WindowManager.getDefault();
      ProductSceneImage origSceneImage = sceneView.getSceneImage();
      ProductSceneImage newSceneImage;
      RasterDataNode[] imageRasters = origSceneImage.getRasters();
      if (imageRasters.length == 1) {
        newSceneImage = new ProductSceneImage(imageRasters[0], origSceneImage.getConfiguration(), ProgressMonitor.NULL);
      } else {
        newSceneImage = new ProductSceneImage(origSceneImage.getName() + "_" + bookmark.getName(), imageRasters[0],
            imageRasters[1], imageRasters[2], origSceneImage.getConfiguration(), ProgressMonitor.NULL);
      }
      ProductSceneView productSceneView = new ProductSceneView(newSceneImage);
      productSceneView.getViewport().setViewBounds(bookmark.getViewBounds());
      ProductSceneViewTopComponent floatingPsv = new ProductSceneViewTopComponent(productSceneView, null);
      DocumentWindowManager.getDefault().openWindow(floatingPsv);
      floatingPsv.setBounds(bookmark.getViewBounds());
      floatingPsv.invalidate();
      floatingPsv.getParent().doLayout();
      // floatingPsv.setPreferredSize(bookmark.getViewBounds().getSize());
      wm.setTopComponentFloating(floatingPsv, true);
      floatingPsv.requestSelected();
      productSceneView.getViewport().setOrientation(bookmark.getRotation());

      // sceneView.getViewport().setOrientation(bookmark.getRotation());
      BookmarkRoi bookmarkRoi = bookmark.getRoi();
      PixelPos upperLeft = geoCoding.getPixelPos(bookmarkRoi.getUpperLeft(), new PixelPos());
      // PixelPos upperRight = geoCoding.getPixelPos(bookmarkRoi.getUpperRight(), new PixelPos());
      PixelPos lowerRight = geoCoding.getPixelPos(bookmarkRoi.getLowerRight(), new PixelPos());
      // PixelPos lowerLeft = geoCoding.getPixelPos(bookmarkRoi.getLowerLeft(), new PixelPos());

      AffineTransform imageToModelTransform = productSceneView.getBaseImageLayer().getImageToModelTransform();
      imageToModelTransform.transform(upperLeft, upperLeft);
      // imageToModelTransform.transform(upperRight, upperRight);
      imageToModelTransform.transform(lowerRight, lowerRight);
      // imageToModelTransform.transform(lowerLeft, lowerLeft);
      Rectangle2D rectangle2D = new Rectangle((int) upperLeft.getX(), (int) upperLeft.getY(),
          (int) (lowerRight.getX() - upperLeft.getX()), (int) (lowerRight.getY() - upperLeft.getY()));
      rectangle2D = productSceneView.getViewport().getModelToViewTransform().createTransformedShape(rectangle2D)
          .getBounds2D();
      productSceneView.zoom(500, 300, 3.5);
      // productSceneView.zoom(rectangle2D.getCenterX(), rectangle2D.getCenterY(), productSceneView.getZoomFactor());
      System.out.println();

    }


  }
  private static class MenuUpdater extends MouseInputAdapter {


    private final JMenu menu;

    public MenuUpdater(JMenu mainMenu) {
      menu = mainMenu;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      updateMenu(menu);
    }
  }}
