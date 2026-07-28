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

package org.eomasters.eomtbx.spex.gui;

import javax.swing.tree.DefaultMutableTreeNode;
import org.eomasters.eomtbx.spex.AbstractSpex;

public class SpexDbTreeNode extends DefaultMutableTreeNode {

  private final AbstractSpex index;

  public SpexDbTreeNode(AbstractSpex index) {
    super(index);
    this.index = index;
  }

  public AbstractSpex getIndex() {
    return index;
  }

  @Override
  public String toString() {
    return index.getName();
  }

  public boolean containsText(String text) {
    if (text == null) {
      return true;
    }
    String normalizedText = text.trim().toLowerCase();
    if (normalizedText.isEmpty()) {
      return true;
    }

    if (index.getName() != null && index.getName().toLowerCase().contains(normalizedText)) {
      return true;
    }
    if (index.getDescription() != null && index.getDescription().toLowerCase().contains(normalizedText)) {
      return true;
    }
    if (index.getDomain() != null && index.getDomain().name().toLowerCase().contains(normalizedText)) {
      return true;
    }
    if (index.getFormula() != null && index.getFormula().toLowerCase().contains(normalizedText)) {
      return true;
    }
    if (index.getSupportedPlatforms() != null
        && index.getSupportedPlatforms().stream().anyMatch(s -> s.getName().toLowerCase().contains(normalizedText))) {
      return true;
    }
    return false;
  }
}
