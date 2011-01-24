package org.meaningfulweb.client;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.detect.TypeDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.xml.sax.helpers.DefaultHandler;

public class MeaningfulClient {

	static Detector buildDetector(){
		return new Detector(){
			private Detector typeDetector = new TypeDetector();
			private Detector defaultDetector = new DefaultDetector();
			
			@Override
			public MediaType detect(InputStream input, Metadata metadata)
					throws IOException {
				MediaType type = typeDetector.detect(input, metadata);
				if (MediaType.OCTET_STREAM == type){
					System.out.println("fail over to default detector");
					type = defaultDetector.detect(input, metadata);
				}
				System.out.println("returning type: "+type);
				return type;
			}
			
		};
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		String url ="http://sunset.usc.edu/classes/cs572_2010/Introduction_to_Tika.ppt";
        Detector detector = buildDetector();
		
		HttpClient httpClient = new HttpClient();
		
		GetMethod get = new GetMethod(url);
		
		httpClient.executeMethod(get);
		
		Metadata meta = new Metadata();
		
		meta.set(Metadata.CONTENT_TYPE, get.getResponseHeader(Metadata.CONTENT_TYPE).getValue());
		
		AutoDetectParser parser = new AutoDetectParser(detector);
		
		parser.parse(get.getResponseBodyAsStream(), new DefaultHandler(), meta);
		
		System.out.println(meta);
		get.releaseConnection();
	}

}
