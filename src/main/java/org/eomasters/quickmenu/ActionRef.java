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

  private final String actionId;
  private final MenuRef menuRef;
  private long clicks = 0;

  public ActionRef(String actionId, MenuRef menuRef) {
    this.actionId = actionId;
    this.menuRef = menuRef;
  }

  public ActionRef(String actionId, String path, String displayName) {
    this(actionId, new MenuRef(path, replaceShortCutIndicator(displayName)));
  }

  private static String replaceShortCutIndicator(String displayName) {
    return displayName.replaceAll("&", "");
  }

  public MenuRef getMenuRef() {
    return menuRef;
  }

  public void incrementClicks() {
    System.out.println("Clicked = " + this);
    clicks++;
  }

  public long getClicks() {
    return clicks;
  }

  @Override
  public String toString() {
    return String.format("ActionRef{displayName='%s', path='%s', clicks=%d, actionName='%s'}",
        getMenuRef().getText(), getMenuRef().getPath(), clicks, actionId);
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
    return Objects.equals(getMenuRef(), actionRef.getMenuRef())
        && Objects.equals(actionId, actionRef.actionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getMenuRef(), actionId);
  }
}
