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
import java.util.Collections;
import java.util.List;

/**
 * Represents the raw features extracted from a item (usually news item composed of one or more web pages) from the web.
 * 
 * This is a mutable type built up by the scraper.
 * @version $Revision: 105335 $
 */
public class ExtractedContents
{
  String _baseURL;
  int _count = 0;
  final List<ImageMeta> _images;
  String _thumbnailImageUrl;
  int _titlePosition = -1;
  
  public ExtractedContents(String baseURL)
  {
    _baseURL = baseURL;
    _images = new ArrayList<ImageMeta>();
  }
  
  public ExtractedContents(String baseURL, List<ImageMeta> images)
  {
    _baseURL = baseURL;
    _images = images;
  }
  
  public ExtractedContents(String baseURL,List<ImageMeta> images, int titlePosition, int count)
  {
    _baseURL = baseURL;
    _images = images;
    _titlePosition = titlePosition;
    _count = count;
  }
  
  public String getBaseURL()
  {
    return _baseURL;
  }
  
  public String getThumbnailImageUrl()
  {
    return _thumbnailImageUrl;
  }
  
  public void setThumbnailImageUrl(String thumbnailImageUrl)
  {
    _thumbnailImageUrl = thumbnailImageUrl;
  }
  
  public void addImage(ImageMeta image)
  {
    _images.add(image);
  }
  
  public void setCount(int count)
  {
    _count = count;
  }
  
  public int getCount()
  {
    return _count;
  }
  
  public int getTitlePosition()
  {
    return _titlePosition;
  }
  
  public void setTitlePosition(int titlePosition)
  {
    _titlePosition = titlePosition;
  }

  public List<ImageMeta> getImages()
  {
    return Collections.unmodifiableList(_images);
  }
  
  public ExtractedContents subList(int offsetTextElements, int maxTextElements, int offsetImages, int maxImages)
  {
    ExtractedContents copy = new ExtractedContents(_baseURL,
                                                   _images.subList(Math.min(offsetImages,_images.size()), Math.min(maxImages, _images.size())),
                                                   _titlePosition,
                                                   _count);
    copy.setThumbnailImageUrl(getThumbnailImageUrl());
    return copy;
  }
  
  public ExtractedContents clone(List<ImageMeta> replacementImages)
  {
    ExtractedContents copy = new ExtractedContents(_baseURL,
                                                   replacementImages,
                                                   _titlePosition,
                                                   _count);
    copy.setThumbnailImageUrl(getThumbnailImageUrl());
    return copy;
  }
}
