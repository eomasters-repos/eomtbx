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

package org.eomasters.eomtbx.utils;

import static org.eomasters.utils.Exceptions.throwIf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

/**
 * A generic class for loading and holding registered services. Services are loaded by using a {@code ServiceLoader}.
 */
public abstract class ServiceRegistry<T extends Service> implements Iterable<T> {

  protected final Map<String, T> services;

  /**
   * Creates an instance and loads the services.
   */
  protected ServiceRegistry() {
    services = new TreeMap<>(String::compareToIgnoreCase);
    loadServices();
  }

  /**
   * Retrieves a list of the available services.
   *
   * @return a list of services
   */
  public List<T> getServices() {
    return new ArrayList<>(services.values());
  }

  /**
   * Retrieves the names of the available service.
   *
   * @return a list of service names
   */
  public List<String> getServiceNames() {
    return new ArrayList<>(services.keySet());
  }

  /**
   * Get a service by the given id.
   *
   * @param id the id of the service
   * @return the service
   */
  public T getService(String id) {
    return services.get(id);
  }

  /**
   * Determines if the collection of services contains a specific service.
   *
   * @param service the service to check for in the collection
   * @return true if the service is found, false otherwise
   */
  public boolean contains(T service) {
    return services.containsValue(service);
  }

  /**
   * @return the number of services
   */
  public int size() {
    return services.size();
  }

  @Override
  public Iterator<T> iterator() {
    return services.values().iterator();
  }

  /**
   * The type of the services registered.
   *
   * @return the service type
   */
  protected abstract Class<T> getServiceType();

  /**
   * The services are loaded  and stored in the registry.
   */
  protected void loadServices() {
    final Iterable<T> loadedServices = ServiceLoader.load(getServiceType());
    for (final T service : loadedServices) {
      throwIf(services.containsKey(service.getId()), new IllegalStateException(
          String.format("Service with ID '%s' already registered", service.getId())));
      services.put(service.getId(), service);
    }
  }
}
