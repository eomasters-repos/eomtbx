package org.eomasters.eomtbx.utils;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class test_directory_watcher {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Create test directory structure
        Path testRoot = Paths.get("test_watch_dir");
        Path subDir = testRoot.resolve("subdir");
        
        // Clean up if exists
        if (Files.exists(testRoot)) {
            deleteRecursively(testRoot);
        }
        
        Files.createDirectories(subDir);
        System.out.println("[DEBUG_LOG] Created test directories: " + testRoot + " and " + subDir);
        
        DirectoryWatcher watcher = new DirectoryWatcher();
        watcher.addListener(new DirectoryWatcher.DirectoryChangeListener() {
            @Override
            public void onDirectoryChanged(DirectoryWatcher.DirectoryChangeEvent event) {
                System.out.println("[DEBUG_LOG] Event detected: " + event);
            }
            
            @Override
            public void onWatchingStarted(Path directory) {
                System.out.println("[DEBUG_LOG] Started watching: " + directory);
            }
        });
        
        // Start watching only the root directory
        watcher.startWatching(testRoot);
        Thread.sleep(1000); // Give watcher time to start
        
        // Test 1: Create file in root directory (should be detected)
        Path rootFile = testRoot.resolve("root_file.txt");
        Files.createFile(rootFile);
        System.out.println("[DEBUG_LOG] Created file in root: " + rootFile);
        Thread.sleep(1000);
        
        // Test 2: Create file in subdirectory (should NOT be detected with current implementation)
        Path subFile = subDir.resolve("sub_file.txt");
        Files.createFile(subFile);
        System.out.println("[DEBUG_LOG] Created file in subdirectory: " + subFile);
        Thread.sleep(1000);
        
        // Test 3: Create new subdirectory (should be detected as creation)
        Path newSubDir = testRoot.resolve("new_subdir");
        Files.createDirectories(newSubDir);
        System.out.println("[DEBUG_LOG] Created new subdirectory: " + newSubDir);
        Thread.sleep(1000);
        
        // Test 4: Create file in new subdirectory (should NOT be detected)
        Path newSubFile = newSubDir.resolve("new_sub_file.txt");
        Files.createFile(newSubFile);
        System.out.println("[DEBUG_LOG] Created file in new subdirectory: " + newSubFile);
        Thread.sleep(2000);
        
        watcher.stopWatching();
        watcher.shutdown();
        
        // Cleanup
        deleteRecursively(testRoot);
        System.out.println("[DEBUG_LOG] Test completed - cleaned up test directories");
    }
    
    private static void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.list(path).forEach(child -> {
                try {
                    deleteRecursively(child);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        Files.deleteIfExists(path);
    }
}
