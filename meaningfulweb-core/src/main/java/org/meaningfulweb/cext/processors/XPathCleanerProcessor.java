package org.meaningfulweb.cext.processors;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.meaningfulweb.cext.HtmlContentProcessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

public class XPathCleanerProcessor
  extends HtmlContentProcessor {

  public static final Log LOG = LogFactory.getLog(XPathCleanerProcessor.class);

  private Set<String> xpaths = new LinkedHashSet<String>();

  @Override
  public boolean processContent(Document document) {

    if (xpaths != null && xpaths.size() > 0) {

      for (String xpath : xpaths) {
        try {
          XPath xp = XPath.newInstance(xpath);
          List<Element> selectedNodes = xp.selectNodes(document);
          if (selectedNodes != null && selectedNodes.size() > 0) {
            for (Content content : selectedNodes) {
              content.getParent().removeContent(content);
            }
          }
        }
        catch (JDOMException e) {
          e.printStackTrace();
        }
      }
    }

    return true;
  }

  public Collection<String> getXpaths() {
    return xpaths;
  }

  public void setXpaths(Collection<String> xpaths) {

    if (xpaths != null) {
      if (xpaths instanceof Set) {
        this.xpaths = (Set<String>)xpaths;
      }
      else {
        Set<String> newXpaths = new HashSet<String>();
        newXpaths.addAll(xpaths);
        this.xpaths = newXpaths;
      }
    }
  }

}
