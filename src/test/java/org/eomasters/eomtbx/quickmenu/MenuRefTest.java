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
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class MenuRefTest {

    @Test
    public void testGetPath() {
        MenuRef menuRef = new MenuRef("path/to/menu", "Menu Text");

        assertEquals("path/to/menu", menuRef.getPath());
    }

    @Test
    public void testGetText() {
        MenuRef menuRef = new MenuRef("path/to/menu", "Menu &Text");

        assertEquals("Menu Text", menuRef.getText());
    }

    @Test
    public void testEqualsAndHashCode() {
        MenuRef menuRef1 = new MenuRef("path/to/menu", "Menu Text");
        MenuRef menuRef2 = new MenuRef("path/to/menu", "Menu Text");

        assertEquals(menuRef1, menuRef2);
        assertEquals(menuRef1.hashCode(), menuRef2.hashCode());
      //noinspection SimplifiableAssertion,ConstantValue
      assertFalse(menuRef1.equals(null));
      //noinspection SimplifiableAssertion
        assertFalse(menuRef1.equals(new Object())  );
    }

    @Test
    public void testRemoveShortCutIndicator() {
        String inputText = "Menu &Text";
        String expectedOutput = "Menu Text";

        String result = MenuRef.removeShortCutIndicator(inputText);

        assertEquals(expectedOutput, result);
    }

    @Test
    public void testToString() {
        MenuRef menuRef = new MenuRef("path/to/menu", "Menu Text");

        assertEquals("MenuRef{path='path/to/menu', text='Menu Text'}", menuRef.toString());
    }
}
