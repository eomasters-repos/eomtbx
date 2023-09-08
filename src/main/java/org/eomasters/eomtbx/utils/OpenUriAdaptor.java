/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
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
 * -> http://www.gnu.org/licenses/gpl-3.0.html
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx.utils;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

/**
 * Implements an action listener that opens a URI in the default browser.
 */
class OpenUriAdaptor implements ActionListener {

  private final URI uri;

  /**
   * Creates a new instance of {@link OpenUriAdaptor}.
   *
   * @param uri the URI to open
   */
  public OpenUriAdaptor(String uri) {
    this.uri = URI.create(uri);
  }

  /**
   * Creates a new instance of {@link OpenUriAdaptor}.
   *
   * @param uri the URI to open
   */
  public OpenUriAdaptor(URI uri) {
    this.uri = uri;
  }

  @Override
  public void actionPerformed(ActionEvent e1) {
    try {
      Desktop.getDesktop().browse(uri);
    } catch (IOException ex) {
      ErrorHandler.handleError("Error opening browser", ex.getMessage(), ex);
    }
  }
}
