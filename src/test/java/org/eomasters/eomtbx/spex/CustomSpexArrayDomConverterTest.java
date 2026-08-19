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

package org.eomasters.eomtbx.spex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bc.ceres.binding.ConversionException;
import com.bc.ceres.binding.dom.DefaultDomElement;
import com.bc.ceres.binding.dom.DomElement;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

class CustomSpexArrayDomConverterTest {

  @Test
  void testArrayToDom() throws ConversionException, ParseException {
    CustomSpex spex1 = new CustomSpex("NDVI");
    spex1.setFormula("2-1/1+2");
    CustomSpex spex2 = new CustomSpex("TEST");
    spex2.setWktRegion(new WKTReader().read("POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))"));
    CustomSpex spex3 = new CustomSpex("DEPRECATED");
    spex3.setDeprecation("Don't use it");
    CustomSpex[] spexes = {spex1, spex2, spex3};
    CustomSpexArrayDomConverter converter = new CustomSpexArrayDomConverter();
    DefaultDomElement parentElement = new DefaultDomElement("spexes");
    converter.convertValueToDom(spexes, parentElement);

    assertEquals(3, parentElement.getChildCount());
    DomElement firstSpex = parentElement.getChild(0);
    assertEquals("spex", firstSpex.getName());
    assertEquals("NDVI", firstSpex.getChild("name").getValue());
    assertEquals("2-1/1+2", firstSpex.getChild("formula").getValue());
    DomElement secondSpex = parentElement.getChild(1);
    assertEquals("spex", secondSpex.getName());
    assertEquals("TEST", secondSpex.getChild("name").getValue());
    assertEquals("POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))", secondSpex.getChild("wktRegion").getValue());
    DomElement thirdSpex = parentElement.getChild(2);
    assertEquals("spex", thirdSpex.getName());
    assertEquals("DEPRECATED", thirdSpex.getChild("name").getValue());
    assertEquals("Don't use it", thirdSpex.getChild("deprecation").getValue());

  }

  @Test
  void testDomToArray() throws ConversionException, ParseException {
    CustomSpexArrayDomConverter converter = new CustomSpexArrayDomConverter();
    DefaultDomElement parentElement = new DefaultDomElement("spexes");
    DefaultDomElement spex1 = new DefaultDomElement("spex");
    parentElement.addChild(spex1);
    spex1.addChild(new DefaultDomElement("name", "NDVI"));
    spex1.addChild(new DefaultDomElement("formula", "2-1/1+2"));
    spex1.addChild(new DefaultDomElement("wktRegion", "POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))"));
    DefaultDomElement spex2 = new DefaultDomElement("spex");
    parentElement.addChild(spex2);
    spex2.addChild(new DefaultDomElement("name", "Index"));
    spex2.addChild(new DefaultDomElement("formula", "A+B/pow(A+B,2)"));
    spex2.addChild(new DefaultDomElement("reference", "DOI:47.11"));
    spex2.addChild(new DefaultDomElement("validExpression", "true"));
    spex2.addChild(new DefaultDomElement("description", "a long description"));
    DefaultDomElement spex3 = new DefaultDomElement("spex");
    parentElement.addChild(spex3);
    spex3.addChild(new DefaultDomElement("name", "DEPRECATED"));
    spex3.addChild(new DefaultDomElement("deprecation", "use something else"));
    DefaultDomElement symbolMapElem = new DefaultDomElement("symbolMap");
    symbolMapElem.addChild(new DefaultDomElement("A", "1"));
    symbolMapElem.addChild(new DefaultDomElement("B", "0.33"));
    spex2.addChild(symbolMapElem);
    CustomSpex[] spexes = (CustomSpex[]) converter.convertDomToValue(parentElement, null);

    assertEquals(3, spexes.length);
    assertEquals("NDVI", spexes[0].getName());
    assertEquals("2-1/1+2", spexes[0].getFormula());
    assertEquals(new WKTReader().read("POLYGON((0 0, 0 1, 1 1, 1 0, 0 0))"), spexes[0].getWktRegion());

    assertEquals("Index", spexes[1].getName());
    assertEquals("A+B/pow(A+B,2)", spexes[1].getFormula());
    assertEquals("DOI:47.11", spexes[1].getReference());
    assertEquals("true", spexes[1].getValidExpression());
    assertEquals("a long description", spexes[1].getDescription());

    assertEquals("DEPRECATED", spexes[2].getName());
    assertEquals("use something else", spexes[2].getDeprecation());
  }

  @Test()
  void testThrowsExceptionWithoutShortName() {
    CustomSpexArrayDomConverter converter = new CustomSpexArrayDomConverter();
    DefaultDomElement parentElement = new DefaultDomElement("spexes");
    DefaultDomElement spex = new DefaultDomElement("spex");
    parentElement.addChild(spex);
    spex.addChild(new DefaultDomElement("expression", "A+B/pow(A+B,2)"));

    ConversionException thrown = assertThrows(ConversionException.class,
        () -> converter.convertDomToValue(parentElement, null));
    assertTrue(thrown.getMessage().contains("name is missing"));
  }
}
