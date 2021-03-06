package org.meaningfulweb.cext.processors;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.imgext.ExtractedContents;
import org.meaningfulweb.imgext.ImageFetcher;
import org.meaningfulweb.imgext.ImageFilter;
import org.meaningfulweb.imgext.ImageInfo;
import org.meaningfulweb.imgext.ImageMeta;
import org.meaningfulweb.imgext.ImageSelector;
import org.meaningfulweb.util.URLUtil;

public class BestImageProcessor
  extends HtmlContentProcessor {

  private int imageMinWidth = 0;
  private int imageMinHeight = 0;
  private boolean removeImagesNoWidthHeight = true;
  private int maxRecurseDepth = 250;

  private final ImageFilter imageFilter = new ImageFilter();
  private final ImageFetcher imageFetcher = new ImageFetcher();
  private final ImageSelector imgSelector = new ImageSelector(imageFilter,
    imageFetcher);

  private void extractFromNodes(String baseUrl, String contextUrl,int level, Content node,
    LinkedList<ImageMeta> imgMetas) {

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

        String width = null;
        if (widthAttr != null) {
          width = widthAttr.getValue();
        }

        String height = null;
        if (heightAttr != null) {
          height = heightAttr.getValue();
        }

        /*
        int widthVal = NumberUtils.toInt(width, -1);
        int heightVal = NumberUtils.toInt(height, -1);
        boolean hasWidth = (!removeImagesNoWidthHeight && widthVal == -1)
          || widthVal >= imageMinWidth;
        boolean hasHeight = (!removeImagesNoWidthHeight && heightVal == -1)
          || heightVal >= imageMinHeight;
*/
        Attribute srcAttr = elem.getAttribute("src");
        String src = null;
        if (srcAttr != null) {
          src = srcAttr.getValue();
        }

        Attribute titleAttr = elem.getAttribute("title");
        String title = null;
        if (titleAttr != null) {
          title = StringUtils.lowerCase(titleAttr.getValue());
        }

        Attribute altAttr = elem.getAttribute("alt");
        String alt = null;
        if (altAttr != null) {
          alt = StringUtils.lowerCase(altAttr.getValue());
        }

        Attribute onclickAttr = elem.getAttribute("onclick");
        String onclick = null;
        if (onclickAttr != null) {
          onclick = StringUtils.lowerCase(onclickAttr.getValue());
        }

        if (src!=null) {
          String url = URLUtil.toAbsoluteURL(baseUrl, contextUrl,src);
          ImageMeta imgInfo = new ImageMeta(imgMetas.size(), alt, title, width,
            height,-1L, url, onclick);
          imgMetas.add(imgInfo);
        }

      }

      List<Content> children = elem.getContent();
      if (children != null && children.size() > 0) {
        for (Content child : children) {
          extractFromNodes(baseUrl, contextUrl,++level, child, imgMetas);
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

  @Override
  public boolean processContent(Document document) {
    Map<String,Object> extractedMap = this.getExtracted();
    
    // image already extracted
    if (extractedMap.containsKey("image")){
    	return true;
    }
    
    String baseUrl = null;
    String url = (String)getMetadata().get("url");
    String protocol = URLUtil.getProtocol(url);
    String host = URLUtil.getHost(url);
    baseUrl = protocol + "://" + host + "/";

    LinkedList<ImageMeta> imgMetas = new LinkedList<ImageMeta>();

    Element rootElem = document.getRootElement();
    List<Content> contents = rootElem.getContent();
    for (Content child : contents) {
      extractFromNodes(baseUrl, url,0, child, imgMetas);
    }

    ExtractedContents extracted = new ExtractedContents(baseUrl, imgMetas);
    ImageInfo mediaContentInfo = imgSelector.getBestImage(extracted, baseUrl,
      true, true);
    if (mediaContentInfo != null) {
      addExtractedValue("image", mediaContentInfo.getUri());
      Long size = mediaContentInfo.getSize();
      if (size!=null && size>0){
          addExtractedValue("image-content-length",size);
      }
    }
    return true;
  }

}
