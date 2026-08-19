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

package org.eomasters.eomtbx.spex;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import org.eomasters.eomtbx.EomToolbox;
import org.eomasters.utils.JsonUtils;
import org.jdesktop.swingx.event.WeakEventListenerList;

public class SpexDb {

  private static final String USER_SPECTRAL_INDICES = "User Spectral Indices";
  protected static final String SPEX_DB_PREF_NODE = "spexDB";
  private static final String USER_SPEX_PREF = "userSpex";
  private final Preferences preferences;
  private final List<Map<String, ? extends AbstractSpex>> spexMapList;
  private final WeakEventListenerList changeListenerList;
  private final InternalPreferenceChangeListener preferenceChangeListener;
  private final Map<String, AbstractSpex> userSpexMap;
  private final Object spexSyncToken = new Object();

  public static SpexDb getInstance() {
    return Holder.INSTANCE;
  }

  private SpexDb(Preferences preferences) throws IOException {
    this.preferences = preferences;
    spexMapList = Collections.synchronizedList(new ArrayList<>());
    spexMapList.add(loadDatabase("awesome_v090.json"));
    spexMapList.add(loadDatabase("idb_202408.json"));
    spexMapList.add(loadDatabase("eomasters_202507.json"));
    userSpexMap = Collections.synchronizedMap(loadUser(preferences));

    preferenceChangeListener = new InternalPreferenceChangeListener();
    preferences.addPreferenceChangeListener(preferenceChangeListener);
    changeListenerList = new WeakEventListenerList();
  }

  public boolean contains(String spexName) {
    synchronized (spexSyncToken) {
      for (Map<String, ? extends AbstractSpex> spexMap : spexMapList) {
        if (spexMap.containsKey(spexName)) {
          return true;
        }
      }
      return userSpexMap.containsKey(spexName);
    }
  }

  public AbstractSpex get(String spexName) {
    synchronized (spexSyncToken) {
      for (Map<String, ? extends AbstractSpex> spexMap : spexMapList) {
        if (spexMap.containsKey(spexName)) {
          return spexMap.get(spexName);
        }
      }
      return userSpexMap.get(spexName);
    }
  }

  static SpexDb create(Preferences preferences) throws IOException {
    return new SpexDb(preferences);
  }

  public void addIndex(AbstractSpex index) {
    index.setSourceName(USER_SPECTRAL_INDICES);
    synchronized (spexSyncToken) {
      userSpexMap.put(index.getName(), index);
      preferences.put(USER_SPEX_PREF, getJson(userSpexMap));
    }
  }

  public void removeIndex(AbstractSpex index) {
    synchronized (spexSyncToken) {
      if (userSpexMap.remove(index.getName()) != null) {
        preferences.put(USER_SPEX_PREF, getJson(userSpexMap));
      }
    }
  }

  public boolean isUserSpex(AbstractSpex index) {
    synchronized (spexSyncToken) {
      return userSpexMap.containsKey(index.getName());
    }
  }

  private String getJson(Map<String, AbstractSpex> map) {
    return new Gson().toJson(Map.copyOf(map));
  }

  private static Map<String, AbstractSpex> loadUser(Preferences preferences) throws IOException {
    String customSpex = preferences.get(USER_SPEX_PREF, null);
    Map<String, DbSpex> custom = new HashMap<>();
    if (customSpex != null) {
      custom = JsonUtils.readMap(new ByteArrayInputStream(customSpex.getBytes(StandardCharsets.UTF_8)),
          new TypeToken<>() {
          });
      ((Map<String, ? extends DbSpex>) custom).values().forEach(index -> {
        index.setSourceName(USER_SPECTRAL_INDICES);
        index.setSourceUrl(null);
      });
    }
    // needed to fulfill the contract of lists and generics when adding and retrieving / I don't understand
    Map<String, AbstractSpex> mapped = new HashMap<>();
    custom.values().forEach(spex -> mapped.put(spex.getName(), spex));
    return mapped;
  }

  private static Map<String, DbSpex> loadDatabase(String name) throws IOException {
    Map<String, DbSpex> stringDbSpexMap = JsonUtils.readMap(DbSpex.class.getResourceAsStream(name),
        new TypeToken<>() {
        });
    Collection<DbSpex> indices = stringDbSpexMap.values();
    for (DbSpex index : indices) {
      if (!index.isValid()) {
        throw new IOException(String.format("Invalid spectral index read: %s", index));
      }
    }
    return stringDbSpexMap;
  }

  private void fireSpexDbChanged() {
    for (ChangeListener changeListener : changeListenerList.getListeners(ChangeListener.class)) {
      changeListener.spexDbChanged();
    }
  }

  public void addChangeListener(ChangeListener changeListener) {
    changeListenerList.add(ChangeListener.class, changeListener);
  }

  public void removeChangeListener(ChangeListener changeListener) {
    changeListenerList.remove(ChangeListener.class, changeListener);
  }

  public Collection<AbstractSpex> getIndices() {
    List<AbstractSpex> collected;
    synchronized (spexSyncToken) {
      collected = spexMapList.stream()
                             .parallel()
                             .flatMap(spexMap -> spexMap.values().stream())
                             .collect(Collectors.toList());
      collected.addAll(userSpexMap.values());
    }
    return collected;
  }

  public void close() throws IOException {
    try {
      preferences.flush();
    } catch (BackingStoreException e) {
      throw new IOException(e);
    }
    preferences.removePreferenceChangeListener(preferenceChangeListener);
  }


  private static class Holder {

    private static final SpexDb INSTANCE;

    static {
      try {
        INSTANCE = create(EomToolbox.getPreferences().node(SPEX_DB_PREF_NODE));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public interface ChangeListener extends EventListener {

    void spexDbChanged();
  }

  private class InternalPreferenceChangeListener implements PreferenceChangeListener {

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
      if (evt.getKey().equals(USER_SPEX_PREF)) {
        try {
          Map<String, AbstractSpex> userIndices = loadUser(preferences);
          userSpexMap.clear();
          userSpexMap.putAll(userIndices);
          fireSpexDbChanged();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }
}
