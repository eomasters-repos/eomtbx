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

package org.eomasters.eomtbx.quickmenu.gui;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;
import org.eomasters.eomtbx.quickmenu.QuickMenu;

/**
 * Options for the QuickMenu.
 */
class QuickMenuOptions implements Cloneable {

  public static final String PREFERENCE_KEY_NUM_QUICK_ACTIONS = "numQuickActions";
  public static final int DEFAULT_NUM_QUICK_ACTIONS = 5;
  private final Preferences preferences;
  private AtomicInteger numActions = new AtomicInteger(DEFAULT_NUM_QUICK_ACTIONS);

  QuickMenuOptions() {
    this(QuickMenu.getInstance().getPreferences());
  }

  QuickMenuOptions(Preferences preferences) {
    this.preferences = preferences;
    setNumActions(this.preferences.getInt(QuickMenuOptions.PREFERENCE_KEY_NUM_QUICK_ACTIONS,
        QuickMenuOptions.DEFAULT_NUM_QUICK_ACTIONS));
  }

  public static void putToPreferences(QuickMenuOptions currentOptions, Preferences preferences) {
    preferences.put(QuickMenuOptions.PREFERENCE_KEY_NUM_QUICK_ACTIONS,
        Integer.toString(currentOptions.numActions.get()));
  }

  public int getNumActions() {
    return numActions.get();
  }

  public void setNumActions(int value) {
    numActions.set(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QuickMenuOptions that = (QuickMenuOptions) o;
    return numActions.get() == that.numActions.get();
  }

  @Override
  public int hashCode() {
    return Objects.hash(numActions);
  }

  public QuickMenuOptions clone() {
    QuickMenuOptions clone;
    try {
      clone = (QuickMenuOptions) super.clone();
    } catch (CloneNotSupportedException e) {
      return new QuickMenuOptions(preferences);
    }
    clone.numActions = new AtomicInteger(this.numActions.get());
    return clone;
  }

}
