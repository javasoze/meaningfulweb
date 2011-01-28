package org.meaningfulweb.util;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.net.URLCodec;

public class EncodingUtils {

  public static String getEncodedString(byte[] contentBytes, String encoding) {

    String content = null;
    try {
      content = new String(contentBytes, encoding);
    }
    catch (UnsupportedEncodingException uee) {
      content = getEncodedString(contentBytes, "UTF-8");
    }
    return content;
  }
  
  public static String urlEncode(String text) {
    URLCodec codec = new URLCodec();
    try {
      return codec.encode(text);
    }
    catch (Exception e) {
      return null;
    }    
  }
  
  public static String urlDecode(String encoded) {
    URLCodec codec = new URLCodec();
    try {
      return codec.decode(encoded);
    }
    catch (Exception e) {
      return null;
    }    
  }
}
