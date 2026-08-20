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

import static org.eomasters.utils.Exceptions.throwIf;

import com.bc.ceres.binding.ConversionException;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A named site on Earth defined by a {@link #x x-coordinate}, {@link #y y-coordinate} and {@link #crs CRS} .
 */
public class Site {

  private String name;
  private double x;
  private double y;
  private String description;
  private String crs;
  private SiteStyle style;

  /**
   * Creates a new instance of {@link Site}. The CRS is defaulted to {@code EPSG:4326}.
   *
   * @param name the name of the site
   * @param x    the x-coordinate in CRS units
   * @param y    the y-coordinate in CRS units
   */
  public Site(String name, double x, double y) {
    this(name, x, y, String.format("%s at (%.3f, %.3f)", name, x, y), "EPSG:4326");
  }

  /**
   * Creates a new instance of {@link Site}. The crs should be defined as WKT string or EPSG code.
   *
   * @param name        the name of the site
   * @param x           the x-coordinate in CRS units
   * @param y           the y-coordinate in CRS units
   * @param description the description
   * @param crs         the CRS
   */
  public Site(String name, double x, double y, String description, String crs) {
    this.name = name;
    throwIf(name == null || name.isBlank(), new IllegalStateException("name must not be null or empty"));
    this.x = x;
    this.y = y;
    this.description = description;
    this.crs = crs;
    this.style = new SiteStyle();
  }

  /**
   * Returns the name of the site.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the site.
   *
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the x-coordinate in CRS units.
   *
   * @return the x-coordinate
   */
  public double getX() {
    return x;
  }

  /**
   * Sets the x-coordinate in CRS units.
   *
   * @param x the new x-coordinate
   */
  public void setX(double x) {
    this.x = x;
  }

  /**
   * Returns the y-coordinate in CRS units.
   *
   * @return the y-coordinate
   */
  public double getY() {
    return y;
  }

  /**
   * Sets the y-coordinate in CRS units.
   *
   * @param y the new y-coordinate
   */
  public void setY(double y) {
    this.y = y;
  }

  /**
   * Returns the description of the site.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description of the site.
   *
   * @param description the new description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the CRS.
   *
   * @return the CRS as WKT string or EPSG code.
   */
  public String getCrs() {
    return crs;
  }

  /**
   * Sets the CRS.
   *
   * @param crs the new CRS
   */
  public void setCrs(String crs) {
    this.crs = crs;
  }

  /**
   * Returns the style of the object.
   *
   * @return the style
   */
  public SiteStyle getStyle() {
    return style;
  }

  /**
   * Sets the style of the object.
   *
   * @param style the new style to set
   */
  public void setStyle(SiteStyle style) {
    this.style = style;
  }


  @Override
  public String toString() {
    String descPart = "";
    if (description != null && !description.isBlank()) {
      descPart = String.format("description='%s', ", description.replace("'", "''"));
    }
    return String.format("Site{label='%s', x='%.8f', y='%.8f', crs='%s', " + descPart + "style='%s'}",
        name.replace("'", "''"), x, y, crs.replace("'", "''"), style.toCssString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Site site = (Site) o;
    return Double.compare(getX(), site.getX()) == 0 && Double.compare(getY(), site.getY()) == 0
        && Objects.equals(getName(), site.getName()) && Objects.equals(getDescription(),
        site.getDescription()) && Objects.equals(getCrs(), site.getCrs()) && Objects.equals(style,
        site.style);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getDescription(), getX(), getY(), getCrs(), style);
  }

  public static class Converter implements com.bc.ceres.binding.Converter<Site> {

    @Override
    public Class<Site> getValueType() {
      return Site.class;
    }

    @Override
    public Site parse(String text) throws ConversionException {
      if (text == null || text.isEmpty()) {
        return null;
      }
      if (text.startsWith("Site{") && text.endsWith("}")) {
        text = text.substring(5, text.length() - 1); // remove 'Site{' and '}'
      } else {
        throw new ConversionException("'" + text + "'" + " is not a valid Site object.");
      }

      String pattern = "(\\w+)='(.*?)(?<!')'(,|$)";
      Pattern r = Pattern.compile(pattern);
      Matcher m = r.matcher(text);

      HashMap<String, String> map = new HashMap<>();

      // group values by their labels
      while (m.find()) {
        map.put(m.group(1), m.group(2));
      }

      try {
        double x = Double.parseDouble(map.get("x"));
        double y = Double.parseDouble(map.get("y"));
        String label = map.get("label").replace("''", "'");
        String description = map.get("description");
        if (description != null) {
          description = description.replace("''", "'");
        } else {
          description = "";
        }
        String crs = map.get("crs").replace("''", "'");
        String style = map.get("style");

        Site site = new Site(label, x, y, description, crs);
        site.setStyle(SiteStyle.create(style));
        return site;

      } catch (NumberFormatException ex) {
        throw new ConversionException("'" + text + "'" + " is not a valid Site object.", ex);
      }
    }

    @Override
    public String format(Site value) {
      return value.toString();
    }
  }
}
