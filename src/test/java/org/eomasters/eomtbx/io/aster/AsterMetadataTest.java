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

package org.eomasters.eomtbx.io.aster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import org.eomasters.eomtbx.io.aster.odl.OdlGroup;
import org.eomasters.eomtbx.io.aster.odl.OdlParser;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData.UTC;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import ucar.nc2.NetcdfFile;

@TestInstance(Lifecycle.PER_CLASS)
class AsterMetadataTest {

  private static final String ODL_SWATH_STRING = "GROUP=SwathStructure\n"
      + "\tGROUP=SWATH_1\n"
      + "\t\tSwathName=\"VNIR_Swath\"\n"
      + "\t\tGROUP=Dimension\n"
      + "\t\t\tOBJECT=Dimension_1\n"
      + "\t\t\t\tDimensionName=\"GeoTrack\"\n"
      + "\t\t\t\tSize=11\n"
      + "\t\t\tEND_OBJECT=Dimension_1\n"
      + "\t\t\tOBJECT=Dimension_2\n"
      + "\t\t\t\tDimensionName=\"GeoXtrack\"\n"
      + "\t\t\t\tSize=11\n"
      + "\t\t\tEND_OBJECT=Dimension_2\n"
      + "\t\t\tOBJECT=Dimension_3\n"
      + "\t\t\t\tDimensionName=\"ImageLine\"\n"
      + "\t\t\t\tSize=4855\n"
      + "\t\t\tEND_OBJECT=Dimension_3\n"
      + "\t\t\tOBJECT=Dimension_4\n"
      + "\t\t\t\tDimensionName=\"ImagePixel\"\n"
      + "\t\t\t\tSize=5521\n"
      + "\t\t\tEND_OBJECT=Dimension_4\n"
      + "\t\t\tOBJECT=Dimension_5\n"
      + "\t\t\t\tDimensionName=\"ImageLine3B\"\n"
      + "\t\t\t\tSize=4855\n"
      + "\t\t\tEND_OBJECT=Dimension_5\n"
      + "\t\tEND_GROUP=Dimension\n"
      + "\t\tGROUP=DimensionMap\n"
      + "\t\t\tOBJECT=DimensionMap_1\n"
      + "\t\t\t\tGeoDimension=\"GeoTrack\"\n"
      + "\t\t\t\tDataDimension=\"ImageLine\"\n"
      + "\t\t\t\tOffset=0\n"
      + "\t\t\t\tIncrement=485\n"
      + "\t\t\tEND_OBJECT=DimensionMap_1\n"
      + "\t\t\tOBJECT=DimensionMap_2\n"
      + "\t\t\t\tGeoDimension=\"GeoXtrack\"\n"
      + "\t\t\t\tDataDimension=\"ImagePixel\"\n"
      + "\t\t\t\tOffset=0\n"
      + "\t\t\t\tIncrement=552\n"
      + "\t\t\tEND_OBJECT=DimensionMap_2\n"
      + "\t\t\tOBJECT=DimensionMap_3\n"
      + "\t\t\t\tGeoDimension=\"GeoTrack\"\n"
      + "\t\t\t\tDataDimension=\"ImageLine3B\"\n"
      + "\t\t\t\tOffset=0\n"
      + "\t\t\t\tIncrement=485\n"
      + "\t\t\tEND_OBJECT=DimensionMap_3\n"
      + "\t\tEND_GROUP=DimensionMap\n"
      + "\t\tGROUP=GeoField\n"
      + "\t\t\tOBJECT=GeoField_1\n"
      + "\t\t\t\tGeoFieldName=\"Latitude\"\n"
      + "\t\t\t\tDataType=DFNT_FLOAT64\n"
      + "\t\t\t\tDimList=(\"GeoTrack\",\"GeoXtrack\")\n"
      + "\t\t\tEND_OBJECT=GeoField_1\n"
      + "\t\t\tOBJECT=GeoField_2\n"
      + "\t\t\t\tGeoFieldName=\"Longitude\"\n"
      + "\t\t\t\tDataType=DFNT_FLOAT64\n"
      + "\t\t\t\tDimList=(\"GeoTrack\",\"GeoXtrack\")\n"
      + "\t\t\tEND_OBJECT=GeoField_2\n"
      + "\t\tEND_GROUP=GeoField\n"
      + "\tEND_GROUP=SWATH_1\n"
      + "END_GROUP=SwathStructure\n"
      + "END\n";

  String ODL_INVENTORY_STRING = "\n"
      + "GROUP                  = INVENTORYMETADATA\n"
      + "  GROUPTYPE            = MASTERGROUP\n"
      + "\n"
      + "  OBJECT                 = SHORTNAME\n"
      + "    NUM_VAL              = 1\n"
      + "    VALUE                = \"AST_L1T\"\n"
      + "  END_OBJECT             = SHORTNAME\n"
      + "\n"
      + "  OBJECT                 = SIZEMBDATAGRANULE\n"
      + "    NUM_VAL              = 1\n"
      + "    VALUE                = 85.595\n"
      + "  END_OBJECT             = SIZEMBDATAGRANULE\n"
      + "\n"
      + "  OBJECT                 = PRODUCTIONDATETIME\n"
      + "    NUM_VAL              = 1\n"
      + "    VALUE                = \"2024-08-13T12:30:34.000Z\"\n"
      + "  END_OBJECT             = PRODUCTIONDATETIME\n"
      + "\n"
      + "  OBJECT                 = PLATFORMSHORTNAME\n"
      + "    VALUE                = \"Terra\"\n"
      + "    NUM_VAL              = 1\n"
      + "  END_OBJECT             = PLATFORMSHORTNAME\n"
      + "\n"
      + "  OBJECT                 = INSTRUMENTSHORTNAME\n"
      + "    VALUE                = \"ASTER\"\n"
      + "    NUM_VAL              = 1\n"
      + "  END_OBJECT             = INSTRUMENTSHORTNAME\n"
      + "\n"
      + "  GROUP                  = BOUNDINGRECTANGLE\n"
      + "\n"
      + "    OBJECT                 = WESTBOUNDINGCOORDINATE\n"
      + "      NUM_VAL              = 1\n"
      + "      VALUE                = 138.218010246092\n"
      + "    END_OBJECT             = WESTBOUNDINGCOORDINATE\n"
      + "\n"
      + "    OBJECT                 = NORTHBOUNDINGCOORDINATE\n"
      + "      NUM_VAL              = 1\n"
      + "      VALUE                = -34.5331198630214\n"
      + "    END_OBJECT             = NORTHBOUNDINGCOORDINATE\n"
      + "\n"
      + "    OBJECT                 = EASTBOUNDINGCOORDINATE\n"
      + "      NUM_VAL              = 1\n"
      + "      VALUE                = 139.141661225418\n"
      + "    END_OBJECT             = EASTBOUNDINGCOORDINATE\n"
      + "\n"
      + "    OBJECT                 = SOUTHBOUNDINGCOORDINATE\n"
      + "      NUM_VAL              = 1\n"
      + "      VALUE                = -35.2064028921335\n"
      + "    END_OBJECT             = SOUTHBOUNDINGCOORDINATE\n"
      + "\n"
      + "  END_GROUP              = BOUNDINGRECTANGLE\n"
      + "\n"
      + "  GROUP                  = SINGLEDATETIME\n"
      + "\n"
      + "    OBJECT                 = TIMEOFDAY\n"
      + "      NUM_VAL              = 1\n"
      + "      VALUE                = \"002443486000Z\"\n"
      + "    END_OBJECT             = TIMEOFDAY\n"
      + "\n"
      + "    OBJECT                 = CALENDARDATE\n"
      + "      NUM_VAL              = 1\n"
      + "      VALUE                = \"20231217\"\n"
      + "    END_OBJECT             = CALENDARDATE\n"
      + "\n"
      + "  END_GROUP              = SINGLEDATETIME\n"
      + "\n"
      + "  GROUP                  = REVIEW\n"
      + "\n"
      + "    OBJECT                 = FUTUREREVIEWDATE\n"
      + "      NUM_VAL              = 1\n"
      + "      VALUE                = \"20230621\"\n"
      + "    END_OBJECT             = FUTUREREVIEWDATE\n"
      + "\n"
      + "    OBJECT                 = SCIENCEREVIEWDATE\n"
      + "      NUM_VAL              = 1\n"
      + "      VALUE                = \"20230621\"\n"
      + "    END_OBJECT             = SCIENCEREVIEWDATE\n"
      + "\n"
      + "  END_GROUP              = REVIEW\n"
      + "\n"
      + "  GROUP                  = QASTATS\n"
      + "\n"
      + "    OBJECT                 = QAPERCENTMISSINGDATA\n"
      + "      NUM_VAL              = 1\n"
      + "      VALUE                = 0.0173036400228739\n"
      + "    END_OBJECT             = QAPERCENTMISSINGDATA\n"
      + "\n"
      + "    OBJECT                 = QAPERCENTOUTOFBOUNDSDATA\n"
      + "      NUM_VAL              = 1\n"
      + "      VALUE                = 0.0173036400228739\n"
      + "    END_OBJECT             = QAPERCENTOUTOFBOUNDSDATA\n"
      + "\n"
      + "    OBJECT                 = QAPERCENTINTERPOLATEDDATA\n"
      + "      NUM_VAL              = 1\n"
      + "      VALUE                = 0.0\n"
      + "    END_OBJECT             = QAPERCENTINTERPOLATEDDATA\n"
      + "\n"
      + "  END_GROUP              = QASTATS\n"
      + "\n"
      + "  OBJECT                 = REPROCESSINGACTUAL\n"
      + "    NUM_VAL              = 1\n"
      + "    VALUE                = \"not reprocessed\"\n"
      + "  END_OBJECT             = REPROCESSINGACTUAL\n"
      + "\n"
      + "  OBJECT                 = PGEVERSION\n"
      + "    NUM_VAL              = 1\n"
      + "    VALUE                = \"1.0\"\n"
      + "  END_OBJECT             = PGEVERSION\n"
      + "\n"
      + "  OBJECT                 = PROCESSINGLEVELID\n"
      + "    NUM_VAL              = 1\n"
      + "    VALUE                = \"1T\"\n"
      + "  END_OBJECT             = PROCESSINGLEVELID\n"
      + "\n"
      + "  OBJECT                 = MAPPROJECTIONNAME\n"
      + "    NUM_VAL              = 1\n"
      + "    VALUE                = \"Universal Transverse Mercator\"\n"
      + "  END_OBJECT             = MAPPROJECTIONNAME\n"
      + "\n"
      + "  OBJECT                 = IDENTIFIER_PRODUCT_DOI_AUTHORITY\n"
      + "    VALUE                = \"http://dx.doi.org\"\n"
      + "    NUM_VAL              = 1\n"
      + "  END_OBJECT             = IDENTIFIER_PRODUCT_DOI_AUTHORITY\n"
      + "\n"
      + "  OBJECT                 = IDENTIFIER_PRODUCT_DOI\n"
      + "    VALUE                = \"10.5067/ASTER/AST_L1T.003\"\n"
      + "    NUM_VAL              = 1\n"
      + "  END_OBJECT             = IDENTIFIER_PRODUCT_DOI\n"
      + "\n"
      + "END_GROUP              = INVENTORYMETADATA\n"
      + "\n"
      + "END\n";
  private NetcdfFile ncFile;

  @BeforeAll
  void setUpMocks() {
    this.ncFile = mock(NetcdfFile.class);
  }

  @Test
  void testGetSpectralInfo() {
    AsterMetadata asterMetadata = new AsterMetadata(ncFile);
    AsterMetadata.SpectralInfo info = asterMetadata.getSpectralInfo("VNIR_BAND_1");

    assertEquals(560f, info.centralWvl);
    assertEquals(80f, info.bandWidth);
    assertEquals(1, info.spectralIndex);
  }

  @Test
  void testGetSpectralInfoInvalidName() {
    AsterMetadata asterMetadata = new AsterMetadata(ncFile);
    AsterMetadata.SpectralInfo info = asterMetadata.getSpectralInfo("INVALID_BAND_NAME");

    assertNull(info);
  }

  @Test
  void testAddChildrenTo() throws IOException {
    AsterMetadata asterMetadata = new AsterMetadata(ncFile);
    OdlGroup odlGroup = new OdlParser().parse(ODL_SWATH_STRING);
    MetadataElement root = new MetadataElement("root");
    asterMetadata.addChildrenTo(odlGroup, root);

    assertEquals(1, root.getNumElements());
    MetadataElement swathStructureElem = root.getElements()[0];
    assertEquals("SwathStructure", swathStructureElem.getName());
    assertEquals(1, swathStructureElem.getNumElements());
    MetadataElement swath1Elem = swathStructureElem.getElements()[0];
    assertEquals("SWATH_1", swath1Elem.getName());
    assertEquals(3, swath1Elem.getNumElements());
    MetadataElement dimensionMapElem = swath1Elem.getElement("DimensionMap");
    MetadataElement dimensionMap3Elem = dimensionMapElem.getElement("DimensionMap_3");
    assertEquals(0, dimensionMap3Elem.getNumElements());
    assertEquals(4, dimensionMap3Elem.getNumAttributes());
    assertEquals("485", dimensionMap3Elem.getAttributeString("Increment"));
  }
  // Test methods for 'getSpectralInfo' method are here

  @Test
  void testGetObservationTime() throws Exception {
    OdlGroup odlGroup = new OdlParser().parse(ODL_INVENTORY_STRING);
    UTC observedTime = AsterMetadata.getObservationTime(odlGroup);
    assertNotNull(observedTime);
  }

}
