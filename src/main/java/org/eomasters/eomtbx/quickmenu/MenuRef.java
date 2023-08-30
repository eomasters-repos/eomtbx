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

import java.util.Objects;

/**
 * Reference to a menu item.
 */
public class MenuRef {

  private final String path;
  private final String text;

  /**
   * Creates a new MenuRef with the given path and text.
   *
   * @param path the path
   * @param text the text
   */
  public MenuRef(String path, String text) {
    this.path = path;
    this.text = removeShortCutIndicator(text);
  }

  static String removeShortCutIndicator(String displayName) {
    return displayName.replaceAll("&", "");
  }

  /**
   * Returns the path.
   *
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * Returns the text.
   *
   * @return the text
   */
  public String getText() {
    return text;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MenuRef other = (MenuRef) o;
    return Objects.equals(this.getPath(), other.getPath())
        && Objects.equals(this.getText(), other.getText());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getPath(), getText());
  }

  @Override
  public String toString() {
    return String.format("%s/%s", path, text);
  }


}
