package org.meaningfulweb.cext;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ly.bit.http.HttpClientService;
import ly.bit.http.HttpException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
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
  private ContentService contentService;

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

  private Map extractContent(byte[] content, List<String> pipelines,
    List<String> components, Map<String, Object> config,
    Map<String, Object> metadata) {

    Map output = new HashMap();
    if (content != null && content.length > 0) {

      Extract extract = new Extract();
      extract.setPipelines(pipelines);
      extract.setComponents(components);
      extract.setConfig(config);
      extract.setContent(content);
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

  @RequestMapping(value = "/ghash.json", method = RequestMethod.POST)
  public @ResponseBody
  Map extractContentFromGhash(@RequestBody ExtractForm extractForm,
    HttpServletRequest request, HttpServletResponse response) {

    Map errors = new HashMap();

    // check for blank global hash
    String globalHash = extractForm.getGlobalHash();
    if (StringUtils.isBlank(globalHash)) {
      errors.put("globalhash.required",
        "Global hash is required and cannot be blank");
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

    // get the archived content
    Map output = new HashMap();
    byte[] content;
    try {
      content = contentService.getArchivedContent(globalHash);
    }
    catch (IOException e) {
      Map<String, String> errMap = getErrors(e);
      errors.put("errors", errMap);
      return errors;
    }

    // get url info and set url into metadata if found
    ShortUrlInfo urlInfo = contentService.getUrlInfo(globalHash);
    if (urlInfo != null) {
      String url = urlInfo.getUrlFetched();
      extractForm.getMetadata().put("url", url);
    }

    // put global hash into metadata
    extractForm.getMetadata().put("globalHash", globalHash);

    // return an empty map if no content
    if (content == null || content.length == 0) {
      return output;
    }

    // process the content and return anything extracted
    return extractContent(content, pipelines, components,
      extractForm.getConfig(), extractForm.getMetadata());
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

    // process the content and return anything extracted
    return extractContent(content, pipelines, components,
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

    // process the content and return anything extracted
    return extractContent(contentBytes, pipelines, components,
      extractForm.getConfig(), extractForm.getMetadata());
  }

  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  public void setHtmlExtractor(HtmlExtractor htmlExtractor) {
    this.htmlExtractor = htmlExtractor;
  }

  public void setHttpClientService(HttpClientService httpClientService) {
    this.httpClientService = httpClientService;
  }

}
