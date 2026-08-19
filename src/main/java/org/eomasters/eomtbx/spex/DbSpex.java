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

public class DbSpex extends AbstractSpex {

  private String name;
  private Domain domain;
  private String formula;
  private String description;
  private String deprecation;
  private String reference;
  private String sourceName;
  private String sourceUrl;


  /**
   * Standard constructor for serialization.
   */
  @SuppressWarnings("unused")
  public DbSpex() {
  }

  /**
   * Constructor for spex.
   *
   * @param name The short name of the spectral index.
   */
  public DbSpex(String name) {
    this.name = name;
  }

  public DbSpex(AbstractSpex spex) {
    name = spex.getName();
    domain = spex.getDomain();
    formula = spex.getFormula();
    description = spex.getDescription();
    deprecation = spex.getDeprecation();
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

}
