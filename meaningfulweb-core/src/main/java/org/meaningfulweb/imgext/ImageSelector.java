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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.meaningfulweb.imgext.ImageSizeExtractor.ImageSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages selection of best images from a list of possible images.  This uses the ImageFilter to sort and filter images and additionally downloads
 * select images to improve the sorting and filtering process.
 */
public class ImageSelector{

  private static Logger logger = LoggerFactory.getLogger(ImageSelector.class);
  private final ImageFilter _imageFilter;
  private final ImageSizeExtractor _imageSizeExtractor;
  
  private final int _imagesToFetchPerArticle;
  private final int _maxImagesToScrape;
  
  public ImageSelector(ImageFilter imageFilter, ImageFetcher imageFetcher)
  {
	this(imageFilter,imageFetcher,10,7);  
  }
  
  public ImageSelector(ImageFilter imageFilter, ImageSizeExtractor imageSizeExtractor, int imagesToFetchPerArticle, int maxImagesToScrape)
  {
    _imageFilter = imageFilter;
    _imageSizeExtractor = imageSizeExtractor;
    _imagesToFetchPerArticle = imagesToFetchPerArticle;
    _maxImagesToScrape = maxImagesToScrape;
  }
  
  /**
   * Given the extracted contents of a web page, sorts and filters the images by their HTML metadata, then downloads the first N images and filters them again
   * using content size, dimensions, etc...
   * 
   * @param extractedContents
   * @param thumbnailImageUrl
   * @param articleURL
   * @param filterByImageContents
   * @param filterFirstImage
   * @return
   */
  public List<ImageInfo> fetchAndSortImages(ExtractedContents extractedContents,
                                                  String articleURL, boolean filterByImageContents, boolean filterFirstImage)
  {
    // split up the list, note we're dealing with very small lists here and we always do this since to keep later image list lookups cheap
    ExtractedContents sortedByMetadata = _imageFilter.sortAndFilterByMetadataOnly(extractedContents);
    
    // fetch images for the first N items
    List<ImageMeta> images;
    
    int count = sortedByMetadata._images.size();
    if(filterByImageContents && count>1)
    {
      ExtractedContents imagesToFetch = sortedByMetadata.subList(0, 0, 0, _imagesToFetchPerArticle);
      List<ImageMeta> imagesNotToFetch = sortedByMetadata.getImages().subList(Math.min(sortedByMetadata.getImages().size(), _imagesToFetchPerArticle),
                                                                                      sortedByMetadata.getImages().size());
      Map<ImageMeta, ImageSize> fetchedImages = _imageSizeExtractor.extractSize(imagesToFetch.getImages());
      ExtractedContents imagesSortedByContents = _imageFilter.sortAndFilterByImageContents(imagesToFetch, fetchedImages);
      
      images = new ArrayList<ImageMeta>(imagesSortedByContents.getImages());
      images.addAll(imagesNotToFetch);
    }
    else if (filterFirstImage && count>1)
    {
      images = filterFirstImage(new ArrayList<ImageMeta>(sortedByMetadata.getImages()));
    }
    else
    {
      images = new ArrayList<ImageMeta>(sortedByMetadata.getImages());
    }
    
    // prefer the image_src if we have it
    if(extractedContents.getThumbnailImageUrl() != null)
    {
      ImageMeta thumbnail = new ImageMeta(-1, null, null, null, null, extractedContents.getThumbnailImageUrl(), null);
      
      try
      {
        if(filterFirstImage == false || _imageFilter.accept(_imageSizeExtractor.extractSize(thumbnail)))
        {
          images.add(0, thumbnail);
        }
      }
      catch (Exception e)
      {
        logger.error("failed to fetch image_src image: " + thumbnail.getUri(),e);
      }
      
    }
    
    List<ImageInfo> imageInfoList = new ArrayList<ImageInfo>(images);
    
    return imageInfoList.subList(0, Math.min(_maxImagesToScrape, imageInfoList.size()));
  }
  
  public ImageInfo getBestImage(ExtractedContents extractedContents,String articleURL, boolean filterByImageContents, boolean filterFirstImage){
	  List<ImageInfo> images = fetchAndSortImages(extractedContents,articleURL,filterByImageContents,filterFirstImage);
	  return images.size() > 0 ? images.get(0) : null;
  }
  
  /**
   * Fetches and removes unacceptable images from the beginning ordered image list until an acceptable first image has been found.
   * @param imagesSortedByMetadata
   * @return
   */
  private List<ImageMeta> filterFirstImage(List<ImageMeta> imagesSortedByMetadata)
  {
    for(Iterator<ImageMeta> imageIter = imagesSortedByMetadata.iterator(); imageIter.hasNext(); )
    {
      try
      {
    	ImageMeta image = imageIter.next();
    	ImageSize imageSize = _imageSizeExtractor.extractSize(image);
        if(_imageFilter.accept(imageSize))
        {
          return imagesSortedByMetadata;
        }
        else
        {
          imageIter.remove();
        }
      }
      catch (Exception e){
        imageIter.remove();
      }
    }
    return imagesSortedByMetadata;
  }
}
