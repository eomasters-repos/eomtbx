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

package org.eomasters.eomtbx.s2superres;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtException;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;
import org.eomasters.eomtbx.s2superres.onnx.Onnx;

public class S2SuperResolveTensorInput {
  private static final boolean PREFER_ARRAYS = true;
  private final Rectangle rect;
  private final long[] shape;

  public S2SuperResolveTensorInput(Rectangle rect, int numImages) {
    if (rect.height < 0 || rect.width < 0) {
      throw new IllegalStateException("Illegal rectangle dimension");
    }
    this.rect = rect;
    shape = new long[]{1, numImages, rect.height, rect.width};
  }

  public OnnxTensor createOnnxTensor(List<RenderedImage> sourceImageList)
      throws OrtException {
    if (PREFER_ARRAYS) {
      return OnnxTensor.createTensor(Onnx.ENVIRONMENT, getAsTensorArray(sourceImageList));

    } else {
      // causes crash in nvcuda64.dll
      FloatBuffer inputData = getAsTensorBuffer(sourceImageList);
      return OnnxTensor.createTensor(Onnx.ENVIRONMENT, inputData, getShape());
    }
  }

  float[][][][] getAsTensorArray(List<RenderedImage> srcImages) {
    int rectWidth = rect.width;
    int rectHeight = rect.height;
    float[][][][] tensorData = new float[1][srcImages.size()][rectHeight][rectWidth];
    float[][][] tensorImageData = tensorData[0];
    float[] fData = new float[rectWidth * rectHeight];
    for (int i = 0; i < srcImages.size(); i++) {
      RenderedImage srcImage = srcImages.get(i);
      Raster data = srcImage.getData(rect);
      Arrays.fill(fData, Float.NaN);
      data.getSamples(rect.x, rect.y, rectWidth, rectHeight, 0, fData);

      // convert fdata into two-dimensional float[][] array using rect.height and rect.width
      float[][] fData2d = new float[rectHeight][rectWidth];
      for (int j = 0; j < rectHeight; j++) {
        System.arraycopy(fData, j * rectWidth, fData2d[j], 0, rectWidth);
      }
      tensorImageData[i] = fData2d;
    }
    return tensorData;
  }

  FloatBuffer getAsTensorBuffer(List<RenderedImage> srcImages) {
    FloatBuffer tensorBuffer = FloatBuffer.allocate(srcImages.size() * rect.height * rect.width);
    int rectWidth = rect.width;
    int rectHeight = rect.height;
    tensorBuffer.clear();
    float[] fData = new float[rectWidth * rectHeight];
    for (RenderedImage srcImage : srcImages) {
      Raster data = srcImage.getData(rect);
      Arrays.fill(fData, Float.NaN);
      data.getSamples(rect.x, rect.y, rectWidth, rectHeight, 0, fData);
      tensorBuffer.put(fData);
    }
    tensorBuffer.flip();
    return tensorBuffer;
  }

  public long[] getShape() {
    return shape;
  }

}
