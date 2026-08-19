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

package org.eomasters.eomtbx.assets.gui;

import java.awt.EventQueue;
import javax.swing.JFrame;
import org.eomasters.eomtbx.assets.TestLibrary;
import org.eomasters.eomtbx.converter.ConverterRegistrar;
import org.eomasters.eomtbx.assets.AssetLibrary;

public class AssetLibraryFormMain {

  @SuppressWarnings("CallToPrintStackTrace")
  public static void main(String[] args) {
    // UIManager.getLookAndFeelDefaults()
    //          .keySet()
    //          .stream()
    //          .map(String::valueOf)
    //          .sorted()
    //          .forEach(System.out::println);

    ConverterRegistrar.registerConverter();
    EventQueue.invokeLater(() -> {
      try {
        final JFrame frame = new JFrame("Asset Library GUI Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        AssetLibrary library = TestLibrary.createTestLibrary();
        frame.setContentPane(new AssetLibraryForm(library));
        frame.setLocation(100, 650);
        frame.setVisible(true);
        frame.pack();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

}
