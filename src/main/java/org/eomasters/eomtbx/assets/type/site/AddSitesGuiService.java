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

package org.eomasters.eomtbx.assets.type.site;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.swing.binding.BindingContext;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.PropertyHelper;
import org.eomasters.eomtbx.assets.gui.AddAssetGuiService;
import org.eomasters.eomtbx.assets.gui.PropertiesGuiHelper;

public class AddSitesGuiService extends AddAssetGuiService {

  @Override
  public Class<? extends AssetType> getAssetType() {
    return SitesType.class;
  }

  @Override
  public JPanel createPanel(BindingContext context) {
    PropertySet propertySet = context.getPropertySet();
    PropertyHelper.createSelectionGroup(propertySet.getProperty(SitesType.PROP_ADD_TO_PINS), propertySet.getProperty(SitesType.PROP_ADD_TO_GCP),
        propertySet.getProperty(SitesType.PROP_ADD_TO_VDN), propertySet.getProperty(SitesType.PROP_NEW_VECTOR_NODE));

    JPanel panel = PropertiesGuiHelper.createPropertiesPanel(context);
    JComponent[] components = context.getBinding(SitesType.PROP_NEW_NODE_NAME).getComponents();
    PropertiesGuiHelper.changePreferredWidth(components, JTextField.class, 120);
    context.bindEnabledState(SitesType.PROP_SELECTED_VECTOR_NODE, true, SitesType.PROP_ADD_TO_VDN, true);
    context.bindEnabledState(SitesType.PROP_NEW_NODE_NAME, true, SitesType.PROP_NEW_VECTOR_NODE, true);


    // if no nodes selectable, disable ADD_TO_VDN component
    Property selectedVectorGroup = propertySet.getProperty(SitesType.PROP_SELECTED_VECTOR_NODE);
    int selectableNodesCount = selectedVectorGroup.getDescriptor().getValueSet().getItems().length;
    context.setComponentsEnabled(SitesType.PROP_ADD_TO_VDN, selectableNodesCount != 0);

    return panel;
  }


  @Override
  public String getHelpId() {
    return super.getHelpId() + ".type.sites";
  }
}
