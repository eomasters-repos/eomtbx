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

import java.util.Objects;

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
