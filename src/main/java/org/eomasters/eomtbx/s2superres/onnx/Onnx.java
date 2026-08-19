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

package org.eomasters.eomtbx.s2superres.onnx;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtLoggingLevel;
import ai.onnxruntime.OrtProvider;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.RunOptions;
import ai.onnxruntime.OrtSession.SessionOptions;
import ai.onnxruntime.providers.OrtCUDAProviderOptions;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.eomasters.eomtbx.EomtbxRuntime;

public class Onnx {

  public static final OrtEnvironment ENVIRONMENT = OrtEnvironment.getEnvironment();
  private static final int NUM_CPU_THREADS = Runtime.getRuntime().availableProcessors();


  public static OrtSession createOrtSession(byte[] modelData, SessionOptions options) throws OrtException {
    int gpuDeviceId = 0; // The GPU device ID to execute on
    tryAddProvider(() -> options.addCUDA(new OrtCUDAProviderOptions(gpuDeviceId)), "CUDA");
    // Default is true - It is said it should improve performance, but might allocate too much memory.
    // Not seen a performance penalty with set to false.
    boolean useArena = false;
    tryAddProvider(() -> options.addCPU(useArena), "CPU");
    return ENVIRONMENT.createSession(modelData, options);
  }

  public static SessionOptions createDefaultSessionOptions() throws OrtException {
    SessionOptions options = new SessionOptions();
    options.setIntraOpNumThreads(NUM_CPU_THREADS);
    options.setInterOpNumThreads(NUM_CPU_THREADS);
    options.setSessionLogLevel(OrtLoggingLevel.ORT_LOGGING_LEVEL_ERROR);
    options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
    return options;
  }

  public static String[] getAvailableExecutionProviderNames() {
    EnumSet<OrtProvider> availableProviders = OrtEnvironment.getAvailableProviders();
    List<String> collect = availableProviders.stream()
                                             .map(OrtProvider::name)
                                             .collect(Collectors.toList());
    return collect.toArray(new String[]{});
  }

  public static byte[] retrieveModelData(String absResourcePath) throws IOException {
    try (InputStream resourceAsStream = Onnx.class.getResourceAsStream(absResourcePath)) {
      if (resourceAsStream != null) {
        return resourceAsStream.readAllBytes();
      }
    }
    throw new IllegalStateException(String.format("Unable to load the model '%s'.", absResourcePath));
  }

  private static void tryAddProvider(OrtExecutionProviderAdder adder, String providerName) {
    try {
      adder.addProvider();
    } catch (OrtException e) {
      EomtbxRuntime.LOGGER.log(Level.WARNING, "Not able to add execution provider for '" + providerName + "'");
      EomtbxRuntime.LOGGER.log(Level.FINE, "Cause: ", e);
    }
  }

  public static RunOptions createDefaultRunOptions(String runTag) throws OrtException {
    final RunOptions runOptions;
    runOptions = new RunOptions();
    runOptions.setRunTag(runTag);
    // this log level doesn't seem to have an effect
    runOptions.setLogLevel(OrtLoggingLevel.ORT_LOGGING_LEVEL_ERROR);
    return runOptions;
  }

  @FunctionalInterface
  private interface OrtExecutionProviderAdder {

    void addProvider() throws OrtException;
  }
}
