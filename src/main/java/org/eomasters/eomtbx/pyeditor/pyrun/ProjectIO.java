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

package org.eomasters.eomtbx.pyeditor.pyrun;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.eomasters.eomtbx.pyeditor.Properties;

/**
 * Utility class for serializing and deserializing Project objects to/from JSON files.
 */
public class ProjectIO {

  public static final String PROJECT_FILE_EXTENSION = ".scp";
  private static final Gson gson = new GsonBuilder()
      .setPrettyPrinting()
      .registerTypeHierarchyAdapter(Path.class, new PathTypeAdapter())
      .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
      .create();

  public static void saveProject(Project project) throws IOException {
    Path filePath = project.getProjectFile();
    if (filePath == null) {
      filePath = Properties.getUserProjectsDirectory().resolve(project.getName()).resolve(project.getName() + PROJECT_FILE_EXTENSION);
    }
    try (FileWriter writer = new FileWriter(filePath.toFile())) {
      project.updateLastUsed();
      gson.toJson(project, writer);
    }
  }

  /**
   * Saves a Project object to a JSON file.
   *
   * @param project  the Project object to save
   * @param filePath the path to the file where the project will be saved
   * @throws IOException if an I/O error occurs
   */
  public static void saveProject(Project project, Path filePath) throws IOException {
    try (FileWriter writer = new FileWriter(filePath.toFile())) {
      gson.toJson(project, writer);
    }
  }

  /**
   * Loads a Project object from a JSON file.
   *
   * @param filePath the path to the file containing the project data
   * @return the loaded Project object
   * @throws IOException if an I/O error occurs
   */
  public static Project loadProject(Path filePath) throws IOException {
    try (FileReader reader = new FileReader(filePath.toFile())) {
      var project = gson.fromJson(reader, Project.class);
      if (project != null) {
        project.setProjectFile(filePath);
      }
      return project;
    }
  }

  /**
   * Custom TypeAdapter for serializing and deserializing Path objects. This adapter converts Path objects to strings
   * during serialization and back to Path objects during deserialization.
   */
  private static class PathTypeAdapter extends TypeAdapter<Path> {

    @Override
    public void write(JsonWriter out, Path value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value.toString());
      }
    }

    @Override
    public Path read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      String pathString = in.nextString();
      return Paths.get(pathString);
    }
  }

  private static class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value(value.format(FORMATTER));
      }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
      if (in.peek() == JsonToken.NULL) {
        in.nextNull();
        return null;
      }
      return LocalDateTime.parse(in.nextString(), FORMATTER);
    }
  }

}
