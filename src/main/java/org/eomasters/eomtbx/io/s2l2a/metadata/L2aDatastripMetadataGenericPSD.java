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

import com.bc.ceres.core.Assert;
import eu.esa.opt.dataio.s2.VirtualPath;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.metadata.GenericXmlMetadata;
import org.esa.snap.core.metadata.XmlMetadataParser;
import org.xml.sax.SAXException;

/**
 * Created by obarrile on 06/02/2018.
 */
public class L2aDatastripMetadataGenericPSD extends GenericXmlMetadata implements IL2aDatastripMetadata {

    private static class L2aDatastripMetadataGenericPSDParser extends XmlMetadataParser<L2aDatastripMetadataGenericPSD> {

        public L2aDatastripMetadataGenericPSDParser(Class metadataFileClass, IL2aMetadataPathsProvider metadataPathProvider) {
            super(metadataFileClass);
            setSchemaLocations(metadataPathProvider.getDatastripSchemaLocations());
            setSchemaBasePath(metadataPathProvider.getDatastripSchemaBasePath());
        }

        @Override
        protected boolean shouldValidateSchema() {
            return false;
        }
    }

    public static L2aDatastripMetadataGenericPSD create(VirtualPath path, IL2aMetadataPathsProvider metadataPathProvider) throws IOException, ParserConfigurationException, SAXException {
        Assert.notNull(path);
        L2aDatastripMetadataGenericPSD result = null;
        InputStream stream = null;
        try {
            if (path.exists()) {
                stream = path.getInputStream();
                L2aDatastripMetadataGenericPSDParser parser = new L2aDatastripMetadataGenericPSDParser(L2aDatastripMetadataGenericPSD.class, metadataPathProvider);
                result = parser.parse(stream);
                result.setName("Level-2A_DataStrip_ID");
                result.setMetadataPathsProvider(metadataPathProvider);
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return result;
    }

    private IL2aMetadataPathsProvider metadataPathProvider = null;

    private void setMetadataPathsProvider(
        IL2aMetadataPathsProvider metadataPathProvider) {
        this.metadataPathProvider = metadataPathProvider;
    }
    public L2aDatastripMetadataGenericPSD(String name) {
        super(name);
    }
    public L2aDatastripMetadataGenericPSD(String name, IL2aMetadataPathsProvider metadataPathProvider) {
        super(name);
        setMetadataPathsProvider(metadataPathProvider);
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
    public MetadataElement getMetadataElement() {
        return rootElement;
    }
}
