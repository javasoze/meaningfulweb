package org.meaningfulweb.cext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.htmlcleaner.TagNode;
import org.jdom.Attribute;
import org.jdom.Element;
import org.meaningfulweb.util.HtmlExtractUtils;

public class ExtractUtils {

  public static String getExtractedStringValue(Map<String, Object> extracted,
    String name) {

    Object current = extracted.get(name);
    if (current instanceof List) {
      List<String> values = (List<String>)current;
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < values.size(); i++) {
        String curVal = values.get(i);
        if (StringUtils.isNotBlank(curVal)) {
          builder.append(curVal);
          if (i < (values.size() - 1)) {
            builder.append(" ");
          }
        }
      }
      return builder.toString();
    }
    else if (current instanceof String) {
      return (String)current;
    }

    return null;
  }

  public static Map<String, String> getAndConvertExtractedFields(
    Map<String, Object> extracted, Map<String, String> extractMapping) {

    Map<String, String> output = new LinkedHashMap<String, String>();
    if (extracted != null && extracted.size() > 0) {
      for (Entry<String, String> extractEntry : extractMapping.entrySet()) {

        String extractName = extractEntry.getKey();
        String fieldName = extractEntry.getValue();
        boolean isNested = extractName.contains("[");

        // if nested objects otherwise if exact name
        if (isNested) {

          // split on [ the nested object character, will remove ending later
          // easiest way to get the nested names
          String[] nameParts = StringUtils.split(extractName, "[");
          int numNameParts = nameParts != null ? nameParts.length : 0;

          if (numNameParts > 0) {

            // set the current map to be the
            int partIndex = 0;
            Map<String, Object> currentMap = extracted;

            // loop through the name parts
            while (partIndex < numNameParts) {

              // if there is a nested object with the part name, remove the
              // ending nested object character
              String currentPart = nameParts[partIndex];
              currentPart = StringUtils.removeEnd(currentPart, "]");

              // is there a nested object
              if (currentMap.containsKey(currentPart)) {

                // if more name parts, get the next nested map, if it is a map
                // if no more parts get the value as a string
                if (partIndex < (numNameParts - 1)) {
                  Object nestedObj = currentMap.get(currentPart);
                  if (!(nestedObj instanceof Map)) {
                    break;
                  }
                  currentMap = (Map<String, Object>)nestedObj;
                }
                else {
                  String extractStr = getExtractedStringValue(currentMap,
                    currentPart);
                  output.put(fieldName, extractStr);
                }
              }
              else {
                break;
              }

              partIndex++;
            } // end loop over name parts for nested objects
          } // end number of name parts
        }
        else if (extracted.containsKey(extractName)) {
          String extractStr = getExtractedStringValue(extracted, extractName);
          output.put(fieldName, extractStr);
        }
      }
    }

    return output;
  }

  public static List<String> getClassAndIdVals(Element elem) {

    List<String> attrVals = new ArrayList<String>();
    Attribute classAttr = elem.getAttribute("class");
    if (classAttr != null) {
      String classVal = StringUtils.lowerCase(classAttr.getValue());
      if (StringUtils.isNotBlank(classVal)) {
        attrVals.add(classVal);
      }
    }
    Attribute idAttr = elem.getAttribute("id");
    if (idAttr != null) {
      String idVal = StringUtils.lowerCase(idAttr.getValue());
      if (StringUtils.isNotBlank(idVal)) {
        attrVals.add(idVal);
      }
    }

    return attrVals;
  }

  public static void cleanInvalidAttributes(TagNode parent) {
    List nodes = parent.getChildren();
    if (nodes != null) {
      for (int i = 0; i < nodes.size(); i++) {
        Object curChild = nodes.get(i);
        if (curChild instanceof TagNode) {
          TagNode curNode = (TagNode)curChild;
          Map attrMap = curNode.getAttributes();
          Set<String> toRemove = new HashSet<String>();
          for (Object entryObj : attrMap.entrySet()) {
            Entry entry = (Entry)entryObj;
            String attrName = (String)entry.getKey();
            if (!HtmlExtractUtils.isValidAttribute(attrName)) {
              toRemove.add(attrName);
            }
          }
          for (String remove : toRemove) {
            curNode.removeAttribute(remove);
          }
          cleanInvalidAttributes(curNode);
        }
      }
    }
  }
}
