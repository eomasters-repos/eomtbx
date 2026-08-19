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

package org.eomasters.eomtbx.io;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.SnapFileFilter;

// todo - move to eom-commons-snap
public abstract class AbstractReaderPlugin implements ProductReaderPlugIn {

  private static final Map<Class<?>, ToPathInterface> INPUT_TO_PATH_CONVERTER = Map.of(
      Path.class, input -> (Path) input,
      File.class, input -> ((File) input).toPath(),
      String.class, input -> new File((String) input).toPath());
  private final String formatName;
  private final String description;
  private final String[] fileExtensions;

  public AbstractReaderPlugin(String formatName, String description, String[] fileExtensions) {
    this.formatName = formatName;
    this.description = description;
    this.fileExtensions = fileExtensions;
  }

  public Class[] getInputTypes() {
    return INPUT_TO_PATH_CONVERTER.keySet().toArray(new Class[0]);
  }

  public static Path getAsPath(Object input) {
    for (Class<?> aClass : INPUT_TO_PATH_CONVERTER.keySet()) {
      if (aClass.isInstance(input)) {
        return INPUT_TO_PATH_CONVERTER.get(aClass).toPath(input);
      }
    }
    throw new IllegalArgumentException(String.format("Unsupported input: %s", input));
  }

  public String[] getDefaultFileExtensions() {
    return fileExtensions;
  }

  @Override
  public String[] getFormatNames() {
    return new String[]{formatName};
  }

  @Override
  public String getDescription(Locale locale) {
    return description;
  }

  @Override
  public SnapFileFilter getProductFileFilter() {
    return new SnapFileFilter(getFormatNames()[0], getDefaultFileExtensions(), getDescription(Locale.ENGLISH));
  }

  @FunctionalInterface
  private interface ToPathInterface {

    Path toPath(Object input);
  }
}
