package org.meaningfulweb.client;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.parser.image.ExtractedContents;
import org.apache.tika.parser.image.ImageFetcher;
import org.apache.tika.parser.image.ImageFilter;
import org.apache.tika.parser.image.ImageInfo;
import org.apache.tika.parser.image.ImageMeta;
import org.apache.tika.parser.image.ImageSelector;

public class BestImageExtraction {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		String url = "http://twitpic.com/3sryl9";
		
		HtmlParser parser = new HtmlParser();
		
        HttpClient httpClient = new HttpClient();
		
		GetMethod get = new GetMethod(url);
		
		httpClient.executeMethod(get);
		
		Metadata metadata = new Metadata();
		metadata.add(Metadata.RESOURCE_NAME_KEY, url);
		metadata.add(Metadata.CONTENT_TYPE, get.getResponseHeader(Metadata.CONTENT_TYPE).getValue());

		List<ImageMeta> imgInfos = new LinkedList<ImageMeta>();
		org.apache.tika.parser.image.ImageExtractionContentHandler imgHandler = new org.apache.tika.parser.image.ImageExtractionContentHandler(imgInfos);


		parser.parse(get.getResponseBodyAsStream(), imgHandler, metadata, new ParseContext());
		
        ImageFilter imageFilter = new ImageFilter(); 
		
		ImageFetcher imageFetcher = new ImageFetcher();
		
		ImageSelector imgSelector = new ImageSelector(imageFilter,imageFetcher);
		
		ExtractedContents extractedContents = new ExtractedContents(url,imgInfos);
		
		ImageInfo mediaContentInfo = imgSelector.getBestImage(extractedContents, url, true, true);
		
		System.out.println("best image: "+mediaContentInfo);
		get.releaseConnection();
	}

}
