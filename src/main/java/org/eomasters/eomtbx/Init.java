/*-
 * ========================LICENSE_START=================================
 * EOMTBX - EOMasters Toolbox for SNAP
 * --> https://www.eomasters.org/sw/EOMTBX
 * ======================================================================
 * Copyright (C) 2023 Marco Peters
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
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * =========================LICENSE_END==================================
 */

package org.eomasters.eomtbx;

import org.eomasters.eomtbx.quickmenu.QuickMenu;
import org.eomasters.eomtbx.quickmenu.SnapMenuAccessor;
import org.openide.modules.OnStart;
import org.openide.windows.OnShowing;

public class Init {

  @OnStart
  public static class OnStartOperation implements Runnable {

    @Override
    public void run() {
      new Thread(() -> QuickMenu.getInstance().start()).start();
    }
  }

  @OnShowing
  public static class OnShowingOperation implements Runnable {

    @Override
    public void run() {
      new Thread(SnapMenuAccessor::initClickCounter).start();
    }

  }
}
