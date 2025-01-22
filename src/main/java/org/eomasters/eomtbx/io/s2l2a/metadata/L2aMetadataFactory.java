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

import eu.esa.opt.dataio.s2.S2Metadata;
import eu.esa.opt.dataio.s2.VirtualPath;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Created by obarrile on 05/10/2016.
 * update 15/10/2021 fdouziech
 */
public class L2aMetadataFactory {
    public static IL2aProductMetadata createL2aProductMetadata(VirtualPath metadataPath) throws IOException, ParserConfigurationException, SAXException {
        int psd = S2Metadata.getFullPSDversion(metadataPath);
        if(psd == 14 || psd == 13 || psd == 12 || psd == 0 )  {
            return L2aProductMetadataGenericPSD.create(metadataPath, new L2aMetadataPathsProviderPSD13());
        } else if (psd == 143) {
            return L2aProductMetadataGenericPSD.create(metadataPath, new L2aMetadataPathsProviderPSD143());
        }else if (psd > 147 && psd < 150) {
            return L2aProductMetadataGenericPSD.create(metadataPath, new L2aMetadataPathsProviderPSD148());
        } else if (psd >= 150) {
            return L2aProductMetadataGenericPSD.create(metadataPath, new L2aMetadataPathsProviderPSD150());
        } else {
            //TODO
            return null;
        }
    }

    public static IL2aGranuleMetadata createL2aGranuleMetadata(VirtualPath metadataPath) throws IOException, ParserConfigurationException, SAXException {
        int psd = S2Metadata.getFullPSDversion(metadataPath);
        if(psd == 14 || psd == 13 || psd == 12 || psd == 0 )  {
            return L2aGranuleMetadataGenericPSD.create(metadataPath, new L2aMetadataPathsProviderPSD13());
        } else if (psd == 143) {
            return L2aGranuleMetadataGenericPSD.create(metadataPath, new L2aMetadataPathsProviderPSD143());
        } else if (psd > 147 && psd < 150) {
            return L2aGranuleMetadataGenericPSD.create(metadataPath, new L2aMetadataPathsProviderPSD148());
        } else if (psd >= 150) {
            return L2aGranuleMetadataGenericPSD.create(metadataPath, new L2aMetadataPathsProviderPSD150());
        } else {
            //TODO
            return null;
        }
    }


    public static IL2aDatastripMetadata createL2aDatastripMetadata(VirtualPath metadataPath) throws IOException, ParserConfigurationException, SAXException {
        int psd = S2Metadata.getFullPSDversion(metadataPath);
        if(psd == 14 || psd == 13 || psd == 12 || psd == 0 )  {
            return L2aDatastripMetadataGenericPSD.create(metadataPath, new L2aMetadataPathsProviderPSD13());
        } else if (psd == 143) {
            return L2aDatastripMetadataGenericPSD.create(metadataPath, new L2aMetadataPathsProviderPSD143());
        } else if (psd >147 && psd < 150) {
            return L2aDatastripMetadataGenericPSD.create(metadataPath, new L2aMetadataPathsProviderPSD148());
        } else if (psd >= 150) {
            return L2aDatastripMetadataGenericPSD.create(metadataPath, new L2aMetadataPathsProviderPSD150());
        } else {
            //TODO
            return null;
        }
    }


}
