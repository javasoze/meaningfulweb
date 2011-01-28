package org.meaningfulweb.cext.processors;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.util.XMLUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class RegexProcessor
  extends HtmlContentProcessor {

  public static final Log LOG = LogFactory.getLog(RegexProcessor.class);

  private Map<String, List<String>> regexes = new LinkedHashMap<String, List<String>>();

  @Override
  public boolean processContent(Document document) {

    if (regexes != null && regexes.size() > 0) {

      Document tempDoc = new Document();
      tempDoc.addContent(document.cloneContent());
      for (Entry<String, List<String>> entry : regexes.entrySet()) {

        String name = entry.getKey();
        List<String> regexValues = entry.getValue();
        for (String regex : regexValues) {
          try {
            Pattern curPattern = Pattern.compile(regex);
            String cleanedHtml = XMLUtils.toXml(tempDoc);
            Matcher curMatcher = curPattern.matcher(cleanedHtml);
            Set<String> uniqueMatches = new LinkedHashSet<String>();
            while (curMatcher.find()) {
              for (int i = 0; i <= curMatcher.groupCount(); i++) {
                uniqueMatches.add(curMatcher.group(i));
              }
            }
            List<String> matches = new ArrayList<String>();
            if (uniqueMatches != null && uniqueMatches.size() > 0) {
              matches.addAll(uniqueMatches);
              addExtractedValue(name + ".matches", matches);
            }
          }
          catch (Exception e) {
            // continue to the next regex
          }
        }
      }
    }

    return true;
  }

  public Map<String, List<String>> getRegexes() {
    return regexes;
  }

  public void setRegexes(Map<String, List<String>> regexes) {
    this.regexes = regexes;
  }

}
