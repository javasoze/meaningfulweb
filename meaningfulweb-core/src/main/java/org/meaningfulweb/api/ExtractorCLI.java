package org.meaningfulweb.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.meaningfulweb.cext.Extract;
import org.meaningfulweb.cext.HtmlContentProcessorFactory;
import org.meaningfulweb.cext.HtmlExtractor;
import org.meaningfulweb.util.http.HttpComponentsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtractorCLI {

  private static Logger LOG = LoggerFactory.getLogger(ExtractorCLI.class);

  public static void main(String[] args)
    throws Exception {

    // the config file and the url
    File configFile = new File("config/cextr.json");
    String url = "http://twitpic.com/3sryl9";
    HtmlContentProcessorFactory processorFactory = new HtmlContentProcessorFactory(
      configFile);
    HtmlExtractor htmlExtractor = new HtmlExtractor();
    htmlExtractor.setProcessorFactory(processorFactory);
    HttpComponentsServiceImpl httpclient = new HttpComponentsServiceImpl();
    httpclient.initialize();

    // create base config
    Map<String, Object> config = new HashMap<String, Object>();
    config.put("perComponentDOM", false);
    config.put("perPipelineDOM", true);
    config.put("fullcontent.extractHtml", true);
    config.put("fullcontent.extractText", true);

    // create base metadata
    Map<String, Object> metadata = new HashMap<String, Object>();
    metadata.put("url", url);

    // create the pipelines and components to run
    List<String> pipelines = new ArrayList<String>();
    List<String> components = new ArrayList<String>();
    components.add("fullcontent");

    // get the html from the url
    long start = System.currentTimeMillis();
    byte[] contentBytes = null;
    try {
      contentBytes = httpclient.get(url);
    }
    catch (IOException e) {
      LOG.debug("Error getting url info: " + url, e);
    }

    if (contentBytes != null && contentBytes.length > 0) {

      MimeTypes detector = TikaConfig.getDefaultConfig().getMimeRepository();
      MimeType mimeType = detector.getMimeType(contentBytes);
      if (mimeType.getName().contains("html")) {

        Extract extract = new Extract();
        extract.setConfig(config);
        extract.setContent(contentBytes);
        extract.setMetadata(metadata);

        try {

          // add extraction pipelines
          if (pipelines != null && pipelines.size() > 0) {
            extract.getPipelines().addAll(pipelines);
          }

          // add extraction components
          if (components != null && components.size() > 0) {
            extract.getComponents().addAll(components);
          }

          htmlExtractor.extract(extract);
        }
        catch (Exception e) {
          LOG.error("Error extracting content", e);
        }

        // translate from the extract name to field names
        System.out.println(extract.getExtracted());

      }
    }

    httpclient.shutdown();
  }
}
