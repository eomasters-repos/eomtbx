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

package org.eomasters.quickmenu;

import java.io.IOException;
import java.util.Objects;
import javax.swing.Action;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

public class ActionRef {

  private final String displayName;
  private final String path;
  private long clicks = 0;
  private String actionName;

  public ActionRef(String path, String displayName) {
    this.path = path;
    this.displayName = replaceShortCutIndicator(displayName);
  }

  private static String replaceShortCutIndicator(String displayName) {
    return displayName.replaceAll("&", "");
  }

  public void setActionName(String actionName) {
    this.actionName = actionName;
  }

  public String getActionName() {
    return actionName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getPath() {
    return path;
  }

  public void incrementClicks() {
    clicks++;
  }

  public long getClicks() {
    return clicks;
  }

  public Action getAction() throws Exception {
    try {
      FileObject configFile = FileUtil.getConfigFile(getFullPath());
      DataObject dob = DataObject.find(configFile);
      InstanceCookie ic = dob.getLookup().lookup(InstanceCookie.class);
      if (ic != null) {
        Object instance = ic.instanceCreate();
        if (instance instanceof Action) {
          return ((Action) instance);
        }
      }
      throw new Exception("No Action found for " + getFullPath());
    } catch (IOException | ClassNotFoundException e) {
      throw new Exception("No Action found for " + getFullPath(), e);
    }
  }

  private String getFullPath() {
    return getPath() + "/" + getActionName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActionRef actionRef = (ActionRef) o;
    return Objects.equals(displayName, actionRef.displayName)
        && Objects.equals(path, actionRef.path)
        && Objects.equals(actionName, actionRef.actionName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(displayName, path, actionName);
  }

}
