package org.meaningfulweb.cext;
import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class ShortUrlInfo
  implements Serializable, Cloneable {

  private String url;
  private String originalUrl;
  private String urlFetched;
  private String contentType;
  private int contentLength;
  private String category;
  private String sourceDomain;
  private boolean archived;
  private long archivedTime;

  public ShortUrlInfo() {

  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getOriginalUrl() {
    return originalUrl;
  }

  public void setOriginalUrl(String originalUrl) {
    this.originalUrl = originalUrl;
  }

  public String getUrlFetched() {
    return urlFetched;
  }

  public void setUrlFetched(String urlFetched) {
    this.urlFetched = urlFetched;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public int getContentLength() {
    return contentLength;
  }

  public void setContentLength(int contentLength) {
    this.contentLength = contentLength;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getSourceDomain() {
    return sourceDomain;
  }

  public void setSourceDomain(String sourceDomain) {
    this.sourceDomain = sourceDomain;
  }

  public boolean isArchived() {
    return archived;
  }

  public void setArchived(boolean archived) {
    this.archived = archived;
  }

  public long getArchivedTime() {
    return archivedTime;
  }

  public void setArchivedTime(long archivedTime) {
    this.archivedTime = archivedTime;
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
