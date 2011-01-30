package org.meaningfulweb.cext.processors;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.tika.parser.image.ExtractedContents;
import org.apache.tika.parser.image.ImageFetcher;
import org.apache.tika.parser.image.ImageFilter;
import org.apache.tika.parser.image.ImageInfo;
import org.apache.tika.parser.image.ImageMeta;
import org.apache.tika.parser.image.ImageSelector;
import org.jdom.Document;
import org.jdom.Element;
import org.meaningfulweb.cext.HtmlContentProcessor;
import org.meaningfulweb.util.JDomUtils;

public class BestImageContentProcessor extends HtmlContentProcessor {

	private final String _baseUrl;

	private final ImageFilter imageFilter = new ImageFilter(); 
	private final ImageFetcher imageFetcher = new ImageFetcher();
	private final ImageSelector imgSelector = new ImageSelector(imageFilter,imageFetcher);

	public static String toAbsoluteURL(String baseURL, String relativeURL) {
		// first we try to use java.net.URL to perform the conversion, if that
		// fails we can try using our own routine.
		try {
			URL base = new URL(baseURL);
			String absolute = new URL(base, relativeURL).toExternalForm();
			return absolute;
		} catch (MalformedURLException e) {
			String absolute = convertToAbsoluteURL(baseURL, relativeURL);
			return absolute;
		}
	}

	private static String convertToAbsoluteURL(String baseURL,
			String relativeURL) {
		if (baseURL == null || baseURL.length() < 8)
			throw new IllegalArgumentException("baseURL must be a valid URL");
		if (relativeURL == null)
			return null;

		// rooted relative URL
		if (relativeURL.startsWith("/")) {
			int pos = baseURL.indexOf("/", 8);
			if (pos > -1) {
				baseURL = baseURL.substring(0, pos);
			}
		} else {
			int slashPosition = baseURL.lastIndexOf('/');
			if (slashPosition < 0)
				throw new IllegalArgumentException(
						"baseURL must be a valid URL");
			baseURL = baseURL.substring(0, slashPosition);
			relativeURL = "/" + relativeURL;
		}
		return baseURL + relativeURL;
	}

	public BestImageContentProcessor(String baseUrl) {
		_baseUrl = baseUrl;
	}

	@Override
	public boolean processContent(Document doc) {
		Element rootElem = doc.getRootElement();
		List<Element> imgtags = JDomUtils.getElementsByName(rootElem, "img");
		int position = 0;
		LinkedList<ImageMeta> imgMeta = new LinkedList<ImageMeta>();
		for (Element imgtag : imgtags){
			String src = imgtag.getAttributeValue("src");
			String title = imgtag.getAttributeValue("title");
			String width = imgtag.getAttributeValue("width");
			
			String height=imgtag.getAttributeValue("height");
			
			String alt = imgtag.getAttributeValue("alt");
			String onclick = imgtag.getAttributeValue("onclick");
			
			String url =toAbsoluteURL(_baseUrl,src);
			ImageMeta imgInfo = new ImageMeta(position,alt,title,width,height,url,onclick);
			imgMeta.add(imgInfo);
			position ++;
		}
		

		ExtractedContents extractedContents = new ExtractedContents(_baseUrl,imgMeta);
		ImageInfo mediaContentInfo = imgSelector.getBestImage(extractedContents, _baseUrl, true, true);
		if (mediaContentInfo!=null){
			addExtractedValue("image", mediaContentInfo.getUri());
		}
		return true;
	}

}
