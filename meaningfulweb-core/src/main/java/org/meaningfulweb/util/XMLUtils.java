package org.meaningfulweb.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XMLUtils {

  private static void getText(Content node, StringBuilder builder) {

    if (node instanceof Element) {
      Element elem = (Element)node;
      List<Content> children = elem.getContent();
      if (children != null && children.size() > 0) {
        for (Content child : children) {
          getText(child, builder);
        }
      }
    }
    else if (node instanceof Text) {
      String textVal = StringUtils.trim(((Text)node).getTextNormalize() + " ");
      if (StringUtils.isNotBlank(textVal)) {
        String escaped = StringEscapeUtils.unescapeXml(textVal);
        builder.append(escaped + " ");
      }
    }
    else if (node instanceof Comment) {
      return;
    }

  }

  /**
   * Changes a non-ascii string into an HTML encoded ascii string.
   * 
   * @param notAscii The string to change.
   * 
   * @return The converted string.
   */
  public static String toAscii(String notAscii) {

    StringBuilder builder = new StringBuilder();
    char[] charArray = notAscii.toCharArray();
    for (int i = 0; i < charArray.length; ++i) {
      char a = charArray[i];
      if ((int)a > 255) {
        builder.append("&#" + (int)a + ";");
      }
      else {
        builder.append(a);
      }
    }
    return builder.toString();
  }

  /**
   * This method ensures that the output String has only valid XML unicode
   * characters as specified by the XML 1.0 standard. For reference, please see
   * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
   * standard</a>. This method will return an empty String if the input is null
   * or empty.
   * 
   * @param in The String whose non-valid characters we want to remove.
   * @return The in String, stripped of non-valid characters.
   */
  public static String stripNonValidXMLCharacters(String in) {
    StringBuffer out = new StringBuffer(); // Used to hold the output.
    char current; // Used to reference the current character.

    if (in == null || ("".equals(in)))
      return ""; // vacancy test.
    for (int i = 0; i < in.length(); i++) {
      current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here;
                              // it should not happen.
      if ((current == 0x9) || (current == 0xA) || (current == 0xD)
        || ((current >= 0x20) && (current <= 0xD7FF))
        || ((current >= 0xE000) && (current <= 0xFFFD))
        || ((current >= 0x10000) && (current <= 0x10FFFF)))
        out.append(current);
    }
    return out.toString();
  }

  public static String toXml(Document doc) {
    return toXml(doc, "UTF-8");
  }

  public static String toXml(Document doc, String encoding) {

    String htmlstr = null;
    try {
      // write out the xml to a string
      StringWriter writer = new StringWriter();
      Format format = Format.getPrettyFormat();
      format.setExpandEmptyElements(true);
      format.setOmitDeclaration(true);
      format.setEncoding(encoding);
      XMLOutputter out = new XMLOutputter(format);
      out.output(doc, writer);

      // xml processing will escape out certain characters that are legal in
      // html we convert those characters back here to html entity codes instead
      // of xml entitity codes. We also replace unicodeish characters to their
      // html entity equivalents. This helps in displaying with people don't
      // have the correct charset packs installed
      String output = StringEscapeUtils.unescapeXml(writer.toString());
      htmlstr = StringEscapeUtils.unescapeHtml(output);
      writer.close();
    }
    catch (IOException e) {
      // do nothing
    }

    return htmlstr;
  }

  public static String toHtml(Document doc) {
    return toHtml(doc, "UTF-8");
  }

  /**
   * Converts an XML Document object to HTML. This includes pretty printing the
   * document and adding the appropriate DocType headers.
   */
  public static String toHtml(Document doc, String encoding) {

    String htmlstr = null;
    try {
      // write out the xml to a string, without the xml declaration and use the
      // HTML outputter to add in an html doctype
      StringWriter writer = new StringWriter();
      Format format = Format.getPrettyFormat();
      format.setExpandEmptyElements(true);
      format.setOmitDeclaration(true);
      format.setEncoding(encoding);
      HTMLOutputter out = new HTMLOutputter(format);
      out.output(doc, writer);

      // xml processing will escape out certain characters that are legal in
      // html
      // we convert those characters back here to html entity codes instead of
      // xml entitity codes. We also replace unicodeish characters to their html
      // entity equivalents. This helps in displaying with people don't have the
      // correct charset packs installed
      String output = StringEscapeUtils.unescapeXml(writer.toString());
      htmlstr = StringEscapeUtils.unescapeHtml(output);
      writer.close();
    }
    catch (IOException e) {
      // do nothing
    }

    return htmlstr;
  }

  public static String toHtml(Element elem) {
    return toHtml(elem, "UTF-8");
  }

  public static String toHtml(Element elem, String encoding) {

    String htmlstr = null;
    try {
      // write out the xml to a string
      StringWriter writer = new StringWriter();
      Format format = Format.getPrettyFormat();
      format.setExpandEmptyElements(true);
      format.setOmitDeclaration(true);
      format.setEncoding(encoding);
      XMLOutputter out = new XMLOutputter(format);
      out.output(elem, writer);

      // xml processing will escape out certain characters that are legal in
      // html we convert those characters back here to html entity codes instead
      // of xml entitity codes. We also replace unicodeish characters to their
      // html entity equivalents. This helps in displaying with people don't
      // have the correct charset packs installed
      String output = StringEscapeUtils.unescapeXml(writer.toString());
      htmlstr = StringEscapeUtils.unescapeHtml(output);
      writer.close();
    }
    catch (IOException e) {
      // do nothing
    }

    return htmlstr;
  }

  /**
   * Converts an XML Document object to text.
   */
  public static String toText(Document doc) {
    Element rootElem = doc.getRootElement();
    return toText(rootElem);
  }

  /**
   * Converts an XML Document object to text.
   */
  public static String toText(Element elem) {

    // get only the text nodes from the dom
    StringBuilder builder = new StringBuilder();
    List<Content> contents = elem.getContent();
    for (Content child : contents) {
      getText(child, builder);
    }

    String text = builder.toString();
    text = HtmlExtractUtils.removeNewlines(text);
    text = HtmlExtractUtils.removeTags(text);
    text = HtmlExtractUtils.removeContiguousWhitespace(text);
    text = StringEscapeUtils.unescapeXml(text);
    text = StringUtils.trim(StringEscapeUtils.unescapeHtml(text));

    return text;
  }
}
