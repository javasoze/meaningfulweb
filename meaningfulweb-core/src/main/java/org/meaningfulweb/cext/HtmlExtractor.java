package org.meaningfulweb.cext;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;
import org.apache.tika.parser.txt.CharsetDetector;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.JDomSerializer;
import org.htmlcleaner.TagNode;
import org.jdom.Document;
import org.meaningfulweb.util.EncodingUtils;
import org.meaningfulweb.util.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlExtractor {

  private final static Logger LOG = LoggerFactory
    .getLogger(HtmlExtractor.class);

  // config variables affecting operation
  public static final String perComponentDOM = "perComponentDOM";
  public static final String perPipelineDOM = "perPipelineDOM";

  private HtmlContentProcessorFactory processorFactory;

  public void extract(Extract extract)
    throws Exception {

    // get the character set
    byte[] content = extract.getContent();
    CharsetDetector charDetect = new CharsetDetector();
    charDetect.setText(content);
    String charSet = charDetect.detect().getName();

    // clean the content of invalid xml characters
    String unclean = EncodingUtils.getEncodedString(content, charSet);
    String contentStr = XMLUtils.stripNonValidXMLCharacters(unclean);

    // setup the cleaner
    HtmlCleaner cleaner = new HtmlCleaner();
    CleanerProperties props = cleaner.getProperties();
    props.setUseCdataForScriptAndStyle(false);
    props.setOmitComments(true);
    props.setOmitUnknownTags(true);
    props.setOmitDoctypeDeclaration(true);
    props.setOmitXmlDeclaration(true);
    props.setRecognizeUnicodeChars(false);
    props.setAdvancedXmlEscape(true);
    props.setTranslateSpecialEntities(false);
    props.setNamespacesAware(false);
    props.setAllowHtmlInsideAttributes(true);
    props.setAllowMultiWordAttributes(true);

    // clean the html and serialize it to jdom
    TagNode nodes = cleaner.clean(contentStr);
    ExtractUtils.cleanInvalidAttributes(nodes);
    Document doc;
    try {
      doc = new JDomSerializer(props, true).createJDom(nodes);
    }
    catch (Exception e) {
      LOG.error("Error extracting content.", e);
      return;
    }

    // metadata, runtime config and extract
    Map<String, Object> runtime = extract.getConfig();
    Map<String, Object> metadata = extract.getMetadata();
    Map<String, Object> extracted = extract.getExtracted();

    // one or more components in a given order
    Set<String> compNames = extract.getComponents();
    if (compNames != null && compNames.size() > 0) {
      for (String compName : compNames) {

        // components can share dom or each can get a clean copy
        Document tempDoc = doc;
        boolean isPerComponentDOM = BooleanUtils.toBoolean((Boolean)runtime
          .get(perComponentDOM));
        if (isPerComponentDOM) {
          tempDoc = new Document();
          tempDoc.addContent(doc.cloneContent());
        }

        HtmlContentProcessor processor = processorFactory.getComponent(compName, runtime);
        processor.setMetadata(metadata);
        processor.processContent(tempDoc);

        Map<String, Object> curExtract = processor.getExtracted();
        for (String key : curExtract.keySet()) {
          String fullname = processor.getName() + "." + key;
          Object extractVal = curExtract.get(key);
          extracted.put(fullname, extractVal);
        }
      }
    }

    // one or more pipelines in a given order
    Set<String> plNames = extract.getPipelines();
    if (plNames != null && plNames.size() > 0) {
      for (String name : plNames) {

        // pipelines can share dom or each can get a clean copy
        Document tempDoc = doc;
        boolean isPerPipelineDOM = BooleanUtils.toBoolean((Boolean)runtime
          .get(perPipelineDOM));
        if (isPerPipelineDOM) {
          tempDoc = new Document();
          tempDoc.addContent(doc.cloneContent());
        }

        HtmlContentPipeline pipeline = processorFactory.getPipeline(name,
          runtime);
        if (pipeline != null) {
          pipeline.setMetadata(metadata);
          Map<String, Object> plOutput = pipeline.processPipeline(tempDoc);
          extracted.putAll(plOutput);
        }
      }
    }
  }

  public void setProcessorFactory(HtmlContentProcessorFactory processorFactory) {
    this.processorFactory = processorFactory;
  }
}
