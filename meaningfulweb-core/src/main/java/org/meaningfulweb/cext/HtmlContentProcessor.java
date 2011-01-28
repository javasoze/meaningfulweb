package org.meaningfulweb.cext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public abstract class HtmlContentProcessor {

  private Map<String, Object> extract = new LinkedHashMap<String, Object>();
  private String name;
  private String description;
  private Map<String, Object> metadata = new LinkedHashMap<String, Object>();

  protected void addExtractedValue(String name, Object value) {

    if (value != null
      && (!(value instanceof String) || StringUtils.isNotBlank((String)value))) {

      Object current = extract.get(name);
      if (current == null) {
        extract.put(name, value);
      }
      else if (current instanceof List) {
        ((List)current).add(value);
      }
      else {
        List values = new ArrayList();
        values.add(current);
        values.add(value);
        extract.put(name, values);
      }
    }
  }

  public Map<String, Object> getExtracted() {
    return extract;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Map<String, Object> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  public abstract boolean processContent(Document document);
}
