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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.meaningfulweb.imgext.ImageSizeExtractor.ImageSize;
import org.meaningfulweb.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Filters and sorts images based on data about the IMG tags from HTML.  The filtering and sorting uses a variety of heuristics
 * that aim to give the highest score to the IMG most likely to represent the article.
 * @version $Revision: 113091 $
 */
public class ImageFilter{
  private static Logger logger = LoggerFactory.getLogger(ImageFilter.class);
  
  private final int _maxAcceptableAspectRatio;
  private final double _minAcceptableAspectRatio;
  private final int _preferedImageSizeInPixels;
  private final int _minAcceptableImageHeight;
  private final int _minAcceptableImageWidth;
  private final int _numberOfImagesBoostedAfterTitle; // number of images found after the title that get a score boost for proximity to the title
  private final int _minAcceptableImageSizeInBytes;
  private final double _minScoreThreshold;
  private final boolean _allowAllImageFormats;

  private final int _positionScoreWeight;
  private final double _formatScoreWeight;
  private final int _sizeScoreWeight;
  private final double _filenameScoreWeight;
  private final double _attributeScoreWeight;
  
  public ImageFilter(int maxAcceptableAspectRatio,
                     double minAcceptableAspectRatio,
                     int preferedImageSizeInPixels,
                     int minAcceptableImageHeight,
                     int minAcceptableImageWidth,
                     int numberOfImagesBoostedAfterTitle,
                     int minAcceptableImageSizeInBytes,
                     double minScoreThreshold,
                     boolean allowAllImageFormats,
                     
                     int positionScoreWeight,
                     double formatScoreWeight,
                     int sizeScoreWeight,
                     double filenameScoreWeight,
                     double attributeScoreWeight)
  {
    _maxAcceptableAspectRatio = maxAcceptableAspectRatio;
    _minAcceptableAspectRatio = minAcceptableAspectRatio;
    _preferedImageSizeInPixels = preferedImageSizeInPixels;
    _minAcceptableImageHeight = minAcceptableImageHeight;
    _minAcceptableImageWidth = minAcceptableImageWidth;
    _numberOfImagesBoostedAfterTitle = numberOfImagesBoostedAfterTitle;
    _minAcceptableImageSizeInBytes = minAcceptableImageSizeInBytes;
    _minScoreThreshold = minScoreThreshold;
    _allowAllImageFormats = allowAllImageFormats;
    
    _positionScoreWeight = positionScoreWeight;
    _formatScoreWeight = formatScoreWeight;
    _sizeScoreWeight = sizeScoreWeight;
    _filenameScoreWeight = filenameScoreWeight;
    _attributeScoreWeight = attributeScoreWeight;
  }
  
  public ImageFilter(){
	  this(4,0.25,6400,50,50,2,2048,0.1,true,5,1.5,1,0.3,0);
  }
  
  /**
   * Scores the images on a variety of factors including size, aspect ratio, placement and URL.  Sorts the images by score and filters out
   * any that are unacceptable (too small, bad image, anchor, domain, etc..).
   * 
   * @param baseURL
   * @param images
   * @return
   */
  public ExtractedContents sortAndFilterByMetadataOnly(ExtractedContents extractedContents)
  {
    List<ImageMeta> images = extractedContents.getImages();
    List<ImageMeta> uniqueImages = removeDuplicatesByURL(images);
    
    Map<ImageMeta, Double> scores = new HashMap<ImageMeta, Double>();
    List<ImageMeta> result1 = new ArrayList<ImageMeta>();
    List<ImageMeta> results = new ArrayList<ImageMeta>();
    String baseURL = extractedContents.getBaseURL();
    if (baseURL!=null){
    	baseURL = baseURL.trim();
    }
    String domain = URLUtil.extractDomainFromUrl(baseURL);
    
    int adjustedTitlePosition = extractedContents.getTitlePosition();
    long maxSize = 0L;
    for(ImageMeta image : uniqueImages)
    {
      if(accept(image))
      {
    	long s = 0L;
    	if (image.getWidth()!=null && image.getHeight()!=null){
    		if (image.getWidth()==1 || image.getHeight()==1) continue;
    		if (image.getWidth()==0 || image.getHeight()==0) continue;
    		s = image.getWidth()*image.getHeight();
    	}
    	if (s>maxSize) maxSize=s;
    	result1.add(image);	  
      }
      else if (image.getPosition() > adjustedTitlePosition)
      {
        adjustedTitlePosition++; // we need the title position to be relative only to
                                 // acceptable images, so if an image is unacceptable, we
                                 // increment to adjust for the gap in the numbering the
                                 // unacceptable image creates.  This is a bit of a hack...
      }
    }
    
    for(ImageMeta image : result1)
    {
      double score = score(adjustedTitlePosition, domain, image, null,maxSize);
      if(score > _minScoreThreshold)
      {
        scores.put(image, score);
        results.add(image);
      }
    }
    
    Collections.sort(results, new ScoreComparator(scores));
    
    return extractedContents.clone(results);
  }
  
  /**
   * Filters images using data available only by retrieving http headers for the images.
   * @param images
   * @param headers
   * @return
   */
  public ExtractedContents sortAndFilterByImageHeaders(ExtractedContents extractedContents, Map<ImageMeta, ImageHeader> headers)
  {
    List<ImageMeta> images = extractedContents.getImages();
    List<ImageMeta> uniqueImages = removeDuplicatesByURL(images);
    // TODO LOW we could potentially remove duplicates by hash or signature for this case as well
    List<ImageMeta> results = new ArrayList<ImageMeta>();
    for(ImageMeta image : uniqueImages)
    {
    	ImageHeader header = headers.get(image);
      if(header != null && accept(header))
      {
        results.add(image);
      }
    }
    // TODO: sort
    return extractedContents.clone(results);
  }
  
  /**
   * Filters images using data available only be retireving 
   * @param images
   * @param imagesContents
   * @return
   */
  public ExtractedContents sortAndFilterByImageContents(ExtractedContents extractedContents, Map<ImageMeta, ImageSize> imagesContents)
  {
    List<ImageMeta> images = extractedContents.getImages();
    //List<ImageMeta> uniqueImages = removeDuplicatesBySignature(removeDuplicatesByURL(images), imagesContents);
    Map<ImageMeta, Double> scores = new HashMap<ImageMeta, Double>();
    List<ImageMeta> results = new ArrayList<ImageMeta>();
    String baseURL = extractedContents.getBaseURL();
    if (baseURL!=null){
    	baseURL=baseURL.trim();
    }
    String domain = URLUtil.extractDomainFromUrl(baseURL);
    long maxSize = 0L;
    for(ImageMeta image : images)
    {
      ImageSize imgSize = imagesContents.get(image);
      if(imgSize != null && accept(imgSize))
      {
    	long s = imgSize.width * imgSize.height;
    	if (maxSize<s) maxSize=s;
    	image.setSize(imgSize.size);
    	results.add(image);
      }
    }
    
    for (ImageMeta result : results){
    	ImageSize imgSize = imagesContents.get(result);
    	double score = score(extractedContents.getTitlePosition(), domain, result, imgSize,maxSize);
        scores.put(result, score);	
    }
  
    Collections.sort(results, new ScoreComparator(scores));
    
    return extractedContents.clone(results);
  }
  
  // TODO LOW: This might be the wrong approach, rather than just keeping a single copy of each image, should we keep the count for each
  // image,  we will likely need to either not show images that appear more than once or penalize them when scoring
  public List<ImageMeta> removeDuplicatesByURL(List<ImageMeta> images)
  {
    Set<String> urls = new HashSet<String>();
    ArrayList<ImageMeta> results = new ArrayList<ImageMeta>();
    for(ImageMeta image : images)
    {
      if(image.getUri() != null && urls.contains(image.getUri()) == false)
      {
        urls.add(image.getUri());
        results.add(image);
      }
    }
    return results;
  }
  
  public static String generateSignature(byte[] bytes) throws Exception
  {
    MessageDigest md;
    try
    {
      md = MessageDigest.getInstance("MD5");
      md.reset();
      md.update(bytes);
      byte[] digest = md.digest();
      BigInteger bigInt = new BigInteger(1,digest);
      String hashtext = bigInt.toString(16);
      while(hashtext.length() < 32)
      {
        hashtext = "0" + hashtext;
      }
      return hashtext;
    }
    catch (NoSuchAlgorithmException e)
    {
      throw new Exception("unable to generate image signature", e);
    }

  }
  
  /**
   * Filters out duplicate images by checking if they have the same signatures, which means they are almost certainly identical.
   * @param images
   * @param imagesContents
   * @return
   */
  public List<ImageMeta> removeDuplicatesBySignature(List<ImageMeta> images, Map<ImageMeta, BufferedImage> imagesContents)
  {
    Set<String> signatures = new HashSet<String>();
    ArrayList<ImageMeta> results = new ArrayList<ImageMeta>();
    for(ImageMeta image : images)
    {
      BufferedImage imageContents = imagesContents.get(image);
      if(imageContents == null) continue;
      byte[] ds;
      String signature = null;
      try
      {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
    	ImageIO.write(imageContents, "jpeg", baos);
    	baos.flush();
    	ds = baos.toByteArray();
    	baos.close();
        signature = generateSignature(ds);
        
        if(signature != null && signatures.contains(signature) == false)
        {
          signatures.add(signature);
          results.add(image);
        }
      }
      catch (IOException e)
      {
        logger.error("IOException generating signature for: " + image.getUri(),e);
      }
      catch (Exception e)
      {
    	logger.error(" InternalException generating signature for: " + image.getUri(),e);
      }
    }
    return results;
  }
  
  /**
   * Sorts entries using the provided scores.
   * @version $Revision: 113091 $
   */
  private static class ScoreComparator implements Comparator<ImageMeta>
  {
    private final Map<ImageMeta, Double> _scores;

    public ScoreComparator(Map<ImageMeta, Double> scores)
    {
      _scores = scores;
    }

    public int compare(ImageMeta image1, ImageMeta image2)
    {
      final Double score1 = _scores.get(image1);
      final Double score2 = _scores.get(image2);
      return Double.compare(score2, score1); // swapped params here to sort by score from highest to lowest
    }
  }
  
  public boolean accept(ImageMeta image)
  {
    if(image == null) return false;
    return isAcceptableFormat(image);
    
    // using width/height as a filter for html image attributes does not work, it results in to many false negatives
    //return isAcceptableSize(image) &&
    //       isAcceptableAspectRatio(image);
  }
  
  public boolean accept(ImageSize imageSize)
  {
    if(imageSize == null) return false;
    return isAcceptableSize(imageSize.width, imageSize.height) &&
            isAcceptableAspectRatio(imageSize.width, imageSize.height);
  }
  
  public boolean accept(ImageHeader imageHeader)
  {
    if(imageHeader == null) return false;
    return isAcceptableSize(imageHeader);
  }
  
  private double score(int titlePosition, String domain, ImageMeta imageMetadata, ImageSize imageSize,long maxSize)
  {
    // TODO: weigh the various factors?
    double attributesScore = scoreAttributes(imageMetadata); 
    //double domainScore = scoreDomain(domain, image);  // initial numbers seem to indicate this just makes things worse
    double filenameScore = scoreImageName(imageMetadata);
    
    double sizeScore;
    if(imageSize != null) // use the best data available, if we have the actual image, use it's size info, otherwise try to use html width/height attributes
    {
      sizeScore = scoreSize(imageSize.width,imageSize.height,maxSize);
    }
    else
    {
      sizeScore = scoreSize(imageMetadata.getWidth(),imageMetadata.getHeight(),maxSize);
    }
    
    double formatScore = scoreFormat(imageMetadata);
    double positionScore = scorePosition(imageMetadata, titlePosition);
    double score = attributesScore*_attributeScoreWeight + 
                    /*domainScore*DOMAIN_SCORE_WEIGHT +*/ 
                    filenameScore*_filenameScoreWeight + 
                    sizeScore*_sizeScoreWeight + 
                    formatScore*_formatScoreWeight + 
                    positionScore*_positionScoreWeight;
    return score;
  }
  
  private boolean isAcceptableSize(ImageMeta image)
  {
    if(image.getWidth() == null || image.getHeight() == null) return false;
    return isAcceptableSize(image.getWidth(), image.getHeight());
  }
  
  private boolean isAcceptableSize(int width, int height)
  {
    return (width > _minAcceptableImageWidth) && (height > _minAcceptableImageHeight);
  }
  
  private boolean isAcceptableSize(ImageHeader imageHeader)
  {
    return (imageHeader.getContentLength() > _minAcceptableImageSizeInBytes);
  }
  
  private boolean isAcceptableAspectRatio(int width, int height)
  {
    double aspectRatio =  ((double)width)/height;
    boolean okayRatio = (aspectRatio > _minAcceptableAspectRatio && aspectRatio < _maxAcceptableAspectRatio);
    
    return okayRatio;
  }
  
  private boolean isAcceptableFormat(ImageMeta image)
  {
    // images from a variety of sources, including bloomberg do not include image format as a file extension in the URL... so disabling the filter
    if(_allowAllImageFormats) return true;
    
    String uri = image.getUri();
    try
    {
      // we first try getting the path from the URL because this will strip off any query parameters, giving us the best possible filename to check
      URL url = new URL(uri);
      String path = url.getPath().toUpperCase();
      // TODO: extract image file extension list
      if(path.endsWith(".GIF")) return false;
      else return true;
    }
    catch (MalformedURLException e)
    {
      // if we can't work with the path from the URL we fallback to a simple URL string check
      logger.warn("unable to determine format because URL could not be parsed by java.net.URL");
      String url = uri.trim().toUpperCase();
      if(url.endsWith(".GIF")) return false;
      else return true;
    }
  }
  
  private double scoreSize(Integer width,Integer height,long maxSize)
  {
    if(width == null || height == null) return 0;
    long size = width*height;
    if(isAcceptableAspectRatio(width,height) == false) return 0;
    long ms = maxSize == 0L ? _preferedImageSizeInPixels : maxSize;
    return (double)size/(double)ms;
  }
  
  private double scoreAttributes(ImageMeta image)
  {
    double altScore = (image.getAlt() == null) ? 0 : .5;
    double titleScore = (image.getTitle() == null) ? 0 : .5;
    // score can be improved by looking at length, then potentially language of text, keywords ...
    
    return altScore + titleScore;
  }
  
 
  
  private double scoreDomain(String domain, ImageMeta image)
  {
    // TODO: this is way to simple, most images come from CDNs now, we either need to whitelist those (difficult)
    // or blacklist ad sites, but that is also relatively difficult..
    if(image.getUri() == null) return 0;
    String imgUri = image.getUri();
    if (imgUri!=null){
    	imgUri=URLUtil.extractDomainFromUrl(imgUri.trim());
    	return imgUri.equals(domain) ? 1 : 0;
    }
    return 0;
  }
  
  private double scoreImageName(ImageMeta image)
  {
    // facebook_connect.png
    String src = image.getUri();
    int slashPosition = src.lastIndexOf('/');
    if(slashPosition < 0) return 0;
    String filename = src.substring(slashPosition, src.length());
    if(filename.contains("twitter") || 
        filename.contains("facebook") || 
        filename.contains("rss") || 
        filename.contains("logo") ||
        filename.contains("spacer")) return 0; // TODO: put a blacklist somewhere
    return 1;
  }
  
  private double scoreFormat(ImageMeta image)
  {
    try
    {
      String uri = image.getUri();
      URL url = new URL(uri);
      String path = url.getPath().toUpperCase();
      if(path.endsWith(".JPG") || path.endsWith(".JPEG")) return 1;
      if(path.endsWith(".PNG")) return .4;
      if(path.endsWith(".BMP")) return .3;
      if(path.endsWith(".GIF")) return 0; // we don't like gifs
      else return .1; // unknown format-- we score these slightly higher than gifs
    }
    catch (MalformedURLException e)
    {
      logger.warn("unable to determine format because URL could not be parsed by java.net.URL");
      return 0;
    }
  }
  
  private double scorePosition(ImageMeta image, int titlePosition)
  {
    if(titlePosition == -1) return 0; // we don't know where the title is so we cannot use it to help with scoring
    
    // position may be negative if it is before where the article title appears in the body
    // for first pass we are only looking at images appearing after the title.
    int relativePosition = image.getPosition() - (titlePosition+1);
    if(relativePosition < 0 || relativePosition > _numberOfImagesBoostedAfterTitle) return 0;
    return (double)(_numberOfImagesBoostedAfterTitle - relativePosition) / (double)_numberOfImagesBoostedAfterTitle;
  }
}
