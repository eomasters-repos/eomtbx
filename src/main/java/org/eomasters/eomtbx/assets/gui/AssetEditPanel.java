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

import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.swing.binding.BindingContext;
import java.util.EventListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.EventListenerList;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.assets.HighlightingProblemListener;
import org.eomasters.eomtbx.assets.Asset;

public class AssetEditPanel extends JPanel {


  private final EventListenerList changeListenerList;
  private Asset asset;
  private JTextField nameField;
  private JTextField descriptionField;
  private JTextField tagsField;
  private BindingContext baseBinding;
  private JTextArea notesField;

  public AssetEditPanel() {
    changeListenerList = new EventListenerList();
    initPanel();
  }

  void setEditableAsset(Asset asset) {
    if (asset != this.asset) {
      this.asset = asset;

      if (baseBinding != null) {
        baseBinding.unbind(baseBinding.getBinding("name"));
        baseBinding.unbind(baseBinding.getBinding("description"));
        baseBinding.unbind(baseBinding.getBinding("tags"));
        baseBinding.unbind(baseBinding.getBinding("userNotes"));
        baseBinding = null;
      }
      if (asset != null) {
        PropertyContainer objectBacked = PropertyContainer.createObjectBacked(asset);
        baseBinding = new BindingContext(objectBacked);
        baseBinding.bind("name", nameField);
        baseBinding.bind("description", descriptionField);
        baseBinding.bind("tags", tagsField);
        baseBinding.bind("userNotes", notesField);
        PropertiesGuiHelper.changeProblemListener(baseBinding, new HighlightingProblemListener());
        objectBacked.addPropertyChangeListener(evt -> fireAssetChanged(asset));
        enableComponents(true);
      } else {
        enableComponents(false);
        clearComponents();
      }
    }
  }

  public Asset getEditableAsset() {
    return asset;
  }

  private void enableComponents(boolean enabled) {
    nameField.setEnabled(enabled);
    descriptionField.setEnabled(enabled);
    tagsField.setEnabled(enabled);
    notesField.setEnabled(enabled);
  }

  private void clearComponents() {
    nameField.setText("");
    descriptionField.setText("");
    tagsField.setText("");
    notesField.setText("");
  }

  private void initPanel() {
    setLayout(new MigLayout(
        "top, left",
        "[][fill, grow, push]",
        "[][fill][fill][][fill, grow]"
    ));
    add(new JLabel("Name:"));
    nameField = new JTextField();
    add(nameField, ", wrap");

    add(new JLabel("Description:"));
    descriptionField = new JTextField();
    add(descriptionField, "wrap");

    add(new JLabel("Tags:"));
    tagsField = new JTextField();
    add(tagsField, "wrap");

    add(new JLabel("User Notes:"), "span 2, wrap");
    notesField = new JTextArea(6, 40);
    notesField.setLineWrap(true);
    notesField.setWrapStyleWord(true);

    JScrollPane notesScrollPane = new JScrollPane(notesField);
    notesScrollPane.setMinimumSize(notesField.getPreferredScrollableViewportSize());
    notesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    notesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    add(notesScrollPane, "span 2, grow, push");
  }

  public void addChangeListener(ChangeListener changeListener) {
    changeListenerList.add(ChangeListener.class, changeListener);
  }

  public void removeChangeListener(ChangeListener changeListener) {
    changeListenerList.remove(ChangeListener.class, changeListener);
  }

  private void fireAssetChanged(Asset asset) {
    for (ChangeListener changeListener : changeListenerList.getListeners(ChangeListener.class)) {
      changeListener.assetChanged(asset);
    }
  }

  public interface ChangeListener extends EventListener {

    void assetChanged(Asset asset);
  }

}
