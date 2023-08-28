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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ActionRefTest {

  @Test
  public void testGetActionId() {
    MenuRef menuRef = new MenuRef("tools", "123-Action");
    ActionRef actionRef = new ActionRef("action123", menuRef);

    assertEquals("action123", actionRef.getActionId());
  }

  @Test
  public void testGetMenuRef() {
    MenuRef menuRef = new MenuRef("tools", "123-Action");
    ActionRef actionRef = new ActionRef("action123", menuRef);

    assertEquals(menuRef, actionRef.getMenuRef());
  }

  @Test
  public void testAddMenuRef() {
    MenuRef menuRef1 = new MenuRef("main/special/tools", "123-Action");
    MenuRef menuRef2 = new MenuRef("tools", "123-Action");
    ActionRef actionRef = new ActionRef("action123", menuRef1);
    actionRef.addMenuRef(menuRef2);

    assertEquals(2, actionRef.getMenuRefs().size());
    assertTrue(actionRef.getMenuRefs().contains(menuRef1));
    assertTrue(actionRef.getMenuRefs().contains(menuRef2));
  }

  @Test
  public void testIncrementClicks() {
    MenuRef menuRef = new MenuRef("tools", "123-Action");
    ActionRef actionRef = new ActionRef("action123", menuRef);

    actionRef.incrementClicks();
    actionRef.incrementClicks();

    assertEquals(2, actionRef.getClicks());
  }

  @Test
  public void testEqualsAndHashCode() {
    MenuRef menuRef1 = new MenuRef("main/special/tools", "123-Action");
    MenuRef menuRef2 = new MenuRef("main/special/tools", "123-Action");

    ActionRef actionRef1 = new ActionRef("action123", menuRef1);
    ActionRef actionRef2 = new ActionRef("action123", menuRef2);

    assertEquals(actionRef1, actionRef2);
    assertEquals(actionRef1, actionRef1);
    assertNotEquals(actionRef1, null);
    assertNotEquals(actionRef1, new Object());
    assertEquals(actionRef1.hashCode(), actionRef2.hashCode());
  }

  @Test
  void testToString() {
    MenuRef menuRef = new MenuRef("tools", "123-Action");
    ActionRef actionRef = new ActionRef("action123", menuRef);

    assertEquals("ActionRef{actionId='action123', clicks=0, menuRefs=[tools/123-Action]}", actionRef.toString());
  }

}
