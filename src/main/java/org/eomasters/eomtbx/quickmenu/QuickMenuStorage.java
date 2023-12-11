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

package org.eomasters.eomtbx.quickmenu;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Proves methods to save and load action references to and from a JSON file.
 */
class QuickMenuStorage {

  static final String ACTIONS_NODE = "actions";
  private final Preferences qmPreferences;

  QuickMenuStorage(Preferences qmPreferences) {
    this.qmPreferences = qmPreferences;
  }

  List<ActionRef> load() {
    Preferences qmActionsNode = qmPreferences.node(ACTIONS_NODE);
    ArrayList<ActionRef> actionRefs = new ArrayList<>();
    try {
      String[] strings = qmActionsNode.keys();
      for (String string : strings) {
        ActionRef actionRef = new ActionRef(string);
        actionRef.setClicks(qmActionsNode.getInt(string, 0));
        actionRefs.add(actionRef);
      }
    } catch (BackingStoreException e) {
      throw new RuntimeException(e);
    }

    return actionRefs;
  }

  void save(List<ActionRef> actionReferences) {
    Preferences qmActionsNode = qmPreferences.node(ACTIONS_NODE);
    try {
      qmActionsNode.clear();
    } catch (BackingStoreException ignore) {
    }
    actionReferences.stream()
                    .filter(actionRef -> actionRef.getClicks() > 0)
                    .forEach(actionRef -> qmActionsNode.putInt(actionRef.getActionId(), (int) actionRef.getClicks()));
  }

  static void store(List<ActionRef> actionReferences) {
    QuickMenuStorage qmStorage = new QuickMenuStorage(QuickMenu.getInstance().getPreferences());
    qmStorage.save(actionReferences);
  }

  static List<ActionRef> restore() {
    QuickMenuStorage qmStorage = new QuickMenuStorage(QuickMenu.getInstance().getPreferences());
    return qmStorage.load();
  }

}
