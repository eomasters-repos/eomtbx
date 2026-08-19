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

import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.cmap.FlagsAndMasks;
import org.openide.awt.ColorComboBox;

public class MaskAppearancePanel extends JPanel {

  protected static final int MAX_ALPHA = 255;
  private final MaskOptionsModel flagMasks;

  public static void main(String[] args) throws Exception {
    // when SNAP is run the NetBeans dependency needs to be declared in POM
    // UIManager.setLookAndFeel(FlatLightLaf.class.getName());

    final JFrame frame = new JFrame("Mask Options");
    frame.setContentPane(new MaskAppearancePanel());

    frame.setSize(400, 600);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    SwingUtilities.invokeLater(() -> frame.setVisible(true));
  }

  public MaskOptionsModel getFlagMasks() {
    return flagMasks;
  }

  public MaskAppearancePanel() {
    super(new MigLayout("gap 5, fillx"));
    flagMasks = new MaskOptionsModel(FlagsAndMasks.FLAG_MASKS);

    JLabel nameHeader = new JLabel("<html><b>Mask Name");
    nameHeader.setHorizontalAlignment(SwingConstants.CENTER);
    add(nameHeader, "pushx 10, alignx center");
    JLabel colorHeader = new JLabel("<html><b>Color");
    colorHeader.setHorizontalAlignment(SwingConstants.CENTER);
    add(colorHeader, "alignx center");
    JLabel transHeader = new JLabel("<html><b>Transparency");
    transHeader.setHorizontalAlignment(SwingConstants.CENTER);
    add(transHeader, "alignx center, wrap");

    NumberFormat transparencyFormat = DecimalFormat.getInstance();
    transparencyFormat.setMaximumFractionDigits(2);
    for (MaskModel flagMask : flagMasks) {
      add(new JLabel("<html><b>" + flagMask.name), "grow 10");
      ColorComboBox colorComboBox = new ColorComboBox();
      colorComboBox.setSelectedColor(flagMask.color);
      add(colorComboBox, "pushx, growx");
      colorComboBox.addActionListener(e -> flagMasks.setColor(flagMask.name, colorComboBox.getSelectedColor()));

      JPanel transparencyPanel = new JPanel(new MigLayout("gapy 0, fill"));
      JSlider alphaSlider = new JSlider(0, MAX_ALPHA);
      alphaSlider.setMinorTickSpacing(5);
      alphaSlider.setMajorTickSpacing(25);
      JFormattedTextField transparencyField = new JFormattedTextField(transparencyFormat);
      transparencyField.setFont(transparencyField.getFont().deriveFont(Font.BOLD));
      transparencyField.setHorizontalAlignment(JFormattedTextField.CENTER);
      transparencyPanel.add(transparencyField, "grow, wrap");
      transparencyPanel.add(alphaSlider, "grow");
      add(transparencyPanel, "push, grow, wrap");

      alphaSlider.addChangeListener(e -> {
        double transparency = toTransparency(alphaSlider.getValue());
        if (!transparencyFormat.format(transparency).equals(transparencyField.getValue())) {
          transparencyField.setValue(transparency);
          flagMasks.setTransparency(flagMask.name, transparency);
        }
      });
      transparencyField.addActionListener(e -> {
        Number alphaNumber = (Number) transparencyField.getValue();
        int alpha = toAlpha(alphaNumber.doubleValue());
            if (alphaSlider.getValue() != alpha) {
              alphaSlider.setValue(alpha);
            }
          }
      );
      transparencyField.setValue(flagMask.transparency);
    }
  }

  private static int toAlpha(double transparency) {
    return (int) (MAX_ALPHA * transparency);
  }

  private static float toTransparency(int alpha) {
    return (float) alpha / MAX_ALPHA;
  }


}
