package org.meaningfulweb.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class HtmlExtractUtils {

  private static final Pattern HEAD = Pattern.compile("<head.*?>.*?</head>",
    Pattern.CASE_INSENSITIVE);
  private static final Pattern STYLE_SHEETS = Pattern.compile(
    "<style.*?>.*?</style>", Pattern.CASE_INSENSITIVE);
  private static final Pattern SCRIPTS = Pattern.compile(
    "<script.*?>.*?</script>", Pattern.CASE_INSENSITIVE);
  private static final Pattern TAGS = Pattern.compile("<.*?>");
  private static final Pattern COMMENTS = Pattern.compile("<!--.*?-->");
  private static final Pattern SPECIAL = Pattern.compile("&.*?;");
  private static final Pattern NEWLINES = Pattern.compile("\n+");
  private static final Pattern WHITESPACE = Pattern.compile("\\s+");

  private static final Set<String> attributes = new HashSet<String>();
  static {
    String[] validAttrs = new String[]{"abbr", "accept-charset", "accept",
      "accesskey", "action", "align", "alink", "alt", "archive", "axis",
      "background", "bgcolor", "border", "cellpadding", "cellspacing", "char",
      "charoff", "charset", "checked", "cite", "class", "classid", "clear",
      "code", "codebase", "codetype", "color", "cols", "colspan", "compact",
      "content", "coords", "data", "datetime", "declare", "defer", "dir",
      "disabled", "enctype", "face", "for", "frame", "frameborder", "headers",
      "height", "href", "hreflang", "hspace", "http-equiv", "id", "ismap",
      "label", "lang", "language", "link", "longdesc", "marginheight",
      "marginwidth", "maxlength", "media", "method", "multiple", "name",
      "nohref", "noresize", "noshade", "nowrap", "object", "onblur",
      "onchange", "onclick", "ondblclick", "onfocus", "onkeydown",
      "onkeypress", "onkeyup", "onload", "onmousedown", "onmousemove",
      "onmouseout", "onmouseover", "onmouseup", "onreset", "onselect",
      "onsubmit", "onunload", "profile", "prompt", "property", "readonly",
      "rel", "rev", "rows", "rowspan", "rules", "scheme", "scope", "scrolling",
      "selected", "shape", "size", "span", "src", "standby", "start", "style",
      "summary", "tabindex", "target", "text", "title", "type", "usemap",
      "valign", "value", "valuetype", "version", "vlink", "vspace", "width"};
    attributes.addAll(Arrays.asList(validAttrs));
  }

  public static String removeHead(String content) {
    // Remove any contiguous whitespace and replace with single space
    Matcher head = HEAD.matcher(content);
    while (head.find()) {
      content = head.replaceAll(" ");
    }
    return content;
  }

  public static String removeNewlines(String content) {
    // Remove new line characters, replace with spaces
    Matcher mnLines = NEWLINES.matcher(content);
    while (mnLines.find()) {
      content = mnLines.replaceAll(" ");
    }
    return content;
  }

  public static String removeStyleSheets(String content) {
    // Remove style tags & inclusive content
    Matcher mstyles = STYLE_SHEETS.matcher(content);
    while (mstyles.find()) {
      content = mstyles.replaceAll("");
    }
    return content;
  }

  public static String removeScripts(String content) {
    // Remove script tags & inclusive content
    Matcher mscripts = SCRIPTS.matcher(content);
    while (mscripts.find()) {
      content = mscripts.replaceAll("");
    }
    return content;
  }

  public static String removeTags(String content) {
    // Remove primary HTML tags
    Matcher mtags = TAGS.matcher(content);
    while (mtags.find()) {
      content = mtags.replaceAll(" ");
    }
    return content;
  }

  public static String removeComments(String content) {
    // Remove comment tags & inclusive content
    Matcher mcomments = COMMENTS.matcher(content);
    while (mcomments.find()) {
      content = mcomments.replaceAll(" ");
    }
    return content;
  }

  public static String removeSpecialCharacters(String content) {
    // Remove special characters, such as &nbsp;
    Matcher msChars = SPECIAL.matcher(content);
    while (msChars.find()) {
      content = msChars.replaceAll("");
    }
    return content;
  }

  public static String removeContiguousWhitespace(String content) {
    // Remove any contiguous whitespace and replace with single space
    Matcher endWhites = WHITESPACE.matcher(content);
    while (endWhites.find()) {
      content = endWhites.replaceAll(" ");
    }
    return StringUtils.trim(content);
  }

  public static String extractAllText(byte[] htmlBytes) {
    String content = new String(htmlBytes);
    content = removeNewlines(content);
    content = removeStyleSheets(content);
    content = removeScripts(content);
    content = removeTags(content);
    content = removeComments(content);
    content = removeSpecialCharacters(content);
    return content;
  }

  public static String extractBodyText(byte[] htmlBytes) {
    String content = new String(htmlBytes);
    content = removeNewlines(content);
    content = removeHead(content);
    content = removeStyleSheets(content);
    content = removeScripts(content);
    content = removeTags(content);
    content = removeComments(content);
    content = removeSpecialCharacters(content);
    return extractAllText(content.getBytes());
  }

  public static boolean isValidAttribute(String name) {
    return attributes.contains(StringUtils.lowerCase(name));
  }
}
