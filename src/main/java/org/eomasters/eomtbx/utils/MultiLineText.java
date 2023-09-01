package org.eomasters.eomtbx.utils;

import java.awt.Dimension;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class MultiLineText extends JTextArea {

  public MultiLineText(String text) {
    super(text);
    setEditable(false);
    setLineWrap(true);
    setWrapStyleWord(true);
    JLabel label = new JLabel();
    setFont(label.getFont());
    setBackground(label.getBackground());
    int textWidth = getTextWidth(text);
    int preferredWidth = (int) (Math.ceil(textWidth / 50.) * 50);
    setPreferredWidth(preferredWidth);
  }

  public void setPreferredWidth(int preferredWidth) {
    setPreferredSize(new Dimension(preferredWidth, getPreferredSize().height));
  }

  private int getTextWidth(String text) {
    AffineTransform identity = new AffineTransform();
    FontRenderContext frc = new FontRenderContext(identity, true, true);
    return (int) (getFont().getStringBounds(text, frc).getWidth());
  }
}
