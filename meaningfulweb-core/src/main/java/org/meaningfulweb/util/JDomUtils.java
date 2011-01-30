package org.meaningfulweb.util;
import org.apache.commons.lang.StringUtils;
import org.jdom.Attribute;
import org.jdom.Element;

public class JDomUtils {

  public static String getAttributeValue(Element elem, String name) {
    Attribute attr = elem.getAttribute(name);
    String value = attr != null ? attr.getValue() : null;
    return StringUtils.isNotBlank(value) ? value : null;
  }
}
