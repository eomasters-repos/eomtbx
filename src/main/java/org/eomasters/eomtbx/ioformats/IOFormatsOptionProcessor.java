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

package org.eomasters.eomtbx.ioformats;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.esa.snap.core.dataio.ProductIOPlugIn;
import org.esa.snap.core.dataio.ProductIOPlugInManager;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.dataio.ProductWriterPlugIn;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionGroups;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.NbBundle;

/**
 * Option processor for the --formats option.
 * <p>With this option, a list of available readable and writable data formats will be printed.
 * <p>Example: <code>snap --formats</p>
 * <p>To prevent the GUI and splash screen from showing, add also {@code --nogui} {@code --nosplash} <br>
 */
@org.openide.util.lookup.ServiceProvider(service = OptionProcessor.class)
@NbBundle.Messages({
    "DSC_Formats=Print list of available data formats: snap --formats; "
        + "Add also --nogui --nosplash to prevent GUI and splash screen from showing."
})
public class IOFormatsOptionProcessor extends OptionProcessor {

  private static final Option formatsOpt;
  private static final Set<Option> optionSet;

  static {
    String b = IOFormatsOptionProcessor.class.getPackageName() + ".Bundle";
    formatsOpt = Option.shortDescription(Option.withoutArgument(Option.NO_SHORT_NAME, "formats"), b, "DSC_Formats");
    optionSet = Collections.singleton(OptionGroups.allOf(formatsOpt));
  }

  private PrintStream out;


  @Override
  protected Set<Option> getOptions() {
    return optionSet;
  }

  @Override
  protected void process(Env env, Map<Option, String[]> optionValues) {

    if (optionValues.containsKey(formatsOpt)) {
      out = env.getOutputStream();
      ProductIOPlugInManager ioPlugInManager = ProductIOPlugInManager.getInstance();

      println();
      List<ProductReaderPlugIn> readerPlugins = toList(ioPlugInManager.getAllReaderPlugIns());
      readerPlugins.sort(Comparator.comparing(o -> o.getClass().getSimpleName()));
      println(0, "Readable Data Formats:");
      printPlugins(readerPlugins);

      println();
      println();

      List<ProductWriterPlugIn> writerPlugins = toList(ioPlugInManager.getAllWriterPlugIns());
      writerPlugins.sort(Comparator.comparing(o -> o.getClass().getSimpleName()));
      println(0, "Writable Data Formats:");
      printPlugins(writerPlugins);

      out.flush();
      System.exit(0);
    }
  }

  private <T> List<T> toList(Iterator<T> iterator) {
    List<T> list = new ArrayList<T>();

    while (iterator.hasNext()) {
      list.add(iterator.next());
    }

    return list;
  }

  private void printPlugins(List<? extends ProductIOPlugIn> readerPlugins) {
    if (readerPlugins.isEmpty()) {
      println(0, "No data formats found!");
      return;
    }

    for (ProductIOPlugIn readerPlugin : readerPlugins) {
      printPluginInfo(readerPlugin);
      println();
    }
  }

  private void printPluginInfo(ProductIOPlugIn plugin) {
    Class<? extends ProductIOPlugIn> pluginClass = plugin.getClass();
    println(0, String.format("%s (%s)", pluginClass.getSimpleName(), pluginClass.getPackageName()));
    println(1, String.format("Description: %s", plugin.getDescription(Locale.ENGLISH)));
    println(1, String.format("Format Names: %s", String.join(", ", plugin.getFormatNames())));
    println(1, String.format("File Extensions: %s", String.join(", ", plugin.getDefaultFileExtensions())));
  }

  private void println() {
    println(0, "");
  }
  private void println(int indent, String text) {
    for (int i = 0; i < indent; i++) {
      out.print("   ");
    }
    out.println(text);
  }


}
