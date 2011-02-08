package org.meaningfulweb.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.tika.detect.Detector;
import org.apache.tika.detect.TypeDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.parser.txt.TXTParser;
import org.meaningfulweb.cext.Extract;
import org.meaningfulweb.cext.HtmlContentProcessorFactory;
import org.meaningfulweb.cext.HtmlExtractor;
import org.meaningfulweb.opengraph.OGObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MetaContentExtractor {

	private static Logger logger = LoggerFactory.getLogger(MetaContentExtractor.class);
	
	private final Detector _detector;
	private final Parser _autoParser;
	private final TXTParser _txtParser;
	private final HtmlParser _htmlParser;
	
	
	private final HtmlContentProcessorFactory processorFactory;
	private final HtmlExtractor htmlExtractor;
	
	public MetaContentExtractor() throws Exception{
	  
	  _detector = new TypeDetector();
	  _autoParser = new AutoDetectParser(_detector);
	  _txtParser = new TXTParser();
	  _htmlParser = new HtmlParser();
	// the config file and the url
	  
	  // TODO: should refactor here to take some sort of configuration object
	  String jsonConfig = "{\"components\": [{"+
	      "\"name\": \"meaningfulweb\","+
	      "\"class\": \"org.meaningfulweb.cext.processors.MeaningfulwebCompositeProcessor\"}]}";
	  processorFactory = new HtmlContentProcessorFactory(jsonConfig);
	  htmlExtractor = new HtmlExtractor();
	  htmlExtractor.setProcessorFactory(processorFactory);
	}
	
	private Map<String,Object> extractHTMLContent(String url,InputStream in,String charset) {
	
	  // create base config
	  Map<String, Object> config = new HashMap<String, Object>();
	  config.put("perComponentDOM", false);
	  config.put("perPipelineDOM", true);

	    // create base metadata
	  Map<String, Object> metadata = new HashMap<String, Object>();
	  metadata.put("url", url);

	  // create the pipelines and components to run
	  List<String> components = new ArrayList<String>();

	  components.add("meaningfulweb");
	    
	  Map<String,Object> output = new HashMap<String,Object>();
      Extract extract = new Extract(in,charset);
      extract.getComponents().addAll(components);
      extract.setConfig(config);
      extract.setMetadata(metadata);

      try {
        htmlExtractor.extract(extract);
        output = extract.getExtracted();
      }
      catch (Exception e) {
        logger.error("Error extracting content", e);
      }
	  
	  return output;
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

	  ogMeta.put("content-type",type.toString());
	  if ("image".equals(type.getType())){
		ogMeta.put("image", url);
		ogMeta.put("title", url);
		ogMeta.put("url", url);
	  }
	  else if ("text".equals(type.getType())){
		String subtype = type.getSubtype();
		if ("plain".equals(subtype)){
			parseMeta(_txtParser,in,meta,ogMeta);
		}
		else if ("html".equals(subtype)){

			Map<String,Object> extracted = extractHTMLContent(url,in,charset);
			
			// We now have a string of text from the the page.
			ogMeta.put("url", url);
			ogMeta.put("title",(String)extracted.get("meaningfulweb.title"));
			ogMeta.put("description", (String)extracted.get("meaningfulweb.description"));
			ogMeta.put("image", (String)extracted.get("meaningfulweb.image"));
			ogMeta.put("content", (String)extracted.get("meaningfulweb.text"));
			
			Set<Entry<String,Object>> entries = extracted.entrySet();
			for (Entry<String,Object> entry : entries){
				ogMeta.put(entry.getKey(), String.valueOf(entry.getValue()));
			}
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
