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
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.binding.ValueSet;
import com.bc.ceres.swing.figure.FigureStyle;
import com.bc.ceres.swing.figure.support.CssColorConverter;
import com.bc.ceres.swing.figure.support.DefaultFigureStyle;
import com.bc.ceres.swing.figure.support.NamedSymbol;
import java.awt.Color;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.eomasters.utils.Colors;

public class SiteStyle {

  public static final String FILL = "fill";
  public static final String FILL_OPACITY = "fill-opacity";
  public static final String STROKE = "stroke";
  public static final String STROKE_OPACITY = "stroke-opacity";
  public static final String STROKE_WIDTH = "stroke-width";
  public static final String SYMBOL = "symbol";

  private static final String[] SYMBOL_NAMES = new String[]{
      NamedSymbol.CROSS.getName(),
      NamedSymbol.PIN.getName(),
      NamedSymbol.PLUS.getName(),
      NamedSymbol.STAR.getName(),
      NamedSymbol.CIRCLE.getName(),
      NamedSymbol.SQUARE.getName()
  };

  private static final String SEPARATOR = ";";
  private static final String KEY_VALUE_SEPARATOR = ":";

  private final PropertyContainer styles;


  public SiteStyle() {
    styles = new PropertyContainer();
    Property symbol = createProperty(SYMBOL, String.class, "The name of the symbol.");
    symbol.getDescriptor().setValueSet(new ValueSet(SYMBOL_NAMES));
    symbol.getDescriptor().setDefaultValue(SYMBOL_NAMES[0]);

    Property fill = createProperty(FILL, Color.class, "The fill color used to draw the symbol.");
    fill.getDescriptor().setDefaultValue(Colors.create("#FFFF00"));
    fill.getDescriptor().setConverter(new CssColorConverter());

    Property fillOpacity = createProperty(FILL_OPACITY, Double.class, "The opacity of the symbol fill color.");
    fillOpacity.getDescriptor().setDefaultValue(1.0);

    Property stroke = createProperty(STROKE, Color.class, "The stroke color used to draw the symbol.");
    stroke.getDescriptor().setDefaultValue(Colors.create("#FFFFFF"));
    stroke.getDescriptor().setConverter(new CssColorConverter());

    Property strokeOpacity = createProperty(STROKE_OPACITY, Double.class, "The opacity of the symbol outline.");
    fillOpacity.getDescriptor().setDefaultValue(1.0);

    Property strokeWidth = createProperty(STROKE_WIDTH, Double.class, "The stroke color used to draw the symbol.");
    strokeWidth.getDescriptor().setDefaultValue(0.5);

    styles.addProperties(symbol, fill, fillOpacity, stroke, strokeOpacity, strokeWidth);
    styles.setDefaultValues();
  }

  /**
   * Sets the style based on the input string. The string is a semi-colon-separated list of key-value pairs. Each pair
   * is separated by a ':'. For example: "stroke:red;fill:blue;symbol:star"
   *
   * @param text the input text representing the style
   */
  public void setStyleFromText(String text) {
    String[] split = text.split(SEPARATOR);
    for (String keyValue : split) {
      String[] keyValueSplit = keyValue.split(KEY_VALUE_SEPARATOR);
      if (keyValueSplit.length == 2) {
        String styleName = keyValueSplit[0].trim();
        if (styles.isPropertyDefined(styleName)) {
          try {
            styles.getProperty(styleName).setValueFromText(keyValueSplit[1]);
          } catch (ValidationException e) {
            throw new IllegalArgumentException("Failed to set style", e);
          }
        } else {
          throw new IllegalArgumentException("Unknown style property: " + styleName);
        }
      }
    }
  }

  public FigureStyle getAsFigureStyle() {
    return DefaultFigureStyle.createFromCss(getAsText());
  }

  public String toCssString() {
    return getAsFigureStyle().toCssString();
  }

  private @Nonnull String getAsText() {
    StringBuilder sb = new StringBuilder();
    for (Property property : styles.getProperties()) {
      sb.append(property.getName());
      sb.append(KEY_VALUE_SEPARATOR);
      sb.append(property.getValueAsText());
      sb.append(SEPARATOR);
    }
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }

  public void setStyle(String name, Object value) {
    styles.setValue(name, value);
  }

  public <T> T getStyle(String name) {
    return styles.getValue(name);
  }

  private static Property createProperty(String name, Class<?> type, String description) {
    Property property = Property.create(name, type);
    property.getDescriptor().setDescription(description);
    return property;
  }


  public static SiteStyle create(String text) {
    SiteStyle style = new SiteStyle();
    style.setStyleFromText(text);
    return style;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SiteStyle style = (SiteStyle) o;
    Property[] properties = styles.getProperties();
    for (Property property : properties) {
      boolean equals = Objects.equals(property.getValue(), style.getStyle(property.getName()));
      if (!equals) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return getAsText();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(styles);
  }
}
