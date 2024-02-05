/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 - 2024 Marco Peters
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

package org.eomasters.eomtbx.bandmathsext;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.lang.ref.WeakReference;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.dataop.barithm.RasterDataEvalEnv;
import org.esa.snap.core.jexp.EvalEnv;
import org.esa.snap.core.jexp.EvalException;
import org.esa.snap.core.jexp.impl.AbstractSymbol;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;


abstract class MapPosSymbol extends AbstractSymbol.D {

  public static final Point2D.Double INVALID_MAP_POS = new Point2D.Double(Double.NaN, Double.NaN);

  private final WeakReference<GeoCoding> geocodingRef;
  private final Dimension size;

  protected MapPosSymbol(String name, GeoCoding geocoding, Dimension size) {
    super(name);
    this.geocodingRef = new WeakReference<>(geocoding);
    this.size = size;
  }

  @Override
  public double evalD(EvalEnv env) throws EvalException {
    GeoCoding geocoding = geocodingRef.get();
    if (geocoding != null) {
      if (geocoding.canGetGeoPos()) {
        Point2D.Double mapCoord = getMapPos(geocoding, env, size.width, size.height);
        return getMapCoord(mapCoord);
      }
    } else {
      throw new EvalException("GeoCoding is not available");
    }
    return Double.NaN;
  }

  private static Point2D.Double getMapPos(final GeoCoding geoCoding, EvalEnv env, int width, int height) {
    RasterDataEvalEnv rasterEnv = (RasterDataEvalEnv) env;
    int pixelX = rasterEnv.getPixelX();
    int pixelY = rasterEnv.getPixelY();
    if (pixelX >= 0 && pixelX < width && pixelY >= 0 && pixelY < height) {
      MathTransform transform = geoCoding.getImageToMapTransform();
      try {
        DirectPosition position = transform.transform(new DirectPosition2D(pixelX + 0.5, pixelY + 0.5), null);
        double[] coordinate = position.getCoordinate();
        return new Point2D.Double(coordinate[0], coordinate[1]);
      } catch (TransformException e) {
        throw new EvalException(String.format("Error while retrieving Map position at [%d,%d]", pixelX, pixelY), e);
      }
    }
    return INVALID_MAP_POS;
  }

  protected abstract double getMapCoord(Point2D.Double mapCoord);

  static final class MapXSymbol extends MapPosSymbol {

    MapXSymbol(GeoCoding geocoding, Dimension size) {
      super("MAPX", geocoding, size);
    }

    @Override
    protected double getMapCoord(Point2D.Double mapCoord) {
      return mapCoord.getX();
    }
  }

  static final class MapYSymbol extends MapPosSymbol {

    MapYSymbol(GeoCoding geocoding, Dimension size) {
      super("MAPY", geocoding, size);
    }

    @Override
    protected double getMapCoord(Point2D.Double mapCoord) {
      return mapCoord.getY();
    }
  }
}



