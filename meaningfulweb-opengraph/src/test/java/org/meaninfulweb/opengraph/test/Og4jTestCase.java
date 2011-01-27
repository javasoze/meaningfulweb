package org.meaningfulweb.opengraph.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import junit.framework.TestCase;
import org.meaningfulweb.opengraph.OGObject;
import org.meaningfulweb.opengraph.OpenGraphParser;

public class Og4jTestCase extends TestCase {

	static final File TestDataDir = new File("src/test/test-data");
	
	private static OGObject read(File f) throws IOException{
		FileReader reader = new FileReader(f);
		OGObject obj = OpenGraphParser.parse(reader);
		reader.close();
		return obj;
	}
	
	public void testEmpty() throws Exception{
		File emptyHtml = new File(TestDataDir,"empty.html");
		OGObject obj = read(emptyHtml);
		assertTrue(obj.isEmpty());
		assertFalse(obj.isValid());
	}
	
	public void testInvalid() throws Exception{
		File invalidHtml = new File(TestDataDir,"invalid.html");
		OGObject obj = read(invalidHtml);
		Map<String,String> metaMap = obj.getMeta();
		assertEquals("img", metaMap.get("image"));
		assertEquals("invalid", metaMap.get("title"));
		assertFalse(obj.isValid());
	}
	
	public void testValidWithAudioVideo() throws Exception{
		File audioVideo = new File(TestDataDir,"audiovideo.html");
		OGObject obj = read(audioVideo);
		assertTrue(obj.isValid());
		assertFalse(obj.isEmpty());

		Map<String,String> metaMap = obj.getMeta();
		assertEquals("url",metaMap.get("url"));
		
		Map<String,String> audioMeta = obj.getAudio();
		assertEquals("audiourl",audioMeta.get("url"));
		assertEquals("audiotype",audioMeta.get("type"));
		
		Map<String,String> videoMeta = obj.getVideo();
		assertEquals("videourl",videoMeta.get("url"));
		assertEquals("videotype",videoMeta.get("type"));
	}
	

	public void testValidWithVideoOnlyInvalid() throws Exception{
		File audioVideo = new File(TestDataDir,"videoOnlyInvalid.html");
		OGObject obj = read(audioVideo);
		assertFalse(obj.isValid());
		assertFalse(obj.isEmpty());
		
		Map<String,String> metaMap = obj.getMeta();
		assertEquals("url",metaMap.get("url"));
		
		Map<String,String> videoMeta = obj.getVideo();
		assertEquals("videotype",videoMeta.get("type"));
		
		Map<String,String> audioMeta = obj.getAudio();
		assertTrue(audioMeta.isEmpty());
	}
	
	public void testValidWithAudioOnly() throws Exception{
		File audioOnly = new File(TestDataDir,"audioOnly.html");
		OGObject obj = read(audioOnly);
		assertTrue(obj.isValid());
		assertFalse(obj.isEmpty());
		
		Map<String,String> metaMap = obj.getMeta();
		assertEquals("url",metaMap.get("url"));
		
		Map<String,String> audioMeta = obj.getAudio();
		assertEquals("audiourl",audioMeta.get("url"));
		assertEquals("audiotype",audioMeta.get("type"));
		
		Map<String,String> videoMeta = obj.getVideo();
		assertTrue(videoMeta.isEmpty());
	}
}
