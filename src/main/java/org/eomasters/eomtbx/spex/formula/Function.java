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

import static org.eomasters.utils.Exceptions.throwIf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eomasters.eomtbx.utils.BandUtils;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;

public class Function {


  private final String funcDefinition;

  public Function(String functionDefinition) {
    this.funcDefinition = functionDefinition;
  }

  public String getFunctionDefinition() {
    return funcDefinition;
  }

  public static List<Function> getFunctionsUsedInFormula(String formula) {
    // extract all occurrences of the following 'bnd(integer:integer)' pattern from the formula in a list of strings
    String regex = "bnd\\(\\d+:\\d+\\)";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(formula);
    List<Function> functions = new ArrayList<>();
    while (matcher.find()) {
      functions.add(new Function(matcher.group()));
    }
    return functions;
  }

  public String bnd(Product source) {
    // extract the range within the parentheses of a string like 'bnd(range)'
    String wvlRange = funcDefinition.substring(funcDefinition.indexOf("(") + 1, funcDefinition.indexOf(")"));

    String[] splits = wvlRange.split(":");
    throwIf(splits.length != 2, new IllegalStateException("Wavelength range must be specified as 'min:max'."));

    double min = Double.parseDouble(splits[0]);
    double max = Double.parseDouble(splits[1]);
    Band[] bands = source.getBands();
    Band closestBand = BandUtils.findClosestToCenterBand(bands, min, max);
    if (closestBand != null) {
      return closestBand.getName();
    }
    return null;
  }

}
