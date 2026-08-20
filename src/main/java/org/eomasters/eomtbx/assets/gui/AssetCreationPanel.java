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

package org.eomasters.eomtbx.assets.gui;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.swing.binding.BindingContext;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.assets.AssetCreationService;
import org.eomasters.eomtbx.assets.HighlightingProblemListener;
import org.eomasters.eomtbx.assets.PropertyHelper;

class AssetCreationPanel extends JPanel {


  private final BindingContext basicBindings;
  private final BindingContext specificBindings;

  public AssetCreationPanel(AssetCreationService factory) {
    basicBindings = new BindingContext(createBasicProperties());
    specificBindings = new BindingContext(factory.createFactoryProperties());

    setLayout(new MigLayout("top, left, fillx,", "[grow, fill]", "[]"));
    add(new JLabel("General Attributes:"), "wrap");
    add(createBasicAttributesPanel(basicBindings), "wrap");
    add(new JSeparator(), "wrap");
    add(new JLabel("Specific Attributes:"), "wrap");
    JPanel panelWrapper = new JPanel(new MigLayout("top, left, fillx", "[grow, fill]"));
    AssetCreationGuiService factoryGui = AssetCreationGuiRegistry.instance().getUiFor(factory);
    JPanel factoryGuiPanel = factoryGui.createPanel(specificBindings);
    panelWrapper.add(factoryGuiPanel);
    add(panelWrapper);
    doLayout();
    Dimension preferredSize = getPreferredSize();
    setPreferredSize(new Dimension(450, preferredSize.height));
  }

  public String getAssetName() {
    return basicBindings.getPropertySet().getProperty("name").getValue();
  }

  public String getAssetDescription() {
    return basicBindings.getPropertySet().getProperty("description").getValue();
  }

  public String[] getAssetTags() {
    return basicBindings.getPropertySet().getProperty("tags").getValue();
  }

  public PropertySet getAssetProperties() {
    return specificBindings.getPropertySet();
  }

  private JPanel createBasicAttributesPanel(BindingContext basicBindings) {
    JPanel panel = new JPanel(new MigLayout("top, left, fillx", "[grow 0][fill, push, grow 100]"));
    panel.add(new JLabel("Name:"));
    JTextField nameField = new JTextField();
    BindingContext bc = basicBindings;
    PropertiesGuiHelper.changeProblemListener(bc, new HighlightingProblemListener());
    bc.bind("name", nameField);
    panel.add(nameField, "wrap");

    panel.add(new JLabel("Description:"));
    JTextArea descriptionField = new JTextArea(1, 60);
    descriptionField.setLineWrap(true);
    descriptionField.setWrapStyleWord(true);
    JScrollPane descrScroll = new JScrollPane(descriptionField);
    Dimension descrViewSize = descriptionField.getPreferredScrollableViewportSize();
    descrScroll.setPreferredSize(new Dimension(descrViewSize.width, descrViewSize.height * 2));
    descrScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    descrScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    bc.bind("description", descriptionField);
    panel.add(descrScroll, "wrap");

    panel.add(new JLabel("Tags:"));
    JTextArea tagsField = new JTextArea(1, 60);
    tagsField.setLineWrap(true);
    tagsField.setWrapStyleWord(true);
    JScrollPane tagsScroll = new JScrollPane(tagsField);
    Dimension tagsViewSize = tagsField.getPreferredScrollableViewportSize();
    tagsScroll.setPreferredSize(new Dimension(tagsViewSize.width, tagsViewSize.height * 2));
    tagsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    tagsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    bc.bind("tags", tagsField);
    panel.add(tagsScroll);

    return panel;

  }

  private PropertyContainer createBasicProperties() {
    final PropertyContainer model = new PropertyContainer();
    Property name = PropertyHelper.createProperty("name", String.class, "The name of the asset.",
        "My Asset");
    PropertyDescriptor nameDescriptor = name.getDescriptor();
    nameDescriptor.setNotEmpty(true);
    nameDescriptor.setNotNull(true);
    model.addProperty(name);

    model.addProperty(PropertyHelper.createProperty("description", String.class, "The description of the asset."));
    model.addProperty(PropertyHelper.createProperty("tags", String[].class, "The tags of the asset."));

    PropertyHelper.initDefaults(model);
    return model;
  }

  public boolean verifyUserInput() {
    boolean basicValid = PropertiesGuiHelper.validateValues(basicBindings);
    boolean specificValid = PropertiesGuiHelper.validateValues(specificBindings);
    return basicValid && specificValid;
  }

}
