package org.meaningfulweb.ndd;
import java.util.List;

public interface NearDuplicateService {
  
  public List<String> getNearDuplicates(String content);
}
