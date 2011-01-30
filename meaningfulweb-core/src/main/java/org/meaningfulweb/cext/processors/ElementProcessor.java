package org.meaningfulweb.cext.processors;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.util.JDomUtils;
import org.meaningfulweb.util.XMLUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;

public class ElementProcessor
  extends HtmlContentProcessor {

  public static final Log LOG = LogFactory.getLog(ElementProcessor.class);

  private Set<String> elements = new LinkedHashSet<String>();
  private Set<String> headers = new LinkedHashSet<String>();
  private boolean extractHtml = true;
  private boolean extractText = true;
  private int maxRecurseDepth = 250;

  private void extractFromNodes(int level, Content node) {

    // don't go on forever, spider traps can kill JVM through stack overflow
    if (node == null || level == maxRecurseDepth) {
      return;
    }

    if (node instanceof Element) {

      Element elem = (Element)node;
      String name = StringUtils.lowerCase(elem.getName());

      // extract out elements by name
      if (elements.contains(name)) {
        if (extractHtml) {
          addExtractedValue(name, XMLUtils.toHtml(elem));
        }
        if (extractText) {
          addExtractedValue(name + ".text", XMLUtils.toText(elem));
        }
      }
      else if (name.equalsIgnoreCase("meta")) {

        // parse and extract out meta tag values (headers) by name
        String metaIdent = JDomUtils.getAttributeValue(elem, "name");
        if (metaIdent == null) {
          metaIdent = JDomUtils.getAttributeValue(elem, "http-equiv");
        }
        if (metaIdent == null) {
          metaIdent = JDomUtils.getAttributeValue(elem, "property");
        }
        if (metaIdent != null) {
          metaIdent = StringUtils.trim(StringUtils.lowerCase(metaIdent));
          if (headers.contains(metaIdent)) {
            String metaValue = JDomUtils.getAttributeValue(elem, "content");
            if (StringUtils.isNotBlank(metaValue)) {
              addExtractedValue(metaIdent, metaValue);
            }
          }
        }
      }
      else if (name.equalsIgnoreCase("link")) {

        // parse and extract link rel values
        Map<String, String> linkAttrs = new LinkedHashMap<String, String>();
        String rel = JDomUtils.getAttributeValue(elem, "rel");
        if (StringUtils.isNotBlank(rel)) {
          linkAttrs.put("rel", rel);
        }
        String type = JDomUtils.getAttributeValue(elem, "type");
        boolean hasType = StringUtils.isNotBlank(type);
        if (hasType) {
          linkAttrs.put("type", type);
        }
        String href = JDomUtils.getAttributeValue(elem, "href");
        if (StringUtils.isNotBlank(href)) {
          linkAttrs.put("href", href);
        }

        if (headers.contains("link")
          || headers.contains("link:" + rel)
          || (hasType && headers.contains("link:" + rel + ":" + type))) {
          addExtractedValue("link", linkAttrs);
        }
      }

      List<Content> children = elem.getContent();
      if (children != null && children.size() > 0) {
        for (Content child : children) {
          extractFromNodes(++level, child);
        }
      }
    }
    else if (node instanceof Comment) {
      // possibly extract out comments
      if (elements.contains("comment")) {
        addExtractedValue("comment", ((Comment)node).getText());
      }
    }
  }

  public Collection<String> getElements() {
    return elements;
  }

  public void setElements(Collection<String> elements) {
    if (elements != null) {
      if (elements instanceof Set) {
        this.elements = (Set<String>)elements;
      }
      else {
        Set<String> newElements = new LinkedHashSet<String>();
        newElements.addAll(elements);
        this.elements = newElements;
      }
    }
  }

  public Collection<String> getHeaders() {
    return headers;
  }

  public void setHeaders(Collection<String> headers) {
    if (headers != null) {
      if (headers instanceof Set) {
        this.headers = (Set<String>)headers;
      }
      else {
        Set<String> newHeaders = new LinkedHashSet<String>();
        newHeaders.addAll(headers);
        this.headers = newHeaders;
      }
    }
  }

  public boolean isExtractHtml() {
    return extractHtml;
  }

  public void setExtractHtml(boolean extractHtml) {
    this.extractHtml = extractHtml;
  }

  public boolean isExtractText() {
    return extractText;
  }

  public void setExtractText(boolean extractText) {
    this.extractText = extractText;
  }

  public int getMaxRecurseDepth() {
    return maxRecurseDepth;
  }

  public void setMaxRecurseDepth(int maxRecurseDepth) {
    this.maxRecurseDepth = maxRecurseDepth;
  }

  @Override
  public boolean processContent(Document document) {

    boolean hasElements = (elements != null && elements.size() > 0);
    boolean hasHeaders = (headers != null && headers.size() > 0);
    if (hasElements || hasHeaders) {
      Element rootElem = document.getRootElement();
      List<Content> contents = rootElem.getContent();
      for (Content child : contents) {
        extractFromNodes(0, child);
      }
    }

    return true;
  }
}
