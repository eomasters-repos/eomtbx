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

import java.util.ArrayList;
import java.util.List;
import org.eomasters.eomtbx.utils.ServiceRegistry;

/**
 * This class defines the registry of {@link AssetCreationService}s. There is only a single instance available which can be
 * retrieved by calling {@code #instance()}.
 */
public class AssetCreationRegistry extends ServiceRegistry<AssetCreationService> {


  /**
   * Provides access to the single instance of this registry.
   *
   * @return the registry instance.
   */
  public static AssetCreationRegistry instance() {
    return Holder.instance;
  }

  @Override
  protected Class<AssetCreationService> getServiceType() {
    return AssetCreationService.class;
  }

  public List<AssetCreationService> getFactories(AssetType type) {
    return getFactories(type.getClass());
  }

  public List<AssetCreationService> getFactories(Class<? extends AssetType> type) {
    ArrayList<AssetCreationService> supportingFactories = new ArrayList<>();
    for (AssetCreationService factory : services.values()) {
      if (factory.supports(type)) {
        supportingFactories.add(factory);
      }
    }
    return supportingFactories;
  }

  private static class Holder {

    private static final AssetCreationRegistry instance = new AssetCreationRegistry();
  }

}
