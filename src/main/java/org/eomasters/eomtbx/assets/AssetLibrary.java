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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import org.eomasters.eomtbx.EomToolbox;
import org.eomasters.eomtbx.utils.OccurrenceCounter;
import org.eomasters.utils.ErrorHandler;

public class AssetLibrary implements Iterable<Asset> {

  public static final String HELP_ID = "eomtbx.assetLibrary";

  private static final String LIBRARY_NODE = "assetLibrary";
  private static final Preferences LIBRARY_PREFERENCES = EomToolbox.getPreferences().node(LIBRARY_NODE);
  private static final String ASSETS_KEY = "assets";

  private final ArrayList<Asset> assets;
  private final OccurrenceCounter<String> tags;
  private final transient List<Listener> listeners;
  private final transient Preferences preferences;
  private final transient AssetStore store;
  private final transient InternalPreferenceChangeListener preferenceChangeListener;


  public static AssetLibrary getInstance() {
    return AssetLibrary.Holder.INSTANCE;
  }

  /**
   * Creates a new empty library
   *
   * @param preferences The library preferences where to store the assets
   */
  private AssetLibrary(Preferences preferences) {
    this.preferences = preferences;
    preferenceChangeListener = new InternalPreferenceChangeListener();
    this.assets = new ArrayList<>();
    this.tags = new OccurrenceCounter<>();
    listeners = new ArrayList<>();
    store = new AssetStore();
    preferences.addPreferenceChangeListener(preferenceChangeListener);
  }

  // for tests
  static AssetLibrary create(Preferences preferences) throws IOException {
    AssetLibrary assetLibrary = new AssetLibrary(preferences);
    String json = preferences.get(ASSETS_KEY, null);
    if (json != null) {
      try {
        assetLibrary.setAssets(new AssetStore().fromJson(json));
        return assetLibrary;
      } catch (Throwable t) {
        ErrorHandler.handleError("Asset Library", "Could not restore asset library.", t);
      }
    }
    return assetLibrary;
  }

  /**
   * Retrieves a list of assets in the library.
   *
   * @return A list of assets in the library.
   */
  public List<Asset> getAssets() {
    return Collections.unmodifiableList(assets);
  }

  /**
   * Retrieves a list of assets from the asset library based on a search text.
   *
   * @param searchText The text to search for in the assets. Case-insensitive.
   * @return A list of assets that contain the search text in their name, description, tags, or user notes.
   */
  public List<Asset> getAssets(String searchText) {
    return assets.stream()
                 .filter(asset -> assetContains(searchText, asset))
                 .collect(Collectors.toList());
  }

  private static boolean assetContains(String query, Asset asset) {
    return textContains(asset.getName(), query)
        || textContains(asset.getDescription(), query)
        || tagsContain(asset, query)
        || textContains(asset.getUserNotes(), query);
  }

  private static boolean tagsContain(Asset asset, String query) {
    return Arrays.stream(asset.getTags()).anyMatch(tag -> textContains(tag, query));
  }

  private static boolean textContains(String text, String query) {
    return text.toLowerCase().contains(query.toLowerCase());
  }


  /**
   * @return The number of assets this library has
   */
  public int size() {
    return assets.size();
  }

  /**
   * @return true, if the library is empty
   */
  public boolean isEmpty() {
    return assets.isEmpty();
  }

  /**
   * Retrieves the set of tags used in the library.
   *
   * @return A set of strings representing the tags.
   */
  public Set<String> getTags() {
    return tags.getValues();
  }

  /**
   * Checks if an Asset is present in the Asset Library. The comparison is based on the name of the asset and is not
   * case-sensitive.
   *
   * @param asset The Asset to check for existence.
   * @return True if an Asset with the same name (ignoring case) is present in the AssetLibrary, false otherwise.
   */
  public boolean contains(final Asset asset) {
    return contains(asset.getName());
  }


  /**
   * Checks if a given asset name is present in the Asset Library. The comparison is case-insensitive.
   *
   * @param assetName The name of the asset to check for existence.
   * @return true if an asset with the same name (ignoring case) is present in the Asset Library, false otherwise.
   */
  public boolean contains(String assetName) {
    return assets.parallelStream().anyMatch(res -> res.getName().equalsIgnoreCase(assetName));
  }

  @Override
  public Iterator<Asset> iterator() {
    return assets.iterator();
  }

  /**
   * Retrieves the {@link Asset} at the specified index.
   *
   * @param index The index of the {@link Asset} to retrieve.
   * @return The {@link Asset} at the specified index.
   */
  public Asset get(final int index) {
    return assets.get(index);
  }


  /**
   * This method is used to add a new asset to the library. If the asset already exists, no action is taken, and false
   * is returned. If the asset does not exist, it is added to the library and its tags are added to the tag list of the
   * library, and true is returned.
   *
   * @param asset The Asset object to be added to the library
   * @return true if the asset was added successfully, false if the asset already exists
   */
  public boolean add(final Asset asset) {
    if (!addAsset(asset)) {
      return false;
    }
    notifyAddition(List.of(asset));
    return true;
  }

  /**
   * Tries to add all the assets to the asset library.
   *
   * @param assets The assets to be added.
   * @return true if at least one asset was added, false otherwise.
   */
  public boolean add(Asset... assets) {
    return add(List.of(assets));
  }

  /**
   * Tries to add all the assets from the given collection to the asset library.
   *
   * @param collection The collection of Asset objects to be added.
   * @return true if at least one asset was added, false otherwise.
   */
  public boolean add(Collection<Asset> collection) {
    List<Asset> addAssets = addAssets(collection);
    if (addAssets.isEmpty()) {
      return false;
    }
    notifyAddition(addAssets);
    return true;
  }

  /**
   * Sets the assets in the {@link AssetLibrary} by clearing the current assets and adding the specified list of
   * assets.
   *
   * @param assetList The list of assets to set in the library.
   */
  public void setAssets(List<Asset> assetList) {
    clear();
    add(assetList);
  }

  /**
   * Clears the library and removes all the assets.
   */
  public void clear() {
    removeAssets(List.copyOf(assets));
  }

  /**
   * Removes the asset from the library if it exists.
   *
   * @param asset the asset to remove from the library
   * @return true, if removal was successful
   */
  public boolean remove(final Asset asset) {
    if (asset == null) {
      return false;
    }
    boolean removed = removeAsset(asset);
    if (removed) {
      listeners.forEach(l -> l.onAssetsRemoved(List.of(asset)));
    }
    return removed;
  }

  /**
   * Removes all the assets in the provided collection from the library if they exist.
   *
   * @param collection the collection of Assets to be removed from the library
   * @return true, if any asset was successfully removed, false otherwise.
   */
  public boolean remove(Collection<Asset> collection) {
    List<Asset> removeAssets = removeAssets(collection);
    if (removeAssets.isEmpty()) {
      return false;
    }
    notifyRemoval(removeAssets);
    return true;
  }

  private List<Asset> addAssets(Collection<Asset> collection) {
    List<Asset> addedAssets = new ArrayList<>();
    for (Asset asset : collection) {
      if (addAsset(asset)) {
        addedAssets.add(asset);
      }
    }
    return addedAssets;
  }

  private boolean addAsset(Asset asset) {
    if (contains(asset)) {
      return false;
    }
    boolean added = assets.add(asset);
    tags.addAll(Arrays.asList(asset.getTags()));
    return added;
  }

  private List<Asset> removeAssets(Collection<Asset> collection) {
    List<Asset> removedAssets = new ArrayList<>();
    for (Asset asset : collection) {
      if (removeAsset(asset)) {
        removedAssets.add(asset);
      }
    }
    return removedAssets;
  }

  private boolean removeAsset(Asset asset) {
    boolean removed = assets.remove(asset);
    if (removed) {
      tags.removeAll(List.of(asset.getTags()));
    }
    return removed;
  }


  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  private void notifyAddition(List<Asset> addedAssets) {
    listeners.forEach(l -> l.onAssetsAdded(addedAssets));
  }

  private void notifyRemoval(List<Asset> removedAssets) {
    listeners.forEach(l -> l.onAssetsRemoved(removedAssets));
  }

  public void close() throws IOException {
    saveToPreferences();
    preferences.removePreferenceChangeListener(preferenceChangeListener);
  }

  public synchronized void saveToPreferences() {
    preferences.put(ASSETS_KEY, store.toJson(getAssets()));
  }


  private static class Holder {

    private static final AssetLibrary INSTANCE;

    static {
      try {
        INSTANCE = create(LIBRARY_PREFERENCES);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public interface Listener {

    void onAssetsAdded(List<Asset> assets);

    void onAssetsRemoved(List<Asset> assets);

    void onAssetChanged(Asset asset);
  }

  private class InternalPreferenceChangeListener implements PreferenceChangeListener {

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
      if (evt.getKey().equals(ASSETS_KEY)) {
        setAssets(store.fromJson(evt.getNewValue()));
      }
    }
  }

}
