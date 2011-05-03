package org.meaningfulweb.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MetaContentExtractor {

	private static Logger logger = LoggerFactory.getLogger(MetaContentExtractor.class);
	
	private static final String RESOLVED_URL = "resolved-url";
	private static final String STATUS_CODE = "status-code";
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
			Object title = extracted.get("meaningfulweb.title");
			if (title!=null){
			  ogMeta.put("title",String.valueOf(title));
			}
			Object desc = extracted.get("meaningfulweb.description");
			if (desc!=null){
			  ogMeta.put("description", String.valueOf(desc));
			}
			Object img = extracted.get("meaningfulweb.image");
			if (img!=null){
			  ogMeta.put("image", String.valueOf(img));
			}
			Object content = extracted.get("meaningfulweb.text");
			if (content!=null){
			  ogMeta.put("content", String.valueOf(content));
			}
			
			Set<Entry<String,Object>> entries = extracted.entrySet();
			for (Entry<String,Object> entry : entries){
				Object val = entry.getValue();
				if (val!=null){
				  ogMeta.put(entry.getKey(), String.valueOf(val));
				}
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
	
	public MeaningfulWebObject extractFromUrl(String url) throws IOException{
		HttpClientService httpClient = HttpClientFactory.getHttpClientService();
		
		HttpGet httpget = null;
		
		String resolvedUrl;

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
		   HttpContext context = new BasicHttpContext();
		   HttpResponse response = httpClient.process(httpget,context);
		   
		   HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute( 
	                ExecutionContext.HTTP_REQUEST);
	       HttpHost currentHost = (HttpHost)  context.getAttribute( 
	                ExecutionContext.HTTP_TARGET_HOST);
	       resolvedUrl = currentHost.toURI() + currentReq.getURI();
	        
		   int statusCode = response.getStatusLine().getStatusCode();
		   
		   HttpEntity entity = response.getEntity();
		   if (statusCode < 400) {
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
				 httpget.abort();
			   }
			   
			   
		   }
		   else{
			   httpget.abort();
		   }
			
		   Map<String,String> metaMap = obj.getMeta();
		   metaMap.put(RESOLVED_URL, resolvedUrl);
		   metaMap.put(STATUS_CODE, String.valueOf(statusCode));
		 }
		 catch(IOException e){
		   httpget.abort();
		   throw e;
		 }
		 return obj;
	}
	
	public static void main(String[] args) throws Exception{
		MetaContentExtractor extractor = new MetaContentExtractor();
		String url = "http://twitpic.com/3sryl9";
		//String url = "http://www.amazon.co.jp/gp/product/B004O6LVMM?ie=UTF8&ref_=pd_ts_d_3&s=dvd&linkCode=shr&camp=1207&creative=8411&tag=pokopon0e-22";
		//String url ="http://www.useit.com/papers/anti-mac.html";
		//String url ="http://sns.mx/WGdXy4";
		//String url = "http://bit.ly/eL7wGH";
		//String url = "http://bit.ly/dK8DdN";
		
        MeaningfulWebObject obj = extractor.extractFromUrl(url);
		
		System.out.println(obj);
	}
	
	public static void main2(String[] args) throws Exception{
		String urlFile = new String("/Users/john/github/meaningfulweb/nulls.txt");
		File f = new File(urlFile);
		File outFile = new File("/Users/john/github/meaningfulweb/outfile.txt");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
		BufferedReader reader = new BufferedReader(new FileReader(f));
		int offset = "skipping url with null title: ".length();
		MetaContentExtractor extractor = new MetaContentExtractor();
		int count = 0;
		while(true){
			String line = reader.readLine();
			if (line==null) break;
			if (count%10 == 0){
				System.out.println(count+" urls processed.");
			}
			count++;
			try{
			  line = line.substring(offset);
			  MeaningfulWebObject obj = extractor.extractFromUrl(line);
			  String title = obj.getTitle();
			  if (title==null){
				  writer.write("no title: "+line+"\n");
			  }
			  else if ("null".equals(title)){
				  writer.write("null title: "+line+"\n");
			  }
			  writer.flush();
			}
			catch(Exception e){
				e.printStackTrace();
			}
			Thread.sleep(200);
		}
		
		reader.close();
		writer.close();
	}
}
