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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

// Custom PrintStream that captures output and notifies listeners
public class CapturePrintStream extends PrintStream {

  private final ByteArrayOutputStream buffer;
  private final boolean isError;
  private final CaptureStreamCallback callback;

  public CapturePrintStream(boolean isError, CaptureStreamCallback callback) {
    super(new ByteArrayOutputStream());
    this.buffer = (ByteArrayOutputStream) out;
    this.isError = isError;
    this.callback = callback;
  }

  @Override
  public void write(int b) {
    super.write(b);
    notifyListeners();
  }

  @Override
  public void write(byte[] buf, int off, int len) {
    super.write(buf, off, len);
    notifyListeners();
  }

  private void notifyListeners() {
    String text = buffer.toString();
    callback.onCapture(text, isError);
    buffer.reset(); // Clear buffer after notifying
  }

}
