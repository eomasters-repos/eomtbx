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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DirectoryWatcher {

  private final ExecutorService executor;
  private final List<DirectoryChangeListener> listeners;
  private final Map<WatchKey, Path> watchKeys;
  private WatchService watchService;
  private volatile boolean watching;
  private CompletableFuture<Void> watchingTask;

  public DirectoryWatcher() {
    this.executor = Executors.newSingleThreadExecutor();
    this.listeners = new CopyOnWriteArrayList<>();
    this.watchKeys = new ConcurrentHashMap<>();
  }

  /**
   * Add a listener for directory change events
   */
  public void addListener(DirectoryChangeListener listener) {
    listeners.add(listener);
  }

  /**
   * Remove a listener for directory change events
   */
  public void removeListener(DirectoryChangeListener listener) {
    listeners.remove(listener);
  }

  /**
   * Start watching the specified directory recursively
   */
  public void startWatching(Path directoryPath) throws IOException{
    if (watching) {
      throw new IllegalStateException("Watcher is already running");
    }

    if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
      throw new IllegalArgumentException("Directory does not exist: " + directoryPath);
    }

    watchService = FileSystems.getDefault().newWatchService();
    watchKeys.clear();
    
    // Register directory and all subdirectories recursively
    registerRecursively(directoryPath);

    watching = true;

    watchingTask = CompletableFuture.runAsync(() -> {
      try {
        fireWatchingStarted(directoryPath);

        while (watching) {
          WatchKey key = watchService.take();
          Path directory = watchKeys.get(key);

          if (directory == null) {
            continue;
          }

          for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();

            if (kind == StandardWatchEventKinds.OVERFLOW) {
              continue;
            }

            Path filename = (Path) event.context();
            Path fullPath = directory.resolve(filename);

            DirectoryChangeEvent changeEvent = new DirectoryChangeEvent(
                fullPath, mapEventType(kind));

            fireDirectoryChanged(changeEvent);

            // If a new directory was created, register it recursively
            if (kind == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(fullPath)) {
              try {
                registerRecursively(fullPath);
              } catch (IOException e) {
                System.err.println("Failed to register new directory: " + fullPath + " - " + e.getMessage());
              }
            }
          }

          boolean valid = key.reset();
          if (!valid) {
            watchKeys.remove(key);
            break;
          }
        }

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        fireWatchingStopped();
      } catch (Exception e) {
        fireWatchingError(e);
      } finally {
        fireWatchingStopped();
      }
    }, executor);
  }

  /**
   * Stop watching the directory
   */
  public void stopWatching() throws IOException {
    watching = false;
    if (watchService != null) {
      watchService.close();
    }
    if (watchingTask != null) {
      watchingTask.cancel(true);
    }
    watchKeys.clear();
  }

  /**
   * Checks whether the watcher is currently active and monitoring a directory.
   *
   * @return true if the watcher is actively monitoring a directory, false otherwise.
   */
  public boolean isWatching() {
    return watching;
  }

  /**
   * Shutdown the watcher and cleanup resources
   */
  public void shutdown() throws IOException {
    stopWatching();
    executor.shutdown();
  }

  /**
   * Register the given directory and all its subdirectories recursively
   */
  private void registerRecursively(Path directory) throws IOException {
    // Register the directory itself
    WatchKey watchKey = directory.register(watchService,
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY);
    watchKeys.put(watchKey, directory);

    // Register all subdirectories recursively
    if (Files.isDirectory(directory)) {
      try {
        Files.list(directory).forEach(path -> {
          if (Files.isDirectory(path)) {
            try {
              registerRecursively(path);
            } catch (IOException e) {
              System.err.println("Failed to register directory: " + path + " - " + e.getMessage());
            }
          }
        });
      } catch (IOException e) {
        System.err.println("Failed to list directory: " + directory + " - " + e.getMessage());
      }
    }
  }

  private DirectoryChangeEvent.EventType mapEventType(WatchEvent.Kind<?> kind) {
    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
      return DirectoryChangeEvent.EventType.FILE_CREATED;
    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
      return DirectoryChangeEvent.EventType.FILE_DELETED;
    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
      return DirectoryChangeEvent.EventType.FILE_MODIFIED;
    }
    return DirectoryChangeEvent.EventType.FILE_MODIFIED;
  }

  // Event firing methods
  private void fireDirectoryChanged(DirectoryChangeEvent event) {
    for (DirectoryChangeListener listener : listeners) {
      try {
        listener.onDirectoryChanged(event);
      } catch (Exception e) {
        // Log error but continue notifying other listeners
        System.err.println("Error in listener: " + e.getMessage());
      }
    }
  }

  private void fireWatchingStarted(Path directory) {
    for (DirectoryChangeListener listener : listeners) {
      try {
        listener.onWatchingStarted(directory);
      } catch (Exception e) {
        System.err.println("Error in listener: " + e.getMessage());
      }
    }
  }

  private void fireWatchingStopped() {
    for (DirectoryChangeListener listener : listeners) {
      try {
        listener.onWatchingStopped();
      } catch (Exception e) {
        System.err.println("Error in listener: " + e.getMessage());
      }
    }
  }

  private void fireWatchingError(Exception error) {
    for (DirectoryChangeListener listener : listeners) {
      try {
        listener.onWatchingError(error);
      } catch (Exception e) {
        System.err.println("Error in listener: " + e.getMessage());
      }
    }
  }

  // Listener interface
  public interface DirectoryChangeListener {

    /**
     * Called when a file or directory change is detected
     */
    void onDirectoryChanged(DirectoryChangeEvent event);

    /**
     * Called when watching starts successfully
     */
    default void onWatchingStarted(Path directory) {
      // Default empty implementation
    }

    /**
     * Called when watching stops
     */
    default void onWatchingStopped() {
      // Default empty implementation
    }

    /**
     * Called when an error occurs during watching
     */
    default void onWatchingError(Exception error) {
      // Default empty implementation
    }
  }

  // Event class
  public static class DirectoryChangeEvent {

    private final Path path;
    private final EventType eventType;
    private final long timestamp;

    public DirectoryChangeEvent(Path path, EventType eventType) {
      this.path = path;
      this.eventType = eventType;
      this.timestamp = System.currentTimeMillis();
    }

    public Path getPath() {return path;}

    public EventType getEventType() {return eventType;}

    public long getTimestamp() {return timestamp;}

    public boolean isFileCreated() {return eventType == EventType.FILE_CREATED;}

    public boolean isFileDeleted() {return eventType == EventType.FILE_DELETED;}

    public boolean isFileModified() {return eventType == EventType.FILE_MODIFIED;}

    @Override
    public String toString() {
      return String.format("%s: %s (at %d)", eventType, path, timestamp);
    }

    public enum EventType {
      FILE_CREATED, FILE_DELETED, FILE_MODIFIED
    }
  }
}
