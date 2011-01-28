package org.meaningfulweb.cext.processors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.util.XMLUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.xpath.XPath;

public class XPathProcessor
  extends HtmlContentProcessor {

  public static final Log LOG = LogFactory.getLog(XPathProcessor.class);

  private Set<String> xpaths = new LinkedHashSet<String>();
  private boolean cleaning = false;
  private boolean extractHtml = true;
  private boolean extractText = true;

  @Override
  public boolean processContent(Document document) {

    if (xpaths != null && xpaths.size() > 0) {

      List<Element> selectedList = new ArrayList<Element>();

      for (String xpath : xpaths) {
        List selNodes = null;
        try {
          XPath xp = XPath.newInstance(xpath);
          selNodes = xp.selectNodes(document);
        }
        catch (JDOMException e) {
          e.printStackTrace();
        }
        if (selNodes != null && selNodes.size() > 0) {
          for (Object objNode : selNodes) {
            if (objNode instanceof Element) {
              Element selElem = (Element)objNode;
              if (extractHtml) {
                addExtractedValue(xpath, XMLUtils.toHtml(selElem));
              }
              if (extractText) {
                addExtractedValue(xpath + ".text", XMLUtils.toText(selElem));
              }
              if (cleaning) {
                selectedList.add(selElem);
              }
            }
            else if (objNode instanceof Text) {
              addExtractedValue(xpath, ((Text)objNode).getTextNormalize());
            }
            else if (objNode instanceof Comment) {
              addExtractedValue(xpath, ((Comment)objNode).getText());
            }
          }
        }
      }

      if (cleaning && selectedList.size() > 0) {
        Element root = document.getRootElement();
        for (Element connected : selectedList) {
          connected.detach();
        }
        root.setContent(selectedList);
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

  public boolean isCleaning() {
    return cleaning;
  }

  public void setCleaning(boolean cleaning) {
    this.cleaning = cleaning;
  }

}
