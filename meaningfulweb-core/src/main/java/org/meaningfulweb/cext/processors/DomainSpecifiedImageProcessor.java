package org.meaningfulweb.cext.processors;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;
import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.util.URLUtil;

public class DomainSpecifiedImageProcessor extends HtmlContentProcessor {

	public static class ExtractedImage{
		public String fullImage;
		public String thumbnailImage;
	}
	
	public static abstract class DomainImageExtractionHandler{
		public abstract ExtractedImage extract(String url);
	}
	
	private Map<String,DomainImageExtractionHandler> _handlerMap = new HashMap<String,DomainImageExtractionHandler>();
	
	public void addExtractionHandler(String host,DomainImageExtractionHandler handler){
	  _handlerMap.put(host,handler);
	}
	
	@Override
	public boolean processContent(Document document) {
		Map<String,Object> extractedMap = this.getExtracted();
	    
		// image already extracted
	    if (extractedMap.containsKey("image")){
	    	return true;
	    }
	    
	    String url = (String)getMetadata().get("url");
	    String host = URLUtil.getHost(url);
	    
	    DomainImageExtractionHandler handler = _handlerMap.get(host);
	    if (handler!=null){
	    	ExtractedImage img = handler.extract(url);
	    	if (img!=null){
	    		extractedMap.put("image", img.thumbnailImage);
	    		extractedMap.put("fullimage", img.fullImage);
	    		return true;
	    	}
	    }
	    return false;
	}
}
