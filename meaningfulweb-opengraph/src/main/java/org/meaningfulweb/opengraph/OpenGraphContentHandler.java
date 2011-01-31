package org.meaningfulweb.opengraph;

import java.util.HashMap;
import java.util.Map;

import net.htmlparser.jericho.HTMLElementName;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OpenGraphContentHandler extends DefaultHandler {
	private final Map<String,String> metaMap = new HashMap<String,String>();
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		
		if (HTMLElementName.META.equals(localName) && HTMLElementName.META.equals(qName)){
			String attrVal = atts.getValue("property");
			if (attrVal!=null && attrVal.startsWith("og:")){
				String content = atts.getValue("content");
				String name = attrVal.substring(OpenGraphParser.OG_PREFIX_CHAR_COUNT);
				metaMap.put(name, content);
			}
		}

	}
	
	public OGObject extractOpenGraphMeta(){
		return OpenGraphParser.parse(metaMap);
	}
}
