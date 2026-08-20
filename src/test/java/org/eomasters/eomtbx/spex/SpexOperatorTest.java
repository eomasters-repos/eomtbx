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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bc.ceres.binding.dom.DomElement;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;
import java.util.logging.Logger;
import org.eomasters.eomtbx.ToolboxTestUtils;
import org.eomasters.eomtbx.converter.ConverterRegistrar;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.graph.Graph;
import org.esa.snap.core.gpf.graph.GraphException;
import org.esa.snap.core.gpf.graph.GraphIO;
import org.esa.snap.core.gpf.graph.GraphProcessingObserver;
import org.esa.snap.core.gpf.graph.Node;
import org.esa.snap.core.gpf.graph.NodeSource;
import org.esa.snap.core.gpf.main.CommandLineContext;
import org.esa.snap.core.gpf.main.CommandLineTool;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

class SpexOperatorTest {

  @Test
  void testGpfRegistration() {
    GPF defaultInstance = GPF.getDefaultInstance();
    defaultInstance.getOperatorSpiRegistry().loadOperatorSpis();
    assertNotNull(defaultInstance.getOperatorSpiRegistry().getOperatorSpi("Spex"));
    assertNotNull(defaultInstance.getOperatorSpiRegistry().getOperatorSpi("org.eomasters.eomtbx.spex.SpexOperator$Spi"));

    // ensuring that the converter are registered, especially the PathConverter
    // TODO - Can be removed with SNAP 11
    ConverterRegistrar.registerConverter();
  }

  // @Test
  // void testFullSize() throws IOException {
    // TODO - this is slow in tests, so we skip it - find out why
    // DummyProductBuilder builder = new DummyProductBuilder();
    // builder.size(Size.MEDIUM);
    // Product product = builder.create();
    // product.getBand("band_a").setSpectralWavelength(665); // R(ed)
    // product.getBand("band_b").setSpectralWavelength(833);  // N(IR)
    //
    // JAI.getDefaultInstance().getTileScheduler().setParallelism(Runtime.getRuntime().availableProcessors());
    // HashMap<String, Object> parameters = new HashMap<>();
    // parameters.put("spexList", new String[]{"BAI"});
    // Product spex = GPF.createProduct("Spex", parameters, product);
    // Band baiBand = spex.getBand("BAI");
    // MultiLevelImage sourceImage = baiBand.getSourceImage();
    // sourceImage.prefetchTiles(
    //     sourceImage.getTileIndices(new Rectangle(0, 0, sourceImage.getWidth(), sourceImage.getHeight())));
    // baiBand.loadRasterData();
  // }

  @Test
  void testPredefinedSpexComputation() throws IOException {
    Product product = ToolboxTestUtils.createProduct();
    product.getBand("B1").setSpectralWavelength(665); // R(ed)
    product.getBand("B2").setSpectralWavelength(833); // N(IR)

    SpexOperator spexOperator = new SpexOperator();
    spexOperator.setParameterDefaultValues();
    spexOperator.setSourceProduct(product);
    spexOperator.setSpexList(new String[]{"BAI"}); // 1.0 / (pow(0.1 - R, 2.0) + pow(0.06 - N, 2.0)
    Product targetProduct = spexOperator.getTargetProduct();

    assertEquals(1, targetProduct.getNumBands());
    Band baiBand = targetProduct.getBandAt(0);

    assertEquals("BAI", baiBand.getName());
    assertEquals("Burned Area Index", baiBand.getDescription());
    assertTrue(Double.isNaN(baiBand.getNoDataValue()));
    assertTrue(baiBand.isNoDataValueUsed());
    baiBand.loadRasterData();
    ProductData data = baiBand.getRasterData();

    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(0, 0))));
    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(1, 8))));
    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(5, 5))));

    assertEquals(0.21864614f, data.getElemFloatAt(ToolboxTestUtils.toElemIndex(1, 0)), 1e-6f);
    assertEquals(9.070843e-5f, data.getElemFloatAt(ToolboxTestUtils.toElemIndex(7, 4)), 1e-6f);
    assertEquals(3.757577e-5f, data.getElemFloatAt(ToolboxTestUtils.toElemIndex(3, 7)), 1e-6f);
  }

  @Test
  void testPredefinedSpexWithCustomFormula() throws IOException {
    Product product = ToolboxTestUtils.createProduct();
    product.getBand("B1").setSpectralWavelength(665); // R(ed)
    product.getBand("B2").setSpectralWavelength(833); // N(IR)

    SpexOperator spexOperator = new SpexOperator();
    spexOperator.setParameterDefaultValues();
    spexOperator.setSourceProduct(product);
    spexOperator.setValidExpression("Y < 8");

    CustomSpex spexBai = new CustomSpex("BAI");
    spexBai.setFormula("1.0 / (pow(0.1 - B2, 2.0) + pow(0.06 - B1, 2.0))");
    spexBai.setDescription("BAI description");
    spexOperator.setCustomSpex(new CustomSpex[]{spexBai});
    Product targetProduct = spexOperator.getTargetProduct();

    assertEquals(1, targetProduct.getNumBands());
    Band baiBand = targetProduct.getBandAt(0);
    assertEquals("BAI", baiBand.getName());
    assertEquals("BAI description", baiBand.getDescription());
    assertTrue(Double.isNaN(baiBand.getNoDataValue()));
    assertTrue(baiBand.isNoDataValueUsed());
    baiBand.loadRasterData();
    ProductData data = baiBand.getRasterData();

    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(0, 0))));
    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(1, 8))));
    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(5, 5))));
    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(7, 9))));

    assertEquals(0.2225387f, data.getElemFloatAt(ToolboxTestUtils.toElemIndex(1, 0)), 1e-6f);
    assertEquals(9.073938e-5f, data.getElemFloatAt(ToolboxTestUtils.toElemIndex(7, 4)), 1e-6f);
    assertEquals(3.7584017e-5, data.getElemFloatAt(ToolboxTestUtils.toElemIndex(3, 7)), 1e-6f);
  }

  @Test
  void testCustomSpexWithFormula() throws IOException {
    Product product = ToolboxTestUtils.createProduct();
    product.getBand("B1").setSpectralWavelength(665); // R(ed)
    product.getBand("B2").setSpectralWavelength(833); // N(IR)

    SpexOperator spexOperator = new SpexOperator();
    spexOperator.setParameterDefaultValues();
    spexOperator.setSourceProduct(product);
    CustomSpex mySpex = new CustomSpex("MIX");
    mySpex.setDescription("My Index");
    mySpex.setFormula("B2 / B1");
    spexOperator.setCustomSpex(new CustomSpex[]{mySpex});
    Product targetProduct = spexOperator.getTargetProduct();

    assertEquals(1, targetProduct.getNumBands());
    Band baiBand = targetProduct.getBandAt(0);
    assertEquals("MIX", baiBand.getName());
    assertEquals("My Index", baiBand.getDescription());
    assertTrue(Double.isNaN(baiBand.getNoDataValue()));
    assertTrue(baiBand.isNoDataValueUsed());
    baiBand.loadRasterData();
    ProductData data = baiBand.getRasterData();

    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(0, 0))));
    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(1, 8))));
    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(5, 5))));

    assertEquals(2.0f, data.getElemFloatAt(ToolboxTestUtils.toElemIndex(1, 0)), 1e-6f);
    assertEquals(2.0f, data.getElemFloatAt(ToolboxTestUtils.toElemIndex(7, 4)), 1e-6f);
    assertEquals(2.0f, data.getElemFloatAt(ToolboxTestUtils.toElemIndex(3, 7)), 1e-6f);
  }

  @Test()
  void testCustomSpexWithValidExpression() throws IOException {
    Product product = ToolboxTestUtils.createProduct();
    Band b1 = product.getBand("B1");
    b1.setSpectralWavelength(665); // R(ed)
    b1.setValidPixelExpression(null);
    Band b2 = product.getBand("B2");
    b2.setSpectralWavelength(833); // N(IR)
    b2.setValidPixelExpression(null);

    SpexOperator spexOperator = new SpexOperator();
    spexOperator.setParameterDefaultValues();
    spexOperator.setSourceProduct(product);
    CustomSpex mySpex = new CustomSpex("MIX");
    mySpex.setDescription("My Index");
    mySpex.setFormula("B2 / B1"); // bands swapped
    mySpex.setValidExpression("Y < 5.5 || Y > 7.5");
    spexOperator.setCustomSpex(new CustomSpex[]{mySpex});
    Product targetProduct = spexOperator.getTargetProduct();

    assertEquals(1, targetProduct.getNumBands());
    Band mixBand = targetProduct.getBandAt(0);
    assertEquals("MIX", mixBand.getName());
    assertEquals("My Index", mixBand.getDescription());
    assertTrue(Double.isNaN(mixBand.getNoDataValue()));
    assertTrue(mixBand.isNoDataValueUsed());
    assertNull(mixBand.getValidPixelExpression());
    mixBand.loadRasterData();
    ProductData data = mixBand.getRasterData();

    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(0, 0))));
    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(1, 8))));
    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(8, 9))));
    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(9, 9))));
    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(4, 6))));

    assertEquals(2.0f, data.getElemFloatAt(ToolboxTestUtils.toElemIndex(5, 0)), 1e-6f);
    assertEquals(2.0f, data.getElemFloatAt(ToolboxTestUtils.toElemIndex(5, 4)), 1e-6f);
    assertEquals(2.0f, data.getElemFloatAt(ToolboxTestUtils.toElemIndex(8, 4)), 1e-6f);
  }

  @Test()
  void testGeneralAndCustomMask() throws IOException, ParseException {
    Product product = ToolboxTestUtils.createProduct();
    Band b1 = product.getBand("B1");
    b1.setSpectralWavelength(665); // R(ed)
    b1.setValidPixelExpression(null);
    b1.setNoDataValueUsed(false);
    Band b2 = product.getBand("B2");
    b2.setSpectralWavelength(833); // N(IR)
    b2.setValidPixelExpression(null);
    b2.setNoDataValueUsed(false);

    SpexOperator spexOperator = new SpexOperator();
    spexOperator.setParameterDefaultValues();
    spexOperator.setSourceProduct(product);
    spexOperator.setWktRegion(new WKTReader().read("POLYGON ((2 -2, 8 -2, 8 -8, 2 -8, 2 -2))"));

    CustomSpex mySpex = new CustomSpex("MIX");
    mySpex.setDescription("My Index");
    mySpex.setFormula("B2 / B1"); // bands swapped
    mySpex.setValidExpression("Y < 5.5 || Y > 7.5");
    spexOperator.setCustomSpex(new CustomSpex[]{mySpex});
    Product targetProduct = spexOperator.getTargetProduct();

    assertEquals(1, targetProduct.getNumBands());
    Band mixBand = targetProduct.getBandAt(0);
    mixBand.loadRasterData();
    ProductData data = mixBand.getRasterData();

    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(0, 0))));
    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(6, 8))));
    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(1, 9))));
    assertTrue(Float.isNaN(data.getElemFloatAt(ToolboxTestUtils.toElemIndex(5, 6))));

    assertEquals(2.0f, data.getElemFloatAt(ToolboxTestUtils.toElemIndex(5, 3)), 1e-6f);
    assertEquals(2.0f, data.getElemFloatAt(ToolboxTestUtils.toElemIndex(6, 4)), 1e-6f);
  }


  @Test
  void testReadGraphXml() throws GraphException {
    String graphXml = "<graph id=\"someGraphId\">\n"
        + "  <version>1.0</version>\n"
        + "  <node id=\"spexNode\">\n"
        + "    <operator>Spex</operator>\n"
        + "    <sources>\n"
        + "      <sourceProduct>${sourceProduct}</sourceProduct>\n"
        + "    </sources>\n"
        + "    <parameters>\n"
        + "      <spexList>string,string,string,...</spexList>\n"
        + "      <validExpression>expression</validExpression>\n"
        + "      <wktRegion>geometry</wktRegion>\n"
        + "      <shapefile>file</shapefile>\n"
        + "      <customIndices>"
        + "        <index>\n"
        + "          <name>name1</name>\n"
        + "          <formula>formula1</formula>\n"
        + "          <validExpression>validExpression1</validExpression>\n"
        + "          <wktRegion>wktRegion1</wktRegion>\n"
        + "          <shapefile>file1</shapefile>\n"
        + "        </index>\n"
        + "        <index>\n"
        + "          <name>name2</name>\n"
        + "          <formula>formula2</formula>\n"
        + "          <description>description2</description>\n"
        + "          <validExpression>validExpression2</validExpression>\n"
        + "          <wktRegion>wktRegion2</wktRegion>\n"
        + "          <shapefile>shapefile2</shapefile>\n"
        + "        </index>\n"
        + "      </customIndices>"
        + "    </parameters>\n"
        + "  </node>\n"
        + "</graph>\n";
    Graph g = GraphIO.read(new StringReader(graphXml));
    Node spexNode = g.getNode("spexNode");
    assertEquals("Spex", spexNode.getOperatorName());
    NodeSource[] sources = spexNode.getSources();
    assertEquals(1, sources.length);
    DomElement configuration = spexNode.getConfiguration();
    assertEquals("string,string,string,...", configuration.getChild("spexList").getValue());
    assertEquals("expression", configuration.getChild("validExpression").getValue());
    assertEquals("geometry", configuration.getChild("wktRegion").getValue());
    assertEquals("file", configuration.getChild("shapefile").getValue());
    DomElement customIndices = configuration.getChild("customIndices");
    assertEquals(2, customIndices.getChildCount());
    DomElement child1 = customIndices.getChild(0);
    assertEquals("name1", child1.getChild("name").getValue());
    assertEquals("formula1", child1.getChild("formula").getValue());
    assertEquals("validExpression1", child1.getChild("validExpression").getValue());
    assertEquals("wktRegion1", child1.getChild("wktRegion").getValue());
    assertEquals("file1", child1.getChild("shapefile").getValue());
    DomElement child2 = customIndices.getChild(1);
    assertEquals("name2", child2.getChild("name").getValue());
    assertEquals("formula2", child2.getChild("formula").getValue());
    assertEquals("description2", child2.getChild("description").getValue());
    assertEquals("validExpression2", child2.getChild("validExpression").getValue());
    assertEquals("wktRegion2", child2.getChild("wktRegion").getValue());
    assertEquals("shapefile2", child2.getChild("shapefile").getValue());
  }

  @Test
  void testOperatorHelp() throws Exception {
    TestCommandLineContext context = new TestCommandLineContext();
    CommandLineTool clTool = new CommandLineTool(context);
    clTool.run("Spex", "-h");

    String expected = "Usage:\n"
        + "  gpt Spex [options] \n"
        + "\n"
        + "Description:\n"
        + "  Creates SpeX (SPEctral indeX) images for a source product. See help pages for detailed instructions.\n"
        + "\n"
        + "\n"
        + "Source Options:\n"
        + "  -SsourceProduct=<file>    The source product\n"
        + "                            This is a mandatory source.\n"
        + "\n"
        + "Parameter Options:\n"
        + "  -Pshapefile=<path>                       An ESRI shapefile, providing the considered geographical region(s).\n"
        + "  -PspexList=<string,string,string,...>    List of short names of SpeX to be calculated.\n"
        + "  -PvalidExpression=<string>               The valid expression to be used for the spectral index.\n"
        + "  -PwktRegion=<geometry>                   The considered geographical region as a geometry in well-known text format (WKT).\n"
        + "\n"
        + "Graph XML Format:\n"
        + "  <graph id=\"someGraphId\">\n"
        + "    <version>1.0</version>\n"
        + "    <node id=\"someNodeId\">\n"
        + "      <operator>Spex</operator>\n"
        + "      <sources>\n"
        + "        <sourceProduct>${sourceProduct}</sourceProduct>\n"
        + "      </sources>\n"
        + "      <parameters>\n"
        + "        <spexList>string,string,string,...</spexList>\n"
        + "        <validExpression>string</validExpression>\n"
        + "        <shapefile>path</shapefile>\n"
        + "        <wktRegion>geometry</wktRegion>\n"
        + "        <customIndices>\n"
        + "          <index>\n"
        + "            <name>string</name>\n"
        + "            <domain>domain</domain>\n"
        + "            <formula>string</formula>\n"
        + "            <description>string</description>\n"
        + "            <deprecation>string</deprecation>\n"
        + "            <reference>string</reference>\n"
        + "            <sourceName>string</sourceName>\n"
        + "            <sourceUrl>string</sourceUrl>\n"
        + "            <validExpression>string</validExpression>\n"
        + "            <shapefile>path</shapefile>\n"
        + "            <wktRegion>geometry</wktRegion>\n"
        + "          </index>\n"
        + "          <_.002e../>\n" // TODO - needs to be fixed in CommandLineUsage L485
        + "        </customIndices>\n"
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
