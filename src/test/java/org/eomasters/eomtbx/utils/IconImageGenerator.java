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

package org.eomasters.eomtbx.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignF;
import org.kordamp.ikonli.materialdesign2.MaterialDesignH;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import org.kordamp.ikonli.materialdesign2.MaterialDesignW;
import org.kordamp.ikonli.swing.FontIcon;

public class IconImageGenerator {

  private static final int ICON_SIZE = 24;

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage: IconImageGenerator <output directory>");
      System.exit(1);
    }
    var outputDir = Path.of(args[0]);
    Files.createDirectories(outputDir);
    writeIcons(outputDir,
               FontIcon.of(MaterialDesignF.FILE_CABINET, ICON_SIZE),
               FontIcon.of(MaterialDesignW.WRENCH_OUTLINE, ICON_SIZE),
               FontIcon.of(MaterialDesignC.CLOSE_BOX_OUTLINE, ICON_SIZE),
               FontIcon.of(MaterialDesignF.FOLDER_PLUS, ICON_SIZE),
               FontIcon.of(MaterialDesignF.FILE_PLUS, ICON_SIZE),
               FontIcon.of(MaterialDesignD.DELETE_CIRCLE, ICON_SIZE),
               FontIcon.of(MaterialDesignC.CONTENT_SAVE, ICON_SIZE),
               FontIcon.of(MaterialDesignC.CONTENT_SAVE_ALL, ICON_SIZE),
               FontIcon.of(MaterialDesignF.FORM_TEXTBOX, ICON_SIZE),
               FontIcon.of(MaterialDesignP.PLAY, ICON_SIZE, Color.GREEN.darker()),
               FontIcon.of(MaterialDesignS.STOP, ICON_SIZE, Color.RED.darker()),
               FontIcon.of(MaterialDesignC.CONSOLE, ICON_SIZE),
               FontIcon.of(MaterialDesignD.DELETE_SWEEP_OUTLINE, ICON_SIZE),
               FontIcon.of(MaterialDesignH.HELP, ICON_SIZE));

  }

  private static void writeIcons(Path outputDir, FontIcon... ikons) throws IOException {
    System.out.printf("Writing icons to %s%n", outputDir);
    for (FontIcon icon : ikons) {
      writeIcon(outputDir, icon);
    }
  }

  private static void writeIcon(Path outputDir, FontIcon fontIcon) throws IOException {
    var imageIcon = fontIcon.toImageIcon();
    String ikonName;
    if (fontIcon.getIkon().getClass().isEnum()) {
      var anEnum = (Enum) fontIcon.getIkon();
      ikonName = anEnum.name();
    } else {
      throw new IllegalArgumentException("Icon must be an enum");
    }
    BufferedImage bufferedImage = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
    imageIcon.paintIcon(null, bufferedImage.createGraphics(), 0, 0);
    var fileName = ikonName + "-icon.png";
    System.out.printf("  %s%n", fileName);
    var file = outputDir.resolve(fileName).toFile();
    ImageIO.write(bufferedImage, "png", file);
  }

}
