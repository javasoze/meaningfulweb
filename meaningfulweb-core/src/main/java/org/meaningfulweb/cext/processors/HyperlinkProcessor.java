package org.meaningfulweb.cext.processors;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.util.JDomUtils;
import org.meaningfulweb.util.URLUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;

public class HyperlinkProcessor
  extends HtmlContentProcessor {

  public static final Log LOG = LogFactory.getLog(HyperlinkProcessor.class);

  private Set<String> extensions = new LinkedHashSet<String>();
  private int maxRecurseDepth = 250;

  private void extractLinkExtensions(int level, Content node) {

    // don't go on forever, spider traps can kill JVM through stack overflow
    if (node == null || level == maxRecurseDepth) {
      return;
    }

    if (node instanceof Element) {

      Element elem = (Element)node;
      String name = StringUtils.lowerCase(elem.getName());
      if (StringUtils.equalsIgnoreCase(name, "a")) {
        String href = JDomUtils.getAttributeValue(elem, "href");
        if (StringUtils.isNotBlank(href)) {
          String page = URLUtil.getPage(href);
          String extension = URLUtil.getExtension(page);
          if (extensions.contains(extension)) {
            addExtractedValue(extension, href);
          }
        }
      }

      List<Content> children = elem.getContent();
      if (children != null && children.size() > 0) {
        for (Content child : children) {
          extractLinkExtensions(++level, child);
        }
      }
    }
  }

  public Collection<String> getExtensions() {
    return extensions;
  }

  public void setExtensions(Collection<String> extensions) {
    if (extensions != null) {
      if (extensions instanceof Set) {
        this.extensions = (Set<String>)extensions;
      }
      else {
        Set<String> newExtensions = new LinkedHashSet<String>();
        newExtensions.addAll(extensions);
        this.extensions = newExtensions;
      }
    }
  }

  public int getMaxRecurseDepth() {
    return maxRecurseDepth;
  }

  public void setMaxRecurseDepth(int maxRecurseDepth) {
    this.maxRecurseDepth = maxRecurseDepth;
  }

  @Override
  public boolean processContent(Document document) {

    Element rootElem = document.getRootElement();
    List<Content> contents = rootElem.getContent();
    for (Content child : contents) {
      extractLinkExtensions(0, child);
    }

    return true;
  }
}
