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

import java.util.List;
import org.esa.snap.core.util.StringUtils;

public abstract class AbstractSpex {

  private transient List<Platform> platforms;

  public abstract String getName();

  public abstract Domain getDomain();

  public abstract String getFormula();

  public abstract String getDescription();

  public abstract String getDeprecation();

  public abstract String getReference();

  public abstract String getSourceName();

  public abstract String getSourceUrl();

  public abstract void setName(String name);

  public abstract void setDomain(Domain domain);

  public abstract void setFormula(String formula);

  public abstract void setDescription(String description);

  public abstract void setDeprecation(String deprecation);

  public abstract void setReference(String reference);

  public abstract void setSourceName(String sourceName);

  public abstract void setSourceUrl(String sourceUrl);

  @Override
  public String toString() {
    return getName() + " (" + getDomain() + ")";
  }

  /**
   * Check if the spectral index fulfills the minimal requirements, which are name and formula not null or empty, and
   * the domain is set.
   *
   * @return true if the spectral index is valid, false otherwise.
   */
  public boolean isValid() {
    return StringUtils.isNotNullAndNotEmpty(getName())
        && getDomain() != null
        && StringUtils.isNotNullAndNotEmpty(getFormula());
  }

  public final List<Platform> getSupportedPlatforms() {
    if (platforms == null) {
      platforms = Platform.getSupportedPlatforms(this);
    }
    return platforms;
  }

}
