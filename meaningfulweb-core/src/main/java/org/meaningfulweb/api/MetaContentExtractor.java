package org.meaningfulweb.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.txt.TXTParser;
import org.meaningfulweb.cext.Extract;
import org.meaningfulweb.cext.HtmlContentProcessorFactory;
import org.meaningfulweb.cext.HtmlExtractor;
import org.meaningfulweb.detector.DetectorFactory;
import org.meaningfulweb.util.ImageUtil;
import org.meaningfulweb.util.URIUtils;
import org.meaningfulweb.util.URLUtil;
import org.meaningfulweb.util.http.HttpClientFactory;
import org.meaningfulweb.util.http.HttpClientService;
import org.meaningfulweb.util.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MetaContentExtractor {

	private static Logger logger = LoggerFactory.getLogger(MetaContentExtractor.class);
	
	private final Detector _detector;
	private final Parser _autoParser;
	private final TXTParser _txtParser;
	
	
	private final HtmlContentProcessorFactory processorFactory;
	private final HtmlExtractor htmlExtractor;
	
	public MetaContentExtractor() throws Exception{
	  
	  _detector = DetectorFactory.getInstance().buildDetector();
	  _autoParser = new AutoDetectParser(_detector);
	  _txtParser = new TXTParser();
	// the config file and the url
	  
	  // TODO: should refactor here to take some sort of configuration object
	  String jsonConfig = "{\"components\": [{"+
	      "\"name\": \"meaningfulweb\","+
	      "\"class\": \"org.meaningfulweb.cext.processors.MeaningfulwebCompositeProcessor\"}]}";
	  processorFactory = new HtmlContentProcessorFactory(jsonConfig);
	  htmlExtractor = new HtmlExtractor();
	  htmlExtractor.setProcessorFactory(processorFactory);
	}
	
	private Map<String,Object> extractHTMLContent(String url,InputStream in) throws Exception{
	
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
	  
	  ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] contentBytes;
	    
	  IOUtils.copy(in, baos);
	  contentBytes = baos.toByteArray();
	      
      Extract extract = new Extract(contentBytes);
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
	
	public MeaningfulWebObject extract(String url,InputStream in,Metadata meta) throws Exception{
	  MeaningfulWebObject obj = new MeaningfulWebObject();
	  Map<String,String> ogMeta = obj.getMeta();
	  MediaType type = _detector.detect(in, meta);

	  String domain =  URLUtil.extractDomainFromUrl(url);
	  if (domain!=null){
	    obj.setDomain(domain);
	  }
	  ogMeta.put("content-type",type.toString());
	  if ("image".equals(type.getType())){
		ogMeta.put("image", url);
		ogMeta.put("title", url);
		ogMeta.put("url", url);
	  }
	  else if ("video".equals(type.getType())){
		ogMeta.put("image", ImageUtil.getVideoImage());
		ogMeta.put("title", url);
		ogMeta.put("url", url);
	  }
	  else if ("text".equals(type.getType())){
		ogMeta.put("type", "text");
		String subtype = type.getSubtype();
		if ("plain".equals(subtype)){
			parseMeta(_txtParser,in,meta,ogMeta);
		}
		else if ("html".equals(subtype)){

			Map<String,Object> extracted = extractHTMLContent(url,in);
			
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
		String subType = type.getSubtype();
		String imgUrl=null;
		if (subType.contains("pdf")){
			imgUrl = ImageUtil.getPDFImage();
		}
		else if (subType.contains("ps") || subType.contains("postscript")){
			imgUrl = ImageUtil.getPSImage();
		}
		else if (subType.contains("word") || subType.contains("doc")){
			imgUrl = ImageUtil.getWordImage();
		}
		else if (subType.contains("excel") || subType.contains("xsl")){
			imgUrl = ImageUtil.getExcelImage();
		}
		else if (subType.contains("powerpoint") || subType.contains("ppt")){
			imgUrl = ImageUtil.getPowerpointImage();
		}
		
		if (imgUrl!=null){
			ogMeta.put("image",imgUrl);
		}
	  }
	  else{
		  logger.error("unable to handle media type: "+type);
	  }
	  
	  return obj;
	}
	
	public MeaningfulWebObject extractFromUrl(String url){
		HttpClientService httpClient = HttpClientFactory.getHttpClientService();
		
		HttpGet httpget = null;

		 // if the uri is invalid try and clean it up a little before fetching
		 boolean isValidURI = URIUtils.isValidURI(url);
		 if (!isValidURI) {
		    String fixed = URIUtils.fixInvalidUri(url);
		    logger.info("Fixed invalid URI: " + url + " to " + fixed);
		    url = fixed;
		 }

		 httpget = new HttpGet(url);
		 MeaningfulWebObject obj = new MeaningfulWebObject();
		 try{
		   HttpEntity entity = httpClient.doRequest(httpget);
		   
		   Metadata metadata = new Metadata();
		   metadata.add(Metadata.RESOURCE_NAME_KEY, url);
		   metadata.add(Metadata.CONTENT_TYPE,entity.getContentType().getValue());
		   InputStream is = null;
		   try{
			 is = entity.getContent();
		     obj = extract(url, is, metadata);
		   }
		   catch(Exception e){
			 logger.error(e.getMessage(),e);
		   }
		   finally{
			 if (is!=null){
			   IOUtils.closeQuietly(is);
			 }
		   }
			
		 }
		 catch(HttpException e){
		   httpget.abort();
		 }
		 return obj;
	}
	
	public static void main(String[] args) throws Exception{
		MetaContentExtractor extractor = new MetaContentExtractor();
		//String url = "http://twitpic.com/3sryl9";
		//String url = "http://www.seobook.com/google-kills-ehows-competitors";
		//String url ="http://www.useit.com/papers/anti-mac.html";
		//String url ="http://sns.mx/WGdXy4";
		//String url = "http://bit.ly/eL7wGH";
		String url = "http://bit.ly/dK8DdN";
        MeaningfulWebObject obj = extractor.extractFromUrl(url);
		
		System.out.println(obj);
	}
}
