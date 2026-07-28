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

package org.eomasters.eomtbx.assets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.eomasters.eomtbx.spex.TestPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssetLibraryEventTest {

    private AssetLibrary assetLibrary;
    private AssetLibrary.Listener assetListener;

    @BeforeEach
    void setUp() throws IOException {
        assetLibrary = AssetLibrary.create(new TestPreferences());
        assetListener = mock(AssetLibrary.Listener.class);
        assetLibrary.addListener(assetListener);
    }

    @Test
    void shouldNotifyListenersWhenAssetIsAdded() {
        Asset mockAsset = mock(Asset.class);
        when(mockAsset.getTags()).thenAnswer(invocationOnMock -> new String[0]);
        
        assetLibrary.add(mockAsset);
        
        verify(assetListener, times(1)).onAssetsAdded(any());
    }

    @Test
    void shouldNotifyListenersWhenAssetIsRemoved() {
        Asset mockAsset = mock(Asset.class);
        when(mockAsset.getTags()).thenAnswer(invocationOnMock -> new String[0]);

        assetLibrary.add(mockAsset);

        assetLibrary.remove(mockAsset);

        verify(assetListener, times(1)).onAssetsRemoved(any());

        assetLibrary.remove(mockAsset);
        verify(assetListener, times(1)).onAssetsRemoved(any());
     }
}
