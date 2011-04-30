package org.meaningfulweb.core.test;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meaningfulweb.api.MeaningfulWebObject;
import org.meaningfulweb.api.MetaContentExtractor;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;


public class MWCoreTest{

	private static final Logger logger = Logger.getLogger(MWCoreTest.class);
	
	static final File TestDataDir = new File("src/test/test-data");
	
	private static final int testPort = 4444;
	private static Server server = null;
	
	@BeforeClass
	public static void init(){
		server = new Server(testPort);
		ResourceHandler resource_handler=new ResourceHandler();        
		resource_handler.setResourceBase(TestDataDir.getAbsolutePath());        
        HandlerList handlers = new HandlerList();        
        handlers.setHandlers(new Handler[]{resource_handler,new DefaultHandler()});        
        server.setHandler(handlers);        
        try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}        
	}
	
	@AfterClass
	public static void shutdown(){
		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testHappyPath() throws Exception{
		MetaContentExtractor extractor = new MetaContentExtractor();
		String url = "http://localhost:"+testPort+"/small.html";
		MeaningfulWebObject obj = extractor.extractFromUrl(url);
		TestCase.assertEquals("smalltitle", obj.getTitle());
		TestCase.assertEquals("small description", obj.getDescription());
	}
	
	@Test
	public void testNullTitle() throws Exception{
		MetaContentExtractor extractor = new MetaContentExtractor();
		String url = "http://localhost:"+testPort+"/notitle.html";
		MeaningfulWebObject obj = extractor.extractFromUrl(url);
		TestCase.assertNull(obj.getTitle());
	}
}
