package org.meaningfulweb.cext.processors;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.util.XMLUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;

public class ImageProcessor
  extends HtmlContentProcessor {

  private int maxRecurseDepth = 200;

  private int imageMinWidth = 0;
  private int imageMinHeight = 0;
  private boolean removeImagesNoWidthHeight = true;

  private Set<String> imageExclusions = new LinkedHashSet<String>();
  {
    String[] commonImageNames = {};
    imageExclusions.addAll(Arrays.asList(commonImageNames));
  }

  private void extractFromNodes(int level, Content node) {

    // don't go on forever, spider traps can kill JVM through stack overflow
    if (node == null || level == maxRecurseDepth) {
      return;
    }

    if (node instanceof Element) {

      Element elem = (Element)node;
      String name = StringUtils.lowerCase(elem.getName());

      // extract out elements by name
      if (StringUtils.equalsIgnoreCase(name, "img")) {

        Attribute widthAttr = elem.getAttribute("width");
        Attribute heightAttr = elem.getAttribute("height");
        int width = -1;
        int height = -1;

        if (widthAttr != null) {
          String widthVal = StringUtils.lowerCase(widthAttr.getValue());
          width = NumberUtils.toInt(widthVal, -1);
        }
        if (heightAttr != null) {
          String heightVal = StringUtils.lowerCase(heightAttr.getValue());
          height = NumberUtils.toInt(heightVal, -1);
        }

        Attribute srcAttr = elem.getAttribute("src");
        String src = null;
        if (srcAttr != null) {
          src = StringUtils.lowerCase(srcAttr.getValue());
        }

        boolean hasWidth = (!removeImagesNoWidthHeight && width == -1)
          || width >= imageMinWidth;
        boolean hasHeight = (!removeImagesNoWidthHeight && height == -1)
          || height >= imageMinHeight;

        boolean isExcluded = false;
        for (String excluded : imageExclusions) {
          if (StringUtils.contains(src, excluded)) {
            isExcluded = true;
            break;
          }
        }

        if (!isExcluded && hasWidth && hasHeight) {
          addExtractedValue("html", XMLUtils.toHtml(elem));
        }

      }

      List<Content> children = elem.getContent();
      if (children != null && children.size() > 0) {
        for (Content child : children) {
          extractFromNodes(++level, child);
        }
      }
    }
  }

  public int getMaxRecurseDepth() {
    return maxRecurseDepth;
  }

  public void setMaxRecurseDepth(int maxRecurseDepth) {
    this.maxRecurseDepth = maxRecurseDepth;
  }

  public boolean isRemoveImagesNoWidthHeight() {
    return removeImagesNoWidthHeight;
  }

  public void setRemoveImagesNoWidthHeight(boolean removeImagesNoWidthHeight) {
    this.removeImagesNoWidthHeight = removeImagesNoWidthHeight;
  }

  public int getImageMinWidth() {
    return imageMinWidth;
  }

  public void setImageMinWidth(int imageMinWidth) {
    this.imageMinWidth = imageMinWidth;
  }

  public int getImageMinHeight() {
    return imageMinHeight;
  }

  public void setImageMinHeight(int imageMinHeight) {
    this.imageMinHeight = imageMinHeight;
  }

  public Collection<String> getImageExclusions() {
    return imageExclusions;
  }

  public void setImageExclusions(Collection<String> imageExclusions) {
    if (imageExclusions != null) {
      if (imageExclusions instanceof Set) {
        this.imageExclusions = (Set<String>)imageExclusions;
      }
      else {
        Set<String> newExclusions = new LinkedHashSet<String>();
        newExclusions.addAll(imageExclusions);
        this.imageExclusions = newExclusions;
      }
    }
  }

  @Override
  public boolean processContent(Document document) {

    Element rootElem = document.getRootElement();
    List<Content> contents = rootElem.getContent();
    for (Content child : contents) {
      extractFromNodes(0, child);
    }
    return true;
  }

}
