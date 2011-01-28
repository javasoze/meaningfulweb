package org.meaningfulweb.cext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;

public class HtmlContentPipeline {

  private List<HtmlContentProcessor> processors;
  private int current = 0;
  private String name;
  private Map<String, Object> metadata = new LinkedHashMap<String, Object>();

  public HtmlContentPipeline() {

  }

  public List<HtmlContentProcessor> getProcessors() {
    return processors;
  }

  public void setProcessors(List<HtmlContentProcessor> processors) {
    this.processors = processors;
  }

  public int getCurrent() {
    return current;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Object> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  public Map<String, Object> processPipeline(Document document) {

    Map<String, Object> extract = new LinkedHashMap<String, Object>();
    if (processors != null) {
      for (int i = 0; i < processors.size(); i++) {

        HtmlContentProcessor processor = processors.get(i);
        processor.setMetadata(metadata);
        String procName = processor.getName();
        boolean good = processor.processContent(document);
        Map<String, Object> curExtract = processor.getExtracted();
        for (String key : curExtract.keySet()) {
          String fullname = name + "." + procName + "." + key;
          Object extractVal = curExtract.get(key);
          extract.put(fullname, extractVal);
        }

        if (!good) {
          break;
        }
      }
    }

    return extract;
  }
}
