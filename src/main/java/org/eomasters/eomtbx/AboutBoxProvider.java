/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Free for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2024 Marco Peters
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

package org.eomasters.eomtbx;

import java.awt.Component;

/**
 * Interface for providing a component for the EOMasters Toolbox Free AboutBox. The fully qualified name of implementations
 * of this interface must be added in a file named {@code META-INF/services/org.eomasters.eomtbx.AboutBoxProvider}}. The
 * provided GUI component provided by {@link #getAboutPanel()}will be displayed in a Tab named using the text returned
 * by {@link #getTitle()}.
 */
public interface AboutBoxProvider {

  /**
   * Retrieves the title of the object.
   *
   * @return the title of the object
   */
  String getTitle();

  /**
   * Retrieves the component to be displayed in the AboutBox.
   *
   * @return the component to be displayed in the AboutBox
   */
  Component getAboutPanel();
}
