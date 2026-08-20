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

package org.eomasters.eomtbx.pyeditor.gui.filetree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.eomasters.eomtbx.pyeditor.gui.FileManager;
import org.eomasters.eomtbx.pyeditor.pyrun.PyRunner;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.swing.FontIcon;

/**
 * A custom TreeCellRenderer for the file tree that displays different icons and colors based on file type and state.
 */
public class ProjectTreeCellRenderer extends DefaultTreeCellRenderer {

  private static final int ICON_SIZE = 16;
  // Colors for different file states
  private static final Color DEFAULT_FOREGROUND_COLOR = Color.BLACK;
  private static final Color EDITED_COLOR = Color.GREEN.darker();
  private static final Color WRITE_PROTECTED_COLOR = Color.RED;
  private static final Color SELECTED_FOREGROUND_COLOR = Color.WHITE;
  private final ProjectFileTreeModel model;
  private final FileManager fileManager;
  private final FontIcon scriptIcon;

  /**
   * Creates a new FileTreeCellRenderer with the specified model.
   *
   * @param model       the FileTreeModel to get file states from
   * @param fileManager the FileManager to use for file operations
   */
  public ProjectTreeCellRenderer(ProjectFileTreeModel model, FileManager fileManager) {
    this.model = model;
    this.fileManager = fileManager;

    // Set icons for the renderer
    setOpenIcon(FontIcon.of(MaterialDesignF.FOLDER, ICON_SIZE));
    setClosedIcon(FontIcon.of(MaterialDesignF.FOLDER, ICON_SIZE));
    scriptIcon = FontIcon.of(MaterialDesignF.FILE_CODE_OUTLINE, ICON_SIZE);
    setLeafIcon(FontIcon.of(MaterialDesignF.FILE_DOCUMENT_OUTLINE, ICON_SIZE));
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                boolean expanded, boolean leaf, int row, boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    // If the node is a Path, customize its appearance
    if (value instanceof Path path) {
      // Check if this is the root node
      if (path.equals(model.getRoot())) {
        // Set the text to the project name for the root node
        setText(model.getProject().getName());
      } else {
        // Set the text to the filename for non-root nodes
        setText(path.getFileName().toString());
      }

      // Set the appropriate icon based on whether it's a file or directory
      if (Files.isDirectory(path)) {
        setIcon(expanded ? getOpenIcon() : getClosedIcon());
        setFont(getFont().deriveFont(Font.PLAIN));
        setForeground(selected ? SELECTED_FOREGROUND_COLOR : DEFAULT_FOREGROUND_COLOR);
      } else {
        if (PyRunner.isExecutableScript(path)) {
          setIcon(scriptIcon);
        } else {
          setIcon(getLeafIcon());
        }

        // Apply styling based on file state
        if (model.isFileOpened(path)) {
          setFont(getFont().deriveFont(Font.BOLD));
        } else {
          setFont(getFont().deriveFont(Font.PLAIN));
        }

        if (model.isFileWriteProtected(path)) {
          // Write-protected files are red
          setForeground(WRITE_PROTECTED_COLOR);
        } else if (fileManager.isFileModified(path)) {
          // Edited files are blue
          setForeground(EDITED_COLOR);
        } else {
          // Default files are black
          setForeground(selected ? SELECTED_FOREGROUND_COLOR : DEFAULT_FOREGROUND_COLOR);
        }
      }
    }

    return this;
  }
}
