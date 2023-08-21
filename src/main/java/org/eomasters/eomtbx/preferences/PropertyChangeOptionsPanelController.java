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

package org.eomasters.eomtbx.preferences;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.spi.options.OptionsPanelController;

public abstract class PropertyChangeOptionsPanelController extends OptionsPanelController {

  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  protected void firePropertyChange(String propChanged, boolean oldValue, boolean newValue) {
    pcs.firePropertyChange(propChanged, oldValue, newValue);
  }

  private void firePropertyChange(String propChanged, int oldValue, int newValue) {
    pcs.firePropertyChange(propChanged, oldValue, newValue);
  }

  protected void firePropertyChange(String propChanged, Object oldValue, Object newValue) {
    pcs.firePropertyChange(propChanged, oldValue, newValue);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    pcs.addPropertyChangeListener(propertyChangeListener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    pcs.addPropertyChangeListener(propertyChangeListener);
  }
}
