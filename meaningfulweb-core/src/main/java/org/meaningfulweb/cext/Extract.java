package org.meaningfulweb.cext;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Extract
  implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  private Set<String> components = new HashSet<String>();
  private Set<String> pipelines = new HashSet<String>();
  private final byte[] _content;
  private Map<String, Object> config = new LinkedHashMap<String, Object>();
  private Map<String, Object> metadata = new LinkedHashMap<String, Object>();
  private Map<String, Object> extracted = new LinkedHashMap<String, Object>();

  public Extract(byte[] content) {
	  _content = content;
  }
  
  public byte[] getContent(){
	  return _content;
  }

  public Set<String> getComponents() {
    return components;
  }

  public void setComponents(Set<String> components) {
    this.components = components;
  }

  public Set<String> getPipelines() {
    return pipelines;
  }

  public void setPipelines(Set<String> pipelines) {
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
