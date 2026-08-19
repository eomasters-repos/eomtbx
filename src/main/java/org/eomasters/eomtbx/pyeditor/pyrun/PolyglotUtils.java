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

package org.eomasters.eomtbx.pyeditor.pyrun;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.SourceSection;

public class PolyglotUtils {

  private PolyglotUtils() {}

  /**
   * Formats a PolyglotException into a user-friendly error message similar to Python's native error format.
   *
   * @param exception the PolyglotException to format
   * @return a formatted error string
   */
  public static String formatPolyglotException(PolyglotException exception) {
    StringBuilder errorMessage = new StringBuilder();

    SourceSection sourceLocation = exception.getSourceLocation();
    if (sourceLocation != null) {
      // File and line information
      String fileName = sourceLocation.getSource().getName();
      int lineNumber = sourceLocation.getStartLine();
      int columnStartNumber = sourceLocation.getStartColumn();
      int columnEndNumber = sourceLocation.getEndColumn();

      errorMessage.append("  File \"").append(fileName).append("\"")
                  .append(", line ").append(lineNumber)
                  .append(", cols ").append(columnStartNumber).append(":").append(columnEndNumber)
                  .append("\n");
    }

    // Add the error type and message
    String exceptionMessage = exception.getMessage();
    if (exceptionMessage != null) {
      // Extract error type if it's in the message (e.g., "SyntaxError: invalid syntax")
      if (exceptionMessage.contains(":")) {
        errorMessage.append(exceptionMessage);
      } else {
        // Default to SyntaxError if no specific type is found
        errorMessage.append("SyntaxError: ").append(exceptionMessage);
      }
    } else {
      errorMessage.append("SyntaxError: invalid syntax");
    }

    return errorMessage.toString();
  }
}
