/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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

package org.eomasters.eomtbx.utils.gui;

import java.awt.Color;
import java.awt.Font;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class ComponentHighlighter {

  private Border oldBorder;
  private Color color = Color.red.darker();
  private int margin = 2;
  private double duration = 2.0;
  private JComponent theComponent;
  private String infoMessage;
  private PopupPanel popupComponent;

  public void setColor(Color color) {
    this.color = color;
  }

  public void setMargin(int margin) {
    this.margin = margin;
  }

  /**
   * Sets the highlighting duration in seconds.
   * @param duration in seconds
   */
  public void setDuration(double duration) {
    this.duration = duration;
  }

  public void setInfoMessage(String infoMessage) {
    this.infoMessage = infoMessage;
  }

  public void highlight(JComponent component) {
    oldBorder = component.getBorder();
    theComponent = component;
    theComponent.setBorder(new MatteBorder(margin, margin, margin, margin, color));
    if (infoMessage != null) {
      popupComponent = createPopup(infoMessage);
    }

    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.schedule(this::removeHighlighting, (long) (duration * 1000), TimeUnit.MILLISECONDS);
  }

  private PopupPanel createPopup(String message) {
    MultiLineText textField = new MultiLineText(message);
    textField.setFont(textField.getFont().deriveFont(Font.BOLD));
    textField.setEditable(false);
    textField.setBorder(new EmptyBorder(0, 0, 0, 0));
    float[] rgbComponents = color.getRGBComponents(null);
    textField.setBackground(new Color(color.getColorSpace(), rgbComponents, rgbComponents[3] * 0.3f));
    PopupPanel popupComponent = new PopupPanel(textField);
    popupComponent.showPopupAt(theComponent.getLocationOnScreen().x,theComponent.getLocationOnScreen().y + theComponent.getHeight());
    return popupComponent;
  }

  private void removeHighlighting() {
    theComponent.setBorder(oldBorder);
    if (popupComponent != null) {
      popupComponent.hidePopup();
    }
  }
}
