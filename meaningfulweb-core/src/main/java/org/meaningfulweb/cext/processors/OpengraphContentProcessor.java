package org.meaningfulweb.cext.processors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.Element;
import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.opengraph.OGObject;
import org.meaningfulweb.opengraph.OpenGraphParser;
import org.meaningfulweb.util.JDomUtils;

public class OpengraphContentProcessor extends HtmlContentProcessor {

	@Override
	public boolean processContent(Document doc) {
		Element rootElem = doc.getRootElement();
		List<Element> metatags = JDomUtils.getElementsByName(rootElem, "meta");
		Map<String,String> datamap = new HashMap<String,String>();
		for (Element elem : metatags){
			String attrVal = elem.getAttributeValue("property");
			if (attrVal!=null && attrVal.startsWith("og:")){
				String content = elem.getAttributeValue("content");
				String name = attrVal.substring(OpenGraphParser.OG_PREFIX_CHAR_COUNT);
				datamap.put(name, content);
			}
		}
		
		OGObject ogObj = OpenGraphParser.parse(datamap);
		if (!ogObj.isEmpty()){
		  Map<String,String> metaMap = ogObj.getMeta();
		  if (metaMap.size()>0){
		    Set<Entry<String,String>> entrySet = metaMap.entrySet();
		    for (Entry<String,String> entry : entrySet){
			  addExtractedValue(entry.getKey(), entry.getValue());  
		    }
		  }
		  Map<String,String> audioMap = ogObj.getAudio();
		  if (audioMap.size()>0){
		    addExtractedValue("audio", audioMap);
		  }
		  Map<String,String> videoMap = ogObj.getVideo();
		  if (videoMap.size()>0){
		    addExtractedValue("video", videoMap);
		  }
		}
		return true;
	}

}
