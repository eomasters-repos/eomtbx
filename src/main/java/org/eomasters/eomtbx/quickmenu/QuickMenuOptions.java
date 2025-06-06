/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Basic for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2025 Marco Peters
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

package org.eomasters.eomtbx.quickmenu;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;

/**
 * Options for the QuickMenu.
 */
public class QuickMenuOptions implements Cloneable {

  public static final String PREFERENCE_KEY_NUM_QUICK_ACTIONS = "numActions";
  public static final int DEFAULT_NUM_QUICK_ACTIONS = 5;
  private final Preferences preferences;
  private AtomicInteger numActions = new AtomicInteger(DEFAULT_NUM_QUICK_ACTIONS);

  /**
   * Creates a new instance of QuickMenuOptions. This constructor creates a new instance with the default preferences.
   */
  public QuickMenuOptions() {
    this(QuickMenu.getInstance().getPreferences());
  }

  /**
   * Constructor used for cloning a QuickMenuOptions instance.
   *
   * @param thePrefs the preferences of the instance to be cloned.
   */
  QuickMenuOptions(Preferences thePrefs) {
    this.preferences = thePrefs;
    setNumActions(this.preferences.getInt(QuickMenuOptions.PREFERENCE_KEY_NUM_QUICK_ACTIONS,
        QuickMenuOptions.DEFAULT_NUM_QUICK_ACTIONS));
  }

  /**
   * Adds the current options to the preferences.
   *
   * @param currentOptions the current options
   * @param preferences    the preferences
   */
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

  /**
   * Creates a deep copy of the QuickMenuOptions object.
   *
   * @return a new QuickMenuOptions object
   */
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
