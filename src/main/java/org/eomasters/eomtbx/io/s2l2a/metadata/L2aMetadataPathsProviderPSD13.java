/*-
 * ========================LICENSE_START=================================
 * EOMTBX Basic - EOMasters Toolbox Basic for SNAP
 * -> https://www.eomasters.org/eomtbx
 * ======================================================================
 * Copyright (C) 2023 - 2025 Marco Peters
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

import eu.esa.opt.dataio.s2.l2a.L2aPSD13Constants;

/**
 * Created by obarrile on 06/02/2018.
 */
public class L2aMetadataPathsProviderPSD13 implements IL2aMetadataPathsProvider {

    public L2aMetadataPathsProviderPSD13() {
    }
    @Override
    public String[] getProductSchemaLocations() {
        return L2aPSD13Constants.getProductSchemaLocations();
    }

    @Override
    public String[] getGranuleSchemaLocations() {
        return L2aPSD13Constants.getGranuleSchemaLocations();
    }

    @Override
    public String[] getDatastripSchemaLocations() {
        return L2aPSD13Constants.getDatastripSchemaLocations();
    }

    @Override
    public String getProductSchemaBasePath() {
        return L2aPSD13Constants.getProductSchemaBasePath();
    }

    @Override
    public String getDatastripSchemaBasePath() {
        return L2aPSD13Constants.getDatastripSchemaBasePath();
    }

    @Override
    public String getGranuleSchemaBasePath() {
        return L2aPSD13Constants.getGranuleSchemaBasePath();
    }

    @Override
    public String getPATH_PRODUCT_METADATA_DATATAKE() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_DATATAKE;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_SPACECRAFT() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_SPACECRAFT;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_DATATAKE_TYPE() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_DATATAKE_TYPE;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_SENSING_START() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_SENSING_START;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_SENSING_ORBIT_NUMBER() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_SENSING_ORBIT_NUMBER;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_ORBIT_DIRECTION() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_ORBIT_DIRECTION;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_PRODUCT_START_TIME() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_PRODUCT_START_TIME;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_PRODUCT_STOP_TIME() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_PRODUCT_STOP_TIME;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_PRODUCT_URI() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_PRODUCT_URI;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_PROCESSING_LEVEL() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_PROCESSING_LEVEL;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_PRODUCT_TYPE() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_PRODUCT_TYPE;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_PROCESSING_BASELINE() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_PROCESSING_BASELINE;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_GENERATION_TIME() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_GENERATION_TIME;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_PREVIEW_IMAGE_URL() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_PREVIEW_IMAGE_URL;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_PREVIEW_GEO_INFO() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_PREVIEW_GEO_INFO;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_METADATA_LEVEL() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_METADATA_LEVEL;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_GRANULE_LIST() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_GRANULE_LIST;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_GRANULE_LIST_ALT() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_GRANULE_LIST_ALT;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_DATASTRIP_LIST() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_DATASTRIP_LIST;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_DATASTRIP_LIST_ALT() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_DATASTRIP_LIST_ALT;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_IMAGE_ID() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_IMAGE_ID;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_L1C_TOA_QUANTIFICATION_VALUE() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_L1C_TOA_QUANTIFICATION_VALUE;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_L2A_BOA_QUANTIFICATION_VALUE() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_L2A_BOA_QUANTIFICATION_VALUE;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_L2A_AOT_QUANTIFICATION_VALUE() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_L2A_AOT_QUANTIFICATION_VALUE;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_L2A_WVP_QUANTIFICATION_VALUE() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_L2A_WVP_QUANTIFICATION_VALUE;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_BAND_LIST() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_BAND_LIST;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_PRODUCT_FORMAT() {
        return L2aPSD13Constants.PATH_PRODUCT_METADATA_PRODUCT_FORMAT;
    }

    @Override
    public String getPATH_GRANULE_METADATA_TILE_ID() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_TILE_ID;
    }

    @Override
    public String getPATH_GRANULE_METADATA_HORIZONTAL_CS_NAME() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_HORIZONTAL_CS_NAME;
    }

    @Override
    public String getPATH_GRANULE_METADATA_HORIZONTAL_CS_CODE() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_HORIZONTAL_CS_CODE;
    }

    @Override
    public String getPATH_GRANULE_METADATA_SIZE_RESOLUTION() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_SIZE_RESOLUTION;
    }

    @Override
    public String getPATH_GRANULE_METADATA_SIZE_NROWS() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_SIZE_NROWS;
    }

    @Override
    public String getPATH_GRANULE_METADATA_SIZE_NCOLS() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_SIZE_NCOLS;
    }

    @Override
    public String getPATH_GRANULE_METADATA_GEOPOSITION_RESOLUTION() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_GEOPOSITION_RESOLUTION;
    }

    @Override
    public String getPATH_GRANULE_METADATA_GEOPOSITION_ULX() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_GEOPOSITION_ULX;
    }

    @Override
    public String getPATH_GRANULE_METADATA_GEOPOSITION_ULY() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_GEOPOSITION_ULY;
    }

    @Override
    public String getPATH_GRANULE_METADATA_GEOPOSITION_XDIM() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_GEOPOSITION_XDIM;
    }

    @Override
    public String getPATH_GRANULE_METADATA_GEOPOSITION_YDIM() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_GEOPOSITION_YDIM;
    }

    @Override
    public String getPATH_GRANULE_METADATA_ANGLE_RESOLUTION() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_ANGLE_RESOLUTION;
    }

    @Override
    public String getPATH_GRANULE_METADATA_SUN_ZENITH_ANGLES() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_SUN_ZENITH_ANGLES;
    }

    @Override
    public String getPATH_GRANULE_METADATA_SUN_AZIMUTH_ANGLES() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_SUN_AZIMUTH_ANGLES;
    }

    @Override
    public String getPATH_GRANULE_METADATA_VIEWING_ZENITH_ANGLES() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_VIEWING_ZENITH_ANGLES;
    }

    @Override
    public String getPATH_GRANULE_METADATA_VIEWING_AZIMUTH_ANGLES() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_VIEWING_AZIMUTH_ANGLES;
    }

    @Override
    public String getPATH_GRANULE_METADATA_VIEWING_BAND_ID() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_VIEWING_BAND_ID;
    }

    @Override
    public String getPATH_GRANULE_METADATA_MASK_FILENAME() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_MASK_FILENAME;
    }

    @Override
    public String getPATH_GRANULE_METADATA_MASK_TYPE() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_MASK_TYPE;
    }

    @Override
    public String getPATH_GRANULE_METADATA_MASK_BAND() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_MASK_BAND;
    }

    @Override
    public String getPATH_GRANULE_METADATA_PVI_FILENAME() {
        return L2aPSD13Constants.PATH_GRANULE_METADATA_PVI_FILENAME;
    }

    @Override
    public String getPATH_PRODUCT_METADATA_BOA_ADD_OFFSET_VALUES_LIST() {
        return null;
    }
}
