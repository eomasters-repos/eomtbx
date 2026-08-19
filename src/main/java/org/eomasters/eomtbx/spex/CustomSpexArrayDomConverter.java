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

import com.bc.ceres.binding.ConversionException;
import com.bc.ceres.binding.Converter;
import com.bc.ceres.binding.ConverterRegistry;
import com.bc.ceres.binding.dom.DefaultDomElement;
import com.bc.ceres.binding.dom.DomConverter;
import com.bc.ceres.binding.dom.DomElement;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.esa.snap.core.util.converters.JtsGeometryConverter;
import org.locationtech.jts.geom.Geometry;

public class CustomSpexArrayDomConverter implements DomConverter {

  @Override
  public Class<?> getValueType() {
    return CustomSpex[].class;
  }

  @Override
  public Object convertDomToValue(DomElement parentElement, Object value)
      throws ConversionException {
    final List<CustomSpex> spexList = new ArrayList<>();

    final DomElement[] spexElements = parentElement.getChildren();
    for (DomElement spexElem : spexElements) {
      final CustomSpex spex;

      DomElement shortNameElem = spexElem.getChild("name");
      if (shortNameElem == null) {
        throw new ConversionException(
            String.format("name is missing for spex element in [%s]", parentElement.getName()));
      }
      spex = new CustomSpex(shortNameElem.getValue());
      spexList.add(spex);

      DomElement formulaElem = spexElem.getChild("formula");
      if (formulaElem != null) {
        spex.setFormula(formulaElem.getValue());
      }
      DomElement descriptionElem = spexElem.getChild("description");
      if (descriptionElem != null) {
        spex.setDescription(descriptionElem.getValue());
      }
      DomElement deprecationElem = spexElem.getChild("deprecation");
      if (deprecationElem != null) {
        spex.setDeprecation(deprecationElem.getValue());
      }
      DomElement validExpressionElem = spexElem.getChild("validExpression");
      if (validExpressionElem != null) {
        spex.setValidExpression(validExpressionElem.getValue());
      }
      DomElement wktRegionElem = spexElem.getChild("wktRegion");
      if (wktRegionElem != null) {
        Converter<Geometry> converter = new JtsGeometryConverter();
        spex.setWktRegion(converter.parse(wktRegionElem.getValue()));
      }
      DomElement shapefileElem = spexElem.getChild("shapefile");
      if (shapefileElem != null) {
        Converter<Path> converter = ConverterRegistry.getInstance().getConverter(Path.class);
        if (converter == null) {
          throw new ConversionException("No converter for type Path found. Needed for shapefile.");
        }
        spex.setShapefile(converter.parse(shapefileElem.getValue()));
      }
      DomElement referenceElem = spexElem.getChild("reference");
      if (referenceElem != null) {
        spex.setReference(referenceElem.getValue());
      }

    }
    return spexList.toArray(new CustomSpex[0]);
  }

  @Override
  public void convertValueToDom(Object value, DomElement parentElement) throws ConversionException {
    CustomSpex[] spexes = (CustomSpex[]) value;
    for (CustomSpex spex : spexes) {
      DomElement spexElem = new DefaultDomElement("spex");
      parentElement.addChild(spexElem);
      spexElem.addChild(new DefaultDomElement("name", spex.getName()));

      String expression = spex.getFormula();
      if (expression != null) {
        spexElem.addChild(new DefaultDomElement("formula", expression));
      }
      String description = spex.getDescription();
      if (description != null) {
        spexElem.addChild(new DefaultDomElement("description", description));
      }
      String deprecation = spex.getDeprecation();
      if (deprecation != null) {
        spexElem.addChild(new DefaultDomElement("deprecation", deprecation));
      }
      String validExpression = spex.getValidExpression();
      if (validExpression != null) {
        spexElem.addChild(new DefaultDomElement("validExpression", validExpression));
      }
      Geometry wktRegion = spex.getWktRegion();
      if (wktRegion != null) {
        Converter<Geometry> converter = new JtsGeometryConverter();
        spexElem.addChild(new DefaultDomElement("wktRegion", converter.format(wktRegion)));
      }
      Path shapefile = spex.getShapefile();
      if (shapefile != null) {
        Converter<Path> converter = ConverterRegistry.getInstance().getConverter(shapefile.getClass());
        if (converter == null) {
          throw new ConversionException("No converter for type Path found. Needed for shapefile.");
        }
        spexElem.addChild(new DefaultDomElement("shapefile", converter.format(shapefile)));
      }
      String reference = spex.getReference();
      if (reference != null) {
        spexElem.addChild(new DefaultDomElement("reference", reference));
      }

    }

  }
}
