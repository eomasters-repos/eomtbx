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

package org.eomasters.eomtbx;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.net.URI;
import java.net.URL;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import org.esa.snap.rcp.about.AboutBox;
import org.esa.snap.rcp.util.BrowserUtils;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;

@AboutBox(displayName = "EOMTBX", position = 500)
public class EomtbxAboutBox extends JPanel {


  public EomtbxAboutBox() {
    super(new BorderLayout(4, 4));
    setBorder(new EmptyBorder(4, 4, 4, 4));
    URL aboutLogo = EomtbxAboutBox.class.getResource("about-eomtbx-logo.png");
    ImageIcon aboutImage = new ImageIcon(aboutLogo);
    JLabel iconLabel = new JLabel(aboutImage);
    add(iconLabel, BorderLayout.CENTER);
    add(createVersionPanel(), BorderLayout.SOUTH);
  }

  private JPanel createVersionPanel() {
    String eomLink = "<a href=\"" + EomToolbox.EOMASTERS_URL + "\">EOMasters</a>";
    JLabel copyrightLabel = new JLabel("<html><b>©2023 Marco Peters from "+eomLink+"</b>", SwingConstants.CENTER);
    copyrightLabel.addMouseListener(new BrowserUtils.URLClickAdaptor(EomToolbox.EOMASTERS_URL.toString()));

    final ModuleInfo moduleInfo = Modules.getDefault().ownerOf(EomtbxAboutBox.class);
    JLabel versionLabel = new JLabel(
        "<html><b>EOMasters Toolbox version " + moduleInfo.getImplementationVersion() + "</b>", SwingConstants.CENTER);

    URI changelogUri = URI.create("https://github.com/eomasters-repos/eomtbx/releases");
    final JLabel releaseNoteLabel = new JLabel("<html><a href=\"" + changelogUri + "\">Release Notes</a>",
        SwingConstants.CENTER);
    releaseNoteLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    releaseNoteLabel.addMouseListener(new BrowserUtils.URLClickAdaptor(changelogUri.toString()));

    final JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.add(copyrightLabel);
    mainPanel.add(versionLabel);
    mainPanel.add(releaseNoteLabel);
    return mainPanel;
  }
}
