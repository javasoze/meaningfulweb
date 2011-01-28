package org.meaningfulweb.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonUtils {

  public static boolean looksLikeJson(String content) {
    return StringUtils.trim(content).startsWith("{");
  }

  public static JsonNode parseJson(String json)
    throws IOException {
    
    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = null;
    try {
      root = mapper.readValue(new StringReader(json), JsonNode.class);
    }
    catch (Exception e) {
      throw new IOException("JsonConfig is invalid", e);
    }
    return root;
  }

  public static String getStringValue(JsonNode parent, String name) {
    JsonNode node = parent.get(name);
    if (node != null) {
      return node.getValueAsText();
    }
    return null;
  }

  public static boolean getBooleanValue(JsonNode parent, String name,
    boolean defaultValue) {
    JsonNode node = parent.get(name);
    if (node != null) {
      return node.getBooleanValue();
    }
    return defaultValue;
  }

  public static int getIntValue(JsonNode parent, String name, int defaultValue) {
    JsonNode node = parent.get(name);
    if (node != null) {
      return node.getIntValue();
    }
    return defaultValue;
  }

  public static long getLongValue(JsonNode parent, String name,
    long defaultValue) {
    JsonNode node = parent.get(name);
    if (node != null) {
      return node.getLongValue();
    }
    return defaultValue;
  }

  public static String serializeToJson(Object object) {

    try {

      StringWriter sw = new StringWriter(); // serialize
      ObjectMapper mapper = new ObjectMapper();
      MappingJsonFactory jsonFactory = new MappingJsonFactory();
      JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(sw);
      mapper.writeValue(jsonGenerator, object);
      sw.close();

      return sw.toString();
    }
    catch (Exception e) {
      return null;
    }
  }

  public static Object deserializeFromJson(String json, Class valueType) {

    try {
      ObjectMapper mapper = new ObjectMapper();
      MappingJsonFactory jsonFactory = new MappingJsonFactory();
      JsonParser jsonParser = jsonFactory.createJsonParser(json);
      return mapper.readValue(jsonParser, valueType);
    }
    catch (Exception e) {
      return null;
    }
  }

  public static List<String> getFieldNames(JsonNode parent) {
    Iterator<String> fieldNameIt = parent.getFieldNames();
    List<String> fieldNames = new ArrayList<String>();
    while (fieldNameIt.hasNext()) {
      String fieldName = fieldNameIt.next();
      fieldNames.add(fieldName);
    }
    return fieldNames;
  }
}
