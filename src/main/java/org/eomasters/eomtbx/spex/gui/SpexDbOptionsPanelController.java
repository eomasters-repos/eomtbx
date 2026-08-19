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

package org.eomasters.eomtbx.spex.gui;

import java.awt.Dimension;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.preferences.PropertyChangeOptionsPanelController;
import org.eomasters.eomtbx.spex.CustomSpex;
import org.eomasters.eomtbx.spex.DbSpex;
import org.eomasters.eomtbx.spex.SpexDb;
import org.eomasters.eomtbx.spex.SpexDb.ChangeListener;
import org.eomasters.icons.Icon;
import org.eomasters.icons.Icons;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Controller for the SpeX DB options panel.
 */
@SuppressWarnings("unused")
@OptionsPanelController.SubRegistration(
    id = "spexdb",
    location = "eomtbx",
    keywordsCategory = "EOMTBX",
    keywords = "EOMTBX, EOMASTERS, Toolbox, Spex",
    position = 1,
    displayName = "SpeX DB Manager")
public class SpexDbOptionsPanelController extends PropertyChangeOptionsPanelController {

  public static final String HID_EOMTBX_SPEXDB_OPTIONS = "eomtbx.options.spex.database";
  private JPanel mainPanel;
  private SpexDbPanel dbPanel;
  private JButton removeButton;
  private SpexInfoPanel infoPanel;
  private JButton editButton;

  @Override
  public void update() {
  }

  @Override
  public void applyChanges() {
  }

  @Override
  public void cancel() {
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public boolean isChanged() {
    return false;
  }

  @Override
  public JComponent getComponent(Lookup masterLookup) {
    if (mainPanel == null) {
      dbPanel = new SpexDbPanel(false, null, false);
      dbPanel.setPreferredSize(new Dimension(300, 400));
      dbPanel.addTreeSelectionListener(e -> updateUi());

      JPanel btnPanel = createButtonPanel();
      infoPanel = new SpexInfoPanel();
      infoPanel.setPreferredSize(new Dimension(200, 400));
      infoPanel.setBorder(new TitledBorder("Selected SpeX"));

      mainPanel = new JPanel(new MigLayout("top, left, gap 5"));
      mainPanel.add(dbPanel, "top, left, growx 80, pushy");
      mainPanel.add(btnPanel, "top, left, growx 0, pushy");
      mainPanel.add(infoPanel, "top, left, growx 20, push, wrap");
    }
    updateUi();
    return mainPanel;
  }

  private JPanel createButtonPanel() {
    JPanel btnPanel = new JPanel(new MigLayout("top, left, gapy 4, ins 0 0 0 10,"));
    JButton addCustomButton = new JButton(Icons.PLUS.getImageIcon(Icon.SIZE_24));
    addCustomButton.addActionListener(e -> {
      Window windowAncestor = SwingUtilities.getWindowAncestor(mainPanel);
      UserSpexEditorDialog addCustomSpex = new UserSpexEditorDialog(windowAncestor, false);
      addCustomSpex.setMode(UserSpexEditorDialog.Mode.ADD);
      addCustomSpex.setTitle("Add Custom SpeX");
      addCustomSpex.setLocationRelativeTo(windowAncestor);
      addCustomSpex.setVisible(true);

      CustomSpex createdSpex = addCustomSpex.getCustomSpex();
      if (createdSpex != null) {
        SpexDb spexDb = SpexDb.getInstance();
        spexDb.addChangeListener(new ChangeListener() {
          @Override
          public void spexDbChanged() {
            dbPanel.setSelectedSpex(createdSpex);
            spexDb.removeChangeListener(this);
          }
        });
        spexDb.addIndex(new DbSpex(createdSpex));
      }
    });
    btnPanel.add(addCustomButton, "top, left, wrap");
    removeButton = new JButton(Icons.MINUS.getImageIcon(Icon.SIZE_24));
    removeButton.addActionListener(e -> {
      DbSpex selectedSpex = dbPanel.getSelectedSpex();
      if (selectedSpex != null) {
        SpexDb.getInstance().removeIndex(selectedSpex);
      }
    });
    btnPanel.add(removeButton, "top, left, wrap");
    editButton = new JButton(Icons.PEN.getImageIcon(Icon.SIZE_24));
    editButton.addActionListener(e -> {
      DbSpex selectedSpex = dbPanel.getSelectedSpex();
      if (selectedSpex != null) {
        Window windowAncestor = SwingUtilities.getWindowAncestor(mainPanel);
        UserSpexEditorDialog editCustomSpex = new UserSpexEditorDialog(windowAncestor, false);
        editCustomSpex.setMode(UserSpexEditorDialog.Mode.EDIT);
        editCustomSpex.setTitle("Edit Custom SpeX");
        editCustomSpex.setLocationRelativeTo(windowAncestor);
        editCustomSpex.initBy(selectedSpex);
        editCustomSpex.setVisible(true);

        CustomSpex modifiedSpex = editCustomSpex.getCustomSpex();
        if (modifiedSpex != null) {
          SpexDb spexDb = SpexDb.getInstance();
          spexDb.removeIndex(selectedSpex);
          DbSpex dbSpex = new DbSpex(modifiedSpex);
          spexDb.addChangeListener(new ChangeListener() {
            @Override
            public void spexDbChanged() {
              dbPanel.setSelectedSpex(dbSpex);
              spexDb.removeChangeListener(this);
            }
          });

          spexDb.addIndex(dbSpex);
          // dbPanel.setSelectedSpex(dbSpex);
        }
      }
    });
    btnPanel.add(editButton, "top, left, pushy, wrap");
    return btnPanel;
  }

  private void updateUi() {
    DbSpex selectedSpex = dbPanel.getSelectedSpex();

    infoPanel.display(selectedSpex);
    removeButton.setEnabled(selectedSpex != null && SpexDb.getInstance().isUserSpex(selectedSpex));
    editButton.setEnabled(selectedSpex != null && SpexDb.getInstance().isUserSpex(selectedSpex));
  }

  @Override
  public HelpCtx getHelpCtx() {
    return new HelpCtx(HID_EOMTBX_SPEXDB_OPTIONS);
  }

}
