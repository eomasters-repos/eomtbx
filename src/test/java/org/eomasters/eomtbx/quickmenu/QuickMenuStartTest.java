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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.core.startup.preferences.NbPreferences.UserPreferences;

public class QuickMenuStartTest {

  private QuickMenu quickMenu;

  @BeforeEach
  void setUp() {
    quickMenu = new QuickMenu(new UserPreferences());
  }

  @Test
  public void testGetInstance() {
    assertNotNull(QuickMenu.getInstance());
  }

  @Test
  public void testStart() {
    assertFalse(quickMenu.isInitialised());

    assertThrows(IllegalStateException.class, () -> quickMenu.getActionReferences());

    quickMenu.init();

    assertTrue(quickMenu.isInitialised());

    assertThrows(IllegalStateException.class, quickMenu::init);

  }

  @Test
  public void testActionRefClicked() {
    quickMenu.init();

    ActionRef actionRef1 = new ActionRef("action1", new MenuRef("path1", "Text 1"));
    ActionRef actionRef2 = new ActionRef("action2", new MenuRef("path2", "Text 2"));
    quickMenu.getActionReferences().add(actionRef1);
    quickMenu.getActionReferences().add(actionRef2);

    actionRef1.incrementClicks();

    quickMenu.sort();

    assertEquals(1, actionRef1.getClicks());
    assertEquals(0, actionRef2.getClicks());
  }

}
