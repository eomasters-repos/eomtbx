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

public class QuickMenu {

  private List<ActionRef> actionReferences;

  public static QuickMenu getInstance() {
    return InstanceHolder.INSTANCE;
  }

  public List<ActionRef> getActionReferences() {
    ensureStarted();
    return actionReferences;
  }

  public void start() {
    if (isStarted()) {
      throw new IllegalStateException("QuickMenu already started");
    }
    actionReferences = Collections.synchronizedList(ActionRefCollector.collect());
  }

  private boolean isStarted() {
    return actionReferences != null;
  }

  private void ensureStarted() {
    if (!isStarted()) {
      throw new IllegalStateException("QuickMenu not started");
    }
  }

  public void actionRefClicked(ActionRef actionRef) {
    actionRef.incrementClicks();
    // keeping the list sorted keeps the sorting fast
    actionReferences.sort(Comparator.comparing(ActionRef::getClicks).reversed());
  }

  private static class InstanceHolder {

    private static final QuickMenu INSTANCE = new QuickMenu();
  }
}
