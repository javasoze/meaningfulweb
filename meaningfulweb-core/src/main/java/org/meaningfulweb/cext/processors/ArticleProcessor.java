package org.meaningfulweb.cext.processors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.util.XMLUtils;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;

public class ArticleProcessor
  extends HtmlContentProcessor {

  private boolean extractHtml = true;
  private boolean extractText = true;
  private boolean extractMedia = true;

  private final static String TEXT_SIZE = "textSize";
  private final static String TOTAL_TEXT_IN_LINKS = "totalTextInLinks";
  private final static String TOTAL_TEXT_SIZE = "totalTextSize";
  private final static String TEXT_SIZE_SCORE = "textSizeScore";
  private final static String TOTAL_LINKS = "totalLinks";
  private final static String TOTAL_PTAGS = "totalPTags";
  private final static String TOTAL_IMAGES = "totalImages";
  private final static String TOTAL_H1TAGS = "totalH1Tags";
  private final static String TOTAL_NODES = "totalNodes";
  private final static String TOTAL_ELEMENT_NODES = "totalElementNodes";
  private final static String TOTAL_TEXT_NODES = "totalTextNodes";
  private final static String TOTAL_LEVELS = "totalLevels";
  private final static String HAS_DIRECT_TEXT = "hasDirectText";
  private final static String TEXT_NODE_SCORE = "textNodeScore";
  private final static String LINK_SCORE = "linkScore";
  private final static String LEVEL = "level";
  private final static String POSITION = "position";

  public static final Log LOG = LogFactory.getLog(ArticleProcessor.class);

  private static Set<String> removeElements = new HashSet<String>();
  static {
    // removed "iframe", "label", "pre","blockquote", "code", "br"
    String[] remove = {"head", "script", "noscript", "style", "form", "meta",
      "input", "select", "textarea", "option", "hr", "link", "embed", "h1",
      "iframe"};
    removeElements.addAll(Arrays.asList(remove));
  }

  private static Set<String> ignoreElements = new HashSet<String>();
  static {
    String[] remove = {"br"};
    ignoreElements.addAll(Arrays.asList(remove));
  }

  private static Set<String> mediaElements = new HashSet<String>();
  static {
    String[] remove = {"embed", "img"};
    mediaElements.addAll(Arrays.asList(remove));
  }

  private int maxRecurseDepth = 250;

  private void removeProcessingAttributes(Element elem) {

    elem.removeAttribute(TEXT_SIZE);
    elem.removeAttribute(TOTAL_TEXT_SIZE);
    elem.removeAttribute(TOTAL_TEXT_IN_LINKS);
    elem.removeAttribute(TEXT_SIZE_SCORE);
    elem.removeAttribute(TOTAL_NODES);
    elem.removeAttribute(TOTAL_ELEMENT_NODES);
    elem.removeAttribute(TOTAL_TEXT_NODES);
    elem.removeAttribute(TOTAL_LINKS);
    elem.removeAttribute(TOTAL_PTAGS);
    elem.removeAttribute(TOTAL_H1TAGS);
    elem.removeAttribute(TOTAL_IMAGES);
    elem.removeAttribute(HAS_DIRECT_TEXT);
    elem.removeAttribute(TEXT_NODE_SCORE);
    elem.removeAttribute(LINK_SCORE);
    elem.removeAttribute(LEVEL);
    elem.removeAttribute(TOTAL_LEVELS);
    elem.removeAttribute(POSITION);
  }

  private List<String> getClassAndIdVals(Element elem) {

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

  private void cleanStructureNodes(int level, Content node, Set<Content> remove) {

    // don't go on forever, spider traps can kill JVM through stack overflow
    if (level == maxRecurseDepth) {
      return;
    }

    if (node instanceof Element) {

      Element elem = (Element)node;
      String name = StringUtils.lowerCase(elem.getName());

      List<String> attrVals = getClassAndIdVals(elem);
      String[] removeLabels = {"header", "menu", "footer", "masthead",
        "comment", "quotes", "resources", "tools", "headline", "caption",
        "share", "font", "opinion", "timestamp", "posted", "metadata",
        "toolbar", "disqus", "sign_up", "nav", "like", "advertisement",
        "sidebar", "print", "email", "more", "links", "enlarge", "tags",
        "breadcrumb", "facebook", "stumble", "twitter", "callout", "widget",
        "related", "announcement", "neighbor", "sponsor", "support", "flash"};

      boolean shouldRemove = false;
      if (!StringUtils.equals("body", name)) {
        for (String attrVal : attrVals) {
          for (int i = 0; i < removeLabels.length; i++) {
            if (StringUtils.contains(attrVal, removeLabels[i])) {
              shouldRemove = true;
              remove.add(node);
              break;
            }
          }
        }
      }

      if (!shouldRemove) {

        Attribute styleAttr = elem.getAttribute("style");
        if (styleAttr != null) {
          String styleVal = StringUtils.lowerCase(styleAttr.getValue());
          if (StringUtils.isNotBlank(styleVal)
            & styleVal.matches(".*display:\\s*none.*")) {
            remove.add(node);
          }
        }
      }

      if (!shouldRemove) {

        List<Content> children = elem.getContent();
        if (children != null && children.size() > 0) {
          for (Content child : children) {
            cleanStructureNodes(++level, child, remove);
          }
        }
      }
    }
    else if (node instanceof Comment) {
      remove.add(node);
    }
  }

  private void findHighestNodeScore(int level, Content node,
    List<Content> highest) {

    // don't go on forever, spider traps can kill JVM through stack overflow
    if (level == maxRecurseDepth) {
      return;
    }

    if (node instanceof Element) {

      Element elem = (Element)node;
      String name = StringUtils.lowerCase(elem.getName());

      Attribute hdtAttr = elem.getAttribute(HAS_DIRECT_TEXT);
      if (hdtAttr != null && BooleanUtils.toBoolean(hdtAttr.getValue())
        && !StringUtils.equals("body", name)) {

        Attribute tssAttr = elem.getAttribute(TEXT_SIZE);
        int textSizeScore = NumberUtils.toInt(tssAttr.getValue());

        if (highest.size() == 0) {
          highest.add(node);
        }
        else {
          Element prevHighest = (Element)highest.get(0);
          Attribute prevTss = prevHighest.getAttribute(TEXT_SIZE);
          int prevTextSizeScore = NumberUtils.toInt(prevTss.getValue());
          if (textSizeScore >= prevTextSizeScore) {
            highest.clear();
            highest.add(node);
          }
        }

      }

      List<Content> children = elem.getContent();
      if (children != null && children.size() > 0) {
        ++level;
        for (Content child : children) {
          findHighestNodeScore(level, child, highest);
        }
      }
    }

  }

  private void cleanNonContentNodes(int level, Content node, Set<Content> remove) {

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
      else {
        List<Content> children = elem.getContent();
        if (children != null && children.size() > 0) {
          ++level;
          for (Content child : children) {
            cleanNonContentNodes(level, child, remove);
          }
        }
      }
    }
    else if (node instanceof Comment) {
      remove.add(node);
    }
  }

  private void cleanNonTextNodes(int level, Content node, Set<Content> remove) {

    if (level == maxRecurseDepth) {
      return;
    }

    if (node instanceof Element) {

      Element elem = (Element)node;
      String name = StringUtils.lowerCase(elem.getName());
      if (ignoreElements.contains(name)) {
        return;
      }

      Attribute tiAttr = elem.getAttribute(TOTAL_IMAGES);
      if (tiAttr != null) {
        int totalImages = NumberUtils.toInt(tiAttr.getValue());
        if (totalImages > 0) {
          return;
        }
      }

      Attribute ttnAttr = elem.getAttribute(TOTAL_TEXT_NODES);
      boolean removed = false;
      if (ttnAttr != null) {
        int totalTextNodes = NumberUtils.toInt(ttnAttr.getValue());
        if (totalTextNodes == 0) {
          remove.add(node);
          removed = true;
        }
      }

      if (!removed) {
        Attribute linkScoreAttr = elem.getAttribute(LINK_SCORE);
        Attribute numLinksAttr = elem.getAttribute(TOTAL_LINKS);
        if (linkScoreAttr != null && numLinksAttr != null) {
          double linkScore = NumberUtils.toDouble(linkScoreAttr.getValue());
          double numLinks = NumberUtils.toInt(numLinksAttr.getValue());
          if (numLinks >= 3 && linkScore > 0.60) {
            remove.add(node);
          }
        }
      }

      List<Content> children = elem.getContent();
      if (children != null && children.size() > 0) {
        ++level;
        for (Content child : children) {
          cleanNonTextNodes(level, child, remove);
        }
      }

    }
  }

  private Map<String, Object> scoreNodes(int level, Content node) {

    Map<String, Object> values = new HashMap<String, Object>();

    // don't go on forever, spider traps can kill JVM through stack overflow
    if (level == maxRecurseDepth) {
      return null;
    }

    if (node instanceof Text) {

      Text textNode = (Text)node;
      String text = textNode.getText();
      if (StringUtils.isNotBlank(text)) {

        int curTextSize = StringUtils.trim(text).length();
        values.put(TEXT_SIZE, curTextSize);

        Element parent = node.getParentElement();
        if (parent != null) {
          String parentName = StringUtils.lowerCase(parent.getName());
          if (StringUtils.equals(parentName, "a")) {
            values.put(TOTAL_TEXT_IN_LINKS, curTextSize);
            values.put(TOTAL_LINKS, 1);
          }
        }

        return values;
      }
    }
    else if (node instanceof Element) {

      int totalTextSize = 0;
      int curTextSize = 0;
      int totalTextInLinks = 0;
      int totalNodes = 0;
      int totalElementNodes = 0;
      int totalTextNodes = 0;
      int totalLinks = 0;
      int totalPTags = 0;
      int totalImages = 0;
      int totalH1Tags = 0;
      int totalLevels = 0;
      boolean hasDirectText = false;

      Element elem = (Element)node;
      String name = StringUtils.lowerCase(elem.getName());
      if (StringUtils.equals(name, "a")) {
        totalLinks++;
      }
      else if (StringUtils.equals(name, "p")) {
        totalPTags++;
        hasDirectText = true;
      }
      else if (StringUtils.equals(name, "img")) {
        totalImages++;
      }
      else if (StringUtils.equals(name, "h1")) {
        totalH1Tags++;
      }

      List<Content> children = elem.getContent();
      if (children != null && children.size() > 0) {

        for (Content child : children) {

          if (child instanceof Element) {
            totalNodes++;
            totalElementNodes++;

            Element childElem = (Element)child;
            String childName = StringUtils.lowerCase(childElem.getName());
            if (StringUtils.equals(childName, "p")) {
              hasDirectText = true;
            }
          }
          else if (child instanceof Text) {
            Text textNode = (Text)child;
            String text = textNode.getText();
            if (StringUtils.isNotBlank(text)) {
              totalTextNodes++;
              totalNodes++;
              hasDirectText = true;
            }
          }

          Map<String, Object> nestedValues = scoreNodes(level + 1, child);
          if (nestedValues != null) {
            if (nestedValues.containsKey(TEXT_SIZE)) {
              curTextSize += (Integer)nestedValues.get(TEXT_SIZE);
            }
            if (nestedValues.containsKey(TOTAL_TEXT_IN_LINKS)) {
              totalTextInLinks += (Integer)nestedValues
                .get(TOTAL_TEXT_IN_LINKS);
            }
            if (nestedValues.containsKey(TOTAL_LINKS)) {
              totalLinks += (Integer)nestedValues.get(TOTAL_LINKS);
            }
            if (nestedValues.containsKey(TOTAL_PTAGS)) {
              totalPTags += (Integer)nestedValues.get(TOTAL_PTAGS);
            }
            if (nestedValues.containsKey(TOTAL_H1TAGS)) {
              totalH1Tags += (Integer)nestedValues.get(TOTAL_H1TAGS);
            }
            if (nestedValues.containsKey(TOTAL_IMAGES)) {
              totalImages += (Integer)nestedValues.get(TOTAL_IMAGES);
            }
            if (nestedValues.containsKey(TOTAL_ELEMENT_NODES)) {
              totalElementNodes += (Integer)nestedValues
                .get(TOTAL_ELEMENT_NODES);
              totalNodes += totalElementNodes;
            }
            if (nestedValues.containsKey(TOTAL_TEXT_NODES)) {
              totalTextNodes += (Integer)nestedValues.get(TOTAL_TEXT_NODES);
              totalNodes += totalTextNodes;
            }
            if (nestedValues.containsKey(TOTAL_NODES)) {
              totalNodes += (Integer)nestedValues.get(TOTAL_NODES);
            }
            if (nestedValues.containsKey(TOTAL_LEVELS)) {
              totalLevels = (Integer)nestedValues.get(TOTAL_LEVELS);
            }
          }
        }
      }

      totalTextSize += curTextSize;
      if (hasDirectText) {
        elem.setAttribute(TEXT_SIZE, String.valueOf(curTextSize));
        values.put(TEXT_SIZE, curTextSize);
      }
      elem.setAttribute(TOTAL_TEXT_SIZE, String.valueOf(totalTextSize));
      elem.setAttribute(TOTAL_TEXT_IN_LINKS, String.valueOf(totalTextInLinks));
      elem.setAttribute(TEXT_SIZE_SCORE,
        String.valueOf(totalTextSize - totalTextInLinks));
      elem.setAttribute(TOTAL_NODES, String.valueOf(totalNodes));
      elem.setAttribute(TOTAL_ELEMENT_NODES, String.valueOf(totalElementNodes));
      elem.setAttribute(TOTAL_TEXT_NODES, String.valueOf(totalTextNodes));
      elem.setAttribute(TOTAL_LINKS, String.valueOf(totalLinks));
      elem.setAttribute(TOTAL_PTAGS, String.valueOf(totalPTags));
      elem.setAttribute(TOTAL_H1TAGS, String.valueOf(totalH1Tags));
      elem.setAttribute(TOTAL_IMAGES, String.valueOf(totalImages));
      elem.setAttribute(HAS_DIRECT_TEXT, String.valueOf(hasDirectText));
      elem.setAttribute(LEVEL, String.valueOf(level));
      elem.setAttribute(TOTAL_LEVELS, String.valueOf(totalLevels));

      values.put(TOTAL_TEXT_SIZE, totalTextSize);
      values.put(TOTAL_TEXT_IN_LINKS, totalTextInLinks);
      values.put(TOTAL_LINKS, totalLinks);
      values.put(TOTAL_PTAGS, totalPTags);
      values.put(TOTAL_H1TAGS, totalH1Tags);
      values.put(TOTAL_IMAGES, totalImages);
      values.put(TOTAL_NODES, totalNodes);
      values.put(TOTAL_ELEMENT_NODES, totalElementNodes);
      values.put(TOTAL_TEXT_NODES, totalTextNodes);
      values.put(TOTAL_LEVELS, totalLevels + 1);

      double linkScore = 0.0f;
      if (totalTextSize > 0 && totalTextInLinks > 0) {
        linkScore = (double)totalTextInLinks / (double)totalTextSize;
        elem.setAttribute(LINK_SCORE, String.valueOf(linkScore));
      }

      double textNodeScore = 0.0d;
      if (totalTextSize > 0 && totalElementNodes > 0) {
        textNodeScore = (double)totalTextSize * (1.0 - linkScore);
        elem.setAttribute(TEXT_NODE_SCORE, String.valueOf(textNodeScore));
      }
      else if (totalTextSize > 0) {
        elem.setAttribute(TEXT_NODE_SCORE, String.valueOf(1.0));
      }

      return values;
    }

    return null;

  }

  private int positionNodes(int level, Content node) {

    int pos = level;

    // don't go on forever, spider traps can kill JVM through stack overflow
    if (level == maxRecurseDepth) {
      return pos;
    }

    if (node instanceof Text) {
      return --level;
    }
    else if (node instanceof Element) {

      Element elem = (Element)node;
      elem.setAttribute(POSITION, String.valueOf(level));

      List<Content> children = elem.getContent();
      if (children != null && children.size() > 0) {

        for (Content child : children) {
          level = positionNodes(level + 1, child);
        }
      }
    }

    return level;
  }

  private void extractMediaElements(int level, Content node, List<Element> media) {

    // don't go on forever, spider traps can kill JVM through stack overflow
    if (level == maxRecurseDepth) {
      return;
    }

    if (node instanceof Element) {

      Element elem = (Element)node;
      String name = StringUtils.lowerCase(elem.getName());

      if (mediaElements.contains(name)) {
        media.add(elem);
      }
      else {

        List<Content> children = elem.getContent();
        if (children != null && children.size() > 0) {
          for (Content child : children) {
            extractMediaElements(++level, child, media);
          }
        }
      }
    }
  }

  private void removeTempAttrs(int level, Content node) {

    // don't go on forever, spider traps can kill JVM through stack overflow
    if (level == maxRecurseDepth) {
      return;
    }

    if (node instanceof Text) {
      return;
    }
    else if (node instanceof Element) {

      Element elem = (Element)node;
      removeProcessingAttributes(elem);
      List<Content> children = elem.getContent();
      if (children != null && children.size() > 0) {

        for (Content child : children) {
          removeTempAttrs(level + 1, child);
        }
      }
    }
  }

  @Override
  public boolean processContent(Document doc) {

    Element rootElem = doc.getRootElement();
    Set<Content> remove = new LinkedHashSet<Content>();

    // remove non-content nodes
    List<Content> contents = rootElem.getContent();
    for (Content child : contents) {
      cleanNonContentNodes(0, child, remove);
    }
    for (Content content : remove) {
      content.getParent().removeContent(content);
    }

    // remove structure nodes
    remove.clear();
    contents = rootElem.getContent();
    for (Content child : contents) {
      cleanStructureNodes(0, child, remove);
    }
    for (Content content : remove) {
      content.getParent().removeContent(content);
    }

    // position and score nodes
    contents = rootElem.getContent();
    for (Content child : contents) {
      positionNodes(0, child);
    }
    for (Content child : contents) {
      scoreNodes(0, child);
    }

    // create the holder for article node
    List<Content> articleNodes = new ArrayList<Content>();

    // find the highest scoring content area
    if (articleNodes.size() == 0) {
      for (Content child : contents) {
        findHighestNodeScore(0, child, articleNodes);
      }
    }

    // extract media elements from highest section
    List<Element> media = new ArrayList<Element>();
    if (extractMedia) {
      for (Content node : articleNodes) {
        extractMediaElements(0, node, media);
      }
    }

    // remove non-text nodes
    remove.clear();
    for (Content child : articleNodes) {
      cleanNonTextNodes(0, child, remove);
    }
    for (Content content : remove) {
      content.getParent().removeContent(content);
    }

    // remove temporary attributes from all nodes
    contents = rootElem.getContent();
    for (Content child : contents) {
      removeTempAttrs(0, child);
    }

    // remove temp attributes from media, theses could have gotten detached
    // and not be in the node tree cleaned above
    for (Element child : media) {
      removeTempAttrs(0, child);
      addExtractedValue("media", XMLUtils.toHtml(child));
    }

    if (articleNodes != null && articleNodes.size() > 0) {

      Element article = (Element)articleNodes.get(0);
      if (article != null) {
        if (extractHtml) {
          addExtractedValue("html", XMLUtils.toHtml(article));
        }
        if (extractText) {
          addExtractedValue("text", XMLUtils.toText(article));
        }
      }

      for (Content connected : articleNodes) {
        connected.detach();
      }
      rootElem.setContent(articleNodes);
    }

    return true;
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

  public boolean isExtractMedia() {
    return extractMedia;
  }

  public void setExtractMedia(boolean extractMedia) {
    this.extractMedia = extractMedia;
  }

  public int getMaxRecurseDepth() {
    return maxRecurseDepth;
  }

  public void setMaxRecurseDepth(int maxRecurseDepth) {
    this.maxRecurseDepth = maxRecurseDepth;
  }

}
