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

package org.eomasters.eomtbx.cmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.logging.Logger;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.graph.Graph;
import org.esa.snap.core.gpf.graph.GraphProcessingObserver;
import org.esa.snap.core.gpf.main.CommandLineContext;
import org.esa.snap.core.gpf.main.CommandLineTool;
import org.junit.jupiter.api.Test;

class CoastalMapOperatorTest {

  @Test
  void testGpfRegistration() {
    GPF defaultInstance = GPF.getDefaultInstance();
    defaultInstance.getOperatorSpiRegistry().loadOperatorSpis();
    assertNotNull(defaultInstance.getOperatorSpiRegistry().getOperatorSpi("EOM_CoastalMap"));
    assertNotNull(
        defaultInstance.getOperatorSpiRegistry().getOperatorSpi("org.eomasters.eomtbx.cmap.CoastalMapOp$Spi"));
  }


  @Test
  void testOperatorHelp() throws Exception {
    TestCommandLineContext context = new TestCommandLineContext();
    CommandLineTool clTool = new CommandLineTool(context);
    clTool.run("EOM_CoastalMap", "-h");

    String expected = "Usage:\n"
        + "  gpt EOM_CoastalMap [options] \n"
        + "\n"
        + "Description:\n"
        + "  Generates a map which provides indicators for coastal areas.\n"
        + "\n"
        + "\n"
        + "Source Options:\n"
        + "  -SsourceProduct=<file>    A geo-coded source product.\n"
        + "                            This is a mandatory source.\n"
        + "\n"
        + "Parameter Options:\n"
        + "  -PaddMasks=<boolean>         Beside the flag data, for easier usage, masks are added to the product too.\n"
        + "                               Default value is 'true'.\n"
        + "  -Paggregate=<boolean>        If true, the operator will use aggregation to compute the coastal map, instead of nearest neighbour interpolation. This is useful if the resolution of the source product is significantly lower then the resolution of the coastal map.\n"
        + "                               Default value is 'false'.\n"
        + "  -PincludeSource=<boolean>    If true, the operator will include the source data in the target product.\n"
        + "                               Default value is 'false'.\n"
        + "\n"
        + "Graph XML Format:\n"
        + "  <graph id=\"someGraphId\">\n"
        + "    <version>1.0</version>\n"
        + "    <node id=\"someNodeId\">\n"
        + "      <operator>EOM_CoastalMap</operator>\n"
        + "      <sources>\n"
        + "        <sourceProduct>${sourceProduct}</sourceProduct>\n"
        + "      </sources>\n"
        + "      <parameters>\n"
        + "        <includeSource>boolean</includeSource>\n"
        + "        <aggregate>boolean</aggregate>\n"
        + "        <addMasks>boolean</addMasks>\n"
        + "      </parameters>\n"
        + "    </node>\n"
        + "  </graph>\n";
    String actualText = context.getText();
    assertEquals(expected, actualText);
  }

  private static class TestCommandLineContext implements CommandLineContext {

    private final StringBuilder stringBuilder;

    public TestCommandLineContext() {
      this.stringBuilder = new StringBuilder();
    }

    public String getText() {
      return stringBuilder.toString();
    }

    @Override
    public void print(String s) {
      stringBuilder.append(s);
    }

    @Override
    public Product readProduct(String s) {
      return null;
    }

    @Override
    public void writeProduct(Product product, String s, String s1, boolean b) {

    }

    @Override
    public Graph readGraph(String s, Map<String, String> map) {
      return null;
    }

    @Override
    public void executeGraph(Graph graph, GraphProcessingObserver graphProcessingObserver) {

    }

    @Override
    public Logger getLogger() {
      return null;
    }

    @Override
    public boolean fileExists(String s) {
      return false;
    }

    @Override
    public Reader createReader(String s) {
      return null;
    }

    @Override
    public Writer createWriter(String s) {
      return null;
    }

    @Override
    public String[] list(String s) {
      return new String[0];
    }

    @Override
    public boolean isFile(String s) {
      return false;
    }
  }
}
