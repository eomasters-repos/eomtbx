/*-
 * ========================LICENSE_START=================================
 * EOMTBX Basic - EOMasters Toolbox Basic for SNAP
 * -> https://www.eomasters.org/eomtbx
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

package org.eomasters.eomtbx.io.s2l2a.metadata;

import com.bc.ceres.core.Assert;
import eu.esa.opt.dataio.s2.S2BandInformation;
import eu.esa.opt.dataio.s2.S2Metadata;
import eu.esa.opt.dataio.s2.S2SpatialResolution;
import eu.esa.opt.dataio.s2.VirtualPath;
import eu.esa.opt.dataio.s2.filepatterns.NamingConventionFactory;
import eu.esa.opt.dataio.s2.filepatterns.SAFECOMPACTNamingConvention;
import eu.esa.opt.dataio.s2.l2a.L2aPSD13Constants;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.metadata.GenericXmlMetadata;
import org.esa.snap.core.metadata.XmlMetadataParser;
import org.xml.sax.SAXException;

/**
 * Created by obarrile on 04/10/2016.
 */

public class L2aGranuleMetadataPSD13 extends GenericXmlMetadata implements IL2aGranuleMetadata {

    String format = "";

    private static class L2aGranuleMetadataPSD13Parser extends XmlMetadataParser<L2aGranuleMetadataPSD13> {

        public L2aGranuleMetadataPSD13Parser(Class metadataFileClass) {
            super(metadataFileClass);
            setSchemaLocations(L2aPSD13Constants.getGranuleSchemaLocations());
            setSchemaBasePath(L2aPSD13Constants.getGranuleSchemaBasePath());
        }

        @Override
        protected boolean shouldValidateSchema() {
            return false;
        }
    }

    public static L2aGranuleMetadataPSD13 create(VirtualPath path) throws IOException, ParserConfigurationException, SAXException {
        Assert.notNull(path);
        L2aGranuleMetadataPSD13 result = null;
        InputStream stream = null;
        try {
            if (path.exists()) {
                stream = path.getInputStream();
                L2aGranuleMetadataPSD13Parser parser = new L2aGranuleMetadataPSD13Parser(L2aGranuleMetadataPSD13.class);
                result = parser.parse(stream);
                result.updateName();
                result.format = NamingConventionFactory.getGranuleFormat(path);
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return result;
    }

    public L2aGranuleMetadataPSD13(String name) {
        super(name);
    }

    @Override
    public String getFileName() {
        return null;
    }

    @Override
    public String getMetadataProfile() {
        return null;
    }

    @Override
    public S2Metadata.ProductCharacteristics getTileProductOrganization(VirtualPath path,S2SpatialResolution resolution) {

        S2Metadata.ProductCharacteristics characteristics = new S2Metadata.ProductCharacteristics();
        characteristics.setPsd(S2Metadata.getPSD(path));
        characteristics.setProcessingBaseline(S2Metadata.getProcessingBaseline(path));
        //DatatakeSensingStart is not in the metadata, but it is needed for the image templates. We read it from the file system
        VirtualPath folder = path.resolveSibling("IMG_DATA");
        Pattern pattern = Pattern.compile(SAFECOMPACTNamingConvention.SPECTRAL_BAND_REGEX);
        characteristics.setDatatakeSensingStartTime("Unknown");
        boolean bFound = false;
        if (folder.existsAndHasChildren()) {
            VirtualPath[] resolutions;
            try {
                resolutions = folder.listPaths();
            } catch (IOException e) {
                resolutions = null;
            }
            if (resolutions != null) {
                for (VirtualPath resolutionFolder : resolutions) {
                    if (resolutionFolder.existsAndHasChildren()) {
                        VirtualPath[] images;
                        try {
                            images = resolutionFolder.listPaths();
                        } catch (IOException e) {
                            images = null;
                        }
                        if (images != null && images.length > 0) {
                            for (VirtualPath image : images) {
                                String imageName = image.getFileName().toString();
                                Matcher matcher = pattern.matcher(imageName);
                                if (matcher.matches()) {
                                    characteristics.setDatatakeSensingStartTime(matcher.group(2));
                                    bFound = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (bFound) {
                        break;
                    }
                }
            }
        }
        characteristics.setSpacecraft("Sentinel-2");
        characteristics.setProcessingLevel("Level-2A");
        characteristics.setMetaDataLevel("Standard");

        double boaQuantification = L2aPSD13Constants.DEFAULT_BOA_QUANTIFICATION;
        characteristics.setQuantificationValue(boaQuantification);
        double aotQuantification = L2aPSD13Constants.DEFAULT_AOT_QUANTIFICATION;
        double wvpQuantification = L2aPSD13Constants.DEFAULT_WVP_QUANTIFICATION;

        List<S2BandInformation> aInfo = L2aMetadataProc.getBandInformationList(getFormat(), resolution, characteristics.getPsd(), boaQuantification, aotQuantification, wvpQuantification);
        int size = aInfo.size();
        characteristics.setBandInformations(aInfo.toArray(new S2BandInformation[size]));

        return characteristics;
    }

    @Override
    public Map<S2SpatialResolution, S2Metadata.TileGeometry> getTileGeometries() {
        Map<S2SpatialResolution, S2Metadata.TileGeometry> resolutions = new HashMap<>();
        String[] resolutionsValues = getAttributeValues(L2aPSD13Constants.PATH_GRANULE_METADATA_GEOPOSITION_RESOLUTION);
        if(resolutionsValues == null) {
            return resolutions;
        }
        for (String res : resolutionsValues) {
            S2SpatialResolution resolution = S2SpatialResolution.valueOfResolution(Integer.parseInt(res));
            S2Metadata.TileGeometry tgeox = new S2Metadata.TileGeometry();

            tgeox.setUpperLeftX(Double.parseDouble(getAttributeSiblingValue(L2aPSD13Constants.PATH_GRANULE_METADATA_GEOPOSITION_RESOLUTION, res,
                                                                            L2aPSD13Constants.PATH_GRANULE_METADATA_GEOPOSITION_ULX, "0")));
            tgeox.setUpperLeftY(Double.parseDouble(getAttributeSiblingValue(L2aPSD13Constants.PATH_GRANULE_METADATA_GEOPOSITION_RESOLUTION, res,
                                                                            L2aPSD13Constants.PATH_GRANULE_METADATA_GEOPOSITION_ULY, "0")));
            tgeox.setxDim(Double.parseDouble(getAttributeSiblingValue(L2aPSD13Constants.PATH_GRANULE_METADATA_GEOPOSITION_RESOLUTION, res,
                                                                      L2aPSD13Constants.PATH_GRANULE_METADATA_GEOPOSITION_XDIM, "0")));
            tgeox.setyDim(Double.parseDouble(getAttributeSiblingValue(L2aPSD13Constants.PATH_GRANULE_METADATA_GEOPOSITION_RESOLUTION, res,
                                                                      L2aPSD13Constants.PATH_GRANULE_METADATA_GEOPOSITION_YDIM, "0")));
            tgeox.setNumCols(Integer.parseInt(getAttributeSiblingValue(L2aPSD13Constants.PATH_GRANULE_METADATA_SIZE_RESOLUTION, res,
                                                                       L2aPSD13Constants.PATH_GRANULE_METADATA_SIZE_NCOLS, "0")));
            tgeox.setNumRows(Integer.parseInt(getAttributeSiblingValue(L2aPSD13Constants.PATH_GRANULE_METADATA_SIZE_RESOLUTION, res,
                                                                       L2aPSD13Constants.PATH_GRANULE_METADATA_SIZE_NROWS, "0")));
            resolutions.put(resolution, tgeox);
        }

        return resolutions;
    }

    @Override
    public String getTileID() {
        return getAttributeValue(L2aPSD13Constants.PATH_GRANULE_METADATA_TILE_ID, null);
    }

    @Override
    public String getHORIZONTAL_CS_CODE() {
        return getAttributeValue(L2aPSD13Constants.PATH_GRANULE_METADATA_HORIZONTAL_CS_CODE, null);
    }

    @Override
    public String getHORIZONTAL_CS_NAME() {
        return getAttributeValue(L2aPSD13Constants.PATH_GRANULE_METADATA_HORIZONTAL_CS_NAME, null);
    }

    @Override
    public int getAnglesResolution() {
        return Integer.parseInt(getAttributeValue(L2aPSD13Constants.PATH_GRANULE_METADATA_ANGLE_RESOLUTION, String.valueOf(L2aPSD13Constants.DEFAULT_ANGLES_RESOLUTION)));
    }

    @Override
    public S2Metadata.AnglesGrid getSunGrid() {
        return S2Metadata.wrapAngles(getAttributeValues(L2aPSD13Constants.PATH_GRANULE_METADATA_SUN_ZENITH_ANGLES),
                                     getAttributeValues(L2aPSD13Constants.PATH_GRANULE_METADATA_SUN_AZIMUTH_ANGLES));
    }

    @Override
    public S2Metadata.AnglesGrid[] getViewingAnglesGrid() {
        MetadataElement geometricElement = rootElement.getElement("Geometric_Info");
        if(geometricElement == null) {
            return null;
        }
        MetadataElement tileAnglesElement = geometricElement.getElement("Tile_Angles");
        if(tileAnglesElement == null) {
            return null;
        }
        return S2Metadata.wrapStandardViewingAngles(tileAnglesElement);
    }

    @Override
    public S2Metadata.MaskFilename[] getMasks(VirtualPath path) {
        S2Metadata.MaskFilename[] maskFileNamesArray;
        List<S2Metadata.MaskFilename> aMaskList = new ArrayList<>();
        String[] maskFilenames = getAttributeValues(L2aPSD13Constants.PATH_GRANULE_METADATA_MASK_FILENAME);
        if(maskFilenames == null) {
            return null;
        }
        boolean gmlMaskFormat=false;
        for (String maskFilename : maskFilenames)
        {
            String filenameProcessed = Paths.get(maskFilename).getFileName().toString();
            if(filenameProcessed.endsWith(".gml"))
            {
                gmlMaskFormat = true;
                break;
            }
        }
        for (String maskFilename : maskFilenames) {
            //To be sure that it is not a relative path and finish with .gml
            String filenameProcessed = Paths.get(maskFilename).getFileName().toString();
            if(gmlMaskFormat){
                if(!filenameProcessed.endsWith(".gml")) {
                    filenameProcessed = filenameProcessed + ".gml";
                }
            }
            VirtualPath QIData = path.resolveSibling("QI_DATA");
            VirtualPath maskData = QIData.resolve(filenameProcessed);

            aMaskList.add(new S2Metadata.MaskFilename(getAttributeSiblingValue(L2aPSD13Constants.PATH_GRANULE_METADATA_MASK_FILENAME, maskFilename,
                                                                                L2aPSD13Constants.PATH_GRANULE_METADATA_MASK_BAND, null),
                                                       getAttributeSiblingValue(L2aPSD13Constants.PATH_GRANULE_METADATA_MASK_FILENAME, maskFilename,
                                                                                L2aPSD13Constants.PATH_GRANULE_METADATA_MASK_TYPE, null),
                                                                                maskData));
        }
        maskFileNamesArray = aMaskList.toArray(new S2Metadata.MaskFilename[aMaskList.size()]);
        return maskFileNamesArray;
    }

    @Override
    public MetadataElement getMetadataElement() {
        return rootElement;
    }

    @Override
    public MetadataElement getSimplifiedMetadataElement() {
        //TODO ? new parse? or clone rootElement and remove some elements?
        return rootElement;
    }

    @Override
    public String getFormat() {
        return format;
    }

    private void updateName() {
        String tileId = getAttributeValue(L2aPSD13Constants.PATH_GRANULE_METADATA_TILE_ID, null);
        if(tileId == null || tileId.length()<56) {
            setName("Level-2A_Tile_ID");
            return;
        }
        setName("Level-2A_Tile_" + tileId.substring(50, 55));
    }
}
