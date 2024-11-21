/*-
 * ========================LICENSE_START=================================
 * EOMTBXP Toolbox Module - Toolbox Module for the EOMasters Pro Toolbox for SNAP
 * -> https://www.eomasters.org/eomtbx/modules/eomtbxp-toolbox
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

/*
 * Copyright (C) 2014-2015 CS-SI (foss-contact@thor.si.c-s.fr)
 * Copyright (C) 2013-2015 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.eomasters.eomtbx.io.s2l2a;

import eu.esa.opt.dataio.s2.VirtualPath;
import eu.esa.opt.dataio.s2.masks.MaskInfo;
import java.io.IOException;
import org.eomasters.eomtbx.io.s2l2a.metadata.S2L2aProductMetadataReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;

public class EomS2L2AProductReader extends EomS2OrthoProductReader {

    private static final String L2A_CACHE_DIR = "l2a-reader";

    public EomS2L2AProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected S2L2aProductMetadataReader buildMetadataReader(VirtualPath virtualPath) throws IOException {
        String epsgCode = EomS2L2AUtils.getEpsgCode(virtualPath);
        return new S2L2aProductMetadataReader(virtualPath, epsgCode);
    }

    @Override
    protected String getReaderCacheDir() {
        return L2A_CACHE_DIR;
    }

    @Override
    protected int getMaskLevel() {
        return MaskInfo.L2A;
    }
}
