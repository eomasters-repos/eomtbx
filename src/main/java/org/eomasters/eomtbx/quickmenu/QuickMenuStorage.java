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

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Proves methods to save and load action references to and from a JSON file.
 */
class QuickMenuStorage {

  // save elements of actionReferences to a JSON file using GSON library
  static void save(List<ActionRef> actionReferences, OutputStream outputStream) throws IOException {
    String json = new Gson().toJson(actionReferences.stream()
        .filter(actionRef1 -> actionRef1.getClicks() > 0).toArray());
    outputStream.write(json.getBytes());
  }

  // load actionReferences from JSON stream using GSON library
  static List<ActionRef> load(InputStream inputStream) throws IOException {
    String json = new String(inputStream.readAllBytes());
    return Arrays.asList(new Gson().fromJson(json, ActionRef[].class));
  }
}
