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


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The QuickMenu. Provides access to the action references which are most frequently used.
 */
public class QuickMenu {

  private List<ActionRef> actionReferences;

  /**
   * Returns the QuickMenu instance.
   *
   * @return the QuickMenu instance
   */
  public static QuickMenu getInstance() {
    return InstanceHolder.INSTANCE;
  }

  /**
   * Creates a new QuickMenu. Only used by the singleton and testing.
   */
  QuickMenu() {
  }

  /**
   * Returns the list of all action references.
   *
   * @return the list of all action references
   */
  public List<ActionRef> getActionReferences() {
    ensureStarted();
    return actionReferences;
  }

  /**
   * Starts and initialises the QuickMenu. This is separated from the constructor because it can take some time.
   *
   * @throws IllegalStateException if the QuickMenu is already started
   */
  public void start() {
    if (isStarted()) {
      throw new IllegalStateException("QuickMenu already started");
    }
    actionReferences = Collections.synchronizedList(ActionRefCollector.collect());
  }

  /**
   * Returns true if the QuickMenu is started.
   *
   * @return true if the QuickMenu is started
   */
  boolean isStarted() {
    return actionReferences != null;
  }

  /**
   * Ensures that the QuickMenu is started.
   *
   * @throws IllegalStateException if the QuickMenu is not started
   */
  private void ensureStarted() {
    if (!isStarted()) {
      throw new IllegalStateException("QuickMenu not started");
    }
  }

  /**
   * Sorts the action references by clicks.
   */
  public void sort() {
    actionReferences.sort(Comparator.comparing(ActionRef::getClicks).reversed());
  }

  private static class InstanceHolder {

    private static final QuickMenu INSTANCE = new QuickMenu();
  }
}
