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

package org.eomasters.eomtbx.quickmenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an action in the QuickMenu. It holds the original actionId and a list of MenuRefs also it counts the
 * clicks.
 */
public class ActionRef {

  private final String actionId;
  private final List<MenuRef> menuRefs;
  private long clicks = 0;

  /**
   * Creates a new ActionRef with the given actionId and menuRef.
   *
   * @param actionId the actionId
   * @param menuRef  the menuRef
   */
  public ActionRef(String actionId, MenuRef menuRef) {
    this.actionId = actionId;
    this.menuRefs = new ArrayList<>();
    menuRefs.add(menuRef);
  }

  /**
   * Returns the actionId.
   *
   * @return the actionId
   */
  public String getActionId() {
    return actionId;
  }

  /**
   * Returns the first MenuRef.
   *
   * @return the first MenuRef
   */
  public MenuRef getMenuRef() {
    return menuRefs.get(0);
  }

  /**
   * Returns the list of MenuRefs.
   *
   * @return the list of MenuRefs
   */
  public List<MenuRef> getMenuRefs() {
    return menuRefs;
  }

  /**
   * Adds a MenuRef to the list of MenuRefs.
   *
   * @param menuRef the MenuRef to add
   */
  public void addMenuRef(MenuRef menuRef) {
    menuRefs.add(menuRef);
  }

  /**
   * Increments the click counter by one.
   */
  public void incrementClicks() {
    clicks++;
  }

  /**
   * Returns the click counter.
   *
   * @return the number of clicks
   */
  public long getClicks() {
    return clicks;
  }

  @Override
  public String toString() {
    return String.format("ActionRef{actionId='%s', clicks=%d, menuRefs=%s}", actionId, clicks, menuRefs);
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
    return Objects.equals(getMenuRefs(), actionRef.getMenuRefs())
        && Objects.equals(actionId, actionRef.actionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getMenuRefs(), actionId);
  }

}
