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

package org.eomasters.eomtbx.spex;

import java.nio.file.Path;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.util.converters.JtsGeometryConverter;
import org.locationtech.jts.geom.Geometry;

public class CustomSpex extends AbstractSpex {
  // It is necessary to duplicate the fields here and in DbSpex, because the GPF framework does not handle inheritance
  // and composition in a suitable way for parameters.

  @Parameter(description = "The name of the spectral index.")
  private String name;
  @Parameter(description = "The application domain of the spectral index.")
  private Domain domain;
  @Parameter(description = "The formula to be used for the spectral index. Can be omitted if a known SpeX is specified "
      + "by the name.")
  private String formula;
  @Parameter(description = "The description of the spectral index.")
  private String description;
  @Parameter(description = "Deprecation reason and alternatives.")
  private String deprecation;
  @Parameter(description = "Optional URL to the reference of the spectral index.")
  private String reference;
  @Parameter(description = "Label of the link to the source.")
  private String sourceName;
  @Parameter(description = "URL of the link to the source.")
  private String sourceUrl;

  @Parameter(description = "The valid expression to be used for the spectral index.")
  private String validExpression;
  @Parameter(description = "An ESRI shapefile, providing the considered geographical region(s) given as polygons.")
  private Path shapefile;
  @Parameter(converter = JtsGeometryConverter.class,
      description = "The considered geographical region as a geometry in well-known text format (WKT).")
  private Geometry wktRegion;


  /**
   * Standard constructor for serialization.
   */
  @SuppressWarnings("unused")
  public CustomSpex() {
  }

  /**
   * Constructor for custom SPEX.
   *
   * @param name The short name of the spectral index.
   */
  public CustomSpex(String name) {
    this.name = name;
  }


  public CustomSpex(AbstractSpex spex) {
    name = spex.getName();
    domain = spex.getDomain();
    formula = spex.getFormula();
    description = spex.getDescription();
    reference = spex.getReference();
    sourceName = spex.getSourceName();
    sourceUrl = spex.getSourceUrl();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Domain getDomain() {
    return domain;
  }

  @Override
  public String getFormula() {
    return formula;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getDeprecation() {
    return deprecation;
  }

  @Override
  public String getReference() {
    return reference;
  }

  @Override
  public String getSourceName() {
    return sourceName;
  }

  @Override
  public String getSourceUrl() {
    return sourceUrl;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setDomain(Domain domain) {
    this.domain = domain;
  }

  @Override
  public void setFormula(String formula) {
    this.formula = formula;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public void setDeprecation(String deprecation) {
    this.deprecation = deprecation;
  }

  @Override
  public void setReference(String reference) {
    this.reference = reference;
  }

  @Override
  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  @Override
  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  public String getValidExpression() {
    return validExpression;
  }

  public void setValidExpression(String validExpression) {
    this.validExpression = validExpression;
  }

  public Geometry getWktRegion() {
    return wktRegion;
  }

  public void setWktRegion(Geometry wktRegion) {
    this.wktRegion = wktRegion;
  }

  public Path getShapefile() {
    return shapefile;
  }

  public void setShapefile(Path shapefile) {
    this.shapefile = shapefile;
  }

}
