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

package org.eomasters.eomtbx.assets.type.geometry;

import static org.eomasters.eomtbx.assets.type.geometry.GeometryType.PROP_ADD_TO_VDN;
import static org.eomasters.eomtbx.assets.type.geometry.GeometryType.PROP_CREATE_VECTOR_NODE;
import static org.eomasters.eomtbx.assets.type.geometry.GeometryType.PROP_NEW_NODE_NAME;
import static org.eomasters.eomtbx.assets.type.geometry.GeometryType.PROP_SELECTED_VECTOR_NODE;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerFilter;
import com.bc.ceres.glayer.support.LayerUtils;
import com.bc.ceres.swing.binding.BindingContext;
import eu.esa.snap.netbeans.docwin.WindowUtilities;
import java.util.stream.Stream;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.eomasters.eomtbx.assets.AssetType;
import org.eomasters.eomtbx.assets.PropertyHelper;
import org.eomasters.eomtbx.assets.gui.AddAssetGuiService;
import org.eomasters.eomtbx.assets.gui.PropertiesGuiHelper;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.VectorDataNode;
import org.esa.snap.rcp.windows.ProductSceneViewTopComponent;
import org.esa.snap.ui.product.ProductSceneView;
import org.esa.snap.ui.product.VectorDataLayerFilterFactory;

public class AddGeometryGuiService extends AddAssetGuiService {

  @Override
  public Class<? extends AssetType> getAssetType() {
    return GeometryType.class;
  }

  @Override
  public JPanel createPanel(BindingContext context) {
    PropertySet propertySet = context.getPropertySet();
    PropertyHelper.createSelectionGroup(propertySet.getProperty(PROP_ADD_TO_VDN),
        propertySet.getProperty(PROP_CREATE_VECTOR_NODE));

    JPanel panel = PropertiesGuiHelper.createPropertiesPanel(context);

    JComponent[] components = context.getBinding(PROP_NEW_NODE_NAME).getComponents();
    PropertiesGuiHelper.changePreferredWidth(components, JTextField.class, 120);

    context.bindEnabledState(PROP_SELECTED_VECTOR_NODE, true, PROP_ADD_TO_VDN, true);
    context.bindEnabledState(PROP_NEW_NODE_NAME, true, PROP_CREATE_VECTOR_NODE, true);

    // if no nodes selectable, disable ADD_TO_VDN component
    Property selectedVectorGroup = propertySet.getProperty(PROP_SELECTED_VECTOR_NODE);
    int selectableNodesCount = selectedVectorGroup.getDescriptor().getValueSet().getItems().length;
    context.setComponentsEnabled(PROP_ADD_TO_VDN, selectableNodesCount != 0);

    return panel;
  }

  @Override
  public void finishAddingAsset(Product product, PropertySet addConfiguration) {
    VectorDataNode vectorNode = getVectorDataNode(product, addConfiguration);
    if (vectorNode == null) {
      return;
    }
    Stream<ProductSceneViewTopComponent> openedViews = WindowUtilities.getOpened(ProductSceneViewTopComponent.class);
    Stream<ProductSceneView> viewsOfProduct = openedViews.map(ProductSceneViewTopComponent::getView)
                                                         .filter(view -> view.getProduct() == product);
    viewsOfProduct.forEach(sceneView -> {
      LayerFilter nodeFilter = VectorDataLayerFilterFactory.createNodeFilter(vectorNode);
      Layer newSelectedLayer = LayerUtils.getChildLayer(sceneView.getRootLayer(),
          LayerUtils.SEARCH_DEEP,
          nodeFilter);
      if (newSelectedLayer != null) {
        newSelectedLayer.setVisible(true);
      }
    });
  }

  @Override
  public String getHelpId() {
    return super.getHelpId() + ".type.geometry";
  }

  private VectorDataNode getVectorDataNode(Product product, PropertySet addConfiguration) {
    String vdnName;
    if (addConfiguration.getProperty(PROP_ADD_TO_VDN).getValue()) {
      vdnName = addConfiguration.getProperty(PROP_SELECTED_VECTOR_NODE).getValue();
    } else {
      vdnName = addConfiguration.getProperty(PROP_NEW_NODE_NAME).getValue();
    }
    return product.getVectorDataGroup().get(vdnName);
  }
}
