/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox Basic for SNAP
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
import java.awt.Cursor;
import java.net.URI;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.esa.snap.rcp.util.BrowserUtils;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;

/**
 * Provides the AboutBox for the EOMasters Toolbox Basic.
 */
public class FreeAboutBoxProvider implements AboutBoxProvider {

  @Override
  public String getTitle() {
    return "Free";
  }

  @Override
  public ImageIcon getLogoImage() {
    return EomtbxIcons.EOMTBX_TEXT_BELOW.getImageIcon(365);
  }

  @Override
  public Component getAboutPanel() {
    final JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.add(getCopyrightLabel());
    mainPanel.add(getVersionLabel());
    mainPanel.add(getReleaseNotesLabel());
    return mainPanel;
  }

  private static JLabel getCopyrightLabel() {
    String eomLink = "<a href=\"" + EomToolbox.EOMASTERS_URL + "\">EOMasters</a>";
    JLabel copyrightLabel = new JLabel("<html><b>©2023-2024 Marco Peters from " + eomLink + "</b>",
        SwingConstants.CENTER);
    copyrightLabel.addMouseListener(new BrowserUtils.URLClickAdaptor(EomToolbox.EOMASTERS_URL.toString()));
    return copyrightLabel;
  }

  private static JLabel getReleaseNotesLabel() {
    URI changelogUri = URI.create("https://github.com/eomasters-repos/eomtbx/releases");
    final JLabel releaseNotesLabel = new JLabel(String.format("<html><a href=\"%s\">Release Notes</a>", changelogUri),
        SwingConstants.CENTER);
    releaseNotesLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    releaseNotesLabel.addMouseListener(new BrowserUtils.URLClickAdaptor(changelogUri.toString()));
    return releaseNotesLabel;
  }

  private static JLabel getVersionLabel() {
    ModuleInfo moduleInfo = Modules.getDefault().ownerOf(FreeAboutBoxProvider.class);
    ;
    return new JLabel(String.format("<html><b>EOMasters Toolbox Basic version %s</b>",
        moduleInfo.getImplementationVersion()), SwingConstants.CENTER);
  }

}
