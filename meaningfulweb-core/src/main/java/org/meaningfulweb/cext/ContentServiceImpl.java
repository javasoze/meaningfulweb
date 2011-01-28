package org.meaningfulweb.cext;
import java.io.IOException;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.meaningfulweb.util.http.HttpClientService;
import org.meaningfulweb.util.JsonUtils;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentServiceImpl
  implements ContentService {

  private final static Logger LOG = LoggerFactory
    .getLogger(ContentServiceImpl.class);

  private String apiUrl;
  private String login;
  private String apiKey;
  private MimeTypes detector;
  private HttpClientService httpClientService;
  private HtmlExtractor htmlExtractor;

  public ContentServiceImpl() {
    detector = TikaConfig.getDefaultConfig().getMimeRepository();
  }

  public ShortUrlInfo getUrlInfo(String globalHash) {

    StringBuilder sb = new StringBuilder();
    Formatter formatter = new Formatter(sb, Locale.US);
    formatter.format("%s/bitly/%s/info.json", apiUrl, globalHash);
    String apiUrl = sb.toString();
    ShortUrlInfo urlInfo = new ShortUrlInfo();

    long start = System.currentTimeMillis();
    byte[] content = null;
    try {
      content = httpClientService.get(apiUrl);
    }
    catch (IOException e) {
      LOG.debug("Error getting url info: " + globalHash, e);
    }
    String urlInfoJson = content != null ? new String(content) : "";

    long end = System.currentTimeMillis();
    LOG.debug(urlInfoJson);
    LOG.debug("Got url info: " + globalHash + ", "
      + (float)((end - start) / 1000) + " seconds");

    if (StringUtils.isNotBlank(urlInfoJson)
      && JsonUtils.looksLikeJson(urlInfoJson)) {
      JsonNode root = null;
      try {
        root = JsonUtils.parseJson(urlInfoJson);
      }
      catch (IOException e) {
        LOG.error("Error parsing url info JSON for: " + globalHash);
      }
      if (root != null) {

        String url = JsonUtils.getStringValue(root, "url");
        String originalUrl = JsonUtils.getStringValue(root, "original_url");
        String urlFetched = JsonUtils.getStringValue(root, "url_fetched");
        String category = JsonUtils.getStringValue(root, "category");
        String contentType = JsonUtils.getStringValue(root, "content_type");
        int contentLength = JsonUtils.getIntValue(root, "content_length", -1);
        String sourceDomain = JsonUtils.getStringValue(root, "source_domain");

        // archived means an http code of 200, anything else will throw an
        // error when retrieving from the archive
        String httpCode = JsonUtils.getStringValue(root, "http_code");
        boolean archived = (StringUtils.isBlank(httpCode) || StringUtils
          .equals(httpCode, "200"));

        // timestamp is in seconds on millis, change for date
        long archivedTime = JsonUtils.getLongValue(root, "indexed", 0);
        archivedTime *= 1000;

        urlInfo.setUrl(url);
        urlInfo.setOriginalUrl(originalUrl);
        urlInfo.setCategory(category);
        urlInfo.setContentType(contentType);
        urlInfo.setContentLength(contentLength);
        urlInfo.setSourceDomain(sourceDomain);
        urlInfo.setUrlFetched(urlFetched);
        urlInfo.setArchived(archived);
        urlInfo.setArchivedTime(archivedTime);

        return urlInfo;
      }
    }
    else {
      LOG.debug("Bad url info: " + apiUrl);
    }

    return null;
  }

  public boolean queueForRecrawl(String globalHash) {

    StringBuilder sb = new StringBuilder();
    Formatter formatter = new Formatter(sb, Locale.US);
    formatter.format("%s/crawl?hash=%s&login=%s&apiKey=%s", apiUrl, globalHash,
      login, apiKey);
    String apiUrl = sb.toString();

    long start = System.currentTimeMillis();
    byte[] output = null;
    try {
      output = httpClientService.get(apiUrl);
    }
    catch (IOException e) {
      LOG.debug("Error queueing url for recrawl: " + globalHash, e);
    }

    String queueUrlJson = output != null ? new String(output) : "";
    long end = System.currentTimeMillis();
    LOG.debug(queueUrlJson);
    LOG.debug("Queued url for recrawl: " + globalHash + ", "
      + (float)((end - start) / 1000) + " seconds");

    if (StringUtils.isNotBlank(queueUrlJson)
      && JsonUtils.looksLikeJson(queueUrlJson)) {

      JsonNode root = null;
      try {
        root = JsonUtils.parseJson(queueUrlJson);
      }
      catch (IOException e) {
        LOG.error("Error parsing queue url JSON for: " + globalHash);
      }

      if (root != null) {
        JsonNode dataNode = root.get("data");
        String queuedStr = JsonUtils.getStringValue(dataNode, "queued");
        boolean queued = BooleanUtils.toBoolean(queuedStr);
        return queued;
      }
    }

    return true;
  }

  public byte[] getArchivedContent(String globalHash)
    throws IOException {

    StringBuilder sb = new StringBuilder();
    Formatter formatter = new Formatter(sb, Locale.US);
    formatter.format("%s/archive/%s?login=%s&apiKey=%s", apiUrl, globalHash,
      login, apiKey);
    String apiUrl = sb.toString();
    return httpClientService.get(apiUrl);
  }

  public Map<String, Object> getExtractedContent(String globalHash,
    List<String> extractPipelines, List<String> extractComponents,
    Map<String, Object> config, Map<String, Object> metadata) {

    long start = System.currentTimeMillis();
    byte[] contentBytes = null;
    try {
      contentBytes = getArchivedContent(globalHash);
    }
    catch (Exception e) {
      LOG.debug("Error getting archived content: " + globalHash, e);
      return null;
    }
    long end = System.currentTimeMillis();
    LOG.debug("Got cache: " + globalHash + ", " + (float)((end - start) / 1000)
      + " seconds");

    if (contentBytes != null && contentBytes.length > 0) {

      MimeType mimeType = detector.getMimeType(contentBytes);
      if (mimeType.getName().contains("html")) {

        Extract extract = new Extract();
        extract.setConfig(config);
        extract.setContent(contentBytes);
        extract.setMetadata(metadata);

        try {

          // add extraction pipelines
          if (extractPipelines != null && extractPipelines.size() > 0) {
            extract.getPipelines().addAll(extractPipelines);
          }

          // add extraction components
          if (extractComponents != null && extractComponents.size() > 0) {
            extract.getComponents().addAll(extractComponents);
          }

          htmlExtractor.extract(extract);
        }
        catch (Exception e) {
          LOG.error("Error extracting content for " + globalHash, e);
        }

        // translate from the extract name to field names
        return extract.getExtracted();

      }
    }

    return null;
  }

  public void setApiUrl(String apiUrl) {
    this.apiUrl = apiUrl;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public void setHttpClientService(HttpClientService httpClientService) {
    this.httpClientService = httpClientService;
  }

  public void setHtmlExtractor(HtmlExtractor htmlExtractor) {
    this.htmlExtractor = htmlExtractor;
  }
}
