/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Free for SNAP
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.core.startup.preferences.NbPreferences.UserPreferences;

class QuickMenuStorageTest {

  private static List<ActionRef> actionReferences;
  private static ActionRef actionRef1;
  private static ActionRef actionRef2;

  @BeforeEach
  void before() {
    actionReferences = new ArrayList<>();
    MenuRef menuRef1 = new MenuRef("main/special/tools", "123-Action");
    MenuRef menuRef2 = new MenuRef("tools", "123-Action");
    actionRef1 = new ActionRef("action123", menuRef1, menuRef2);
    actionReferences.add(actionRef1);
    actionRef2 = new ActionRef("action456", new MenuRef("view/windows", "456-Action"));
    actionReferences.add(actionRef2);
    actionRef1.setClicks(3);
    actionRef2.setClicks(1);
  }

  @Test
  void testSaveAllClicksZero() throws BackingStoreException, IOException {
    actionRef1.setClicks(0);
    actionRef2.setClicks(0);
    UserPreferences preferences = new UserPreferences();
    new QuickMenuStorage(preferences).save(actionReferences);
    String[] keys = preferences.node("actions").keys();
    assertEquals(0, keys.length);
  }

  @Test
  void testSave() throws BackingStoreException, IOException {
    UserPreferences preferences = new UserPreferences();
    new QuickMenuStorage(preferences).save(actionReferences);
    Preferences actionsNode = preferences.node("actions");
    String[] keys = actionsNode.keys();
    assertEquals(2, keys.length);
    assertEquals(3, actionsNode.getInt(actionRef1.getActionId(), -1));
    assertEquals(1, actionsNode.getInt(actionRef2.getActionId(), -1));
  }

  @Test
  void testLoad() throws IOException {
    UserPreferences preferences = new UserPreferences();
    new QuickMenuStorage(preferences).save(actionReferences);
    List<ActionRef> actionRefs = new QuickMenuStorage(preferences).load();
    assertEquals(actionReferences, actionRefs);
    Preferences actionsNode = preferences.node("actions");
    assertEquals(3, actionsNode.getInt(actionRef1.getActionId(), -1));
    assertEquals(1, actionsNode.getInt(actionRef2.getActionId(), -1));
  }
}
