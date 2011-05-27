/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.meaningfulweb.imgext;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.meaningfulweb.util.URIUtils;
import org.meaningfulweb.util.http.HttpClientFactory;
import org.meaningfulweb.util.http.HttpClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Retrieves images from the web.  Checks image size before download to defend against excessively large images.
 * @version $Revision: 131490 $
 */
public class ImageFetcher implements ImageSizeExtractor
{
  private static Logger logger = LoggerFactory.getLogger(ImageFetcher.class);
  
  
  /*
  private final int _connectionTimeout;
  private final int _socketTimeout;
  private final int _maxRetries; // maximum number of retries for a single image when fetching only that image
  private final int _maxHeaderRetries = 1;
  private final int _maxBatchFetchRetriesPerImage = 1; // maximum number of retries per image when fetching multiple images during scraping
  
  private MultiThreadedHttpConnectionManager connectionManager;
  private HttpClient client;
  */
  
  public ImageFetcher(){
  }
  
  @Override
  public Map<ImageMeta,ImageSize> extractSize(List<ImageMeta> imgMeta){
	  HashMap<ImageMeta,ImageSize> map = new HashMap<ImageMeta,ImageSize>();
	  for (ImageMeta meta : imgMeta){
		  map.put(meta, extractSize(meta));
	  }
	  return map;
  }
 
  @Override
  public ImageSize extractSize(ImageMeta imgMeta){
	 String path = imgMeta.getUri();
	 ImageSize dim = null;
	 HttpClientService httpClient = HttpClientFactory.getHttpClientService();
		
     HttpGet httpget = null;

     
		 // if the uri is invalid try and clean it up a little before fetching
	 boolean isValidURI = URIUtils.isValidURI(path);
	 if (!isValidURI) {
		    String fixed = URIUtils.fixInvalidUri(path);
		    logger.info("Fixed invalid URI: " + path + " to " + fixed);
		    path = fixed;
	 }
	 //long start = System.currentTimeMillis();

	   ImageProp prop = new ImageProp();
	   prop.setCollectComments(false);
	   prop.setDetermineImageNumber(false);
	   
	   httpget = new HttpGet(path);
	   InputStream in = null;
	   try{
		 HttpEntity entity = httpClient.doGet(httpget);
		 
		 in = entity.getContent();
		 prop.setInput(in);

		 if (prop.check()){
		   dim = new ImageSize(prop.getWidth(),prop.getHeight(),entity.getContentLength());
		 }
		   
	   }
	   catch(Exception e){
		   logger.error("cannot fetch: "+path+":"+e.getMessage(),e);
	   }
	   finally{
		   httpget.abort();
	   }
	
	 //long end = System.currentTimeMillis();
	 return dim;
  }
}
