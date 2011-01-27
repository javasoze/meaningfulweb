package org.meaningfulweb.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.BoilerpipeContentHandler;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.parser.image.ExtractedContents;
import org.apache.tika.parser.image.ImageExtractionContentHandler;
import org.apache.tika.parser.image.ImageFetcher;
import org.apache.tika.parser.image.ImageFilter;
import org.apache.tika.parser.image.ImageInfo;
import org.apache.tika.parser.image.ImageMeta;
import org.apache.tika.parser.image.ImageSelector;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.sax.BodyContentHandler;
import org.meaningfulweb.detector.DetectorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.meaningfulweb.opengraph.OGObject;
import org.meaningfulweb.opengraph.OpenGraphParser;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

public class MetaContentExtractor {

	private static Logger logger = LoggerFactory.getLogger(MetaContentExtractor.class);
	
	private final Detector _detector;
	private final Parser _autoParser;
	private final TXTParser _txtParser;
	private final HtmlParser _htmlParser;
	
	private final ImageFilter imageFilter = new ImageFilter(); 
	
	private final ImageFetcher imageFetcher = new ImageFetcher();
	
	private final ImageSelector imgSelector = new ImageSelector(imageFilter,imageFetcher);
	
	public MetaContentExtractor(){
	  _detector = DetectorBuilder.getInstance(null).buildDetector();
	  _autoParser = new AutoDetectParser(_detector);
	  _txtParser = new TXTParser();
	  _htmlParser = new HtmlParser();
	}
	
	static String trim(String str){
		return str==null ? "" : str.trim();
	}
	
	private static void parseMeta(Parser parser,InputStream in,Metadata meta,Map<String,String> ogmeta) throws IOException, SAXException, TikaException{
	  parser.parse(in, new DefaultHandler(), meta, new ParseContext());
	  String[] propnames = meta.names();
	  for (String propname : propnames){
	    String val = meta.get(propname);
	    ogmeta.put(propname, val);
	  }
	}
	
	public OGObject extract(String url,InputStream in,Metadata meta,String charset) throws Exception{
	  OGObject obj = new OGObject();
	  Map<String,String> ogMeta = obj.getMeta();
	  MediaType type = _detector.detect(in, meta);
	  if ("image".equals(type.getType())){
		ogMeta.put("image", url);
		ogMeta.put("type", "image");
		ogMeta.put("title", url);
		ogMeta.put("url", url);
	  }
	  else if ("text".equals(type.getType())){
		String subtype = type.getSubtype();
		if ("plain".equals(subtype)){
			parseMeta(_txtParser,in,meta,ogMeta);
		}
		else if ("html".equals(subtype)){
			/*obj = OpenGraphParser.parse(new InputStreamReader(in,charset));
			ogMeta = obj.getMeta();
			String title = obj.getTitle();
			String desc = obj.getDescription();
			String img = obj.getImage();
			*/
			String title = null;
			String desc = null;
			String img = null;

			List<ImageMeta> imgInfos = new LinkedList<ImageMeta>();
			
			if (title==null || desc==null){
				ArticleExtractor articleExtractor = new ArticleExtractor();	
				BoilerpipeContentHandler handler = null;
				ContentHandler baseHandler;
				if (img == null){
					baseHandler = new ImageExtractionContentHandler(imgInfos);
				}
				else{
					baseHandler = new DefaultHandler();
				}
				BodyContentHandler bodyhandler = new BodyContentHandler(baseHandler);
				handler =  new BoilerpipeContentHandler(bodyhandler,articleExtractor);
				
				_htmlParser.parse(in, handler, meta, new ParseContext());
				title = trim(meta.get(Metadata.TITLE));
				TextDocument textDoc = handler.toTextDocument();
				desc = articleExtractor.getText(textDoc);
				
				ExtractedContents extractedContents = new ExtractedContents(url,imgInfos);
				
				ImageInfo mediaContentInfo = imgSelector.getBestImage(extractedContents, url, true, true);
				img = mediaContentInfo == null ? "" : mediaContentInfo.getUri();
				
			}
			else if (img == null){
				ImageExtractionContentHandler handler = new ImageExtractionContentHandler(imgInfos);
				_htmlParser.parse(in, handler, meta, new ParseContext());
                ExtractedContents extractedContents = new ExtractedContents(url,imgInfos);
				
				ImageInfo mediaContentInfo = imgSelector.getBestImage(extractedContents, url, true, true);
				img = mediaContentInfo == null ? "" : mediaContentInfo.getUri();
			}
			
			
			// We now have a string of text from the the page.
			ogMeta.put("url", url);
			ogMeta.put("title",title);
			ogMeta.put("description", desc);
			ogMeta.put("image", img);
			
			
		}
	  }
	  else if ("application".equals(type.getType())){
		parseMeta(_autoParser,in,meta,ogMeta);
	  }
	  else{
		  logger.error("unable to handle media type: "+type);
	  }
	  
	  return obj;
	}
	
	public static void main(String[] args) throws Exception{
		MetaContentExtractor extractor = new MetaContentExtractor();
		String url = "http://twitpic.com/3sryl9";
        HttpClient httpClient = new HttpClient();
		
		GetMethod get = new GetMethod(url);
		
		httpClient.executeMethod(get);
		
		Metadata metadata = new Metadata();
		metadata.add(Metadata.RESOURCE_NAME_KEY, url);
		metadata.add(Metadata.CONTENT_TYPE, get.getResponseHeader(Metadata.CONTENT_TYPE).getValue());
		OGObject obj = extractor.extract(url, get.getResponseBodyAsStream(), metadata,get.getResponseCharSet());
		
		get.releaseConnection();
		
		System.out.println(obj);
	}
}
