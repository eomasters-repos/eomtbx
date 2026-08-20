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

/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.eomasters.eomtbx.io.aster;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.List;
import javax.media.jai.PlanarImage;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.image.ImageManager;
import org.esa.snap.core.image.ResolutionLevel;
import org.esa.snap.core.image.SingleBandedOpImage;
import org.esa.snap.dataio.netcdf.util.DimKey;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Variable;

public class AsterImage extends SingleBandedOpImage {

  private final Variable variable;
  private final Object readLock;
  private final int xIndex;
  private final int yIndex;
  private final int startIndexToCopy;


  /**
   * Constructs a new {@code RenderedImage} from the given variable for the RasterDataNode.
   *
   * @param rdn      the raster data node
   * @param variable the netcdf variable
   * @param tileSize the tileSizeUsed.
   */
  public AsterImage(RasterDataNode rdn, Variable variable, Dimension tileSize) {
    super(ImageManager.getDataBufferType(rdn.getDataType()), rdn.getRasterWidth(), rdn.getRasterHeight(), tileSize, null, ResolutionLevel.MAXRES);
    this.variable = variable;
    this.readLock = variable.getNetcdfFile();
    DimensionIndices indices = computeDefaultDimensionIndices(variable);
    this.xIndex = indices.getIndexX();
    this.yIndex = indices.getIndexY();
    this.startIndexToCopy = indices.getStartIndexToCopy();
  }


  @Override
  protected void computeRect(PlanarImage[] sourceImages, WritableRaster tile, Rectangle destRect) {
    Rectangle sourceRect;
    if (getLevel() != 0) {
      sourceRect = getSourceRect(destRect);
    } else {
      sourceRect = destRect;
    }
    final int rank = variable.getRank();
    final int[] origin = new int[rank];
    final int[] shape = new int[rank];
    final int[] stride = new int[rank];
    for (int i = 0; i < rank; i++) {
      shape[i] = 1;
      origin[i] = 0;
      stride[i] = 1;
    }
    shape[yIndex] = sourceRect.height;
    shape[xIndex] = sourceRect.width;

    int[] imageOrigin = new int[0];
    System.arraycopy(imageOrigin, 0, origin, startIndexToCopy, imageOrigin.length);

    origin[yIndex] = sourceRect.y;
    if (origin[yIndex] < 0) {
      shape[yIndex] += origin[yIndex];
      origin[yIndex] = 0;
    }
    origin[xIndex] = sourceRect.x;

    double scale = getScale();
    stride[yIndex] = (int) scale;
    stride[xIndex] = (int) scale;

    Array array;
    synchronized (readLock) {
      try {
        final Section section = new Section(origin, shape, stride);
        array = variable.read(section);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      } catch (InvalidRangeException e) {
        throw new IllegalArgumentException(e);
      }
    }
    if (xIndex < yIndex) {
      array = array.transpose(xIndex, yIndex);
    }
    Object data;
    if (xIndex < yIndex) {
      data = array.copyTo1DJavaArray();
    } else {
      data = array.getStorage();
    }

    tile.setDataElements(destRect.x, destRect.y,
        destRect.width, destRect.height,
        data);
  }

  private Rectangle getSourceRect(Rectangle rect) {
    int sourceX = getSourceX(rect.x);
    int sourceY = getSourceY(rect.y);
    int sourceWidth = getSourceWidth(rect.width);
    int sourceHeight = getSourceHeight(rect.height);
    return new Rectangle(sourceX, sourceY, sourceWidth, sourceHeight);
  }

  private static DimensionIndices computeDefaultDimensionIndices(Variable variable) {
    List<ucar.nc2.Dimension> variableDimensions = variable.getDimensions();
    DimKey rasterDim = new DimKey(variableDimensions.toArray(new ucar.nc2.Dimension[0]));
    int xDimensionIndex = rasterDim.findXDimensionIndex();
    int yDimensionIndex = rasterDim.findYDimensionIndex();
    int startIndexOfBandVariables = DimKey.findStartIndexOfBandVariables(variableDimensions);
    return new DimensionIndices(xDimensionIndex, yDimensionIndex, startIndexOfBandVariables);
  }

  protected static class DimensionIndices {

    private final int xIndex;
    private final int yIndex;
    private final int startIndexToCopy;

    public DimensionIndices(int xDimensionIndex, int yDimensionIndex, int startIndexOfBandVariables) {
      this.xIndex = xDimensionIndex;
      this.yIndex = yDimensionIndex;
      this.startIndexToCopy = startIndexOfBandVariables;
    }

    protected int getIndexX() {
      return xIndex;
    }

    protected int getIndexY() {
      return yIndex;
    }

    protected int getStartIndexToCopy() {
      return startIndexToCopy;
    }
  }
}
