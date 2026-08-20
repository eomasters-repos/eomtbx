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

package org.eomasters.eomtbx.pyeditor.gui.tabs;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.eomasters.eomtbx.pyeditor.pyrun.PolyglotUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.SourceSection;


class EditorPythonParser extends AbstractParser {

  private final String relativeFilePath;
  private final Context parserContext;

  public EditorPythonParser(String relativeFilePath, Context parserContext) {
    this.relativeFilePath = relativeFilePath;
    this.parserContext = parserContext;
  }

  @Override
  public ParseResult parse(RSyntaxDocument document, String style) {
    DefaultParseResult result = new DefaultParseResult(this);
    try {
      String text = document.getText(0, document.getLength());
      Source python = Source.newBuilder("python", text, relativeFilePath).build();
      try {
        parserContext.parse(python);
      } catch (PolyglotException e) {
        SourceSection sourceLocation = e.getSourceLocation();
        int startLine = sourceLocation != null ? sourceLocation.getStartLine() - 1 : 0;
        String formattedError = PolyglotUtils.formatPolyglotException(e);
        result.addNotice(new DefaultParserNotice(this, formattedError, startLine));
      }
      return result;
    } catch (BadLocationException | IOException e) {
      throw new RuntimeException(e);
    }
  }

}
