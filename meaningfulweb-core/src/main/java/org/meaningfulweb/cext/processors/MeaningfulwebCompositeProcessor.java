package org.meaningfulweb.cext.processors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.meaningfulweb.cext.HtmlContentProcessor;

public class MeaningfulwebCompositeProcessor extends HtmlContentProcessor {
	private final OpengraphContentProcessor _opengraphProcessor;
	private final ElementProcessor _elementProcessor;
	private final BoilerpipeArticleProcessor _boilerpipeProcessor;
	private final BestImageProcessor _bestimageProcessor;
	
	private static final String[] INTERESTED_HEADERS = new String[]{"keywords","description","image"};
	
	public MeaningfulwebCompositeProcessor(){
		_opengraphProcessor = new OpengraphContentProcessor();
		_opengraphProcessor.setIncludeAll(true);
		_elementProcessor = new ElementProcessor();
		_elementProcessor.setExtractHtml(false);
		_boilerpipeProcessor = new BoilerpipeArticleProcessor();
		_bestimageProcessor = new BestImageProcessor();
	}
	
	@Override
	public void setName(String name) {
		super.setName(name);
		_opengraphProcessor.setName(name);
		_elementProcessor.setName(name);
		_boilerpipeProcessor.setName(name);
		_bestimageProcessor.setName(name);
	}
	
	
	
	@Override
	public void setMetadata(Map<String, Object> metadata) {
		super.setMetadata(metadata);
		_opengraphProcessor.setMetadata(metadata);
		_elementProcessor.setMetadata(metadata);
		_boilerpipeProcessor.setMetadata(metadata);
		_bestimageProcessor.setMetadata(metadata);
	}

	@Override
	public boolean processContent(Document document) {
		boolean success;
		
		success = _opengraphProcessor.processContent(document);
		if (success){
			Map<String,Object> extracted = _opengraphProcessor.getExtracted();
		    getExtracted().putAll(extracted);   
		}
		
		if (getExtracted().get("title")==null){
			_elementProcessor.setElements(Arrays.asList(new String[]{"title"}));
		}
		
		Set<String> headerSet = new HashSet<String>();
		for (String header : INTERESTED_HEADERS){
		  if (getExtracted().get(header)==null){
			headerSet.add(header);
		  }
		}
		
		if (headerSet.size()>0){
		  _elementProcessor.setHeaders(headerSet);
		}
		
		if (_elementProcessor.getHeaders().size()>0 || _elementProcessor.getElements().size()>0){
			success = _elementProcessor.processContent(document);
			if (success){
				Map<String,Object> extracted = _elementProcessor.getExtracted();
			    getExtracted().putAll(extracted);   
			}
		}
		
		success = _boilerpipeProcessor.processContent(document);
		if (success){
			Map<String,Object> extracted = _boilerpipeProcessor.getExtracted();
		    getExtracted().putAll(extracted);   
		}
		
		if (getExtracted().get("image")==null){
		  success = _bestimageProcessor.processContent(document);
		  if (success){
			Map<String,Object> extracted = _bestimageProcessor.getExtracted();
		    getExtracted().putAll(extracted);
		  }
		}
		return success;
	}

}
