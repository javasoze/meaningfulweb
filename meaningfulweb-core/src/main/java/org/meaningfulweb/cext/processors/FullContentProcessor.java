package org.meaningfulweb.cext.processors;
import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.util.XMLUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class FullContentProcessor
  extends HtmlContentProcessor {

  public static final Log LOG = LogFactory.getLog(FullContentProcessor.class);

  private boolean extractHtml = true;
  private boolean extractText = true;

  public FullContentProcessor() {

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

  @Override
  public boolean processContent(Document document) {

    // add the full html of the document
    if (extractHtml) {
      String fullHtml = XMLUtils.toHtml(document);
      addExtractedValue("html", fullHtml);
    }

    // add the full text of the document
    if (extractText) {
      String fullText = XMLUtils.toText(document);
      addExtractedValue("text", fullText);
    }

    return true;
  }
}
