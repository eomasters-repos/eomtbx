/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2024 Marco Peters
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


import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import org.eomasters.eomtbx.EomToolbox;

/**
 * The QuickMenu. Provides access to the action references which are most frequently used.
 */
public class QuickMenu {

  private static final String QUICKMENU_ID = "quickmenu";
  private final Preferences qmPreferences;
  private List<ActionRef> actionReferences;

  /**
   * Creates a new QuickMenu. Only used by the singleton and testing.
   */
  QuickMenu(Preferences qmPreferences) {
    this.qmPreferences = qmPreferences;
  }

  /**
   * Returns the QuickMenu instance.
   *
   * @return the QuickMenu instance
   */
  public static QuickMenu getInstance() {
    return InstanceHolder.INSTANCE;
  }

  public Preferences getPreferences() {
    return qmPreferences;
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
   * Initialises the QuickMenu. This is separated from the constructor because it can take some time.
   *
   * @throws IllegalStateException if the QuickMenu is already started
   */
  public void init() {
    if (isInitialised()) {
      throw new IllegalStateException("QuickMenu already started");
    }
    actionReferences = Collections.synchronizedList(ActionRefCollector.collect());
    try {
      updateActionReferences(actionReferences, QuickMenuStorage.restore());
      sort();
    } catch (IOException e) {
      EomToolbox.LOG.log(Level.SEVERE, "Failed to restore QuickMenu", e);
    }
  }

  private void updateActionReferences(List<ActionRef> actionRefs, List<ActionRef> updates) {
    actionRefs.stream().parallel().filter(updates::contains).forEach(actionRef -> {
      ActionRef update = updates.get(updates.indexOf(actionRef));
      actionRef.setClicks(update.getClicks());
    });
  }

  /**
   * Starts the QuickMenu. The GUI needs to be installed before the QuickMenu can be started properly,
   * e.g., register click listeners to the menu items.
   */
  public void start() {
    ClickCounter.initMenuItemClickCounter();
  }

  /**
   * Stops the QuickMenu. Saves the action references to a JSON file.
   */
  public void stop() {
    try {
      QuickMenuStorage.store(actionReferences);
    } catch (IOException e) {
      EomToolbox.LOG.log(Level.SEVERE, "Failed to save QuickMenu", e);
    }
    actionReferences = null;
  }

  /**
   * Returns true if the QuickMenu is initialised.
   *
   * @return true if the QuickMenu is initialised
   */
  boolean isInitialised() {
    return actionReferences != null;
  }

  /**
   * Ensures that the QuickMenu is initialised.
   *
   * @throws IllegalStateException if the QuickMenu is not initialised
   */
  private void ensureStarted() {
    if (!isInitialised()) {
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

    private static final QuickMenu INSTANCE = new QuickMenu(EomToolbox.getPreferences().node(QuickMenu.QUICKMENU_ID));
  }
}
