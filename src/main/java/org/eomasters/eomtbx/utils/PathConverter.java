package org.eomasters.eomtbx.utils;

import com.bc.ceres.binding.Converter;
import java.nio.file.Path;

// registered EomToolbox to the ConverterRegistry
// ConverterRegistry.getInstance().setConverter(Path.class, new PathConverter());
public class PathConverter implements Converter<Path> {

  @Override
  public Class<Path> getValueType() {
    return Path.class;
  }

  @Override
  public Path parse(String text) {
    if (text.isEmpty()) {
      return null;
    }
    return Path.of(text);
  }

  @Override
  public String format(Path value) {
    if (value == null) {
      return "";
    }
    return value.toAbsolutePath().toString();
  }
}
