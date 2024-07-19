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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.util.Optional;
import java.util.ServiceLoader;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import org.esa.snap.rcp.about.AboutBox;

/**
 * The common {@link AboutBox} which is registered to SNAP/NetBeans. It provides an extension point usgin the
 * {@link AboutBoxProvider} for variants of the EOMTBX.
 */
@AboutBox(displayName = "EOMTBX", position = 500)
public class EomtbxAboutBox extends JPanel {

  /**
   * Creates a new AboutBox.
   */
  public EomtbxAboutBox() {
    super(new BorderLayout(4, 4));
    setBorder(new EmptyBorder(4, 4, 4, 4));
    ServiceLoader<AboutBoxProvider> eomToolboxServiceLoader = ServiceLoader.load(AboutBoxProvider.class);
    long numBoxes = eomToolboxServiceLoader.stream().count();
    Component infoPanel = null;
    CardLayout logoCards = new CardLayout();
    JPanel logoCardsPanel = new JPanel(logoCards);
    if (numBoxes == 1) {
      Optional<AboutBoxProvider> first = eomToolboxServiceLoader.findFirst();
      if (first.isPresent()) {
        AboutBoxProvider provider = first.get();
        infoPanel = provider.getAboutPanel();
        ImageIcon logoImage = provider.getLogoImage();
        JLabel label = new JLabel(logoImage);
        logoCardsPanel.add(label, provider.getTitle());
      }
    } else {
      JTabbedPane tabbedInfoPanel = new JTabbedPane();
      eomToolboxServiceLoader.forEach(provider -> {
        tabbedInfoPanel.addTab(provider.getTitle(), provider.getAboutPanel());
        ImageIcon logoImage = provider.getLogoImage();
        JLabel label = new JLabel(logoImage);
        logoCardsPanel.add(label, provider.getTitle());
      });
      tabbedInfoPanel.getModel().addChangeListener(e -> {
        String name = tabbedInfoPanel.getTitleAt(tabbedInfoPanel.getSelectedIndex());
        logoCards.show(logoCardsPanel, name);
      });

      infoPanel = tabbedInfoPanel;
    }

    add(logoCardsPanel, BorderLayout.CENTER);
    if (infoPanel != null) {
      add(infoPanel, BorderLayout.SOUTH);
    }
  }

}
