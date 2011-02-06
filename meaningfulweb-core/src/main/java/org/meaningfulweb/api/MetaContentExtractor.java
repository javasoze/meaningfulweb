package org.meaningfulweb.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
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
import org.meaningfulweb.util.http.HttpComponentsServiceImpl;
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
	private final HttpComponentsServiceImpl httpClientService = new HttpComponentsServiceImpl();
	
	public MetaContentExtractor(File configFile) throws Exception{
	  _detector = new TypeDetector();
	  _autoParser = new AutoDetectParser(_detector);
	  _txtParser = new TXTParser();
	  _htmlParser = new HtmlParser();
	  processorFactory = new HtmlContentProcessorFactory(configFile);
	  htmlExtractor = new HtmlExtractor();
	  htmlExtractor.setProcessorFactory(processorFactory);
	  httpClientService.initialize();
	}
	
	private Map doExtractFromHtml(String url,InputStream in,Set<String> components,
			Set<String> pipelines,Map<String,Object> config,Map<String,Object> meta) throws Exception{
			    // check for blank global hash
	    if (StringUtils.isBlank(url)) {
	      throw new IllegalArgumentException("url.required: Url is required and cannot be blank");
	    }

	    // check for no processors
	    boolean hasComponents = (components != null && components.size() > 0);
	    boolean hasPipelines = (pipelines != null && pipelines.size() > 0);
	    if (!hasComponents && !hasPipelines) {
	      throw new Exception("processors.requiredL "+
	        "One or more components or pipelines must be specified to process "
	          + "content");
	    }

	    // get the url content

	    byte[] content;
	    try {
	      content = httpClientService.get(in);
	    }
	    catch (Exception e) {
	      throw e;
	    }

	    // return an empty map if no content
	    if (content == null || content.length == 0) {
	      return new HashMap();
	    }

	    // process the content and return anything extracted
	    return extractContent(content, pipelines, components,config,meta);
	}
	
	private Map extractContent(byte[] content, Set<String> pipelines,
	  Set<String> components, Map<String, Object> config,
	  Map<String, Object> metadata) {
	
	  Map output = new HashMap();
	  if (content != null && content.length > 0) {
	
	      Extract extract = new Extract();
	      extract.setPipelines(pipelines);
	      extract.setComponents(components);
	      extract.setConfig(config);
	      extract.setContent(content);
	      extract.setMetadata(metadata);
	
	      try {
	        htmlExtractor.extract(extract);
	        output = extract.getExtracted();
	      }
	      catch (Exception e) {
	        logger.error("Error extracting content", e);
	      }
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

		    Map<String,Object> metaMap = new HashMap<String,Object>();
		    metaMap.put("url", url);
		    Map<String,Object> config = new HashMap<String,Object>();
			Map extracted = doExtractFromHtml(url,in,processorFactory.getComponentNames(),processorFactory.getPipelineNames(),config,metaMap);
			
			// We now have a string of text from the the page.
			//ogMeta.put("url", url);
			ogMeta.put("title",(String)extracted.get("title"));
			//ogMeta.put("description", desc);
			ogMeta.put("image", (String)extracted.get("image"));
			//System.out.println("extracted out: "+extracted);
			
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
		File confFile = new File("config/cextr.json");
		MetaContentExtractor extractor = new MetaContentExtractor(confFile);
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
