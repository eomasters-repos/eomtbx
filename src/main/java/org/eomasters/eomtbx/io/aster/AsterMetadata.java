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

import static org.eomasters.utils.Exceptions.throwIf;
import static ucar.ma2.DataType.CHAR;
import static ucar.ma2.DataType.STRUCTURE;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eomasters.eomtbx.io.aster.odl.OdlGroup;
import org.eomasters.eomtbx.io.aster.odl.OdlObject;
import org.eomasters.eomtbx.io.aster.odl.OdlParser;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.ProductData.UTC;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.AttributeContainer;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Structure;
import ucar.nc2.Variable;

class AsterMetadata {

  private static final String PRODUCTMETADATA_PREFIX = "productmetadata.";
  private static final String SWATH_STRUCT_METADATA_VARNAME = "StructMetadata.0";
  private static final String ASTER_GENERIC_METADATA_VARNAME = PRODUCTMETADATA_PREFIX + "0";
  private static final String PRODUCT_GENERIC_METADATA_VARNAME = PRODUCTMETADATA_PREFIX + "1";
  private static final String PRODUCT_VNIR_METADATA_VARNAME = PRODUCTMETADATA_PREFIX + "v";
  private static final String PRODUCT_SWIR_METADATA_VARNAME = PRODUCTMETADATA_PREFIX + "s";
  private static final String PRODUCT_TIR_METADATA_VARNAME = PRODUCTMETADATA_PREFIX + "t";
  private static final String INVENTORY_CORE_METADATA_VARNAME = "coremetadata.0";
  private static final String ANCILLARY_DATA_GROUPNAME = "Ancillary_Data";

  private static final Map<String, SpectralInfo> SPECTRAL_INFO_MAP = new HashMap<>();
  private static final String START_TIME_PATTERN = "yyyyMMddHHmmss";

  static {
    SPECTRAL_INFO_MAP.put("VNIR_BAND_1", new SpectralInfo(520f, 600f, 1));
    SPECTRAL_INFO_MAP.put("VNIR_BAND_2", new SpectralInfo(630f, 690f, 2));
    SPECTRAL_INFO_MAP.put("VNIR_BAND_3N", new SpectralInfo(760f, 860f, 3));
    SPECTRAL_INFO_MAP.put("SWIR_BAND_4", new SpectralInfo(760f, 860f, 4));
    SPECTRAL_INFO_MAP.put("SWIR_BAND_5", new SpectralInfo(1600f, 1700f, 5));
    SPECTRAL_INFO_MAP.put("SWIR_BAND_6", new SpectralInfo(2145f, 2185f, 6));
    SPECTRAL_INFO_MAP.put("SWIR_BAND_7", new SpectralInfo(2185f, 2225f, 7));
    SPECTRAL_INFO_MAP.put("SWIR_BAND_8", new SpectralInfo(2235f, 2285f, 8));
    SPECTRAL_INFO_MAP.put("SWIR_BAND_9", new SpectralInfo(2295f, 2365f, 9));
    SPECTRAL_INFO_MAP.put("TIR_BAND_10", new SpectralInfo(2360f, 2430f, 10));
    SPECTRAL_INFO_MAP.put("TIR_BAND_11", new SpectralInfo(8125f, 8475f, 11));
    SPECTRAL_INFO_MAP.put("TIR_BAND_12", new SpectralInfo(8475f, 8825f, 12));
    SPECTRAL_INFO_MAP.put("TIR_BAND_13", new SpectralInfo(8925f, 9275f, 13));
    SPECTRAL_INFO_MAP.put("TIR_BAND_14", new SpectralInfo(10250f, 10950f, 14));
  }

  private final NetcdfFile ncFile;
  private final OdlParser odlParser;

  public AsterMetadata(NetcdfFile ncFile) {
    this.ncFile = ncFile;
    odlParser = new OdlParser();
  }

  public void addToProduct(Product product) throws IOException {
    MetadataElement metadataRoot = product.getMetadataRoot();
    addGlobalAttributes(metadataRoot);
    Group rootGroup = ncFile.getRootGroup();
    addOdlMetadata(rootGroup, SWATH_STRUCT_METADATA_VARNAME, metadataRoot);
    addOdlMetadata(rootGroup, ASTER_GENERIC_METADATA_VARNAME, metadataRoot);
    addOdlMetadata(rootGroup, PRODUCT_GENERIC_METADATA_VARNAME, metadataRoot);
    addOdlMetadata(rootGroup, PRODUCT_VNIR_METADATA_VARNAME, metadataRoot);
    addOdlMetadata(rootGroup, PRODUCT_SWIR_METADATA_VARNAME, metadataRoot);
    addOdlMetadata(rootGroup, PRODUCT_TIR_METADATA_VARNAME, metadataRoot);
    addOdlMetadata(rootGroup, INVENTORY_CORE_METADATA_VARNAME, metadataRoot);
    Group ancillaryData = rootGroup.findGroup(ANCILLARY_DATA_GROUPNAME);
    MetadataElement ancElement = new MetadataElement(ANCILLARY_DATA_GROUPNAME);
    for (Variable variable : ancillaryData.getVariables()) {
      if (variable.getDataType().isString() || variable.getDataType().equals(CHAR)) {
        addAttribute(ancElement, variable.getShortName(), variable.readScalarString());
      } else if (variable.getDataType().equals(STRUCTURE)) {
        Structure structure = (Structure) variable;
        List<Variable> variables = structure.getVariables();
        for (Variable structVar : variables) {
          if (variable.getDataType().isString() || variable.getDataType().equals(CHAR)) {
            addAttribute(ancElement, structVar.getShortName(), structVar.readScalarString());
          }
        }
      }
    }
  }

  private void addOdlMetadata(Group rootGroup, String metadataVarname, MetadataElement root) throws IOException {
    OdlGroup odlGroup = getOdlMetadata(rootGroup, metadataVarname);
    if (odlGroup == null) {
      return;
    }
    MetadataElement metadataElement = new MetadataElement(metadataVarname);
    root.addElement(metadataElement);
    addChildrenTo(odlGroup, metadataElement);
  }

  OdlGroup getOdlMetadata(Group rootGroup, String metadataVarname) throws IOException {
    Variable variable = rootGroup.findVariable(metadataVarname);
    if (variable == null) {
      return null;
    }
    throwIf(!variable.getDataType().isString(), new IllegalStateException(
        String.format("Variable %s must be of type %s but is %s", variable.getShortName(), DataType.CHAR,
            variable.getDataType())));

    return odlParser.parse(variable.readScalarString());
  }

  void addChildrenTo(OdlGroup parent, MetadataElement target) {
    for (OdlGroup child : parent.getSubGroups()) {
      MetadataElement groupElement = new MetadataElement(child.getName());
      target.addElement(groupElement);
      if (!child.getObjects().isEmpty()) {
        for (OdlObject object : child.getObjects()) {
          if (object.hasAttributes()) {
            MetadataElement objElement = new MetadataElement(object.getName());
            groupElement.addElement(objElement);
            Map<String, String> attributes = object.getAttributes();
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
              addAttribute(objElement, entry.getKey(), entry.getValue());
            }
          }
        }
      }
      if (!child.getSubGroups().isEmpty()) {
        addChildrenTo(child, groupElement);
      }
    }
  }

  private void addGlobalAttributes(MetadataElement root) {
    MetadataElement globalAttributesElement = new MetadataElement("GlobalAttributes");
    root.addElement(globalAttributesElement);
    AttributeContainer attributes = ncFile.getRootGroup().attributes();
    for (Attribute attribute : attributes) {
      addAttribute(globalAttributesElement, attribute.getShortName().trim(),
                   String.valueOf(attribute.getStringValue()).trim());
    }
  }

  private static void addAttribute(MetadataElement element, String attributeName, String attributeValue) {
    element.addAttribute(new MetadataAttribute(attributeName, ProductData.createInstance(attributeValue), true));
  }

  public ConversionInfo getConversionInfo(String sensorGroup, String bandName) throws IOException {
    String sensorMetadata = PRODUCTMETADATA_PREFIX + bandName.trim().toLowerCase().toCharArray()[0];
    String[] split = bandName.split("_");
    throwIf(split.length == 0, new IllegalStateException("Not able retrieve band id."));
    String bandId = split[split.length - 1];
    OdlGroup odlElement = getOdlMetadata(ncFile.getRootGroup(), sensorMetadata);
    OdlGroup conversionGroup = odlElement.getSubGroup(
        String.format("PRODUCTSPECIFICMETADATA%1$s/%1$sBAND%2$sDATA/UNITCONVERSIONCOEFF%2$s", sensorGroup, bandId));
    String scaling = conversionGroup.getAttribute(String.format("INCL%s/VALUE", bandId));
    String offset = conversionGroup.getAttribute(String.format("OFFSET%s/VALUE", bandId));
    String unit = conversionGroup.getAttribute(String.format("CONUNIT%s/VALUE", bandId));
    return new ConversionInfo(Double.parseDouble(scaling), Double.parseDouble(offset), unit);
  }

  public SpectralInfo getSpectralInfo(String name) {
    return SPECTRAL_INFO_MAP.get(name);
  }

  public UTC getObservationTime() throws Exception {
    OdlGroup odlGroup;
    try {
      odlGroup = getOdlMetadata(ncFile.getRootGroup(), INVENTORY_CORE_METADATA_VARNAME);
    } catch (IOException e) {
      throw new Exception(e.getMessage(), e);
    }
    if (odlGroup == null) {
      return null;
    }
    return getObservationTime(odlGroup);
  }

  static UTC getObservationTime(OdlGroup odlGroup) throws ParseException {
    String timeOfDay = odlGroup.getAttribute("INVENTORYMETADATA/SINGLEDATETIME/TIMEOFDAY/VALUE");
    String calendarDate = odlGroup.getAttribute("INVENTORYMETADATA/SINGLEDATETIME/CALENDARDATE/VALUE");
    String dateTime = (calendarDate + timeOfDay).substring(0, 14);
    return UTC.parse(dateTime, START_TIME_PATTERN);
  }

  public static class ConversionInfo {

    final double scaling;
    final double offset;
    final String unit;

    public ConversionInfo(double scaling, double offset, String unit) {
      this.scaling = scaling;
      this.offset = offset;
      this.unit = unit;
    }
  }

  static class SpectralInfo {

    float centralWvl;
    float bandWidth;
    int spectralIndex;

    public SpectralInfo(float minWvl, float maxWvl, int spectralIndex) {
      centralWvl = minWvl + (maxWvl - minWvl) / 2;
      bandWidth = maxWvl - minWvl;
      this.spectralIndex = spectralIndex;
    }
  }
}
