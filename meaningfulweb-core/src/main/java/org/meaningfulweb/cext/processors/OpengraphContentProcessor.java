package org.meaningfulweb.cext.processors;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.opengraph.OGObject;
import org.meaningfulweb.opengraph.OpenGraphParser;
import org.meaningfulweb.util.JDomUtils;

public class OpengraphContentProcessor
  extends HtmlContentProcessor {

  private Set<String> names = new LinkedHashSet<String>();
  private boolean includeAll = false;

  public Collection<String> getNames() {
    return names;
  }

  public void setNames(Collection<String> names) {
    if (names != null) {
      if (names instanceof Set) {
        this.names = (Set<String>)names;
      }
      else {
        Set<String> newNames = new LinkedHashSet<String>();
        newNames.addAll(names);
        this.names = newNames;
      }
    }
  }

  public boolean isIncludeAll() {
    return includeAll;
  }

  public void setIncludeAll(boolean includeAll) {
    this.includeAll = includeAll;
  }

  @Override
  public boolean processContent(Document document) {

    Element rootElem = document.getRootElement();
    List<Content> contents = rootElem.getContent();
    for (Content child : contents) {

      // loop through each element in the root, which should be just head, body
      Element elem = (Element)child;
      String name = StringUtils.lowerCase(elem.getName());

      // get the head element
      if (name.equalsIgnoreCase("head")) {

        // create a datamap for open graph processing
        Map<String, String> datamap = new HashMap<String, String>();

        // get all meta tags for the head element
        List<Element> metatags = JDomUtils.getElementsByName(elem, "meta");
        for (Element metaElem : metatags) {
          String metaIdent = JDomUtils.getAttributeValue(metaElem, "property");
          if (metaIdent != null && metaIdent.startsWith("og:")) {
            metaIdent = StringUtils.trim(StringUtils.lowerCase(metaIdent));
            metaIdent = StringUtils.substring(metaIdent,
              OpenGraphParser.OG_PREFIX_CHAR_COUNT);
            String metaValue = JDomUtils.getAttributeValue(metaElem, "content");
            if (StringUtils.isNotBlank(metaValue)) {
              datamap.put(metaIdent, metaValue);
            }
          }
        }

        // parse the elements with opengraph
        OGObject ogObj = OpenGraphParser.parse(datamap);
        if (!ogObj.isEmpty()) {

          Map<String, String> metaMap = ogObj.getMeta();
          if (metaMap.size() > 0) {

            for (Entry<String, String> ogEntry : metaMap.entrySet()) {
              String ogName = ogEntry.getKey();
              if (includeAll || names.contains(ogName)) {
                addExtractedValue(ogName, ogEntry.getValue());
              }
            }

            if (includeAll || names.contains("audio")) {
              Map<String, String> audioMap = ogObj.getAudio();
              if (audioMap.size() > 0) {
                addExtractedValue("audio", audioMap);
              }
            }

            if (includeAll || names.contains("video")) {
              Map<String, String> videoMap = ogObj.getVideo();
              if (videoMap.size() > 0) {
                addExtractedValue("video", videoMap);
              }
            }
          }
        }
      }
    }

    return true;
  }

}
