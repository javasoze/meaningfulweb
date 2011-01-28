package org.meaningfulweb.cext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ContentService {

  public ShortUrlInfo getUrlInfo(String globalHash);

  public byte[] getArchivedContent(String globalHash)
    throws IOException;

  public boolean queueForRecrawl(String globalHash);

  public Map<String, Object> getExtractedContent(String globalHash,
    List<String> extractPipelines, List<String> extractComponents,
    Map<String, Object> config, Map<String, Object> metadata);

}
