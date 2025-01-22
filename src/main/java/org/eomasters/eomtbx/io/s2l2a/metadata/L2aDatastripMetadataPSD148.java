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
import eu.esa.opt.dataio.s2.l2a.L2aPSD148Constants;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.metadata.GenericXmlMetadata;
import org.esa.snap.core.metadata.XmlMetadataParser;
import org.xml.sax.SAXException;


/**
 * Created by fdouziech 15/10/2021
 */

public class L2aDatastripMetadataPSD148 extends GenericXmlMetadata implements IL2aDatastripMetadata {

    private static class L2aDatastripMetadataPSD148Parser extends XmlMetadataParser<L2aDatastripMetadataPSD148> {

        public L2aDatastripMetadataPSD148Parser(Class metadataFileClass) {
            super(metadataFileClass);
            setSchemaLocations(L2aPSD148Constants.getDatastripSchemaLocations());
            setSchemaBasePath(L2aPSD148Constants.getDatastripSchemaBasePath());
        }

        @Override
        protected boolean shouldValidateSchema() {
            return false;
        }
    }

    public static L2aDatastripMetadataPSD148 create(VirtualPath path) throws IOException, ParserConfigurationException, SAXException {
        Assert.notNull(path);
        L2aDatastripMetadataPSD148 result = null;
        InputStream stream = null;
        try {
            if (path.exists()) {
                stream = path.getInputStream();
                L2aDatastripMetadataPSD148Parser parser = new L2aDatastripMetadataPSD148Parser(L2aDatastripMetadataPSD148.class);
                result = parser.parse(stream);
                result.setName("Level-2A_DataStrip_ID");
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return result;
    }

    public L2aDatastripMetadataPSD148(String name) {
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
    public MetadataElement getMetadataElement() {
        return rootElement;
    }
}
