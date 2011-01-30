package org.meaningfulweb.util;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Attribute;
import org.jdom.Element;

public class JDomUtils {

  public static String getAttributeValue(Element elem, String name) {
    Attribute attr = elem.getAttribute(name);
    String value = attr != null ? attr.getValue() : null;
    return StringUtils.isNotBlank(value) ? value : null;
  }
  
  public static List<Element> getElementsByName(Element parent,String name){
	  List<Element> children = parent.getChildren();
	  LinkedList<Element> result = new LinkedList<Element>();
	  for (Element child : children){
		  if (child.getName().equalsIgnoreCase(name)){
			  result.add(child);
		  }
		  List<Element> subList = getElementsByName(child,name);
		  if (subList.size()>0){
			  for (Element e : subList){
				  result.add(e);
			  }
		  }
	  }	  
	  return result;
  }
}
