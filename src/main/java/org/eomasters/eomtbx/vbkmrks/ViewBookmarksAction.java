/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * --> https://www.eomasters.org/sw/EOMTBX
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
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.vbkmrks;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

@ActionID(category = "View",id = "EOMTBX_ViewBookmarksAction")
@ActionRegistration(
        displayName = "#ViewBookmarksActionName",
        menuText = "#ViewBookmarksActionMenuText",
        lazy = false
)
@ActionReference(path = "Menu/File", position = 10)
@NbBundle.Messages({
        "ViewBookmarksActionName=View Bookmarks",
        "ViewBookmarksActionMenuText=View Bookmarks",
        "OpenSettingsMenuText=Open Settings",
        "CreateBookmarkMenuText=Create Bookmark"
})
public final class ViewBookmarksAction extends AbstractAction implements Presenter.Toolbar, Presenter.Menu, Presenter.Popup {

    private final int MAX_BOOKMARKS = 7;

    @Override
    public JMenuItem getMenuPresenter() {

        // List<File> openedFiles = OpenProductAction.getOpenedProductFiles();
        // List<String> pathList = getRecentProductPaths().get();
        //
        // final Preferences preference = SnapApp.getDefault().getPreferences();
        // int maxFileList = preference.getInt(UiBehaviorController.PREFERENCE_KEY_LIST_FILES_TO_REOPEN, MAX_BOOKMARKS);
        //
        // // Add "open recent product file" actions
        JMenu menu = new JMenu(Bundle.ViewBookmarksActionMenuText());
        //
        //
        // pathList.stream().limit(maxFileList).forEach(path->{
        //     File theFile = new File(path);
        //     if (!openedFiles.contains(theFile) && theFile.exists()) {
        //         JMenuItem menuItem = new JMenuItem(path);
        //         OpenProductAction openProductAction = new OpenProductAction();
        //         openProductAction.setFile(theFile);
        //         menuItem.addActionListener(openProductAction);
        //         menu.add(menuItem);
        //     }
        // });
        //
        //
        // // Add "Clear List" action
        // if (menu.getComponentCount() > 0 || pathList.size() > 0) {
        //     menu.addSeparator();
        //     JMenuItem menuItem = new JMenuItem(Bundle.ClearListActionMenuText());
        //     menuItem.addActionListener(e -> getRecentProductPaths().clear());
        //     menu.add(menuItem);
        // }
        return menu;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return getMenuPresenter();
    }

    @Override
    public Component getToolbarPresenter() {
        return getMenuPresenter();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // do nothing
    }
}
