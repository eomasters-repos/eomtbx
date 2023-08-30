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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuickMenuStorageTest {

  private static final String JSON_STRING = "[{\"actionId\":\"action123\",\"menuRefs\":["
      + "{\"path\":\"main/special/tools\",\"text\":\"123-Action\"},"
      + "{\"path\":\"tools\",\"text\":\"123-Action\"}],\"clicks\":3},"
      + "{\"actionId\":\"action456\",\"menuRefs\":["
      + "{\"path\":\"view/windows\",\"text\":\"456-Action\"}],\"clicks\":1}]";
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
  void testSaveAllClicksZero() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    actionRef1.setClicks(0);
    actionRef2.setClicks(0);
    QuickMenuStorage.save(actionReferences, new DataOutputStream(outputStream));
    assertEquals("[]", outputStream.toString());
  }

  @Test
  void testSave() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    QuickMenuStorage.save(actionReferences, new DataOutputStream(outputStream));
    String jsonString = outputStream.toString();
    assertEquals(JSON_STRING, jsonString);
  }

  @Test
  void testLoad() throws IOException {
    List<ActionRef> actionRefs = QuickMenuStorage.load(new ByteArrayInputStream(JSON_STRING.getBytes()));
    assertEquals(actionReferences, actionRefs);
  }
}
