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

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.preferences.PropertyChangeOptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Controller for the View Bookmarks options panel. Allows to remove bookmarks and import and export.
 */
public class ViewBookmarksOptionsPanelController extends PropertyChangeOptionsPanelController {

  public static final String HID_EOMTBX_VIEW_BOOKMARKS = "eomtbxViewBookmarks";
  private JPanel mainPanel;
  private ViewBookmarks currentBookmarks;
  private ViewBookmarks backup;

  @Override
  public void update() {
    fireChange();
  }

  @Override
  public void applyChanges() {
    storePreferences();
    backup = currentBookmarks.clone();
    fireChange();
  }

  @Override
  public void cancel() {
    currentBookmarks = backup.clone();
    update();
    fireChange();
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public boolean isChanged() {
    if (currentBookmarks == null) {
      return false;
    }
    return !currentBookmarks.equals(backup);
  }

  @Override
  public JComponent getComponent(Lookup masterLookup) {
    if (mainPanel == null) {
      MigLayout mainLayout = new MigLayout("wrap 1, gap 5, insets 5");
      mainPanel = new JPanel(mainLayout);
      mainPanel.setBorder(new TitledBorder("Quick Menu"));

      //TODO - edit components here

    }
    return mainPanel;
  }

  @Override
  public HelpCtx getHelpCtx() {
    return new HelpCtx(HID_EOMTBX_VIEW_BOOKMARKS);
  }

  private void storePreferences() {
    // ViewBookmarks.getInstance().save(currentBookmarks);
  }
}
