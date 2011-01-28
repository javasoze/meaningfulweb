package org.meaningfulweb.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import ly.bit.domain.DomainSuffix;
import ly.bit.domain.DomainSuffixes;

import org.apache.commons.lang.StringUtils;

/** Utility class for URL analysis */
public class URLUtil {

  private static Pattern IP_PATTERN = Pattern.compile("(\\d{1,3}\\.){3}(\\d{1,3})");

  /** Returns the domain name of the url. The domain name of a url is
   *  the substring of the url's hostname, w/o subdomain names. As an
   *  example <br><code>
   *  getDomainName(conf, new URL(http://lucene.apache.org/))
   *  </code><br>
   *  will return <br><code> apache.org</code>
   *   */
  public static String getDomainName(URL url) {
    DomainSuffixes tlds = DomainSuffixes.getInstance();
    String host = url.getHost();
    //it seems that java returns hostnames ending with .
    if(host.endsWith("."))
      host = host.substring(0, host.length() - 1);
    if(IP_PATTERN.matcher(host).matches())
      return host;
    
    int index = 0;
    String candidate = host;
    for(;index >= 0;) {
      index = candidate.indexOf('.');
      String subCandidate = candidate.substring(index+1); 
      if(tlds.isDomainSuffix(subCandidate)) {
        return candidate; 
      }
      candidate = subCandidate;
    }
    return candidate;
  }

  /** Returns the domain name of the url. The domain name of a url is
   *  the substring of the url's hostname, w/o subdomain names. As an
   *  example <br><code>
   *  getDomainName(conf, new http://lucene.apache.org/)
   *  </code><br>
   *  will return <br><code> apache.org</code>
   * @throws MalformedURLException
   */
  public static String getDomainName(String url) throws MalformedURLException {
    return getDomainName(new URL(url));
  }

  /** Returns whether the given urls have the same domain name.
   * As an example, <br>
   * <code> isSameDomain(new URL("http://lucene.apache.org")
   * , new URL("http://people.apache.org/"))
   * <br> will return true. </code>
   *
   * @return true if the domain names are equal
   */
  public static boolean isSameDomainName(URL url1, URL url2) {
    return getDomainName(url1).equalsIgnoreCase(getDomainName(url2));
  }

  /**Returns whether the given urls have the same domain name.
  * As an example, <br>
  * <code> isSameDomain("http://lucene.apache.org"
  * ,"http://people.apache.org/")
  * <br> will return true. </code>
  * @return true if the domain names are equal
  * @throws MalformedURLException
  */
  public static boolean isSameDomainName(String url1, String url2)
    throws MalformedURLException {
    return isSameDomainName(new URL(url1), new URL(url2));
  }

  /** Returns the {@link DomainSuffix} corresponding to the
   * last public part of the hostname
   */
  public static DomainSuffix getDomainSuffix(URL url) {
    DomainSuffixes tlds = DomainSuffixes.getInstance();
    String host = url.getHost();
    if(IP_PATTERN.matcher(host).matches())
      return null;
    
    int index = 0;
    String candidate = host;
    for(;index >= 0;) {
      index = candidate.indexOf('.');
      String subCandidate = candidate.substring(index+1);
      DomainSuffix d = tlds.get(subCandidate);
      if(d != null) {
        return d; 
      }
      candidate = subCandidate;
    }
    return null;
  }

  /** Returns the {@link DomainSuffix} corresponding to the
   * last public part of the hostname
   */
  public static DomainSuffix getDomainSuffix(String url) throws MalformedURLException {
    return getDomainSuffix(new URL(url));
  }

  /** Partitions of the hostname of the url by "."  */
  public static String[] getHostSegments(URL url) {
    String host = url.getHost();
    //return whole hostname, if it is an ipv4
    //TODO : handle ipv6
    if(IP_PATTERN.matcher(host).matches())
      return new String[] {host};
    return host.split("\\.");
  }

  /** Partitions of the hostname of the url by "."
   * @throws MalformedURLException */
  public static String[] getHostSegments(String url) throws MalformedURLException {
   return getHostSegments(new URL(url));
  }

  /**
   * Returns the lowercased hostname for the url or null if the url is not well
   * formed.
   * 
   * @param url The url to check.
   * @return String The hostname for the url.
   */
  public static String getHost(String url) {
    try {
      return new URL(url).getHost().toLowerCase();
    }
    catch (MalformedURLException e) {
      return null;
    }
  }

  /**
   * Returns the page for the url.  The page consists of the protocol, host,
   * and path, but does not include the query string.  The host is lowercased
   * but the path is not.
   * 
   * @param url The url to check.
   * @return String The page for the url.
   */
  public static String getPage(String url) {
    try {
      // get the full url, and replace the query string with and empty string
      url = url.toLowerCase();
      String queryStr = new URL(url).getQuery();
      return (queryStr != null) ? url.replace("?" + queryStr, "") : url;
    }
    catch (MalformedURLException e) {
      return null;
    }
  }
  
  public static String getProtocol(String url) {
    try {
      // get the full url, and replace the query string with and empty string
      url = url.toLowerCase();
      String protocolStr = new URL(url).getProtocol();
      return protocolStr;
    }
    catch (MalformedURLException e) {
      return null;
    }
  }
  
  public static String stripProtocol(String url) {
    try {
      // get the full url, and replace the query string with and empty string
      url = url.toLowerCase();
      String protocolStr = new URL(url).getProtocol();
      return (protocolStr != null) ? url.replace(protocolStr + "://", "") : url;
    }
    catch (MalformedURLException e) {
      return null;
    }
  }
  
  public static String getExtension(String filename) {
    int extIndex = StringUtils.lastIndexOf(filename, ".");
    return extIndex > 0 && extIndex + 1 < filename.length() ? StringUtils
      .substring(filename, extIndex + 1) : null;
  }
}
