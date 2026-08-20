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

import java.awt.BorderLayout;
import java.awt.Window;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;
import org.eomasters.eomtbx.spex.AbstractSpex;
import org.eomasters.eomtbx.spex.CustomSpex;
import org.eomasters.eomtbx.spex.DbSpex;
import org.esa.snap.core.datamodel.Product;
import org.locationtech.jts.geom.Geometry;

public class UserSpexEditorDialog extends JDialog {

  private final SpexEditorPanel spexEditorPanel;
  private GeneralMaskPanel maskPanel;
  private CustomSpex spex = null;

  public static void main(String[] args) {
    UserSpexEditorDialog dialog = new UserSpexEditorDialog(null, true);
    dialog.setLocationRelativeTo(null);
    dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    SwingUtilities.invokeLater(() -> dialog.setVisible(true));
  }

  public UserSpexEditorDialog(Window owner, boolean editMasks) {
    super(owner, "Custom SpeX", ModalityType.DOCUMENT_MODAL);
    if (owner != null) {
      setIconImages(owner.getIconImages());
    }
    setResizable(false);

    JPanel editIndexPanel = new JPanel(new MigLayout("top, left, fillx, gap 4 10"));
    editIndexPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
    spexEditorPanel = new SpexEditorPanel();
    spexEditorPanel.setMode(Mode.EDIT);
    editIndexPanel.add(spexEditorPanel, editMasks ? "wrap" : "growx, pushx, wrap");
    if (editMasks) {
      maskPanel = new GeneralMaskPanel();
      editIndexPanel.add(maskPanel, "growx, pushx, wrap");
    }

    JPanel buttonPanel = createButtonPanel();
    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.add(editIndexPanel, BorderLayout.CENTER);
    contentPane.add(buttonPanel, BorderLayout.SOUTH);
    setContentPane(contentPane);
    pack();
  }

  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel(new MigLayout("flowx, center"));
    JButton applyButton = new JButton("OK");
    applyButton.addActionListener(e -> {
      boolean isSpexValid = spexEditorPanel.validateInput();
      boolean areMasksValid = true;
      if (maskPanel != null) {
        areMasksValid = maskPanel.validateInput();
      }
      if (!isSpexValid || !areMasksValid) {
        spex = null;
        return;
      }
      spex = new CustomSpex(spexEditorPanel.getSpex());
      if (maskPanel != null) {
        spex.setValidExpression(maskPanel.getValidExpression());
        spex.setShapefile(maskPanel.getShapefile());
        spex.setWktRegion(maskPanel.getWktGeometry());
      }
      setVisible(false);
    });
    buttonPanel.add(applyButton);
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> setVisible(false));
    buttonPanel.add(cancelButton);
    return buttonPanel;
  }

  public CustomSpex getCustomSpex() {
    return spex;
  }

  public void initBy(CustomSpex spex) {
    initEditPanel(spex);
    if (maskPanel != null) {
      initMaskPanel(spex);
    }
  }

  public void initBy(DbSpex spex) {
    initEditPanel(spex);
  }

  private void initEditPanel(AbstractSpex spex) {
    spexEditorPanel.setSpex(spex);
  }

  private void initMaskPanel(CustomSpex spex) {
    maskPanel.setValidExpression(spex.getValidExpression());
    Path shapefile = spex.getShapefile();
    if (shapefile != null) {
      maskPanel.setShapefile(shapefile);
    }
    Geometry wktRegion = spex.getWktRegion();
    if (wktRegion != null) {
      maskPanel.setWktField(wktRegion.toText());
    }
  }

  public void setMode(Mode mode) {
    spexEditorPanel.setMode(mode);
  }

  public void setReferenceProduct(Product sourceProduct) {
    maskPanel.setReferenceProduct(sourceProduct);
  }

  public enum Mode {
    ADD,
    EDIT
  }
}
