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

package org.eomasters.eomtbx.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetadataUtilsTest {

    private MetadataElement rootElement;

    @BeforeEach
    void setUp() {
        // Create a test metadata hierarchy
        rootElement = new MetadataElement("root");
        
        // Create child elements
        MetadataElement level1 = new MetadataElement("level1");
        MetadataElement level2 = new MetadataElement("level2");
        
        // Add attributes at different levels
        rootElement.addAttribute(new MetadataAttribute("rootAttribute", ProductData.createInstance("rootValue"), true));
        level1.addAttribute(new MetadataAttribute("level1Attribute", ProductData.createInstance("level1Value"), true));
        level2.addAttribute(new MetadataAttribute("level2Attribute", ProductData.createInstance("level2Value"), true));
        
        // Build hierarchy
        level1.addElement(level2);
        rootElement.addElement(level1);
    }

    @Test
    void testGetAttributeFromPath_SimpleAttribute() {
        MetadataAttribute attr = MetadataUtils.getAttributeFromPath(rootElement, "rootAttribute");
        assertNotNull(attr);
        assertEquals("rootAttribute", attr.getName());
        assertEquals("rootValue", attr.getData().getElemString());
    }

    @Test
    void testGetAttributeFromPath_NestedAttribute() {
        MetadataAttribute attr = MetadataUtils.getAttributeFromPath(rootElement, "level1/level1Attribute");
        assertNotNull(attr);
        assertEquals("level1Attribute", attr.getName());
        assertEquals("level1Value", attr.getData().getElemString());
    }

    @Test
    void testGetAttributeFromPath_DeeplyNestedAttribute() {
        MetadataAttribute attr = MetadataUtils.getAttributeFromPath(rootElement, "level1/level2/level2Attribute");
        assertNotNull(attr);
        assertEquals("level2Attribute", attr.getName());
        assertEquals("level2Value", attr.getData().getElemString());
    }

    @Test
    void testGetAttributeFromPath_NonExistentPath() {
        MetadataAttribute attr = MetadataUtils.getAttributeFromPath(rootElement, "nonexistent/path");
        assertNull(attr);
    }

    @Test
    void testGetAttributeFromPath_NonExistentAttribute() {
        MetadataAttribute attr = MetadataUtils.getAttributeFromPath(rootElement, "level1/nonExistentAttribute");
        assertNull(attr);
    }

    @Test
    void testGetAttributeFromPath_NullElement() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> MetadataUtils.getAttributeFromPath(null, "path"));
        assertEquals("MetadataElement cannot be null", exception.getMessage());
    }

    @Test
    void testGetAttributeFromPath_NullPath() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> MetadataUtils.getAttributeFromPath(rootElement, null));
        assertEquals("Path cannot be null", exception.getMessage());
    }

    @Test
    void testGetAttributeFromPath_EmptyPath() {
        MetadataAttribute attr = MetadataUtils.getAttributeFromPath(rootElement, "");
        assertNull(attr);
    }

    @Test
    void testGetAttributeFromPath_PathWithLeadingSlash() {
        MetadataAttribute attr = MetadataUtils.getAttributeFromPath(rootElement, "/level1/level1Attribute");
        assertNotNull(attr);
        assertEquals("level1Attribute", attr.getName());
    }

    @Test
    void testGetAttributeFromPath_PathWithTrailingSlash() {
        MetadataAttribute attr = MetadataUtils.getAttributeFromPath(rootElement, "level1/level1Attribute/");
        assertNotNull(attr); // Java's split() doesn't create trailing empty strings, so this should work
        assertEquals("level1Attribute", attr.getName());
    }

    @Test
    void testGetAttributeFromPath_PathWithMultipleSlashes() {
        MetadataAttribute attr = MetadataUtils.getAttributeFromPath(rootElement, "level1//level2/level2Attribute");
        assertNotNull(attr);
        assertEquals("level2Attribute", attr.getName());
    }

    @Test
    void testGetAttributeFromPath_PathWithSpaces() {
        MetadataAttribute attr = MetadataUtils.getAttributeFromPath(rootElement, " level1 / level1Attribute ");
        assertNotNull(attr);
        assertEquals("level1Attribute", attr.getName());
    }
}
