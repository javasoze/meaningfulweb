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

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Retrieves images from the web.  Checks image size before download to defend against excessively large images.
 * @version $Revision: 131490 $
 */
public class ImageFetcher implements ImageSizeExtractor
{
  private static Logger logger = LoggerFactory.getLogger(ImageFetcher.class);
  
  private final long _maxOriginalImageSizeInKilobytes;
  private final int _connectionTimeout;
  private final int _socketTimeout;
  private final int _maxRetries; // maximum number of retries for a single image when fetching only that image
  private final int _maxHeaderRetries = 1;
  private final int _maxBatchFetchRetriesPerImage = 1; // maximum number of retries per image when fetching multiple images during scraping
  
  private MultiThreadedHttpConnectionManager connectionManager;
  private HttpClient client;
  
  public ImageFetcher(int connectionTimeout, int socketTimeout, int maxRetries, long maxOriginalImageSizeInKilobytes)
  {
    _connectionTimeout = connectionTimeout;
    _socketTimeout = socketTimeout;
    _maxRetries = maxRetries;
    _maxOriginalImageSizeInKilobytes = maxOriginalImageSizeInKilobytes;
    connectionManager = new MultiThreadedHttpConnectionManager();
    client = new HttpClient(connectionManager);
    client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
    client.getParams().setSoTimeout(_socketTimeout);
    client.getParams().setConnectionManagerTimeout(_connectionTimeout);
    client.getHttpConnectionManager().getParams().setConnectionTimeout(_connectionTimeout);
  }
  
  public ImageFetcher(){
	 this(10000,10000,3,1024);
  }
  
  /**
   * Retrieves an image from the web.
   * Checks image size before download to defend against excessively large images.
   * 
   * @param image
   * @return
   */
  public BufferedImage fetch(String imageUrl) throws Exception
  {
    return fetch(imageUrl, _maxRetries);
  }
  
  /**
   * Retrieves an image from the web.
   * Checks image size before download to defend against excessively large images.
   * 
   * @param image
   * @param maxRetries
   * @return
   */
  public BufferedImage fetch(String imageUrl, int maxRetries) throws Exception
  {
    for(int i = 0; i < maxRetries; i++)
    {
      try
      {
    	BufferedImage img =  performFetch(imageUrl);
    	return img;
    	
      }
      catch(IOException e)
      {
    	logger.error(e.getMessage(),e);
      }
    }
    return null;
  }
  
  /**
   * Retrieves multiple images from the web.
   * 
   * @param images
   * @return
   */
  public Map<ImageMeta, BufferedImage> fetchImages(List<ImageMeta> images)
  {
    HashMap<ImageMeta, BufferedImage> results = new HashMap<ImageMeta, BufferedImage>();
    for(ImageMeta image : images)
    {
      try
      {
        results.put(image, fetch(image.getUri(), _maxBatchFetchRetriesPerImage));
      }
      catch (Exception e)
      {
    	logger.error(e.getMessage(),e);
        results.put(image, null);
      }
    }
    return results;
  }
  
  /**
   * Retrieves just the http header for an image from the web.
   * @param imageUrl
   * @return
   */
  public ImageHeader fetchHeader(String imageUrl) throws Exception
  {
    return fetchHeader(imageUrl, _maxHeaderRetries);
  }
  
  /**
   * Retrieves just the http header for an image from the web.
   * @param imageUrl
   * @param maxRetries
   * @return
   */
  public ImageHeader fetchHeader(String imageUrl, int maxRetries) throws Exception
  {
    for(int i = 0; i < maxRetries; i++)
    {
      try
      {
        return performFetchHeader(imageUrl);
      }
      catch(IOException e)
      {
    	logger.error(e.getMessage(),e);
      }
    }
    return null;
  }
  
  /**
   * Retrieves just the http header for multiple images from the web.
   * @param images
   * @return
   */
  public Map<ImageMeta, ImageHeader> fetchHeaders(List<ImageMeta> images)
  {
    HashMap<ImageMeta, ImageHeader> results = new HashMap<ImageMeta, ImageHeader>();
    for(ImageMeta image : images)
    {
      try
      {
        results.put(image, fetchHeader(image.getUri(), _maxBatchFetchRetriesPerImage));
      }
      catch (Exception e)
      {
    	logger.error(e.getMessage(),e);
        results.put(image, null);
      }
    }
    return results;
  }

  private BufferedImage performFetch(String imageUrl) throws Exception, IOException
  {
	GetMethod get = null;
    try{ 
      get = new GetMethod(imageUrl);
      client.executeMethod(get);
      long contentSize = get.getResponseContentLength();
      if(contentSize > _maxOriginalImageSizeInKilobytes*1024) throw new Exception("image size of " + contentSize + " greater than max allowed of " + _maxOriginalImageSizeInKilobytes + " kb");
      
      InputStream in = get.getResponseBodyAsStream();
      try
      {
        synchronized (this) { // synchronized to prevent JVM from crashing badly here, see: NUS-1226
          return ImageIO.read(in);
        }
      }
      catch(ConnectTimeoutException e)
      {
    	logger.error("connection timed out while fetching image. url: " + imageUrl + ", message: " + e.getMessage(),e);
        return null;
      }
      catch (SocketTimeoutException e)
      {
    	logger.error("socket timed out while fetching image. url: " + imageUrl + ", message: " + e.getMessage(),e);
        return null;
      }
      catch(IIOException e)
      {
    	logger.error("error reading image. url: "+ imageUrl +", message:" + e.getMessage(),e);
        return null;
      }
      catch (EOFException e)
      {
    	logger.error("Unable to fetch image, EOF encountered while reading from stream. url: " + imageUrl,e);
        return null;
      }
      catch (UnknownHostException e)
      {
    	logger.error("Unknown host for image. url: " + imageUrl + ", message: " + e.getMessage(),e);
        return null;
      }
      finally
      {
        if(in != null)
        {
          try
          {
            in.close();
          }
          catch (Throwable t)
          {
        	logger.error(t.getMessage(),t);
          }
        }
      }
    }
    catch (IllegalArgumentException e)
    {
      logger.error("Invalid URL when attempting to fetch image: "  + imageUrl,e);
      return null;
    }
    catch (Throwable t)
    {
      throw new Exception("Unknown exception while fetching image", t);
    }
    finally{
    	if (get!=null){
    		get.releaseConnection();
    	}
    }
  }
  
  public static String getMimeTypeFromContentType(HttpMethod head)
  {
    Header contentTypeHeader = head.getResponseHeader(Metadata.CONTENT_TYPE);
    
    if(contentTypeHeader == null) return null;

    String contentType = contentTypeHeader.getValue();
    return getMimeTypeFromContentType(contentType);
  }
  
  public static String getMimeTypeFromContentType(String contentType)
  {
    if(contentType == null) return null;

    String[] parts = contentType.split(";");
    if(parts.length == 0) return null;
    else return parts[0];
  }
  
  public static Date getLastModifiedDate(HttpMethod head) throws DateParseException
  {
    Header lastModifiedHeader = head.getResponseHeader("Last-Modified");
    if(lastModifiedHeader != null)
    {
      String lastModified = lastModifiedHeader.getValue();
      return DateUtil.parseDate(lastModified);
    }
    return null;
  }
  
  private ImageHeader performFetchHeader(String imageUrl) throws Exception, IOException
  {
	HeadMethod head = null;
    try{
      head = new HeadMethod(imageUrl);
      client.executeMethod(head);
      long contentSize = head.getResponseContentLength();
      Date modifiedDate = getLastModifiedDate(head);
      String contentMimeType = getMimeTypeFromContentType(head);
      
      return new ImageHeader(contentSize, modifiedDate, contentMimeType);
      
      }
    catch (Throwable t)
    {
      throw new Exception("Unknown exception while fetching image", t);
    }
    finally{
      if (head!=null){
    	  head.releaseConnection();
      }
    }
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
	 GetMethod get = null;
	 //long start = System.currentTimeMillis();
	 try{
	   ImageProp prop = new ImageProp();
	   prop.setCollectComments(false);
	   prop.setDetermineImageNumber(false);
	   
	   get = new GetMethod(path);
       int status = client.executeMethod(get);
       if (status!=HttpStatus.SC_OK) return null;
       
	   prop.setInput(new BufferedInputStream(get.getResponseBodyAsStream()));

	   if (prop.check()){
		 dim = new ImageSize(prop.getWidth(),prop.getHeight());
	   }
	   
	 }
	 catch(Exception e){
		 logger.error(e.getMessage(),e);
	 }
	 finally{
		 if (get!=null){
			 get.releaseConnection();
		 }
	 }
	 //long end = System.currentTimeMillis();
	 return dim;
  }
}
