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

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.EomtbxIcons;
import org.eomasters.icons.Icon;

class CoastalMapBandSettingsPanel extends JPanel {

  static final String NEAREST = "NEAREST";
  static final String AGGREGATE = "AGGREGATE";
  private ButtonGroup group;
  private final JCheckBox masksCheckbox;

  public CoastalMapBandSettingsPanel() {

    masksCheckbox = new JCheckBox("Yes", true);

    setLayout(new MigLayout("gap 5 10, align left"));
    setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    JLabel icon = new JLabel(EomtbxIcons.COASTAL_MAP.getImageIcon(Icon.SIZE_48));
    JLabel introLabel = new JLabel("<html>Add a coastal map band to the selected product.");
    this.add(icon);
    this.add(introLabel, "wrap");
    JPanel optionsPanel = createOptionsPanel();
    this.add(optionsPanel, "span, grow");
  }

  private JPanel createOptionsPanel() {
    JPanel optionsPanel = new JPanel(new MigLayout("gap 5,  insets 4"));
    optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

    JRadioButton nearest = new JRadioButton("Nearest Neighbour");
    JRadioButton aggregate = new JRadioButton("Aggregate");
    nearest.setActionCommand(NEAREST);
    aggregate.setActionCommand(AGGREGATE);
    group = new ButtonGroup();
    group.add(nearest);
    group.add(aggregate);
    group.setSelected(nearest.getModel(), true);

    JLabel samplingLabel = new JLabel("Select sampling type:");
    samplingLabel.setToolTipText(
        "<html><b>Nearest Neighbour</b> is faster, but <b>Aggregate</b> provides better results if the target resolution <br>is a magnitude lower then the coastal map (10m)");
    optionsPanel.add(samplingLabel, "align left");
    optionsPanel.add(nearest, "wrap");
    optionsPanel.add(aggregate, "skip 1, wrap");

    JLabel masksLabel = new JLabel("Add masks to the product:");
    masksLabel.setToolTipText("<html>Adds masks to the product which make the usage of flags easier.");
    optionsPanel.add(masksLabel, "gaptop 10, align left");
    optionsPanel.add(masksCheckbox);
    return optionsPanel;
  }

  public String getSampling() {
    return group.getSelection().getActionCommand();
  }

  public boolean areMasksEnabled() {
    return masksCheckbox.isSelected();
  }
}
