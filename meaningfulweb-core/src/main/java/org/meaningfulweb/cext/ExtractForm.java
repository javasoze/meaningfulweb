package org.meaningfulweb.cext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class ExtractForm
  implements Serializable, Cloneable {

  private String url;
  private String globalHash;
  private String content;
  private boolean perComponentDOM = true;
  private boolean perPipelineDOM = true;
  private List<String> components = new ArrayList<String>();
  private List<String> pipelines = new ArrayList<String>();
  private Map<String, Object> config = new LinkedHashMap<String, Object>();
  private Map<String, Object> metadata = new LinkedHashMap<String, Object>();

  public ExtractForm() {

  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getGlobalHash() {
    return globalHash;
  }

  public void setGlobalHash(String globalHash) {
    this.globalHash = globalHash;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public boolean isPerComponentDOM() {
    return perComponentDOM;
  }

  public void setPerComponentDOM(boolean perComponentDOM) {
    this.perComponentDOM = perComponentDOM;
  }

  public boolean isPerPipelineDOM() {
    return perPipelineDOM;
  }

  public void setPerPipelineDOM(boolean perPipelineDOM) {
    this.perPipelineDOM = perPipelineDOM;
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
