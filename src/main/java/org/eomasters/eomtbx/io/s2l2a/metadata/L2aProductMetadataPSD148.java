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
import eu.esa.opt.dataio.s2.filepatterns.S2DatastripDirFilename;
import eu.esa.opt.dataio.s2.filepatterns.S2DatastripFilename;
import eu.esa.opt.dataio.s2.l2a.L2aPSD148Constants;
import eu.esa.opt.dataio.s2.l2a.metadata.IL2aProductMetadata;
import eu.esa.opt.dataio.s2.l2a.metadata.L2aMetadataProc;
import eu.esa.opt.dataio.s2.ortho.filepatterns.S2OrthoDatastripFilename;
import eu.esa.opt.dataio.s2.ortho.filepatterns.S2OrthoGranuleDirFilename;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.metadata.GenericXmlMetadata;
import org.esa.snap.core.metadata.XmlMetadataParser;
import org.xml.sax.SAXException;

/**
 * Created by fdouziech 15/10/2021
 */

public class L2aProductMetadataPSD148 extends GenericXmlMetadata implements IL2aProductMetadata {

    private static class L2aProductMetadataPSD148Parser extends XmlMetadataParser<L2aProductMetadataPSD148> {

        public L2aProductMetadataPSD148Parser(Class metadataFileClass) {
            super(metadataFileClass);
            setSchemaLocations(L2aPSD148Constants.getProductSchemaLocations());
            setSchemaBasePath(L2aPSD148Constants.getProductSchemaBasePath());
        }

        @Override
        protected boolean shouldValidateSchema() {
            return false;
        }
    }



    public static L2aProductMetadataPSD148 create(VirtualPath path) throws IOException, ParserConfigurationException, SAXException {
        Assert.notNull(path);
        L2aProductMetadataPSD148 result = null;
        InputStream stream = null;
        try {
            if (path.exists()) {
                stream = path.getInputStream();
                L2aProductMetadataPSD148Parser parser = new L2aProductMetadataPSD148Parser(L2aProductMetadataPSD148.class);
                result = parser.parse(stream);
                result.setName("Level-2A_User_Product");
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return result;
    }


    public L2aProductMetadataPSD148(String name) {
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
    public S2Metadata.ProductCharacteristics getProductOrganization(VirtualPath path, S2SpatialResolution resolution) {
        S2Metadata.ProductCharacteristics characteristics = new S2Metadata.ProductCharacteristics();
        characteristics.setPsd(S2Metadata.getFullPSDversion(path));

        String datatakeSensingStart = getAttributeValue(L2aPSD148Constants.PATH_PRODUCT_METADATA_SENSING_START, null);
        if(datatakeSensingStart!=null && datatakeSensingStart.length()>19) {
            String formattedDatatakeSensingStart = datatakeSensingStart.substring(0,4) +
                    datatakeSensingStart.substring(5,7) +
                    datatakeSensingStart.substring(8,13) +
                    datatakeSensingStart.substring(14,16)+
                    datatakeSensingStart.substring(17,19);
            characteristics.setDatatakeSensingStartTime(formattedDatatakeSensingStart);
        } else {
            characteristics.setDatatakeSensingStartTime("Unknown");
        }

        characteristics.setSpacecraft(getAttributeValue(L2aPSD148Constants.PATH_PRODUCT_METADATA_SPACECRAFT, "Sentinel-2"));
        characteristics.setDatasetProductionDate(getAttributeValue(L2aPSD148Constants.PATH_PRODUCT_METADATA_SENSING_START, "Unknown"));

        characteristics.setProductStartTime(getAttributeValue(L2aPSD148Constants.PATH_PRODUCT_METADATA_PRODUCT_START_TIME, "Unknown"));
        characteristics.setProductStopTime(getAttributeValue(L2aPSD148Constants.PATH_PRODUCT_METADATA_PRODUCT_STOP_TIME, "Unknown"));

        characteristics.setProcessingLevel(getAttributeValue(L2aPSD148Constants.PATH_PRODUCT_METADATA_PROCESSING_LEVEL, "Level-2A"));
        characteristics.setProcessingBaseline(Integer.parseInt(getAttributeValue(L2aPSD148Constants.PATH_PRODUCT_METADATA_PROCESSING_BASELINE, "0").replace(".","")));

        characteristics.setMetaDataLevel(getAttributeValue(L2aPSD148Constants.PATH_PRODUCT_METADATA_METADATA_LEVEL, "Standard"));

        double boaQuantification = Double.valueOf(getAttributeValue(L2aPSD148Constants.PATH_PRODUCT_METADATA_L2A_BOA_QUANTIFICATION_VALUE, String.valueOf(L2aPSD148Constants.DEFAULT_BOA_QUANTIFICATION)));
        if(boaQuantification == 0d) {
            logger.warning("Invalid BOA quantification value, the default value will be used.");
            boaQuantification = L2aPSD148Constants.DEFAULT_BOA_QUANTIFICATION;
        }
        characteristics.setQuantificationValue(boaQuantification);

        double aotQuantification = Double.valueOf(getAttributeValue(L2aPSD148Constants.PATH_PRODUCT_METADATA_L2A_AOT_QUANTIFICATION_VALUE, String.valueOf(L2aPSD148Constants.DEFAULT_AOT_QUANTIFICATION)));
        if(aotQuantification == 0d) {
            logger.warning("Invalid AOT quantification value, the default value will be used.");
            aotQuantification = L2aPSD148Constants.DEFAULT_AOT_QUANTIFICATION;
        }
        double wvpQuantification = Double.valueOf(getAttributeValue(L2aPSD148Constants.PATH_PRODUCT_METADATA_L2A_WVP_QUANTIFICATION_VALUE, String.valueOf(L2aPSD148Constants.DEFAULT_WVP_QUANTIFICATION)));
        if(wvpQuantification == 0d) {
            logger.warning("Invalid WVP quantification value, the default value will be used.");
            wvpQuantification = L2aPSD148Constants.DEFAULT_WVP_QUANTIFICATION;
        }

        List<S2BandInformation> aInfo = L2aMetadataProc.getBandInformationList(getFormat(), resolution, characteristics.getPsd(), boaQuantification, aotQuantification, wvpQuantification);
        int size = aInfo.size();
        characteristics.setBandInformations(aInfo.toArray(new S2BandInformation[size]));

        return characteristics;
    }

    @Override
    public Collection<String> getTiles() {

        String[] granuleList = getAttributeValues(L2aPSD148Constants.PATH_PRODUCT_METADATA_GRANULE_LIST);
        if(granuleList == null) {
            granuleList = getAttributeValues(L2aPSD148Constants.PATH_PRODUCT_METADATA_GRANULE_LIST_ALT);
            if(granuleList == null) {
                //return an empty arraylist
                ArrayList<String> tiles = new ArrayList<>();
                return tiles;
            }
        }
        //New list with only granules with different granuleIdentifier
        //They are repeated when there are more than one resolution folder
        List<String> granuleListReduced = new ArrayList<>();
        Map<String, String> mapGranules = new LinkedHashMap<>(granuleList.length);
        for (String granule : granuleList) {
            mapGranules.put(granule, granule);
        }
        for (Map.Entry<String, String> granule : mapGranules.entrySet()) {
            granuleListReduced.add(granule.getValue());
        }

        return granuleListReduced;
    }

    @Override
    public S2DatastripFilename getDatastrip() {
        String[] datastripList = getAttributeValues(L2aPSD148Constants.PATH_PRODUCT_METADATA_DATASTRIP_LIST);
        if(datastripList == null) {
            datastripList = getAttributeValues(L2aPSD148Constants.PATH_PRODUCT_METADATA_DATASTRIP_LIST_ALT);
            if(datastripList == null) {
                return null;
            }
        }

        S2DatastripDirFilename dirDatastrip = S2DatastripDirFilename.create(datastripList[0], null);

        S2DatastripFilename datastripFilename = null;
        if (dirDatastrip != null) {
            String fileName = dirDatastrip.getFileName(null);

            if (fileName != null) {
                datastripFilename = S2OrthoDatastripFilename.create(fileName);
            }
        }

        return datastripFilename;
    }

    @Override
    public S2DatastripDirFilename getDatastripDir() {
        String[] granuleList = getAttributeValues(L2aPSD148Constants.PATH_PRODUCT_METADATA_GRANULE_LIST);
        String[] datastripList = getAttributeValues(L2aPSD148Constants.PATH_PRODUCT_METADATA_DATASTRIP_LIST);
        if(datastripList == null) {
            datastripList = getAttributeValues(L2aPSD148Constants.PATH_PRODUCT_METADATA_DATASTRIP_LIST_ALT);
            if(datastripList == null) {
                return null;
            }
        }
        if(granuleList == null) {
            granuleList = getAttributeValues(L2aPSD148Constants.PATH_PRODUCT_METADATA_GRANULE_LIST_ALT);
            if(granuleList == null) {
                return null;
            }
        }


        S2OrthoGranuleDirFilename grafile = S2OrthoGranuleDirFilename.create(granuleList[0]);

        S2DatastripDirFilename datastripDirFilename = null;
        if (grafile != null) {
            String fileCategory = grafile.fileCategory;
            String dataStripMetadataFilenameCandidate = datastripList[0];
            datastripDirFilename = S2DatastripDirFilename.create(dataStripMetadataFilenameCandidate, fileCategory);

        }
        return datastripDirFilename;
    }

    @Override
    public MetadataElement getMetadataElement() {
        return rootElement;
    }

    @Override
    public String getFormat() {
        return getAttributeValue(L2aPSD148Constants.PATH_PRODUCT_METADATA_PRODUCT_FORMAT, "SAFE"); //SAFE by default
    }

    @Override
    public String[] getBOAOffsetList() {
        return getAttributeValues(L2aPSD148Constants.PATH_PRODUCT_METADATA_BOA_ADD_OFFSET_VALUES_LIST);
    }
}
