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

import com.bc.ceres.grender.Viewport;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
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
import org.esa.snap.rcp.SnapApp;
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

/**
 * The class inserts the View Bookmarks menu into the menu bar and the context menu of the scene view .
 */
@ActionID(category = "View", id = "EOMTBX_ViewBookmarksAction")
@ActionRegistration(displayName = "#ViewBookmarksActionMenuText", menuText = "#ViewBookmarksActionMenuText", popupText = "View-Bookmarks  >", lazy = false)
@ActionReferences({@ActionReference(path = "Menu/View", position = 400),
    @ActionReference(path = "Context/ProductSceneView", position = 70)})
@NbBundle.Messages({"ViewBookmarksActionMenuText=View-Bookmarks", "OpenOptionsMenuText=Open Options",
    "AddBookmarkMenuText=Add Bookmark"})
public final class ViewBookmarksAction extends AbstractAction implements LookupListener, Presenter.Menu {

  @SuppressWarnings("FieldCanBeLocal")
  private final Lookup.Result<ProductSceneView> result;
  private final JMenu mainMenu;

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
    super(Bundle.ViewBookmarksActionMenuText() + "  >");
    result = lookup.lookupResult(ProductSceneView.class);
    result.addLookupListener(WeakListeners.create(LookupListener.class, this, result));
    mainMenu = new JMenu(Bundle.ViewBookmarksActionMenuText());
    mainMenu.addMouseListener(new MenuUpdater(mainMenu));
  }

  @Override
  public JMenuItem getMenuPresenter() {
    return updateMenu(mainMenu);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JComponent source = (JComponent) e.getSource();
    JComponent[] menuItems = createMenuItems();
    JPopupMenu popupMenu = new JPopupMenu();
    for (JComponent menuItem : menuItems) {
      popupMenu.add(menuItem);
      menuItem.addMouseListener(new MouseInputAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          popupMenu.setVisible(false);
        }
      });
    }

    Container parent = source.getParent();
    parent.setVisible(true);
    Point locationOnScreen = parent.getLocationOnScreen();
    locationOnScreen.setLocation(locationOnScreen.getX() + source.getLocation().getX(),
        locationOnScreen.getY() + source.getLocation().getY());
    locationOnScreen.setLocation(locationOnScreen.getX() + source.getWidth(), locationOnScreen.getY());
    popupMenu.setLocation(locationOnScreen);
    popupMenu.addMouseListener(new MouseExitListener(popupMenu));

    popupMenu.setVisible(true);
  }

  @Override
  public void resultChanged(LookupEvent ev) {
    setEnabled(SnapApp.getDefault().getSelectedProductSceneView() != null);
  }

  private JMenu updateMenu(JMenu menu) {
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
      Viewport viewport = sceneView.getViewport();
      Rectangle viewBounds = viewport.getViewBounds();
      Point ulLocation = viewBounds.getLocation();
      GeoPos upperLeft = geoCoding.getGeoPos(new PixelPos(ulLocation.getX(), ulLocation.getY()), new GeoPos());
      GeoPos upperRight = geoCoding.getGeoPos(
          new PixelPos(ulLocation.getX() + viewBounds.getWidth(), ulLocation.getY()), new GeoPos());
      GeoPos lowerRight = geoCoding.getGeoPos(
          new PixelPos(ulLocation.getX() + viewBounds.getWidth(), ulLocation.getY() + viewBounds.getHeight()),
          new GeoPos());
      GeoPos lowerLeft = geoCoding.getGeoPos(
          new PixelPos(ulLocation.getX(), ulLocation.getY() + viewBounds.getHeight()), new GeoPos());
      String bookmarkName = Dialogs.input("Create Bookmark", "Enter name for bookmark");
      if (bookmarkName == null || bookmarkName.isEmpty()) {
        return;
      }
      Bookmark bookmark = new Bookmark(bookmarkName, new BookmarkRoi(upperLeft, upperRight, lowerRight, lowerLeft),
          sceneView.getOrientation());
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
      GeoCoding geoCoding;
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
      sceneView.getViewport().setOrientation(bookmark.getRotation());
      BookmarkRoi bookmarkRoi = bookmark.getRoi();
      PixelPos upperLeft = geoCoding.getPixelPos(bookmarkRoi.getUpperLeft(), new PixelPos());
      PixelPos upperRight = geoCoding.getPixelPos(bookmarkRoi.getUpperRight(), new PixelPos());
      PixelPos lowerRight = geoCoding.getPixelPos(bookmarkRoi.getLowerRight(), new PixelPos());
      PixelPos lowerLeft = geoCoding.getPixelPos(bookmarkRoi.getLowerLeft(), new PixelPos());
      sceneView.getViewport().setViewBounds(
          new Rectangle((int) upperLeft.getX(), (int) upperLeft.getY(), (int) (lowerRight.getX() - upperLeft.getX()),
              (int) (lowerRight.getY() - upperLeft.getY())));

    }
  }

  private static class MouseExitListener extends MouseInputAdapter {

    private final JPopupMenu popupMenu;
    int countDown = 1;

    public MouseExitListener(JPopupMenu popupMenu) {
      this.popupMenu = popupMenu;
    }

    @Override
    public void mouseExited(MouseEvent e) {
      if (countDown-- == 0) {
        popupMenu.setVisible(false);
      }
    }

  }

  private class MenuUpdater extends MouseInputAdapter {


    private final JMenu menu;

    public MenuUpdater(JMenu mainMenu) {
      menu = mainMenu;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      updateMenu(menu);
    }
  }
}
