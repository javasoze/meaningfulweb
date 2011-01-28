package org.meaningfulweb.cext.processors;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.util.XMLUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;

public class ParagraphProcessor
  extends HtmlContentProcessor {

  private int sentenceThreshold = 2;
  private int numParagraphs = 0;
  private int maxRecurseDepth = 250;
  private boolean extractHtml = true;
  private boolean extractText = true;
  private boolean onlyTitled = false;
  private int minWords = 18;

  private static Set<String> titleContainers = new HashSet<String>();
  static {
    String[] title = {"h1", "h2", "h3"};
    titleContainers.addAll(Arrays.asList(title));
  }

  private static Set<String> ignoreContainers = new HashSet<String>();
  static {
    String[] ignore = {"h1", "h2", "h3", "h4", "h5", "head", "script",
      "noscript", "style", "form", "meta", "input", "iframe", "embed", "hr",
      "img", "link", "label", "table", "td", "th", "tr", "tbody", "thead",
      "tfoot", "col", "colgroup", "ul", "ol"};
    ignoreContainers.addAll(Arrays.asList(ignore));
  }

  private static Set<String> textContainers = new HashSet<String>();
  static {
    String[] containers = {"p", "span"};
    textContainers.addAll(Arrays.asList(containers));
  }

  private boolean findTitledParagraphBlocks(int level, boolean titleFound,
    Content node, List<Content> found) {

    // don't go on forever, spider traps can kill JVM through stack overflow
    if (level == maxRecurseDepth) {
      return titleFound;
    }

    if (node instanceof Element) {

      Element elem = (Element)node;
      String name = StringUtils.lowerCase(elem.getName());

      // ignore certain containers
      if (ignoreContainers.contains(name)) {
        return titleFound;
      }
      else {

        List<Content> children = elem.getContent();

        for (Content child : children) {

          if (child instanceof Element) {

            Element childElem = (Element)child;
            String childName = StringUtils.lowerCase(childElem.getName());
            if (titleContainers.contains(childName)) {
              titleFound = true;
            }

            if (titleFound && textContainers.contains(childName)) {

              // get the full text of the node tree, break into sentences and
              // count the number of sentences
              String contentText = XMLUtils.toText(childElem);
              BreakIterator sentenceIt = BreakIterator.getSentenceInstance();
              sentenceIt.setText(contentText);
              int numSentences = 0;
              while (sentenceIt.next() != BreakIterator.DONE) {
                numSentences++;
              }

              String trimmed = StringUtils.trim(contentText);

              boolean endsWithPunct = (StringUtils.endsWith(trimmed, ".")
                || StringUtils.endsWith(trimmed, "?")
                || StringUtils.endsWith(trimmed, "!") || StringUtils.endsWith(
                trimmed, ")"));
              String[] words = StringUtils.split(trimmed);
              int numWords = words != null ? words.length : 0;

              // if greater than a threshold then add to found
              if (numSentences >= sentenceThreshold && endsWithPunct
                && numWords >= minWords) {
                found.add(childElem);
              }
            }
            else {
              boolean foundATitle = findTitledParagraphBlocks(++level,
                titleFound, child, found);
              if (foundATitle) {
                titleFound = true;
              }
            }
          }
        }
      }
    }

    return titleFound;
  }

  private void findParagraphBlocks(int level, Content node, List<Content> found) {

    // don't go on forever, spider traps can kill JVM through stack overflow
    if (level == maxRecurseDepth) {
      return;
    }

    if (node instanceof Element) {

      Element elem = (Element)node;
      String name = StringUtils.lowerCase(elem.getName());

      // ignore certain containers
      if (ignoreContainers.contains(name)) {
        return;
      }
      else if (textContainers.contains(name)) {

        // get the full text of the node tree, break into sentences and count
        // the number of sentences
        String contentText = XMLUtils.toText((Element)node);
        BreakIterator sentenceIt = BreakIterator.getSentenceInstance();
        sentenceIt.setText(contentText);
        int numSentences = 0;
        while (sentenceIt.next() != BreakIterator.DONE) {
          numSentences++;
        }

        // if greater than a theshold then add to found
        if (numSentences >= sentenceThreshold) {
          found.add(node);
        }
      }
      else {

        // not a text container and not ignore, recurse to find text container
        List<Content> children = elem.getContent();
        if (children != null && children.size() > 0) {
          for (Content child : children) {
            findParagraphBlocks(++level, child, found);
          }
        }
      }
    }
  }

  @Override
  public boolean processContent(Document doc) {

    List<Content> found = new ArrayList<Content>();
    Element rootElem = doc.getRootElement();

    // search for all paragraphs or only titled paragraphs
    List<Content> contents = rootElem.getContent();
    if (onlyTitled) {
      for (Content child : contents) {
        findTitledParagraphBlocks(0, false, child, found);
      }
    }
    else {
      for (Content child : contents) {
        findParagraphBlocks(0, child, found);
      }
    }

    int numFound = found != null ? found.size() : 0;
    if (numFound > 0) {

      for (int i = 0; i < found.size()
        && (numParagraphs == 0 || i < numParagraphs); i++) {

        Content content = found.get(i);

        // add the content html
        if (extractHtml) {
          String contentHtml = XMLUtils.toHtml((Element)content);
          if (StringUtils.isNotBlank(contentHtml)) {
            addExtractedValue("html", contentHtml);
          }
        }

        // add the content text
        if (extractText) {
          String contentText = XMLUtils.toText((Element)content);
          if (StringUtils.isNotBlank(contentText)) {
            addExtractedValue("text", contentText);
          }
        }
      }
    }

    return true;
  }

  public boolean isOnlyTitled() {
    return onlyTitled;
  }

  public void setOnlyTitled(boolean onlyTitled) {
    this.onlyTitled = onlyTitled;
  }

  public int getSentenceThreshold() {
    return sentenceThreshold;
  }

  public void setSentenceThreshold(int sentenceThreshold) {
    this.sentenceThreshold = sentenceThreshold;
  }

  public int getNumParagraphs() {
    return numParagraphs;
  }

  public void setNumParagraphs(int numParagraphs) {
    this.numParagraphs = numParagraphs;
  }

  public int getMaxRecurseDepth() {
    return maxRecurseDepth;
  }

  public void setMaxRecurseDepth(int maxRecurseDepth) {
    this.maxRecurseDepth = maxRecurseDepth;
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

  public int getMinWords() {
    return minWords;
  }

  public void setMinWords(int minWords) {
    this.minWords = minWords;
  }

}
