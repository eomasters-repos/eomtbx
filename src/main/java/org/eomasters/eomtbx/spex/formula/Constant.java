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

package org.eomasters.eomtbx.spex.formula;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Constant {
  C1(6.0, "Coefficient 1 for the aerosol resistance term"),
  C2(7.5, "Coefficient 2 for the aerosol resistance term"),
  L(1.0, "Canopy background adjustment"),
  // PAR(null, "Photosynthetically Active Radiation"),
  alpha(0.1, "Weighting coefficient used for WDRVI"),
  beta(0.05, "Calibration parameter used for NDSInw"),
  c(1.0, "Trade-off parameter in the polynomial kernel"),
  cexp(1.16, "Exponent used for OCVI"),
  epsilon(1.0, "Adjustment constant used for EBI"),
  fdelta(0.581, "Adjustment factor used for SEVI"),
  g(2.5, "Gain factor"),
  gamma(1.0, "Weighting coefficient used for ARVI"),
  k(0.0, "Slope parameter by soil used for NIRvH2"),
  // lambdaG(null, "Green wavelength (nm) used for NDGI"),
  // lambdaN(null, "NIR wavelength (nm) used for NIRvH2 and NDGI"),
  // lambdaR(null, "Red wavelength (nm) used for NIRvH2 and NDGI"),
  nexp(2.0, "Exponent used for GDVI"),
  omega(2.0, "Weighting coefficient used for MBWI"),
  p(2.0, "Kernel degree in the polynomial kernel"),
  sigma(0.5, "Length-scale parameter in the RBF kernel"),
  sla(1.0, "Soil line slope"),
  slb(0.0, "Soil line intercept");


  public final double value;
  public final String description;

  Constant(double value, String description) {
    this.value = value;
    this.description = description;
  }

  public double getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }

  public static boolean contains(String symbol) {
    return Arrays.stream(Constant.values()).anyMatch(value -> value.name().equals(symbol));
  }

  public static List<Constant> getConstantsUsedInFormula(String formula) {
    Stream<String> symbolNames = Stream.of(Constant.values()).map(Constant::name);
    // Find all Constant.name considering word boundaries in formula
    return symbolNames.filter(name -> formula.matches(".*\\b" + name + "\\b.*")).map(Constant::valueOf).collect(Collectors.toList());
  }

}
