/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * -> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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

package org.eomasters.eomtbx.icons;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.swing.ImageIcon;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;

class SvgIcon extends Icon {

  public SvgIcon(String name) {
    super(name);
  }

  @Override
  protected ImageIcon createIcon(SIZE size) {
    SvgTranscoder transcoder = new SvgTranscoder();
    TranscodingHints hints = new TranscodingHints();
    hints.put(ImageTranscoder.KEY_WIDTH, (float) size.getSize());
    hints.put(ImageTranscoder.KEY_HEIGHT, (float) size.getSize());
    hints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION, SVGDOMImplementation.getDOMImplementation());
    hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVGConstants.SVG_NAMESPACE_URI);
    hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, SVGConstants.SVG_SVG_TAG);
    hints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, false);
    transcoder.setTranscodingHints(hints);
    InputStream resource = Icons.class.getResourceAsStream("/org/eomasters/eomtbx/icons/" + getName() + ".svg");
    try {
      transcoder.transcode(new TranscoderInput(resource), null);
    } catch (TranscoderException e) {
      return new ImageIcon(new BufferedImage(size.getSize(),size.getSize(), BufferedImage.TYPE_INT_ARGB));
    }

    return new ImageIcon(transcoder.getImage());
  }

  private static class SvgTranscoder extends ImageTranscoder {

    private BufferedImage image = null;

    public BufferedImage createImage(int w, int h) {
      image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      return image;
    }

    @Override
    protected ImageRenderer createRenderer() {
      ImageRenderer renderer = super.createRenderer();
      RenderingHints hints = renderer.getRenderingHints();
      hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY); // Not a big difference
      hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      renderer.setRenderingHints(hints);
      return renderer;
    }

    public void writeImage(BufferedImage img, TranscoderOutput out) {
      // empty
    }

    public BufferedImage getImage() {
      return image;
    }
  }
}
