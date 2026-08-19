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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eomasters.eomtbx.spex.AbstractSpex;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.dataop.barithm.BandArithmetic;
import org.esa.snap.core.dataop.barithm.RasterDataSymbol;
import org.esa.snap.core.dataop.barithm.SingleFlagSymbol;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.jexp.Namespace;
import org.esa.snap.core.jexp.ParseException;
import org.esa.snap.core.jexp.Parser;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.jexp.impl.ParserImpl;
import org.esa.snap.core.util.StringUtils;

public class BandMathsExpressionFactory {

  private BandMathsExpressionFactory() {
  }

  public static String expandFormula(AbstractSpex spectralIndex, Product product) {
    return expandFormula(spectralIndex.getFormula(), expandSymbols(spectralIndex, product));
  }

  public static boolean areCompatible(AbstractSpex index, Product product) {
    try {
      String expression = expandFormula(index, product);
      return isExpressionCompatible(expression, product);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Tests if the given band arithmetic expression can be computed using this product. That referenced raster are of
   * equal size is explicitly NOT tested. The SpeX operator resamples the input.
   */
  private static boolean isExpressionCompatible(final String expression, Product product) {
    final Namespace namespace = BandArithmetic.createDefaultNamespace(new Product[]{product}, 0);
    Parser parser = new ParserImpl(namespace, false);

    final Term term;
    try {
      term = parser.parse(expression);
    } catch (ParseException e) {
      return false;
    }
    // expression was empty
    if (term == null) {
      return false;
    }

    final RasterDataSymbol[] termSymbols = BandArithmetic.getRefRasterDataSymbols(term);
    for (final RasterDataSymbol termSymbol : termSymbols) {
      final RasterDataNode refRaster = termSymbol.getRaster();
      if (termSymbol instanceof SingleFlagSymbol) {
        final String[] flagNames = ((Band) refRaster).getFlagCoding().getFlagNames();
        final String symbolName = termSymbol.getName();
        final String flagName = symbolName.substring(symbolName.indexOf('.') + 1);
        if (!StringUtils.containsIgnoreCase(flagNames, flagName)) {
          return false;
        }
      }
    }
    return true;
  }

  private static String expandFormula(String formula, Map<String, String> symbolMap) {
    String expression = formula;
    for (Entry<String, String> entry : symbolMap.entrySet()) {
      String key = entry.getKey().replace("(", "\\(");
      key = key.replace(")", "\\)");
      expression = expression.replaceAll(key, entry.getValue());
    }
    return expression;
  }

  private static Map<String, String> expandSymbols(AbstractSpex index, Product product) {
    HashMap<String, String> symbolMap = new HashMap<>();

    String formula = index.getFormula();
    for (Constant constant : Constant.getConstantsUsedInFormula(formula)) {
      symbolMap.put(constant.name(), String.valueOf(constant.value));
    }

    for (Function func : Function.getFunctionsUsedInFormula(formula)) {
      symbolMap.put(func.getFunctionDefinition(), func.bnd(product));
    }

    symbolMap.putAll(getSymbol2BandMap(index, product));
    return symbolMap;
  }

  private static Map<String, String> getSymbol2BandMap(AbstractSpex spectralIndex, Product source) {
    String formula = spectralIndex.getFormula();
    List<SpexBand> bandSymbols = SpexBand.getBandsUsedInFormula(formula);
    Map<String, String> symbol2BandMap = new HashMap<>();
    for (SpexBand spexBand : bandSymbols) {
      try {
        String bandName = findMatchingBand(spectralIndex, spexBand, List.of(source.getBands()));
        symbol2BandMap.put(spexBand.name(), bandName);
      } catch (IllegalArgumentException e) {
        throw new OperatorException(
            java.lang.String.format("Band '%s' used in spectral index '%s' is an unknown symbol.", spexBand,
                spectralIndex.getName()));
      }
    }

    return symbol2BandMap;
  }

  private static String findMatchingBand(AbstractSpex spectralIndex, SpexBand spexBand, List<Band> bandList) {
    String bandName;
    if (spexBand.getMinWavelength() == 0) {
      bandName = getBandNameByCommonName(spexBand, bandList);
      if (bandName == null) {
        throw new OperatorException(
            "Missing band " + spexBand.getCommonName() + " for index '" + spectralIndex.getName() + "'.");
      }
    } else {
      bandName = findWvlBand(spexBand.getMinWavelength(), spexBand.getMaxWavelength(), bandList);
      if (bandName == null) {
        throw new OperatorException(java.lang.String.format(
            "Missing bands between %dnm and %dnm for symbol '%s' for spectral index '" + spectralIndex.getName()
                + "'.", spexBand.getMinWavelength(), spexBand.getMaxWavelength(), spexBand.name()));
      }
    }
    return bandName;
  }

  private static String getBandNameByCommonName(SpexBand bandVar, List<Band> bandList) {
    // if no wvl is specified we use the common name
    if (bandList.stream().parallel().anyMatch(band -> band.getName().equals(bandVar.getCommonName()))) {
      return bandVar.getCommonName();
    } else {
      return null;
    }
  }

  static String findWvlBand(double minWavelength, double maxWavelength, List<Band> bandList) {
    ArrayList<Band> possibleBands = new ArrayList<>(bandList);
    for (Band band : bandList) {
      double bandWavelength = band.getSpectralWavelength();
      if (!(bandWavelength >= minWavelength) || !(bandWavelength <= maxWavelength)) {
        possibleBands.remove(band);
      }
    }

    if (possibleBands.isEmpty()) {
      return null;
    } else if (possibleBands.size() == 1) {
      return possibleBands.get(0).getName();
    } else {
      return findClosest(possibleBands, (minWavelength + maxWavelength) / 2);
    }

  }

  private static String findClosest(ArrayList<Band> bandList, double centralWavelength) {
    double minDistance = Double.MAX_VALUE;
    String closestBand = null;
    for (Band band : bandList) {
      double bandWavelength = band.getSpectralWavelength();
      double distance = Math.abs(bandWavelength - centralWavelength);
      if (distance < minDistance) {
        minDistance = distance;
        closestBand = band.getName();
      } else {
        break;
      }
    }
    return closestBand;
  }

}
