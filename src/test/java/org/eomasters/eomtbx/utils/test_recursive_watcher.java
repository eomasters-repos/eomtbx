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

public class test_recursive_watcher {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("[DEBUG_LOG] Testing recursive DirectoryWatcher functionality");
        
        // Create test directory structure
        Path testRoot = Paths.get("test_recursive_watch");
        Path subDir1 = testRoot.resolve("subdir1");
        Path subDir2 = testRoot.resolve("subdir2");
        Path deepDir = subDir1.resolve("deep");
        
        // Clean up if exists
        if (Files.exists(testRoot)) {
            deleteRecursively(testRoot);
        }
        
        Files.createDirectories(deepDir);
        Files.createDirectories(subDir2);
        System.out.println("[DEBUG_LOG] Created test directories");
        
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
        
        // Start recursive watching
        watcher.startWatching(testRoot);
        Thread.sleep(1000);
        
        // Test 1: Create file in root directory (should be detected)
        Path rootFile = testRoot.resolve("root_file.txt");
        Files.createFile(rootFile);
        System.out.println("[DEBUG_LOG] Created file in root: " + rootFile);
        Thread.sleep(1000);
        
        // Test 2: Create file in first level subdirectory (should be detected)
        Path subFile1 = subDir1.resolve("sub1_file.txt");
        Files.createFile(subFile1);
        System.out.println("[DEBUG_LOG] Created file in subdir1: " + subFile1);
        Thread.sleep(1000);
        
        // Test 3: Create file in second level subdirectory (should be detected)
        Path deepFile = deepDir.resolve("deep_file.txt");
        Files.createFile(deepFile);
        System.out.println("[DEBUG_LOG] Created file in deep directory: " + deepFile);
        Thread.sleep(1000);
        
        // Test 4: Create new subdirectory (should be detected)
        Path newSubDir = testRoot.resolve("dynamic_subdir");
        Files.createDirectories(newSubDir);
        System.out.println("[DEBUG_LOG] Created new subdirectory: " + newSubDir);
        Thread.sleep(1000);
        
        // Test 5: Create file in dynamically created subdirectory (should be detected)
        Path dynamicFile = newSubDir.resolve("dynamic_file.txt");
        Files.createFile(dynamicFile);
        System.out.println("[DEBUG_LOG] Created file in dynamic subdirectory: " + dynamicFile);
        Thread.sleep(1000);
        
        // Test 6: Create nested directory structure dynamically
        Path nestedDir = newSubDir.resolve("nested");
        Files.createDirectories(nestedDir);
        System.out.println("[DEBUG_LOG] Created nested directory: " + nestedDir);
        Thread.sleep(1000);
        
        Path nestedFile = nestedDir.resolve("nested_file.txt");
        Files.createFile(nestedFile);
        System.out.println("[DEBUG_LOG] Created file in nested directory: " + nestedFile);
        Thread.sleep(2000);
        
        watcher.stopWatching();
        watcher.shutdown();
        
        // Cleanup
        deleteRecursively(testRoot);
        System.out.println("[DEBUG_LOG] Test completed - All subdirectory events should have been detected");
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
