package org.meaningfulweb.cext;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.meaningfulweb.util.http.HttpClientService;
import org.meaningfulweb.util.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/content/extract")
public class HtmlExtractorController {

  private final static Logger LOG = LoggerFactory
    .getLogger(HtmlExtractorController.class);

  @Autowired
  private HtmlExtractor htmlExtractor;

  @Autowired
  @Qualifier("httpClientService")
  private HttpClientService httpClientService;

  private Map getErrors(Exception e) {
    Map<String, String> errMap = new LinkedHashMap<String, String>();
    if (e instanceof HttpException) {
      HttpException he = (HttpException)e;
      errMap.put("statusCode", String.valueOf(he.getStatusCode()));
    }
    errMap.put("message", e.getMessage());
    return errMap;
  }

  private Map extractContent(byte[] content, Set<String> pipelines,
    Set<String> components, Map<String, Object> config,
    Map<String, Object> metadata) {

    Map output = new HashMap();
    if (content != null && content.length > 0) {

      Extract extract = new Extract(new ByteArrayInputStream(content),"UTF-8");
      extract.setPipelines(pipelines);
      extract.setComponents(components);
      extract.setConfig(config);
      extract.setMetadata(metadata);

      try {
        htmlExtractor.extract(extract);
        output = extract.getExtracted();
      }
      catch (Exception e) {
        LOG.error("Error extracting content", e);
      }
    }
    return output;
  }

  @RequestMapping(value = "/url.json", method = RequestMethod.POST)
  public @ResponseBody
  Map extractContentFromUrl(@RequestBody ExtractForm extractForm,
    HttpServletRequest request, HttpServletResponse response) {

    Map errors = new HashMap();

    // check for blank global hash
    String url = extractForm.getUrl();
    if (StringUtils.isBlank(url)) {
      errors.put("url.required", "Url is required and cannot be blank");
    }

    // check for no processors
    List<String> components = extractForm.getComponents();
    boolean hasComponents = (components != null && components.size() > 0);
    List<String> pipelines = extractForm.getPipelines();
    boolean hasPipelines = (pipelines != null && pipelines.size() > 0);
    if (!hasComponents && !hasPipelines) {
      errors.put("processors.required",
        "One or more components or pipelines must be specified to process "
          + "content");
    }

    // return errors if any exist
    if (errors.size() > 0) {
      return errors;
    }

    // add the url to the metadata
    extractForm.getMetadata().put("url", url);

    // get the url content
    Map output = new HashMap();
    byte[] content;
    try {
      content = httpClientService.get(url);
    }
    catch (Exception e) {
      Map<String, String> errMap = getErrors(e);
      errors.put("errors", errMap);
      return errors;
    }

    // return an empty map if no content
    if (content == null || content.length == 0) {
      return output;
    }
    
    
    HashSet<String> componetSet = new HashSet<String>();
    componetSet.addAll(components);

    HashSet<String> pipelineSet = new HashSet<String>();
    pipelineSet.addAll(pipelines);

    // process the content and return anything extracted
    return extractContent(content, pipelineSet, componetSet,
      extractForm.getConfig(), extractForm.getMetadata());
  }

  @RequestMapping(value = "/bytes.json", method = RequestMethod.POST)
  public @ResponseBody
  Map extractContentFromBytes(@RequestBody ExtractForm extractForm,
    HttpServletRequest request, HttpServletResponse response) {

    Map errors = new HashMap();

    // check for blank global hash
    String content = extractForm.getContent();
    if (StringUtils.isBlank(content)) {
      errors.put("content.required", "A post of the content bytes "
        + "is required, either utf-8 or base64 encoded.");
    }

    // check for no processors
    List<String> components = extractForm.getComponents();
    boolean hasComponents = (components != null && components.size() > 0);
    List<String> pipelines = extractForm.getPipelines();
    boolean hasPipelines = (pipelines != null && pipelines.size() > 0);
    if (!hasComponents && !hasPipelines) {
      errors.put("processors.required",
        "One or more components or pipelines must be specified to process "
          + "content");
    }

    // return errors if any exist
    if (errors.size() > 0) {
      return errors;
    }

    Map output = new HashMap();
    byte[] contentBytes = content.getBytes();
    if (Base64.isArrayByteBase64(contentBytes)) {

      // base64 decode the content into bytes, which should be the document
      // content to extract from
      try {
        Base64 base64 = new Base64();
        contentBytes = base64.decode(contentBytes);
        contentBytes = StringEscapeUtils.unescapeHtml(new String(contentBytes))
          .getBytes();
      }
      catch (Exception e) {
        Map<String, String> errMap = getErrors(e);
        errors.put("errors", errMap);
        return errors;
      }

    }

    // return an empty map if no content
    if (contentBytes == null || contentBytes.length == 0) {
      return output;
    }

    HashSet<String> componetSet = new HashSet<String>();
    componetSet.addAll(components);

    HashSet<String> pipelineSet = new HashSet<String>();
    pipelineSet.addAll(pipelines);
    // process the content and return anything extracted
    return extractContent(contentBytes, pipelineSet, componetSet,
      extractForm.getConfig(), extractForm.getMetadata());
  }

  public void setHtmlExtractor(HtmlExtractor htmlExtractor) {
    this.htmlExtractor = htmlExtractor;
  }

  public void setHttpClientService(HttpClientService httpClientService) {
    this.httpClientService = httpClientService;
  }

}
