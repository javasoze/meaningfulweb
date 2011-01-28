package org.meaningfulweb.util;

import java.io.IOException;
import java.io.Writer;

import org.jdom.Comment;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * An XMLOutputter that adds a html 4.01 doctype.
 */
public class HTMLOutputter
  extends XMLOutputter {

  public HTMLOutputter(Format format) {
    super(format);
  }

  public void output(Document document, Writer out)
    throws IOException {

    DocType type = new DocType("html", "-//W3C//DTD HTML 4.01//EN",
      "http://www.w3.org/TR/html4/strict.dtd");
    document.setDocType(type);
    
    super.output(document, out);
  }

  @Override
  protected void printComment(Writer out, Comment comment)
    throws IOException {
    return;
  }

}
