package org.meaningfulweb.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class URIUtils {

  private static Map<String, String> unsafe = new HashMap<String, String>();
  static {
    unsafe.put(" ", "20");
    unsafe.put("\"", "22");
    unsafe.put("#", "23");
    unsafe.put("%", "25");
    unsafe.put("<", "3C");
    unsafe.put(">", "3E");
    unsafe.put("{", "7B");
    unsafe.put("}", "7D");
    unsafe.put("|", "7C");
    unsafe.put("\\", "5C");
    unsafe.put("^", "7E");
    unsafe.put("~", "5E");
    unsafe.put("[", "5B");
    unsafe.put("]", "5D");
    unsafe.put("`", "60");
  }

  private static Map<String, String> reserved = new HashMap<String, String>();
  static {
    reserved.put("$", "24");
    reserved.put("&", "26");
    reserved.put("+", "2B");
    reserved.put(",", "2C");
    reserved.put("/", "2F");
    reserved.put(":", "3A");
    reserved.put(";", "3B");
    reserved.put("=", "3D");
    reserved.put("?", "3F");
    reserved.put("@", "40");
  }

  private static String replaceInvalid(String uri, Map<String, String> reps) {

    // replace invalid characters
    StringBuilder builder = new StringBuilder();
    for (char curChar : uri.toCharArray()) {
      String charStr = String.valueOf(curChar);
      if (reps.containsKey(charStr)) {
        builder.append("%" + reps.get(charStr));
      }
      else {
        builder.append(charStr);
      }
    }
    return builder.toString();
  }

  public static boolean isValidURI(String uri) {

    // no protocol scheme
    if (StringUtils.isBlank(URLUtil.getProtocol(uri))) {
      return false;
    }

    // try and create the uri to see if the syntax is valid
    try {
      URI.create(uri);
    }
    catch (Exception e) {
      return false;
    }

    return true;
  }

  public static String fixInvalidUri(String uri) {

    String protocol = null;
    String host = null;
    String path = null;
    String query = null;
    String ref = null;

    try {

      // handle the case of no protocol
      URL url = null;
      try {
        url = new URL(uri);
      }
      catch (MalformedURLException e) {
        uri = "http://" + uri;
        url = new URL(uri);
      }
      
      protocol = url.getProtocol();
      host = url.getHost();
      path = url.getPath();
      query = url.getQuery();
      ref = url.getRef();

      if (StringUtils.isBlank(protocol)) {
        protocol = "http";
      }

      if (StringUtils.isNotBlank(path)) {
        path = replaceInvalid(path, unsafe);
      }

      if (StringUtils.isNotBlank(query)) {
        query = replaceInvalid(query, unsafe);
      }

      if (StringUtils.isNotBlank(ref)) {
        ref = replaceInvalid(ref, unsafe);
        ref = replaceInvalid(ref, reserved);
      }

      StringBuilder newUriBuilder = new StringBuilder();
      newUriBuilder.append(protocol + "://" + host);
      if (StringUtils.isNotBlank(path)) {
        newUriBuilder.append(path);
      }
      if (StringUtils.isNotBlank(query)) {
        newUriBuilder.append("?" + query);
      }
      if (StringUtils.isNotBlank(ref)) {
        newUriBuilder.append("#" + ref);
      }

      uri = newUriBuilder.toString();
    }
    catch (MalformedURLException e) {
      // can't fix
    }

    return uri;
  }
}
