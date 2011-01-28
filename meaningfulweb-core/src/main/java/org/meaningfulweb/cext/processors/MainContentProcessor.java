package org.meaningfulweb.cext.processors;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.util.XMLUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;

public class MainContentProcessor
  extends HtmlContentProcessor {

  public static final Log LOG = LogFactory.getLog(MainContentProcessor.class);

  private double threshold = 10f;
  private int minTextLength = 20;
  private int minWords = 0;
  private int minLinks = 2;
  private int maxRecurseDepth = 250;
  private double linkListThreshold = .70f;

  private boolean extractHtml = true;
  private boolean extractText = true;

  private boolean isContainerSetup(int linkCount, int cntrCount) {
    if (cntrCount <= linkCount) {
      return ((float)cntrCount / (float)linkCount) > linkListThreshold;
    }
    else {
      return ((float)linkCount / (float)cntrCount) > linkListThreshold;
    }
  }

  private static Set<String> removeElements = new HashSet<String>();
  static {
    String[] remove = {"head", "script", "noscript", "style", "form", "meta",
      "input", "iframe", "embed", "hr", "img", "link", "label"};
    removeElements.addAll(Arrays.asList(remove));
  }

  private static Set<String> containerElements = new HashSet<String>();
  static {
    String[] container = {"div", "table", "td", "th", "tr", "tbody", "thead",
      "tfoot", "col", "colgroup", "ul", "ol", "li", "html", "center", "span"};
    containerElements.addAll(Arrays.asList(container));
  }

  private void cleanNodes(int level, Content node, Set<Content> remove) {

    // don't go on forever, spider traps can kill JVM through stack overflow
    if (level == maxRecurseDepth) {
      return;
    }

    if (node instanceof Element) {
      Element elem = (Element)node;
      String name = StringUtils.lowerCase(elem.getName());
      if (removeElements.contains(name)) {
        remove.add(node);
      }
      List<Content> children = elem.getContent();
      if (children != null && children.size() > 0) {
        for (Content child : children) {
          cleanNodes(++level, child, remove);
        }
      }
    }
    else if (node instanceof Comment) {
      remove.add(node);
    }
  }

  private Map cleanLinkContainers(int level, Content node, Set<Content> remove) {

    // don't go on forever, spider traps can kill JVM through stack overflow
    if (node == null || level == maxRecurseDepth) {
      return null;
    }

    int linkCount = 0;
    int liCount = 0;
    int textLength = 0;
    int wordCount = 0;
    int nodeCount = 0;
    int hTagCount = 0;
    boolean delete = false;
    float ratio = 0.0f;
    float totalRatio = 0.0f;
    boolean linkContainer = false;

    if (node instanceof Element) {

      nodeCount++;
      Element elem = (Element)node;
      String name = StringUtils.lowerCase(elem.getName());

      List<Content> children = elem.getContent();
      if (children != null && children.size() > 0) {
        for (Content child : children) {
          Map data = cleanLinkContainers(++level, child, remove);
          if (data != null) {
            boolean deleteChild = (Boolean)data.get("delete");
            if (deleteChild) {
              remove.add(child);
            }
            else {
              textLength += (Integer)data.get("textLength");
            }
            linkCount += (Integer)data.get("linkCount");
            wordCount += (Integer)data.get("wordCount");
            nodeCount += (Integer)data.get("nodeCount");
            totalRatio += (Float)data.get("ratio");
            liCount += (Integer)data.get("liCount");
            hTagCount += (Integer)data.get("hTagCount");
          }
        }
      }

      if (name.equalsIgnoreCase("a")) {
        linkCount++;
      }
      else if (name.equalsIgnoreCase("li")) {
        liCount++;
      }
      else if (name.equalsIgnoreCase("ul")) {
        if (linkCount >= 2 && liCount >= 2
          && isContainerSetup(linkCount, liCount)) {
          // heuristic for lists of links
          linkContainer = true;
          liCount = 0;
        }
      }
      else if (name.equalsIgnoreCase("h1") || name.equalsIgnoreCase("h2")
        || name.equalsIgnoreCase("h3") || name.equalsIgnoreCase("h4")
        || name.equalsIgnoreCase("h5")) {
        hTagCount++;
      }
      else if ((name.equalsIgnoreCase("div") || name.equalsIgnoreCase("p"))
        && linkCount >= 3 && hTagCount >= 3
        && isContainerSetup(linkCount, hTagCount)) {
        // heuristic for things that look like lists of links
        linkContainer = true;
        hTagCount = 0;
      }

      if (containerElements.contains(name)) {

        if (wordCount == 0) {
          delete = true;
        }
        else {
          float linkDenom = (float)linkCount > 0 ? linkCount : 1;
          ratio = (float)wordCount / linkDenom;
          totalRatio += ratio;
        }

        if (linkCount > minLinks && ratio < threshold && totalRatio < threshold) {
          delete = true;
        }
        if (linkContainer) {
          delete = true;
        }
        if (!name.equalsIgnoreCase("span")) {
          if (textLength < minTextLength || wordCount < minWords) {
            delete = true;
          }
        }
      }
    }
    else if (node instanceof Text) {
      Text text = (Text)node;
      String normalized = text.getTextNormalize();
      if (StringUtils.isNotBlank(normalized)) {
        boolean hasText = StringUtils.isNotBlank(normalized);
        if (hasText) {
          textLength += normalized.length();
          wordCount += StringUtils.split(normalized).length;
        }
      }
    }

    Map output = new HashMap();
    output.put("textLength", textLength);
    output.put("wordCount", wordCount);
    output.put("linkCount", linkCount);
    output.put("nodeCount", nodeCount);
    output.put("hTagCount", hTagCount);
    output.put("liCount", liCount);
    output.put("ratio", ratio);
    output.put("delete", delete);

    return output;
  }

  @Override
  public boolean processContent(Document doc) {

    Set<Content> remove = new LinkedHashSet<Content>();
    Element rootElem = doc.getRootElement();

    // remove specific non-content elements
    List<Content> contents = rootElem.getContent();
    for (Content child : contents) {
      cleanNodes(0, child, remove);
    }
    for (Content content : remove) {
      content.getParent().removeContent(content);
    }

    // clear the remove set
    remove.clear();

    // remove link container elements
    List<Content> containerContents = rootElem.getContent();
    for (Content child : containerContents) {
      cleanLinkContainers(0, child, remove);
    }
    for (Content content : remove) {
      content.getParent().removeContent(content);
    }

    // add the content html
    if (extractHtml) {
      String contentHtml = XMLUtils.toHtml(doc);
      // return full html if content html is empty
      if (StringUtils.isNotBlank(contentHtml)) {
        addExtractedValue("html", contentHtml);
      }
    }

    // get the content text
    if (extractText) {
      String contentText = XMLUtils.toText(doc);
      // return full text if the content text is empty
      if (StringUtils.isNotBlank(contentText)) {
        addExtractedValue("text", contentText);
      }
    }

    return true;
  }

  public double getThreshold() {
    return threshold;
  }

  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }

  public int getMinTextLength() {
    return minTextLength;
  }

  public void setMinTextLength(int minTextLength) {
    this.minTextLength = minTextLength;
  }

  public int getMinWords() {
    return minWords;
  }

  public void setMinWords(int minWords) {
    this.minWords = minWords;
  }

  public int getMinLinks() {
    return minLinks;
  }

  public void setMinLinks(int minLinks) {
    this.minLinks = minLinks;
  }

  public int getMaxRecurseDepth() {
    return maxRecurseDepth;
  }

  public void setMaxRecurseDepth(int maxRecurseDepth) {
    this.maxRecurseDepth = maxRecurseDepth;
  }

  public double getLinkListThreshold() {
    return linkListThreshold;
  }

  public void setLinkListThreshold(double linkListThreshold) {
    this.linkListThreshold = linkListThreshold;
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

}
