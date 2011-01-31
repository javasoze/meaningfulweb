package org.meaningfulweb.opengraph;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;


/*
 Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/

public class OpenGraphParser {

	public static final int OG_PREFIX_CHAR_COUNT = 3; // length of "og:"

	private static final int OG_VIDEO_PREFIX_CHAR_COUNT = 6; // length of "video:"
	private static final int OG_AUDIO_PREFIX_CHAR_COUNT = 6; // length of "audio:"
	
	public static OGObject parse(String html){
		return parse(new Source(html));
	}
	
	public static OGObject parse(Map<String,String> ogmap){
		OGObject obj = new OGObject();
		Map<String,String> meta= obj.getMeta();
		Map<String,String> video= obj.getVideo();
		Map<String,String> audio= obj.getAudio();
		
		Set<Entry<String,String>> entries = ogmap.entrySet();
		for (Entry<String,String> entry : entries){
			String content = entry.getValue();
			String name = entry.getKey();
			if (name.startsWith(OpenGraphVocabulary.VIDEO)){
				if (name.equals(OpenGraphVocabulary.VIDEO)){
					video.put(OGObject.MEDIA_URL, content);
				}
				if (name.startsWith(OpenGraphVocabulary.VIDEO_PREFIX)){
					String subName = name.substring(OG_VIDEO_PREFIX_CHAR_COUNT);
					video.put(subName, content);
				}
			}
			else if (name.startsWith(OpenGraphVocabulary.AUDIO)){
				if (name.equals(OpenGraphVocabulary.AUDIO)){
					audio.put(OGObject.MEDIA_URL, content);
				}
				if (name.startsWith(OpenGraphVocabulary.AUDIO_PREFIX)){
					String subName = name.substring(OG_AUDIO_PREFIX_CHAR_COUNT);
					audio.put(subName, content);
				}
			}
			else{
				meta.put(name, content);
			}
		}
		return obj;
	}
	
	public static OGObject parse(Source source){
	    Element htmlTag = source.getFirstElement(HTMLElementName.HTML);
		List<Element> elementList = source.getAllElements(HTMLElementName.META);
		Map<String,String> datamap = new HashMap<String,String>();
		
		String ogPrefix = findOpenGraphNamespacePrefix(htmlTag);
		
		for (Element elem : elementList){
			String attrVal = elem.getAttributeValue("property");
			if (attrVal!=null && attrVal.startsWith(ogPrefix)){
				String content = elem.getAttributeValue("content");
				String name = attrVal.substring(OG_PREFIX_CHAR_COUNT);
				datamap.put(name, content);
			}
		}
		return parse(datamap);
	}

  private static final String XMLNS_PREFIX = "xmlns:";
  private static String findOpenGraphNamespacePrefix(Element htmlTag)
  {
    String prefix = OpenGraphVocabulary.DEFAULT_PREFIX;
    if (htmlTag != null)
    {
      Attributes attributes = htmlTag.getAttributes();

      int attributeCount = attributes.size();
      for (int i = 0; i < attributeCount; i++)
      {
        Attribute attr = attributes.get(i);
        String value = attr.getValue();
        String key = attr.getKey().toLowerCase().trim();
        if (key.startsWith(XMLNS_PREFIX))
        {
          if (value.equals(OpenGraphVocabulary.NAMESPACE))
          {
            prefix = key.substring(XMLNS_PREFIX.length());
          }
        }
      }
    }
    return prefix;
  }
	
	private static final int BUFFER_SIZE = 4*1024; // 4k buffer
	
	public static OGObject parse(Reader reader) throws IOException{
		StringBuffer buffer = new StringBuffer();
		char[] buf = new char[BUFFER_SIZE];
		
		while(true){
			int len = reader.read(buf);
			if (len==-1) break;
			if (len==0) continue;
			buffer.append(buf, 0, len);
		}
		
		return parse(buffer.toString());
	}
	
	public static OGObject fetchAndParse(HttpClient httpClient,String uri) throws IOException{
		GetMethod get = null;
		try{
			get = new GetMethod(uri);
			int status = httpClient.executeMethod(get);
			if (status==HttpStatus.SC_OK){
				InputStreamReader reader = new InputStreamReader(get.getResponseBodyAsStream(),get.getResponseCharSet());		
				return parse(reader);
			}
			else{
				return null;
			}
		}
		finally{
			if (get!=null){
				get.releaseConnection();
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
		String url = "http://techcrunch.com/2011/01/18/microsoft-kinect-developer-johnny-chung-lee-jumps-ship-and-lands-at-google/";
		HttpClient client = new HttpClient();
		OGObject obj = OpenGraphParser.fetchAndParse(client, url);
		System.out.println(obj);
	}
}
