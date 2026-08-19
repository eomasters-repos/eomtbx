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

package org.eomasters.eomtbx.assets.gui;

import org.eomasters.eomtbx.assets.AssetCreationService;
import org.eomasters.eomtbx.utils.ServiceRegistry;

/**
 * This class defines the registry for {@link AssetCreationGuiService}. There is only a single instance available which can be
 * retrieved by calling {@code #instance()}
 */
public class AssetCreationGuiRegistry extends ServiceRegistry<AssetCreationGuiService> {

  /**
   * Retrieves the single instance of this registry.
   *
   * @return the instance of AssetFactoryGuiRegistry
   */
  public static AssetCreationGuiRegistry instance() {
    return Holder.instance;
  }


  /**
   * Retrieves the {@link AssetCreationGuiService} instance for the given {@link AssetCreationService}.
   *
   * @param factory the {@link AssetCreationService} for which the {@link AssetCreationGuiService} instance needs to be retrieved
   * @return the {@link AssetCreationGuiService} instance for the given {@link AssetCreationService}
   */
  public AssetCreationGuiService getUiFor(AssetCreationService factory) {
    for (AssetCreationGuiService ui : services.values()) {
      if (ui.getFactoryType().equals(factory.getClass())) {
        return ui;
      }
    }
    return new AssetCreationGuiServiceDefault();
  }

  @Override
  protected Class<AssetCreationGuiService> getServiceType() {
    return AssetCreationGuiService.class;
  }

  private static class Holder {

    private static final AssetCreationGuiRegistry instance = new AssetCreationGuiRegistry();
  }

}
