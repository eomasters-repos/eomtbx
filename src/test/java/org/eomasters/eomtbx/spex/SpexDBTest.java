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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Collection;
import org.junit.jupiter.api.Test;

public class SpexDBTest {

  @Test
  void testAddingDbSpex() throws IOException {
    TestPreferences preferences = new TestPreferences();
    SpexDb spexDb = SpexDb.create(preferences);
    Collection<AbstractSpex> indices = spexDb.getIndices();
    assertFalse(indices.isEmpty());
    int numPreDefined = indices.size();
    CustomSpex watn = new CustomSpex("WATN");
    watn.setFormula("1-2+3");
    spexDb.addIndex(watn);

    assertNotNull(spexDb.get("WATN"));

    indices = spexDb.getIndices();
    assertEquals(numPreDefined + 1, indices.size());
    indices.forEach(spex1 -> assertNotNull(spex1.getName()));

  }

  @Test
  void testAddingCustomSpex() throws IOException {
    SpexDb spexDb = SpexDb.create(new TestPreferences());
    CustomSpex cstm = new CustomSpex("CSTM");
    cstm.setFormula("3*6");
    spexDb.addIndex(cstm);

    assertNotNull(spexDb.get("CSTM"));

    spexDb.getIndices().forEach(spex1 -> assertNotNull(spex1.getName()));
  }

  @Test
  void testDbWithFilledPrefs() throws Exception {
    TestPreferences preferences = new TestPreferences();
    SpexDb spexDb = SpexDb.create(preferences);
    CustomSpex watn = new CustomSpex("WATN");
    watn.setFormula("1-2+3");
    spexDb.addIndex(watn);
    CustomSpex cstm = new CustomSpex("CSTM");
    cstm.setFormula("3*6");
    spexDb.addIndex(cstm);
    spexDb.close();

    SpexDb filledDb = SpexDb.create(preferences);
    assertNotNull(filledDb.get("WATN"));
    assertNotNull(filledDb.get("CSTM"));

    filledDb.getIndices().forEach(spex -> assertNotNull(spex.getName()));
    filledDb.close();
  }

  @Test
  void testFullyLoadDB() {
    SpexDb instance = SpexDb.getInstance();
    Collection<AbstractSpex> indices = instance.getIndices();
    assertEquals(280, indices.size());
    // Map<String, AbstractSpex> map = new TreeMap<>();
    // for (AbstractSpex spex : indices) {
    //   if (spex.getSourceName().contains("Awesome")) {
    //     map.put(spex.getName(), spex);
    //   }
    // }
    // GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
    // Gson gson = gsonBuilder.create();
    // String json = gson.toJson(map);
    // // System.out.println(json);
  }
}
