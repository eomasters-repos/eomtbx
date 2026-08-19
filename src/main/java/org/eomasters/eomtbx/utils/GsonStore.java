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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

// TODO: move to eom-commons
/**
 * Generic implementation for storing objects as json text using the
 * <a href="https://github.com/google/gson/#readme">Gson library</a>.
 */
public class GsonStore<T> {

    private final Gson gson;
    private final Class<T> type;

    /**
     * Creates a GsonStore for the provided type. Delegates to {@link GsonStore(Class, GsonBuilderConfig )} with an empty
     * configuration definition
     * .
     * @param type the type of objects which shall be de-/serialized
     */
    public GsonStore(Class<T> type) {
        this(type, builder -> {/* empty */});
    }

    /**
     * Creates a GsonStore for the provided type. The configuration of the resulting json code can be
     * specified by providing an implementation of {@link GsonBuilderConfig}.
     * <p>
     * In addition to the default configuration, the underlying {@link GsonBuilder} is pre-configured
     * to use pretty-printing and exclude transient fields.
     *
     * @param type the type of objects which shall be de-/serialized
     * @param config used to configure the {@link GsonBuilder}
     */
    public GsonStore(Class<T> type, GsonBuilderConfig config) {
        this.type = type;
        final GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.excludeFieldsWithModifiers(Modifier.TRANSIENT);
        config.configureBuilder(builder);
        gson = builder.create();
    }

    /**
     * Loads an object of type T from a JSON file located at the specified path.
     *
     * @param location the path to the JSON file
     * @return the deserialized object of type T
     * @throws IOException if an I/O error occurs reading from the file or if the JSON is malformed
     */
    public T load(Path location) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(location)) {
            try {
                return gson.fromJson(reader, type);
            } catch (JsonSyntaxException | JsonIOException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Saves the given object to the specified location as a JSON file.
     *
     * @param obj the object to be serialized and saved
     * @param location the path where the JSON file will be saved
     * @throws IOException if an I/O error occurs writing to the file or during serialization
     */
    public void save(T obj, Path location) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(location, StandardOpenOption.CREATE)) {
            try {
                gson.toJson(obj, writer);
            } catch (JsonIOException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Converts an object of type T to its JSON string representation.
     *
     * @param obj the object to be converted to JSON string
     * @return the JSON string representation of the given object
     */
    public String asString(final T obj) {
        return gson.toJson(obj);
    }
}
