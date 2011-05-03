package org.meaningfulweb.cext.processors;

import org.meaningfulweb.cext.processors.DomainSpecifiedImageProcessor.DomainImageExtractionHandler;
import org.meaningfulweb.cext.processors.DomainSpecifiedImageProcessor.ExtractedImage;
import org.meaningfulweb.util.URLUtil;

public class TwitpicExtractionHandler extends DomainImageExtractionHandler {

	@Override
	public ExtractedImage extract(String url) {
		String host = URLUtil.getHost(url);
		String page = URLUtil.getPage(url);
		if (page.endsWith("/")){
			page = page.substring(0,page.length()-1);
		}
		int lastPart = page.lastIndexOf("/");
		page = page.substring(lastPart+1);
		String protocol = URLUtil.getProtocol(url);
		
		ExtractedImage img = new ExtractedImage();
		img.fullImage = protocol + "://" + host + "/show/full/"+page+".jpg";
		img.thumbnailImage = protocol + "://" + host + "/show/thumb/"+page+".jpg";
		return img;
	}
}
