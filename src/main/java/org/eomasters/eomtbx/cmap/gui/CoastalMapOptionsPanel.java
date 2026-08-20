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

package org.eomasters.eomtbx.cmap.gui;

import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.cmap.CoastalMap;
import org.eomasters.gui.Highlighter;

public class CoastalMapOptionsPanel extends JPanel {


  private final JTextField cacheDirField;
  private final JTextField remoteLocationField;
  private String remoteLocation;
  private Path cacheDir;

  public static void main(String[] args) throws Exception {
    // when SNAP is run the NetBeans dependency needs to be declared in POM
    // UIManager.setLookAndFeel(FlatLightLaf.class.getName());

    final JFrame frame = new JFrame("Coastal Map Options");
    frame.setContentPane(new CoastalMapOptionsPanel());

    frame.setPreferredSize(new Dimension(400, 200));
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    SwingUtilities.invokeLater(() -> frame.setVisible(true));
  }


  public CoastalMapOptionsPanel() {
    super(new MigLayout("top, left, gap 5, fillx", "[grow 0][fill][grow 0]"));
    cacheDir = CoastalMap.getInstance().getCacheDir();
    remoteLocation = CoastalMap.getInstance().getRemoteLocation();

    add(new JLabel("Local Data Cache Dir:"));
    cacheDirField = new JTextField(cacheDir.toAbsolutePath().toString());
    add(cacheDirField, "wmin 10");
    JButton dirBtn = new JButton("...");
    add(dirBtn, "wrap");

    add(new JLabel("Remote Location:"));
    remoteLocationField = new JTextField(remoteLocation);
    add(remoteLocationField, "span 2, wmin 10");

    dirBtn.addActionListener(e -> {
      JFileChooser dirChooser = new JFileChooser();
      dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      dirChooser.setSelectedFile(cacheDir.toFile());
      if (dirChooser.showDialog(this.getParent(), "Select") == JFileChooser.APPROVE_OPTION) {
        cacheDir = dirChooser.getSelectedFile().toPath();
        updateCacheDirField();
      }
    });

    cacheDirField.addActionListener(e -> {
      Path maybeCacheDir = Paths.get(cacheDirField.getText());
      if (!Files.isDirectory(maybeCacheDir)) {
        Highlighter.error(cacheDirField, "Not a valid directory path");
        return;
      }
      cacheDir = maybeCacheDir;
    });

    remoteLocationField.addActionListener(e -> {
      String remoteLocation = remoteLocationField.getText();
      try {
        new URL(remoteLocation);
      } catch (MalformedURLException ex) {
        Highlighter.error(remoteLocationField, "Invalid URL:" + ex.getMessage());
        return;
      }
      this.remoteLocation = remoteLocation;
    });

  }

  private void updateCacheDirField() {
    String newCacheDirText = cacheDir.toAbsolutePath().toString();
    String oldCacheDirText = cacheDirField.getText();
    if (!oldCacheDirText.equals(newCacheDirText)) {
      cacheDirField.setText(newCacheDirText);
    }
  }


  public boolean isChanged() {
    CoastalMap coastalMap = CoastalMap.getInstance();
    return !coastalMap.getCacheDir().equals(cacheDir) && !coastalMap.getRemoteLocation().equals(remoteLocation);
  }

  public void store() {
    CoastalMap coastalMap = CoastalMap.getInstance();
    coastalMap.setCacheDir(cacheDir);
    coastalMap.setRemoteLocation(remoteLocation);
  }
}
