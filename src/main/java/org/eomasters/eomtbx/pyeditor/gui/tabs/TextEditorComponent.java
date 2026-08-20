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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.eomasters.eomtbx.EomtbxRuntime;
import org.eomasters.eomtbx.pyeditor.pyrun.EditorTheme;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.parser.Parser;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * A text editor component that extends RTextScrollPane and provides access to the underlying RSyntaxTextArea methods.
 * This component provides syntax highlighting, code folding, and line numbers for text editing.
 */
public class TextEditorComponent extends RTextScrollPane {

  private final RSyntaxTextArea textArea;
  private final Path filePath;
  private final List<TextChangedListener> textChangedListeners = new ArrayList<>();
  private boolean modified = false;

  /**
   * Creates a new TextEditorComponent with the specified content and syntax highlighting based on the file path.
   *
   * @param content  the text content to display in the text area
   * @param filePath the path of the file being edited (used to determine syntax highlighting)
   * @param theme    the theme to use
   */
  public TextEditorComponent(String content, Path filePath, EditorTheme theme) {
    this(content, filePath, getStyleFromExtension(filePath), theme);
  }

  /**
   * Creates a new TextEditorComponent with the specified content and syntax highlighting style.
   *
   * @param content  the text content to display in the text area
   * @param filePath the path of the file being edited (can be null for new files)
   * @param style    the syntax highlighting style to use (e.g., a constant from SyntaxConstants)
   */
  private TextEditorComponent(String content, Path filePath, String style, EditorTheme theme) {
    // Create a new RSyntaxTextArea
    textArea = new RSyntaxTextArea();
    textArea.setText(content);
    textArea.setCodeFoldingEnabled(true);
    textArea.setSyntaxEditingStyle(style);
    textArea.setTabSize(4);
    textArea.setTabsEmulated(true);
    setTheme(theme);
    // Store the file path
    this.filePath = filePath;

    // Add document listener to track modifications
    textArea.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        setModified(true);
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        setModified(true);
      }

      @Override
      public void changedUpdate(DocumentEvent e) {}
    });

    // Set the view port view to the text area
    setViewportView(textArea);

    // Enable line numbers and other features
    setLineNumbersEnabled(true);
    setFoldIndicatorEnabled(true);
  }

  public void setTheme(EditorTheme theme) {
    try {
      Theme themeStyle = Theme.load(getClass().getResourceAsStream(
          "/org/fife/ui/rsyntaxtextarea/themes/" + theme.getThemeFilename()));
      SwingUtilities.invokeLater(() -> themeStyle.apply(textArea));
    } catch (IOException e) {
      EomtbxRuntime.LOGGER.warning("Could not load theme: " + e.getMessage());
    }
  }

  private static String getStyleFromExtension(Path filePath) {
    if (filePath == null) {
      return SyntaxConstants.SYNTAX_STYLE_NONE;
    }

    String fileName = filePath.getFileName().toString().toLowerCase();
    if (!fileName.contains(".")) {
      return SyntaxConstants.SYNTAX_STYLE_NONE;
    }

    String extension = fileName.substring(fileName.lastIndexOf('.'));
    return switch (extension.toLowerCase()) {
      case ".py" -> SyntaxConstants.SYNTAX_STYLE_PYTHON;
      case ".csv" -> SyntaxConstants.SYNTAX_STYLE_CSV;
      case ".properties" -> SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
      case ".xml" -> SyntaxConstants.SYNTAX_STYLE_XML;
      case ".json" -> SyntaxConstants.SYNTAX_STYLE_JSON;
      case ".html" -> SyntaxConstants.SYNTAX_STYLE_HTML;
      case ".css" -> SyntaxConstants.SYNTAX_STYLE_CSS;
      default -> SyntaxConstants.SYNTAX_STYLE_NONE;
    };
  }

  public void addParser(Parser parser) {
    textArea.addParser(parser);
  }

  /**
   * Gets the text from the text area.
   *
   * @return the text from the text area
   */
  public String getText() {
    return textArea.getText();
  }

  /**
   * Sets the text in the text area.
   *
   * @param text the text to set
   */
  public void setText(String text) {
    textArea.setText(text);
  }

  public String getSyntaxType() {
    return textArea.getSyntaxEditingStyle();
  }

  /**
   * Enables or disables code folding in the text area.
   *
   * @param enabled true to enable code folding, false to disable
   */
  public void setCodeFoldingEnabled(boolean enabled) {
    textArea.setCodeFoldingEnabled(enabled);
  }

  /**
   * Sets the syntax highlighting style for the text area.
   *
   * @param style the syntax highlighting style to use
   */
  public void setSyntaxEditingStyle(String style) {
    textArea.setSyntaxEditingStyle(style);
  }

  /**
   * Checks if the content has been modified since the last save.
   *
   * @return true if the content has been modified, false otherwise
   */
  public boolean isModified() {
    return modified;
  }

  /**
   * Sets the modified state of the content.
   *
   * @param modified true if the content has been modified, false otherwise
   */
  public void setModified(boolean modified) {
    // Only notify listeners if the state actually changes
    if (this.modified != modified) {
      this.modified = modified;
      notifyModifiedStateListeners();
    }
  }

  /**
   * Adds a listener for modified state changes.
   *
   * @param listener the listener to add
   */
  public void addModifiedStateListener(TextChangedListener listener) {
    if (listener != null && !textChangedListeners.contains(listener)) {
      textChangedListeners.add(listener);
    }
  }

  /**
   * Removes a listener for modified state changes.
   *
   * @param listener the listener to remove
   */
  public void removeModifiedStateListener(TextChangedListener listener) {
    textChangedListeners.remove(listener);
  }

  /**
   * Notifies all registered listeners that the modified state has changed.
   */
  private void notifyModifiedStateListeners() {
    for (TextChangedListener listener : textChangedListeners) {
      listener.onChange(filePath, modified);
    }
  }

  /**
   * Gets the file path associated with this editor component.
   *
   * @return the file path, or null if this is a new file
   */
  public Path getFilePath() {
    return filePath;
  }


  /**
   * Adds a key action to the text editor component, binding a specific keystroke to an action identified by a unique
   * action key.
   *
   * @param action    the action to be associated with the specified keystroke
   * @param actionKey a unique identifier for the action
   * @param keyStroke the keystroke to trigger the action
   */
  public void addKeyAction(Action action, String actionKey, KeyStroke keyStroke) {
    if (action != null) {
      InputMap inputMap = textArea.getInputMap(JComponent.WHEN_FOCUSED);
      ActionMap actionMap = textArea.getActionMap();
      inputMap.put(keyStroke, actionKey);
      actionMap.put(actionKey, action);
    }

  }

  /**
   * Interface for listening to changes in the text editor.
   */
  public interface TextChangedListener {

    /**
     * Called when the state of the text editor changes.
     *
     * @param filePath the path of the file being edited
     * @param modified true, if the text has changed
     */
    void onChange(Path filePath, boolean modified);
  }
}
