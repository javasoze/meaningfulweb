package org.meaningfulweb.cext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Extract
  implements Serializable, Cloneable {

  private List<String> components = new ArrayList<String>();
  private List<String> pipelines = new ArrayList<String>();
  private byte[] content;
  private Map<String, Object> config = new LinkedHashMap<String, Object>();
  private Map<String, Object> metadata = new LinkedHashMap<String, Object>();
  private Map<String, Object> extracted = new LinkedHashMap<String, Object>();

  public Extract() {

  }

  public byte[] getContent() {
    return content;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  public List<String> getComponents() {
    return components;
  }

  public void setComponents(List<String> components) {
    this.components = components;
  }

  public List<String> getPipelines() {
    return pipelines;
  }

  public void setPipelines(List<String> pipelines) {
    this.pipelines = pipelines;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public void setConfig(Map<String, Object> config) {
    this.config = config;
  }

  public Map<String, Object> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  public Map<String, Object> getExtracted() {
    return extracted;
  }

  public void setExtracted(Map<String, Object> extracted) {
    this.extracted = extracted;
  }

  public Object clone()
    throws CloneNotSupportedException {
    return super.clone();
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
}
