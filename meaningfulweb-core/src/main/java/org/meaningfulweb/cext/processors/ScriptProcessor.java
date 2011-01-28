package org.meaningfulweb.cext.processors;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.util.XMLUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptProcessor
  extends HtmlContentProcessor {

  private final static Logger LOG = LoggerFactory
    .getLogger(ScriptProcessor.class);
  
  private static ScriptEngineManager factory = new ScriptEngineManager();

  private String scriptDir = null;
  private List<String> scripts = new ArrayList<String>();
  private boolean extractHtml = true;
  private boolean extractText = true;

  private String getScriptType(String filename) {
    if (filename.endsWith("rb")) {
      return "jruby";
    }
    else if (filename.endsWith("groovy") || filename.endsWith("gv")) {
      return "groovy";
    }
    else if (filename.endsWith("js")) {
      return "js";
    }
    return null;
  }

  public String getScriptDir() {
    return scriptDir;
  }

  public void setScriptDir(String scriptDir) {
    this.scriptDir = scriptDir;
  }

  public List<String> getScripts() {
    return scripts;
  }

  public void setScripts(List<String> scripts) {
    this.scripts = scripts;
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

    if (scripts != null && scripts.size() > 0) {

      Document tempDoc = new Document();
      tempDoc.addContent(document.cloneContent());

      for (String script : scripts) {

        // get the source of the script, return if no script or it is blank
        File scriptFile = new File(scriptDir, script);
        if (!scriptFile.exists()) {
          continue;
        }
        String scriptSource = null;
        try {
          scriptSource = FileUtils.readFileToString(scriptFile);
        }
        catch (IOException e) {
        }
        if (StringUtils.isBlank(scriptSource)) {
          continue;
        }

        // get the script engine, if none is available continue
        String engineType = getScriptType(script);
        if (StringUtils.isBlank(engineType)) {
          continue;
        }

        ScriptEngine engine = factory.getEngineByName(engineType);
        engine.put("doc", XMLUtils.toHtml(tempDoc));
        engine.put("output", new HashMap<String, Object>());

        try {
          engine.eval(scriptSource);
        }
        catch (ScriptException e) {
          LOG.error("Error processing script: " + script, e);
          continue;
        }

        Map<String, Object> output = (Map<String, Object>)engine.get("output");
        if (output != null && output.size() > 0) {
          for (Entry<String, Object> entry : output.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            if (val instanceof Element) {
              Element selElem = (Element)entry.getValue();
              if (extractHtml) {
                addExtractedValue(key, XMLUtils.toHtml(selElem));
              }
              if (extractText) {
                addExtractedValue(key + ".text", XMLUtils.toText(selElem));
              }
            }
            else if (val instanceof Text) {
              addExtractedValue(key, ((Text)val).getTextNormalize());
            }
            else if (val instanceof Comment) {
              addExtractedValue(key, ((Comment)val).getText());
            }
          }
        }
      }
    }

    return true;
  }
}
