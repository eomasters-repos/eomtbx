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

import org.eomasters.eomtbx.utils.ServiceRegistry;

/**
 * This class defines the registry for {@link AssetType}. There is only a single instance available which can be
 * retrieved by calling {@code #instance()}
 */
public class AssetTypeRegistry extends ServiceRegistry<AssetType> {


  /**
   * Retrieves the single instance of this registry.
   *
   * @return the instance of AssetTypeRegistry
   */
  public static AssetTypeRegistry instance() {
    return Holder.instance;
  }

  /**
   * The internal constructor only protected for testing
   */
  protected AssetTypeRegistry() {
    super();
  }

  @Override
  protected Class<AssetType> getServiceType() {
    return AssetType.class;
  }

  private static class Holder {

    private static final AssetTypeRegistry instance = new AssetTypeRegistry();
  }

}
