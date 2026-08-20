/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/eomtbx
 * ======================================================================
 * Copyright (C) 2023 - 2026 Marco Peters
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

package org.eomasters.eomtbx.io.aster.odl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class OdlParserTest {

  /**
   * This class is a set of tests for the parser method in the OdlParser class. It checks whether correct output is
   * produced when given specific inputs. A variety of edge cases are covered to ensure the robustness of the method.
   */

  @Test
  public void testOdlParserForSimpleODLString() {
    String input = "GROUP=test_group\nOBJECT=test_object\nEND_OBJECT=\nEND_GROUP=";
    OdlGroup group = new OdlParser().parse(input);

    assertEquals("Root", group.getName());
    assertEquals(1, group.getSubGroups().size());
    assertEquals("test_group", group.getSubGroups().get(0).getName());
    assertEquals(1, group.getSubGroups().get(0).getObjects().size());
    assertEquals("test_object", group.getSubGroups().get(0).getObjects().get(0).getName());
  }

  @Test
  public void testOdlParserForODLStringWithMultipleGroupsAndObjects() {
    String input = "GROUP=test_group\nOBJECT=test_object\nEND_OBJECT=\nOBJECT=second_test_object\nEND_OBJECT=\nEND_GROUP=\nGROUP=second_test_group\nEND_GROUP=";
    OdlGroup group = new OdlParser().parse(input);

    assertEquals("Root", group.getName());
    assertEquals(2, group.getSubGroups().size());
    assertEquals("test_group", group.getSubGroups().get(0).getName());
    assertEquals(2, group.getSubGroups().get(0).getObjects().size());
    assertEquals("second_test_group", group.getSubGroups().get(1).getName());
  }

  @Test
  public void testOdlParserForODLStringWithNestedGroups() {
    String input = "GROUP=test_group\nGROUP=nested_group\nOBJECT=test_object\nEND_OBJECT=\nEND_GROUP=\nEND_GROUP=";
    OdlGroup group = new OdlParser().parse(input);

    assertEquals("Root", group.getName());
    assertEquals("test_group", group.getSubGroups().get(0).getName());
    assertEquals("nested_group", group.getSubGroups().get(0).getSubGroups().get(0).getName());
  }

  @Test
  public void testOdlParserForODLStringWithAttributes() {
    String input = "GROUP=test_group\nOBJECT=test_object\nattribute=value\nEND_OBJECT=\nEND_GROUP=";
    OdlGroup group = new OdlParser().parse(input);

    assertEquals("Root", group.getName());
    assertEquals("test_group", group.getSubGroups().get(0).getName());
    assertEquals("test_object", group.getSubGroups().get(0).getObjects().get(0).getName());
    assertEquals("value", group.getSubGroups().get(0).getObjects().get(0).getAttributes().get("attribute"));
  }
}
